//! Spectrum (FFT) of one trace's record, computed on the PC like the vendor
//! app (`FFTView.java`, `vds/core/fft/`), but over the whole record rather
//! than 2048 points from its centre.

use std::f64::consts::{PI, SQRT_2};

use serde::{Deserialize, Serialize};

use crate::measure::Scale;

/// Window applied before the transform (`WndType.java`).
#[derive(Clone, Copy, Debug, PartialEq, Eq, Serialize, Deserialize, Default)]
pub enum FftWindow {
    Rectangle,
    #[default]
    Hann,
    Hamming,
    Blackman,
}

impl FftWindow {
    pub const ALL: [FftWindow; 4] = [FftWindow::Rectangle, FftWindow::Hann, FftWindow::Hamming, FftWindow::Blackman];

    /// Weight of sample `i` of `n` (periodic form).
    pub fn coef(self, i: usize, n: usize) -> f64 {
        let x = 2.0 * PI * i as f64 / n as f64;
        match self {
            FftWindow::Rectangle => 1.0,
            FftWindow::Hann => 0.5 - 0.5 * x.cos(),
            FftWindow::Hamming => 0.54 - 0.46 * x.cos(),
            FftWindow::Blackman => 0.42 - 0.5 * x.cos() + 0.08 * (2.0 * x).cos(),
        }
    }

    pub fn label(self) -> &'static str {
        match self {
            FftWindow::Rectangle => "Rect",
            FftWindow::Hann => "Hann",
            FftWindow::Hamming => "Hamm",
            FftWindow::Blackman => "Black",
        }
    }

    /// Half-width of the main lobe, in bins of the unpadded record.
    fn main_lobe_bins(self) -> f64 {
        match self {
            FftWindow::Rectangle => 1.0,
            FftWindow::Hann | FftWindow::Hamming => 2.0,
            FftWindow::Blackman => 3.0,
        }
    }
}

#[derive(Clone, Copy, Debug, PartialEq, Eq, Serialize, Deserialize, Default)]
pub enum FftUnit {
    /// dB relative to 1 Vrms.
    #[default]
    Dbv,
    /// Linear, in Vrms.
    Vrms,
}

/// Vertical scale choices for dBV (`FFTControl.dBValuePerDiv`).
pub const DB_PER_DIV: [f64; 5] = [1.0, 2.0, 5.0, 10.0, 20.0];

/// Horizontal scale choices, 1-2-5 from 1 Hz to 5 MHz (Nyquist at 100 MS/s is 50 MHz = 10 × 5 MHz).
pub const FFT_HZ_DIV: [f64; 21] = [
    1.0, 2.0, 5.0, 10.0, 20.0, 50.0, 100.0, 200.0, 500.0, 1e3, 2e3, 5e3, 10e3, 20e3, 50e3, 100e3, 200e3, 500e3, 1e6,
    2e6, 5e6,
];

/// Horizontal divisions of the spectrum display.
pub const FFT_H_DIVS: usize = 10;

/// Largest transform: the next power of two above the 5000-point record.
const MAX_POINTS: usize = 8192;

#[derive(Clone, Copy, Debug, PartialEq, Serialize, Deserialize)]
#[serde(default)]
pub struct FftSettings {
    pub on: bool,
    /// 0 = CH1, 1 = CH2, 2 = Math.
    pub source: usize,
    pub window: FftWindow,
    pub unit: FftUnit,
    /// Index into [`DB_PER_DIV`].
    pub db_div_idx: usize,
    /// dBV at the top of the display.
    pub ref_dbv: i32,
    /// Steps below the default Hz/div (0 = whole band up to Nyquist).
    pub zoom: usize,
}

impl Default for FftSettings {
    fn default() -> Self {
        FftSettings {
            on: false,
            source: 0,
            window: FftWindow::Hann,
            unit: FftUnit::Dbv,
            db_div_idx: 3, // 10 dB/div
            ref_dbv: 20,
            zoom: 0,
        }
    }
}

