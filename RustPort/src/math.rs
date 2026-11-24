//! Math trace: CH1 + CH2 or CH1 − CH2, computed on the PC from the two
//! channels' screen codes (`CompositeWaveForm.complexCompute`), in volts at
//! the probe tips, and drawn with its own V/div and position.

use serde::{Deserialize, Serialize};

use crate::settings::{ChannelSettings, Settings};
use crate::tables::{ADC_MAX, COUNTS_PER_DIV, MATH_VDIV_V, format_si};

#[derive(Clone, Copy, Debug, PartialEq, Eq, Serialize, Deserialize, Default)]
pub enum MathOp {
    #[default]
    Add,
    Sub,
}

impl MathOp {
    pub const ALL: [MathOp; 2] = [MathOp::Add, MathOp::Sub];

    pub fn label(self) -> &'static str {
        match self {
            MathOp::Add => "CH1 + CH2",
            MathOp::Sub => "CH1 − CH2",
        }
    }
}

#[derive(Clone, Copy, Debug, PartialEq, Serialize, Deserialize)]
#[serde(default)]
pub struct MathSettings {
    pub on: bool,
    pub op: MathOp,
    /// Index into [`MATH_VDIV_V`].
    pub vdiv_idx: usize,
    /// Screen position of the math trace's 0 V line, in counts (25 per div).
    pub pos0: i32,
}

impl Default for MathSettings {
    fn default() -> Self {
        MathSettings { on: false, op: MathOp::Add, vdiv_idx: 9, pos0: 0 } // 1 V/div
    }
}

impl MathSettings {
    pub fn volts_per_div(&self) -> f64 {
        MATH_VDIV_V[self.vdiv_idx]
    }

    pub fn vdiv_label(&self) -> String {
        format_si(self.volts_per_div(), "V")
    }

    /// Volts at a screen position given in counts.
    pub fn code_to_volts(&self, code: i32) -> f64 {
        (code - self.pos0) as f64 * self.volts_per_div() / COUNTS_PER_DIV as f64
    }
}

/// Index of the table entry closest to `volts` (on a log scale).
fn nearest_idx(volts: f64) -> usize {
    (0..MATH_VDIV_V.len())
        .min_by(|&a, &b| {
            let d = |i: usize| (MATH_VDIV_V[i] / volts).ln().abs();
            d(a).total_cmp(&d(b))
        })
        .unwrap()
}

/// Starting math V/div (`MathControl.judgeVBidx`): the channels' V/div when
/// they agree, otherwise one step below the larger one.
pub fn default_vdiv(ch: &[ChannelSettings; 2]) -> usize {
    let a = nearest_idx(ch[0].volts_per_div());
    let b = nearest_idx(ch[1].volts_per_div());
    if a == b { a } else { a.max(b) - 1 }
}

pub struct MathTrace {
    /// Screen codes at the math V/div and position, clamped to the screen.
    pub samples: Vec<i8>,
    /// A source channel sat at the screen edge, so its true value (and the
    /// math result there) is unknown.
    pub sources_clipped: bool,
}

