//! Automatic waveform measurements on one channel's on-screen record.
//!
//! Definitions follow the vendor software so readings match it:
//! `uppersoft/vds/core/measure/VerticalValueMeasure.java` (top/base, mean, RMS)
//! and `uppersoft/dso/function/measure/MeasureManager.java` (period, widths,
//! duty, 10–90 % rise/fall).

use crate::settings::ChannelSettings;
use crate::tables::{ADC_MAX, COUNTS_PER_DIV, format_si};

/// All values in SI units (volts, seconds, hertz; duty as a 0..1 fraction).
/// `None` means the value can't be measured from this record.
#[derive(Clone, Debug, Default, PartialEq)]
pub struct Measurements {
    pub freq: Option<f64>,
    pub period: Option<f64>,
    pub pos_width: Option<f64>,
    pub neg_width: Option<f64>,
    pub duty: Option<f64>,
    pub rise: Option<f64>,
    pub fall: Option<f64>,
    /// Edge faster than one sample: the value is an upper bound.
    pub rise_limited: bool,
    pub fall_limited: bool,
    pub vpp: Option<f64>,
    pub vmax: Option<f64>,
    pub vmin: Option<f64>,
    pub vamp: Option<f64>,
    pub vtop: Option<f64>,
    pub vbase: Option<f64>,
    pub mean: Option<f64>,
    pub rms: Option<f64>,
    /// Some samples sit at the screen edge, so voltage readings are a bound.
    pub clipped: bool,
    /// Hardware frequency counter; filled in by the caller from the frame.
    pub counter: Option<f64>,
}

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum Group {
    Time,
    Voltage,
}

/// Display rows in panel order: group, label, formatted value (`—` when unmeasurable).
pub fn table(m: &Measurements) -> Vec<(Group, &'static str, String)> {
    let si = |v: Option<f64>, unit: &str| v.map_or("—".to_owned(), |v| format_si(v, unit));
    let bound = |v: Option<f64>, limited: bool| {
        let s = si(v, "s");
        if limited && v.is_some() { format!("< {s}") } else { s }
    };
    let volts = |v: Option<f64>| {
        let s = si(v, "V");
        if m.clipped && v.is_some() { format!("{s}?") } else { s }
    };
    use Group::*;
    vec![
        (Time, "Frequency", si(m.freq, "Hz")),
        (Time, "Period", si(m.period, "s")),
        (Time, "+Duty", m.duty.map_or("—".to_owned(), |d| format!("{:.1} %", d * 100.0))),
        (Time, "+Width", si(m.pos_width, "s")),
        (Time, "−Width", si(m.neg_width, "s")),
        (Time, "Rise", bound(m.rise, m.rise_limited)),
        (Time, "Fall", bound(m.fall, m.fall_limited)),
        (Time, "Counter", si(m.counter, "Hz")),
        (Voltage, "Vpp", volts(m.vpp)),
        (Voltage, "Vmax", volts(m.vmax)),
        (Voltage, "Vmin", volts(m.vmin)),
        (Voltage, "Vamp", volts(m.vamp)),
        (Voltage, "Vtop", volts(m.vtop)),
        (Voltage, "Vbase", volts(m.vbase)),
        (Voltage, "Vavg", volts(m.mean)),
        (Voltage, "Vrms", volts(m.rms)),
    ]
}

#[derive(Clone, Copy, PartialEq)]
enum Edge {
    Rise,
    Fall,
}

/// A mid-level crossing: interpolated time in samples, and the index of the
/// first sample past the crossing.
#[derive(Clone, Copy)]
struct Crossing {
    t: f64,
    idx: usize,
    edge: Edge,
}

fn mean_of(values: &[f64]) -> Option<f64> {
    (!values.is_empty()).then(|| values.iter().sum::<f64>() / values.len() as f64)
}