impl FftSettings {
    pub fn db_per_div(&self) -> f64 {
        DB_PER_DIV[self.db_div_idx]
    }

    pub fn sanitize(&mut self) {
        if self.source > 2 {
            self.source = 0;
        }
        self.db_div_idx = self.db_div_idx.min(DB_PER_DIV.len() - 1);
        self.ref_dbv = self.ref_dbv.clamp(-200, 200);
        self.zoom = self.zoom.min(FFT_HZ_DIV.len() - 1);
    }
}

/// Hz/div at sample rate `fs`: the smallest table entry that fits Nyquist on
/// screen, then `zoom` steps finer.
pub fn hz_per_div(fs: f64, zoom: usize) -> f64 {
    FFT_HZ_DIV[max_zoom(fs) - zoom.min(max_zoom(fs))]
}

/// Most zoom steps available at sample rate `fs` (down to 1 Hz/div); this is
/// also the table index of the unzoomed Hz/div.
pub fn max_zoom(fs: f64) -> usize {
    let want = fs / 2.0 / FFT_H_DIVS as f64;
    FFT_HZ_DIV.iter().position(|&h| h >= want * (1.0 - 1e-9)).unwrap_or(FFT_HZ_DIV.len() - 1)
}

/// Top-of-screen level for a source at `volts_per_div`: a full-screen sine
/// (5 div peak), rounded up to 10 dB.
pub fn default_ref_dbv(volts_per_div: f64) -> i32 {
    let dbv = 20.0 * (5.0 * volts_per_div / SQRT_2).log10();
    ((dbv / 10.0).ceil() * 10.0) as i32
}

pub fn to_dbv(vrms: f64) -> f64 {
    20.0 * vrms.max(1e-12).log10()
}

pub struct Spectrum {
    /// Frequency step between bins.
    pub bin_hz: f64,
    /// Transform length (record zero-padded to a power of two).
    pub points: usize,
    /// Record samples that went in.
    pub samples: usize,
    pub window: FftWindow,
    /// Vrms per bin, `points / 2 + 1` of them (DC to Nyquist).
    pub vrms: Vec<f64>,
}

/// Radix-2 decimation-in-time FFT, in place. `re.len()` must be a power of two.
fn fft(re: &mut [f64], im: &mut [f64]) {
    let n = re.len();
    debug_assert!(n.is_power_of_two() && im.len() == n);
    let mut j = 0;
    for i in 1..n {
        let mut bit = n >> 1;
        while j & bit != 0 {
            j ^= bit;
            bit >>= 1;
        }
        j |= bit;
        if i < j {
            re.swap(i, j);
            im.swap(i, j);
        }
    }
    let mut len = 2;
    while len <= n {
        let half = len / 2;
        for k in 0..half {
            let (s, c) = (-2.0 * PI * k as f64 / len as f64).sin_cos();
            for start in (0..n).step_by(len) {
                let (a, b) = (start + k, start + k + half);
                let tr = re[b] * c - im[b] * s;
                let ti = re[b] * s + im[b] * c;
                re[b] = re[a] - tr;
                im[b] = im[a] - ti;
                re[a] += tr;
                im[a] += ti;
            }
        }
        len <<= 1;
    }
}