/// The math trace for one capture, or `None` when math is off or a channel's
/// record is missing.
pub fn compute(samples: [Option<&[i8]>; 2], s: &Settings) -> Option<MathTrace> {
    if !s.math.on {
        return None;
    }
    let [Some(a), Some(b)] = samples else { return None };
    let k = s.ch.map(|c| c.volts_per_div() / COUNTS_PER_DIV as f64);
    let sign = match s.math.op {
        MathOp::Add => 1.0,
        MathOp::Sub => -1.0,
    };
    let counts_per_volt = COUNTS_PER_DIV as f64 / s.math.volts_per_div();
    let (p1, p2) = (s.ch[0].pos0, s.ch[1].pos0);
    let mut sources_clipped = false;
    let samples = a
        .iter()
        .zip(b)
        .map(|(&c1, &c2)| {
            sources_clipped |= [c1, c2].iter().any(|&c| (c as i32).abs() >= ADC_MAX);
            let v = (c1 as i32 - p1) as f64 * k[0] + sign * (c2 as i32 - p2) as f64 * k[1];
            let code = (s.math.pos0 as f64 + v * counts_per_volt).round();
            code.clamp(-ADC_MAX as f64, ADC_MAX as f64) as i8
        })
        .collect();
    Some(MathTrace { samples, sources_clipped })
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::measure::measure;

    fn settings(op: MathOp, vdiv: [usize; 2], probe: [u32; 2], math_vdiv: usize) -> Settings {
        let mut s = Settings::default();
        for i in 0..2 {
            s.ch[i].vdiv_idx = vdiv[i];
            s.ch[i].probe = probe[i];
            s.ch[i].pos0 = 0;
        }
        s.math = MathSettings { on: true, op, vdiv_idx: math_vdiv, pos0: 0 };
        s
    }

    #[test]
    fn sum_and_difference() {
        // Both 1 V/div, math 1 V/div: codes add and subtract directly.
        let a = [10i8, 20, -30];
        let b = [5i8, -5, 10];
        let sum = compute([Some(&a), Some(&b)], &settings(MathOp::Add, [7, 7], [1, 1], 9)).unwrap();
        assert_eq!(sum.samples, vec![15, 15, -20]);
        let diff = compute([Some(&a), Some(&b)], &settings(MathOp::Sub, [7, 7], [1, 1], 9)).unwrap();
        assert_eq!(diff.samples, vec![5, 25, -40]);
        assert!(!diff.sources_clipped);
    }

    #[test]
    fn scales_with_probe_and_vdiv() {
        // CH1 500 mV/div x1, CH2 50 mV/div x10 (= 500 mV/div at the tip); math 1 V/div.
        let mut s = settings(MathOp::Sub, [6, 3], [1, 10], 9);
        s.ch[0].pos0 = 25;
        s.ch[1].pos0 = -25;
        s.math.pos0 = 10;
        // CH1 code 100: 75 counts = 3 div = 1.5 V. CH2 code 0: 25 counts = 0.5 V.
        // 1.5 − 0.5 = 1 V = 25 counts at 1 V/div, plus the math position.
        let t = compute([Some(&[100]), Some(&[0])], &s).unwrap();
        assert_eq!(t.samples, vec![35]);
        let m = measure(&t.samples, &s.math, 1e-6);
        assert!((m.vmax.unwrap() - 1.0).abs() < 1e-9);
    }

    #[test]
    fn clamps_and_flags_clipping() {
        let s = settings(MathOp::Add, [7, 7], [1, 1], 9);
        let t = compute([Some(&[100, 0]), Some(&[100, 125])], &s).unwrap();
        assert_eq!(t.samples, vec![125, 125]);
        assert!(t.sources_clipped);
        assert!(measure(&t.samples, &s.math, 1e-6).clipped);
        let t = compute([Some(&[100]), Some(&[100])], &s).unwrap();
        assert!(!t.sources_clipped, "the result clipping is caught by measure, not here");
    }

    #[test]
    fn missing_channel_or_off_gives_none() {
        let mut s = settings(MathOp::Add, [7, 7], [1, 1], 9);
        assert!(compute([Some(&[1]), None], &s).is_none());
        s.math.on = false;
        assert!(compute([Some(&[1]), Some(&[1])], &s).is_none());
    }

    #[test]
    fn unequal_lengths_use_the_shorter() {
        let s = settings(MathOp::Add, [7, 7], [1, 1], 9);
        assert_eq!(compute([Some(&[1, 2, 3]), Some(&[1, 1])], &s).unwrap().samples, vec![2, 3]);
    }

    #[test]
    fn default_vdiv_follows_vendor_rule() {
        let ch = |vdiv_idx, probe| ChannelSettings { vdiv_idx, probe, ..ChannelSettings::default() };
        let label = |i: usize| format_si(MATH_VDIV_V[i], "V");
        assert_eq!(label(default_vdiv(&[ch(7, 1), ch(7, 1)])), "1 V");
        assert_eq!(label(default_vdiv(&[ch(7, 1), ch(5, 1)])), "500 mV");
        assert_eq!(label(default_vdiv(&[ch(7, 10), ch(7, 1)])), "5 V");
        assert_eq!(label(default_vdiv(&[ch(9, 10), ch(9, 10)])), "50 V");
        assert_eq!(label(default_vdiv(&[ch(0, 1), ch(0, 1)])), "5 mV");
        assert_eq!(label(default_vdiv(&[ch(0, 1), ch(1, 1)])), "5 mV");
    }
}