/// `samples` are raw ADC codes (screen position, 0 = centre); `dt` is the
/// sample interval in seconds.
pub fn measure(samples: &[i8], ch: &ChannelSettings, dt: f64) -> Measurements {
    let n = samples.len();
    if n == 0 {
        return Measurements::default();
    }
    let codes: Vec<i32> = samples.iter().map(|&s| s as i32).collect();
    let k = ch.volts_per_div() / COUNTS_PER_DIV as f64;
    let volts = |code: f64| (code - ch.pos0 as f64) * k;

    let max = *codes.iter().max().unwrap();
    let min = *codes.iter().min().unwrap();
    let mean = codes.iter().sum::<i32>() as f64 / n as f64;
    let rms = (codes.iter().map(|&c| ((c - ch.pos0) as f64).powi(2)).sum::<f64>() / n as f64).sqrt() * k;

    let mut m = Measurements {
        vpp: Some((max - min) as f64 * k),
        vmax: Some(volts(max as f64)),
        vmin: Some(volts(min as f64)),
        mean: Some(volts(mean)),
        rms: Some(rms),
        clipped: max >= ADC_MAX || min <= -ADC_MAX,
        ..Default::default()
    };

    // Top/base: most frequent code above / below the mean (getVtopVbase).
    let mut hist = [0u32; 2 * ADC_MAX as usize + 1];
    for &c in &codes {
        hist[(c + ADC_MAX).clamp(0, 2 * ADC_MAX) as usize] += 1;
    }
    let mode = |range: std::ops::Range<i32>| {
        range
            .map(|c| (c, hist[(c + ADC_MAX) as usize]))
            .filter(|&(_, count)| count > 0)
            .fold(None, |best: Option<(i32, u32)>, cur| match best {
                Some(b) if b.1 >= cur.1 => Some(b),
                _ => Some(cur),
            })
            .map(|(c, _)| c)
    };
    let split = mean.floor() as i32 + 1; // codes >= split count as "above the mean"
    let (Some(base), Some(top)) = (mode(-ADC_MAX..split), mode(split..ADC_MAX + 1)) else {
        return m;
    };
    if max == min || top <= base {
        return m;
    }
    let amp = (top - base) as f64;
    m.vtop = Some(volts(top as f64));
    m.vbase = Some(volts(base as f64));
    m.vamp = Some(amp * k);

    let crossings = find_crossings(&codes, (top + base) as f64 / 2.0, (amp / 5.0).clamp(2.0, 7.0));
    let times = |edge: Edge| crossings.iter().filter(|c| c.edge == edge).map(|c| c.t).collect::<Vec<_>>();
    let (rises, falls) = (times(Edge::Rise), times(Edge::Fall));
    let span = |t: &[f64]| (t.len() >= 2).then(|| (t[t.len() - 1] - t[0]) / (t.len() - 1) as f64);
    if let Some(period) = span(&rises).or_else(|| span(&falls)) {
        m.period = Some(period * dt);
        m.freq = Some(1.0 / (period * dt));
    }

    // Widths: from each crossing to the next opposite one.
    let width = |from: Edge| {
        let w: Vec<f64> = crossings
            .windows(2)
            .filter(|p| p[0].edge == from && p[1].edge != from)
            .map(|p| p[1].t - p[0].t)
            .collect();
        mean_of(&w)
    };
    let (pw, nw) = (width(Edge::Rise), width(Edge::Fall));
    m.pos_width = pw.map(|w| w * dt);
    m.neg_width = nw.map(|w| w * dt);
    if let (Some(pw), Some(nw)) = (pw, nw) {
        m.duty = Some(pw / (pw + nw));
    }

    // 10–90 % transition times around each edge.
    let low = base as f64 + amp * 0.1;
    let high = base as f64 + amp * 0.9;
    for edge in [Edge::Rise, Edge::Fall] {
        let t: Vec<f64> = crossings
            .iter()
            .enumerate()
            .filter(|(_, c)| c.edge == edge)
            .filter_map(|(i, c)| {
                let lo_bound = if i > 0 { crossings[i - 1].idx } else { 0 };
                let hi_bound = crossings.get(i + 1).map_or(n, |c| c.idx);
                transition_time(&codes, c.idx, lo_bound, hi_bound, low, high, edge)
            })
            .collect();
        let Some(samples) = mean_of(&t) else { continue };
        let limited = samples < 1.0;
        let value = Some(samples.max(1.0) * dt);
        match edge {
            Edge::Rise => (m.rise, m.rise_limited) = (value, limited),
            Edge::Fall => (m.fall, m.fall_limited) = (value, limited),
        }
    }
    m
}