/// Spectrum of a record of screen codes at sample rate `fs`. Each bin is the
/// Vrms of a tone at that frequency, corrected for the window's gain.
pub fn spectrum(samples: &[i8], scale: Scale, fs: f64, window: FftWindow) -> Spectrum {
    // Records are at most 5000 points; keep the centre of anything longer.
    let start = samples.len().saturating_sub(MAX_POINTS) / 2;
    let samples = &samples[start..samples.len().min(start + MAX_POINTS)];
    let n = samples.len();
    let points = n.next_power_of_two().max(2);
    let k = scale.volts_per_div / crate::tables::COUNTS_PER_DIV as f64;
    let mut re = vec![0.0; points];
    let mut im = vec![0.0; points];
    let mut gain = 0.0;
    for (i, &s) in samples.iter().enumerate() {
        let w = window.coef(i, n);
        gain += w;
        re[i] = (s as i32 - scale.pos0) as f64 * k * w;
    }
    fft(&mut re, &mut im);
    let gain = if gain > 0.0 { gain } else { 1.0 };
    let vrms = (0..=points / 2)
        .map(|b| {
            let mag = re[b].hypot(im[b]) / gain;
            // DC and Nyquist have no mirror image; other bins hold half the
            // amplitude, and a sine of amplitude A is A/√2 Vrms.
            if b == 0 || b == points / 2 { mag } else { 2.0 * mag / SQRT_2 }
        })
        .collect();
    Spectrum { bin_hz: fs / points as f64, points, samples: n, window, vrms }
}

impl Spectrum {
    /// Bins on either side of a tone that belong to its main lobe.
    fn lobe(&self) -> usize {
        (self.window.main_lobe_bins() * self.points as f64 / self.samples.max(1) as f64).ceil() as usize
    }

    /// Strongest component above DC: (frequency in Hz, Vrms), refined by
    /// fitting a parabola to the dB levels around the largest bin.
    pub fn peak(&self) -> Option<(f64, f64)> {
        let first = self.lobe() + 1;
        let last = self.vrms.len().checked_sub(1)?;
        if first >= last {
            return None;
        }
        let k = (first..last).max_by(|&a, &b| self.vrms[a].total_cmp(&self.vrms[b]))?;
        if self.vrms[k] <= 0.0 {
            return None;
        }
        let (a, b, c) = (to_dbv(self.vrms[k - 1]), to_dbv(self.vrms[k]), to_dbv(self.vrms[k + 1]));
        let denom = a - 2.0 * b + c;
        let delta = if denom.abs() > 1e-12 { (0.5 * (a - c) / denom).clamp(-0.5, 0.5) } else { 0.0 };
        let level = b - 0.25 * (a - c) * delta;
        Some(((k as f64 + delta) * self.bin_hz, 10f64.powf(level / 20.0)))
    }

    /// Largest bin within a main lobe of `hz` (for reading harmonics).
    pub fn level_near(&self, hz: f64) -> Option<f64> {
        let centre = (hz / self.bin_hz).round() as usize;
        let lobe = self.lobe();
        let lo = centre.saturating_sub(lobe);
        let hi = (centre + lobe).min(self.vrms.len().checked_sub(1)?);
        (lo <= hi).then(|| self.vrms[lo..=hi].iter().copied().fold(0.0, f64::max))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    /// 1 V per count, zero at code 0.
    const UNIT: Scale = Scale { volts_per_div: 25.0, pos0: 0 };

    /// Sine of `amp` counts at `cycles` per record of `n` samples.
    fn sine(n: usize, cycles: f64, amp: f64) -> Vec<i8> {
        (0..n).map(|i| (amp * (2.0 * PI * cycles * i as f64 / n as f64).sin()).round() as i8).collect()
    }

    #[test]
    fn matches_naive_dft() {
        let n = 64;
        // Deterministic pseudo-random input.
        let x: Vec<f64> = (0..n).map(|i| ((i * 7919 + 13) % 97) as f64 - 48.0).collect();
        let (mut re, mut im) = (x.clone(), vec![0.0; n]);
        fft(&mut re, &mut im);
        for k in 0..n {
            let (mut r, mut m) = (0.0, 0.0);
            for (t, &v) in x.iter().enumerate() {
                let a = -2.0 * PI * (k * t) as f64 / n as f64;
                r += v * a.cos();
                m += v * a.sin();
            }
            assert!((re[k] - r).abs() < 1e-9 && (im[k] - m).abs() < 1e-9, "bin {k}");
        }
    }

    #[test]
    fn bin_centred_sine_reads_its_rms_with_every_window() {
        // 100 counts amplitude, exactly 64 cycles in 4096 samples.
        let s = sine(4096, 64.0, 100.0);
        for w in FftWindow::ALL {
            let sp = spectrum(&s, UNIT, 4096.0, w);
            assert_eq!(sp.points, 4096);
            let v = sp.vrms[64];
            assert!((v - 100.0 / SQRT_2).abs() / (100.0 / SQRT_2) < 0.005, "{w:?}: {v}");
            let (f, pv) = sp.peak().unwrap();
            assert!((f - 64.0).abs() < 0.01, "{w:?}: {f}");
            assert!((pv - v).abs() / v < 0.01, "{w:?}");
        }
    }

    #[test]
    fn off_bin_sine_with_hann() {
        let s = sine(4096, 100.37, 100.0);
        let sp = spectrum(&s, UNIT, 4096.0, FftWindow::Hann);
        let (f, v) = sp.peak().unwrap();
        assert!((f - 100.37).abs() < 0.1, "{f}");
        assert!((to_dbv(v) - to_dbv(100.0 / SQRT_2)).abs() < 1.5);
    }

    #[test]
    fn dc_goes_to_bin_zero_and_is_not_the_peak() {
        let mut s = vec![40i8; 1000];
        let sp = spectrum(&s, UNIT, 1000.0, FftWindow::Hann);
        assert!((sp.vrms[0] - 40.0).abs() < 1e-9);
        // Zero padding (1000 → 1024) spreads it a little; it stays 40 dB down.
        assert!(sp.vrms[10..].iter().all(|&v| v < 0.4), "{}", sp.vrms[10..].iter().copied().fold(0.0, f64::max));
        // A small tone on top of a large offset is still found.
        for (i, v) in sine(1000, 50.0, 10.0).iter().enumerate() {
            s[i] = 40 + v;
        }
        let (f, _) = spectrum(&s, UNIT, 1000.0, FftWindow::Hann).peak().unwrap();
        assert!((f - 50.0).abs() < 0.5, "{f}");
    }

    #[test]
    fn zero_pads_to_a_power_of_two() {
        let sp = spectrum(&sine(5000, 100.0, 50.0), UNIT, 125e3, FftWindow::Hann);
        assert_eq!((sp.points, sp.samples, sp.vrms.len()), (8192, 5000, 4097));
        assert_eq!(sp.bin_hz, 125e3 / 8192.0);
        // 100 cycles in 5000 samples at 125 kS/s = 2.5 kHz.
        let (f, v) = sp.peak().unwrap();
        assert!((f - 2500.0).abs() < sp.bin_hz * 0.2, "{f}");
        assert!((to_dbv(v) - to_dbv(50.0 / SQRT_2)).abs() < 1.5);
        assert!(sp.level_near(2500.0).unwrap() > 30.0);
        assert!(sp.level_near(7500.0).unwrap() < 1.0);
    }

    #[test]
    fn scales() {
        // 125 kS/s: Nyquist 62.5 kHz → 6.25 kHz/div → 10 kHz/div.
        assert_eq!(hz_per_div(125e3, 0), 10e3);
        assert_eq!(hz_per_div(125e3, 1), 5e3);
        assert_eq!(hz_per_div(100e6, 0), 5e6);
        assert_eq!(hz_per_div(125e3, 99), 1.0);
        assert_eq!(max_zoom(125e3), 12);
        assert_eq!(hz_per_div(125e3, 12), 1.0);
        // 1 V/div: 5 V peak = 3.54 Vrms = 11 dBV → 20.
        assert_eq!(default_ref_dbv(1.0), 20);
        // 200 mV/div: 0.707 Vrms = −3 dBV → 0.
        assert_eq!(default_ref_dbv(0.2), 0);
        assert_eq!(default_ref_dbv(0.005), -30);
    }
}