/// Mid-level crossings with hysteresis: the level must move past `mid ± hyst`
/// to count, so noise around the threshold doesn't add edges.
fn find_crossings(codes: &[i32], mid: f64, hyst: f64) -> Vec<Crossing> {
    let state_of = |c: i32| {
        let c = c as f64;
        if c >= mid + hyst {
            Some(Edge::Rise)
        } else if c <= mid - hyst {
            Some(Edge::Fall)
        } else {
            None
        }
    };
    let mut crossings = Vec::new();
    let mut state = None;
    for (i, &c) in codes.iter().enumerate() {
        let Some(new) = state_of(c) else { continue };
        if state.is_some_and(|s| s != new) {
            // Walk back to where the signal actually crossed mid and interpolate.
            let above = |c: i32| (c as f64 >= mid) == (new == Edge::Rise);
            if let Some(j) = (1..=i).rev().find(|&j| above(codes[j]) && !above(codes[j - 1])) {
                let (a, b) = (codes[j - 1] as f64, codes[j] as f64);
                crossings.push(Crossing { t: (j - 1) as f64 + (mid - a) / (b - a), idx: j, edge: new });
            }
        }
        state = Some(new);
    }
    crossings
}

/// Time in samples for an edge to go from the 10 % to the 90 % level
/// (or 90 % to 10 % when falling), searching within `[lo, hi)` around `idx`.
fn transition_time(codes: &[i32], idx: usize, lo: usize, hi: usize, low: f64, high: f64, edge: Edge) -> Option<f64> {
    let (start_level, end_level) = match edge {
        Edge::Rise => (low, high),
        Edge::Fall => (high, low),
    };
    // Whether a code has not yet left `level` (start) / has reached `level` (end).
    let not_left = |c: i32, level: f64| match edge {
        Edge::Rise => (c as f64) <= level,
        Edge::Fall => (c as f64) >= level,
    };
    let reached = |c: i32, level: f64| match edge {
        Edge::Rise => (c as f64) >= level,
        Edge::Fall => (c as f64) <= level,
    };
    let interp = |j: usize, level: f64| {
        let (a, b) = (codes[j] as f64, codes[j + 1] as f64);
        j as f64 + (level - a) / (b - a)
    };
    // Last sample still at the 10 % (90 %) level, and first one at the 90 % (10 %) level.
    let s = (lo..idx).rev().find(|&j| not_left(codes[j], start_level))?;
    let e = (idx..hi).find(|&j| reached(codes[j], end_level))?;
    if e == 0 {
        return None;
    }
    Some(interp(e - 1, end_level) - interp(s, start_level))
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::settings::Coupling;

    /// 1 V/div, so one count = 40 mV.
    fn ch() -> ChannelSettings {
        ChannelSettings { on: true, vdiv_idx: 7, coupling: Coupling::Dc, pos0: 0, probe: 1 }
    }
    const K: f64 = 1.0 / 25.0;
    const DT: f64 = 1e-6;

    fn square(period: usize, high: usize, lo: i8, hi: i8, n: usize) -> Vec<i8> {
        (0..n).map(|i| if (i + period / 2) % period < high { hi } else { lo }).collect()
    }

    fn close(a: Option<f64>, b: f64, rel: f64) -> bool {
        a.is_some_and(|a| (a - b).abs() <= rel * b.abs().max(1e-12))
    }

    #[test]
    fn square_wave() {
        let s = square(500, 150, -50, 50, 5000);
        let m = measure(&s, &ch(), DT);
        assert!(close(m.freq, 2000.0, 1e-9), "{:?}", m.freq);
        assert!(close(m.period, 500e-6, 1e-9));
        assert!(close(m.pos_width, 150e-6, 1e-9), "{:?}", m.pos_width);
        assert!(close(m.neg_width, 350e-6, 1e-9));
        assert!(close(m.duty, 0.3, 1e-9));
        assert_eq!(m.vpp, Some(100.0 * K));
        assert_eq!(m.vtop, Some(50.0 * K));
        assert_eq!(m.vbase, Some(-50.0 * K));
        assert_eq!(m.vamp, Some(100.0 * K));
        assert!(close(m.mean, (0.3 * 50.0 - 0.7 * 50.0) * K, 1e-9));
        assert!(close(m.rms, 50.0 * K, 1e-9));
        // A one-sample step is faster than the sampling can resolve.
        assert!(m.rise_limited && m.fall_limited);
        assert_eq!(m.rise, Some(DT));
        assert!(!m.clipped);
    }

    #[test]
    fn noisy_square_wave() {
        let mut seed = 12345u32;
        let mut noise = || {
            seed = seed.wrapping_mul(1_103_515_245).wrapping_add(12345);
            ((seed >> 16) % 7) as i8 - 3
        };
        let s: Vec<i8> = square(500, 150, -50, 50, 5000).into_iter().map(|v| v + noise()).collect();
        let m = measure(&s, &ch(), DT);
        assert!(close(m.freq, 2000.0, 0.005), "{:?}", m.freq);
        assert!((m.duty.unwrap() - 0.3).abs() < 0.005, "{:?}", m.duty);
    }

    #[test]
    fn sine_wave() {
        let s: Vec<i8> = (0..5000)
            .map(|i| (100.0 * (2.0 * std::f64::consts::PI * i as f64 / 500.0).sin()).round() as i8)
            .collect();
        let m = measure(&s, &ch(), DT);
        assert!(close(m.freq, 2000.0, 0.001), "{:?}", m.freq);
        assert!(close(m.rms, 100.0 / 2f64.sqrt() * K, 0.005), "{:?}", m.rms);
        assert!(close(m.duty, 0.5, 0.01));
        assert_eq!(m.vpp, Some(200.0 * K));
    }

    #[test]
    fn rise_time_on_linear_edges() {
        // Trapezoid: low, a 10-sample ramp from -50 to +50, high, a 10-sample ramp down.
        let mut s = Vec::new();
        for _ in 0..5 {
            s.extend(std::iter::repeat_n(-50i8, 200));
            s.extend((1..=10).map(|k| (-50 + 10 * k) as i8));
            s.extend(std::iter::repeat_n(50i8, 200));
            s.extend((1..=10).map(|k| (50 - 10 * k) as i8));
        }
        let m = measure(&s, &ch(), DT);
        assert!(close(m.rise, 8e-6, 1e-9), "{:?}", m.rise);
        assert!(close(m.fall, 8e-6, 1e-9), "{:?}", m.fall);
        assert!(!m.rise_limited);
        assert!(close(m.freq, 1.0 / 420e-6, 1e-9));
    }

    #[test]
    fn flat_line() {
        let m = measure(&[10; 5000], &ch(), DT);
        assert_eq!(m.vpp, Some(0.0));
        assert_eq!(m.mean, Some(10.0 * K));
        assert_eq!((m.freq, m.vamp, m.duty, m.rise), (None, None, None, None));
    }

    #[test]
    fn single_edge() {
        let mut s = vec![-50i8; 2500];
        s.extend((1..=10).map(|k| (-50 + 10 * k) as i8));
        s.extend(std::iter::repeat_n(50i8, 2490));
        let m = measure(&s, &ch(), DT);
        assert_eq!(m.freq, None);
        assert!(close(m.rise, 8e-6, 1e-9));
        assert_eq!(m.fall, None);
    }

    #[test]
    fn table_formatting() {
        let mut m = measure(&square(500, 150, -50, 50, 5000), &ch(), DT);
        m.counter = Some(1000.0);
        let t = table(&m);
        assert_eq!(t.len(), 16);
        let get = |label: &str| t.iter().find(|r| r.1 == label).unwrap().2.clone();
        assert_eq!(get("Frequency"), "2 kHz");
        assert_eq!(get("+Duty"), "30.0 %");
        assert_eq!(get("Rise"), "< 1 µs");
        assert_eq!(get("Counter"), "1 kHz");
        assert_eq!(get("Vpp"), "4 V");
        m.clipped = true;
        assert_eq!(table(&m).iter().find(|r| r.1 == "Vpp").unwrap().2, "4 V?");
        assert!(table(&Measurements::default()).iter().all(|r| r.2 == "—"));
    }

    #[test]
    fn clipping_and_position() {
        let m = measure(&square(500, 250, -125, 125, 5000), &ch(), DT);
        assert!(m.clipped);
        // Moving the channel up 2 div shifts every voltage reading down by 2 V.
        let up = ChannelSettings { pos0: 50, ..ch() };
        let m = measure(&square(500, 250, 0, 100, 5000), &up, DT);
        assert_eq!(m.vtop, Some(2.0));
        assert_eq!(m.vbase, Some(-2.0));
        assert!(close(m.mean, 0.0, 1e-9) || m.mean.unwrap().abs() < 1e-9);
    }
}
