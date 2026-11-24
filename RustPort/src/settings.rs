//! User-facing scope configuration, shared between the UI and the acquisition thread.

use serde::{Deserialize, Serialize};

use crate::fft::FftSettings;
use crate::math::MathSettings;
use crate::measure::Scale;
use crate::tables::{ADC_MAX, COUNTS_PER_DIV, MATH_VDIV_V, POS0_MAX, TDIV_MAX_IDX, TDIV_MIN_IDX, VDIV_MV, format_si};

#[derive(Clone, Copy, Debug, PartialEq, Eq, Serialize, Deserialize)]
pub enum Coupling {
    Dc,
    Ac,
    Gnd,
}

impl Coupling {
    pub fn next(self) -> Self {
        match self {
            Coupling::Dc => Coupling::Ac,
            Coupling::Ac => Coupling::Gnd,
            Coupling::Gnd => Coupling::Dc,
        }
    }

    pub fn label(self) -> &'static str {
        match self {
            Coupling::Dc => "DC",
            Coupling::Ac => "AC",
            Coupling::Gnd => "GND",
        }
    }
}

#[derive(Clone, Copy, Debug, PartialEq, Eq, Serialize, Deserialize)]
pub enum Sweep {
    Auto,
    Normal,
}

/// Acquisition state, driven by the Run/Stop and Single buttons.
#[derive(Clone, Copy, Debug, PartialEq, Eq, Default)]
pub enum RunMode {
    #[default]
    Running,
    Stopped,
    /// Armed for one triggered capture, after which the scope stops.
    Single,
}

/// Which signal edge fires the trigger.
#[derive(Clone, Copy, Debug, PartialEq, Eq, Serialize, Deserialize, Default)]
pub enum Slope {
    #[default]
    Rising,
    Falling,
}

// `serde(default)`: fields missing from an older settings file keep their defaults.
#[derive(Clone, Copy, Debug, PartialEq, Serialize, Deserialize)]
#[serde(default)]
pub struct ChannelSettings {
    pub on: bool,
    pub vdiv_idx: usize,
    pub coupling: Coupling,
    /// Vertical position of the channel's 0 V line, in ADC counts (25 per div).
    pub pos0: i32,
    /// Probe attenuation (1 or 10). Display-only: the scope always sees the
    /// attenuated signal, readings are scaled to the probe tip (`MachineInfo.java:139-141`).
    pub probe: u32,
}

/// Trigger source index for the external trigger input, after the two
/// channels (the vendor's `TriggerControl.EXTTRG_CHANNEL`).
pub const EXT: usize = 2;

#[derive(Clone, Copy, Debug, PartialEq, Serialize, Deserialize)]
#[serde(default)]
pub struct TriggerSettings {
    /// 0 = CH1, 1 = CH2, [`EXT`] = external input.
    pub source: usize,
    /// Level in volts at the probe tip of the source channel (unused for EXT,
    /// whose threshold is fixed in hardware).
    pub level_v: f64,
    pub slope: Slope,
    pub sweep: Sweep,
}

#[derive(Clone, Copy, Debug, PartialEq, Serialize, Deserialize)]
#[serde(default)]
pub struct Settings {
    pub ch: [ChannelSettings; 2],
    pub tdiv_idx: usize,
    pub trigger: TriggerSettings,
    pub math: MathSettings,
    pub fft: FftSettings,
    /// Not saved: the app always starts acquiring.
    #[serde(skip)]
    pub run: RunMode,
}

impl Default for ChannelSettings {
    fn default() -> Self {
        ChannelSettings {
            on: true,
            vdiv_idx: 7, // 1 V/div
            coupling: Coupling::Dc,
            pos0: 0,
            probe: 1,
        }
    }
}

impl Default for TriggerSettings {
    fn default() -> Self {
        TriggerSettings {
            source: 0,
            level_v: 1.0,
            slope: Slope::Rising,
            sweep: Sweep::Auto,
        }
    }
}

impl Default for Settings {
    fn default() -> Self {
        let ch = ChannelSettings::default();
        Settings {
            ch: [
                ChannelSettings { pos0: 50, ..ch },
                ChannelSettings { pos0: -75, ..ch },
            ],
            tdiv_idx: 15, // 500 µs/div
            trigger: TriggerSettings::default(),
            math: MathSettings::default(),
            fft: FftSettings::default(),
            run: RunMode::Running,
        }
    }
}

impl ChannelSettings {
    /// Volts per division at the probe tip.
    pub fn volts_per_div(&self) -> f64 {
        VDIV_MV[self.vdiv_idx] as f64 / 1000.0 * self.probe as f64
    }

    /// V/div readout at the probe tip, e.g. "500 mV" or "10 V".
    pub fn vdiv_label(&self) -> String {
        format_si(self.volts_per_div(), "V")
    }

    /// Probe-tip voltage at a screen position given in ADC counts
    /// (0 = centre, ±125 = edges; `WFO.java:66-77`).
    pub fn code_to_volts(&self, code: i32) -> f64 {
        (code - self.pos0) as f64 * self.volts_per_div() / COUNTS_PER_DIV as f64
    }

    pub fn clamp_pos0(pos0: i32) -> i32 {
        pos0.clamp(-POS0_MAX, POS0_MAX)
    }
}

impl Settings {
    /// Brings values read from a settings file back into range, so a
    /// hand-edited or stale file can't index out of the tables.
    pub fn sanitize(mut self) -> Self {
        for ch in &mut self.ch {
            ch.vdiv_idx = ch.vdiv_idx.min(VDIV_MV.len() - 1);
            ch.pos0 = ChannelSettings::clamp_pos0(ch.pos0);
            if ch.probe != 1 && ch.probe != 10 {
                ch.probe = 1;
            }
        }
        self.tdiv_idx = self.tdiv_idx.clamp(TDIV_MIN_IDX, TDIV_MAX_IDX);
        if self.trigger.source > EXT {
            self.trigger.source = 0;
        }
        if !self.trigger.level_v.is_finite() {
            self.trigger.level_v = TriggerSettings::default().level_v;
        }
        self.math.vdiv_idx = self.math.vdiv_idx.min(MATH_VDIV_V.len() - 1);
        self.math.pos0 = ChannelSettings::clamp_pos0(self.math.pos0);
        self.fft.sanitize();
        self
    }

    /// Whether the scope captures channel `i`: when it is shown, or when the
    /// math trace or the spectrum needs it (a hidden source is still captured).
    pub fn captures(&self, i: usize) -> bool {
        self.ch[i].on || self.math.on || (self.fft.on && self.fft.source == i)
    }

    /// Screen scale of trace `i`: CH1, CH2, or 2 = Math.
    pub fn trace_scale(&self, i: usize) -> Scale {
        if i < self.ch.len() { (&self.ch[i]).into() } else { (&self.math).into() }
    }

    /// The settings as the scope hardware sees them: channels on when captured,
    /// math (PC-side only) left out so changing it writes nothing.
    pub fn hardware(&self) -> Settings {
        let mut hw = *self;
        for (i, ch) in hw.ch.iter_mut().enumerate() {
            ch.on = self.captures(i);
        }
        hw.math = MathSettings::default();
        hw.fft = FftSettings::default();
        hw
    }

    /// Changes a channel's probe attenuation. If it is the trigger source, the
    /// level is rescaled so the trigger stays at the same screen position.
    pub fn set_probe(&mut self, ch: usize, probe: u32) {
        let old = self.ch[ch].probe;
        if old == probe {
            return;
        }
        self.ch[ch].probe = probe;
        if self.trigger.source == ch {
            self.trigger.level_v *= probe as f64 / old as f64;
        }
    }

    /// Whether the scope should be capturing (running, or armed for a single shot).
    pub fn acquiring(&self) -> bool {
        matches!(self.run, RunMode::Running | RunMode::Single)
    }

    /// Whether captures only happen on a real trigger (no auto-trigger).
    pub fn waits_for_trigger(&self) -> bool {
        self.run == RunMode::Single || self.trigger.sweep == Sweep::Normal
    }

    /// The channel the trigger follows, or `None` for the external input.
    pub fn trigger_channel(&self) -> Option<usize> {
        (self.trigger.source < self.ch.len()).then_some(self.trigger.source)
    }

    /// Trigger level as an absolute ADC code, including the source channel's
    /// position (`EdgeTrigger.java`, `VoltsensableTrigger.java:69-70`).
    /// `None` for the external trigger, which has no adjustable level.
    pub fn trigger_level_code(&self) -> Option<i32> {
        let ch = &self.ch[self.trigger_channel()?];
        let counts = (self.trigger.level_v / ch.volts_per_div() * COUNTS_PER_DIV as f64).round();
        Some((ch.pos0 + counts as i32).clamp(-ADC_MAX, ADC_MAX))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn code_to_volts_follows_position() {
        let ch = ChannelSettings { on: true, vdiv_idx: 7, coupling: Coupling::Dc, pos0: 0, probe: 1 };
        assert_eq!(ch.code_to_volts(125), 5.0);
        assert_eq!(ch.code_to_volts(-125), -5.0);
        // Zero line moved up 2 div: top of screen is now 3 div above it.
        let up = ChannelSettings { pos0: 50, ..ch };
        assert_eq!(up.code_to_volts(125), 3.0);
        assert_eq!(up.code_to_volts(50), 0.0);
        let mv = ChannelSettings { vdiv_idx: 4, ..ch }; // 100 mV/div
        assert!((mv.code_to_volts(25) - 0.1).abs() < 1e-12);
    }

    #[test]
    fn probe_scales_readings() {
        let ch = ChannelSettings { on: true, vdiv_idx: 7, coupling: Coupling::Dc, pos0: 0, probe: 10 };
        assert_eq!(ch.volts_per_div(), 10.0);
        assert_eq!(ch.code_to_volts(25), 10.0);
        assert_eq!(ch.vdiv_label(), "10 V");
        let x1 = ChannelSettings { probe: 1, ..ch };
        assert_eq!(x1.vdiv_label(), "1 V");
        assert_eq!(ChannelSettings { vdiv_idx: 0, ..x1 }.vdiv_label(), "5 mV");
        assert_eq!(ChannelSettings { vdiv_idx: 0, ..ch }.vdiv_label(), "50 mV");
    }

    #[test]
    fn set_probe_keeps_trigger_on_screen() {
        let mut s = Settings::default();
        assert_eq!(s.trigger.source, 0);
        let code = s.trigger_level_code();
        s.set_probe(0, 10);
        assert_eq!(s.trigger.level_v, 10.0);
        assert_eq!(s.trigger_level_code(), code);
        // The other channel's probe doesn't move the CH1 trigger.
        s.set_probe(1, 10);
        assert_eq!(s.trigger.level_v, 10.0);
        s.set_probe(0, 1);
        assert_eq!(s.trigger.level_v, 1.0);
        assert_eq!(s.trigger_level_code(), code);
    }

    #[test]
    fn sanitize_clamps_out_of_range_values() {
        let mut s = Settings::default();
        s.ch[0].vdiv_idx = 99;
        s.ch[0].pos0 = 9999;
        s.ch[1].probe = 7;
        s.tdiv_idx = 2;
        s.trigger.source = 5;
        s.trigger.level_v = f64::NAN;
        s.math.vdiv_idx = 99;
        s.math.pos0 = -9999;
        let s = s.sanitize();
        assert_eq!(s.math.vdiv_idx, MATH_VDIV_V.len() - 1);
        assert_eq!(s.math.pos0, -POS0_MAX);
        assert_eq!(s.ch[0].vdiv_idx, VDIV_MV.len() - 1);
        assert_eq!(s.ch[0].pos0, POS0_MAX);
        assert_eq!(s.ch[1].probe, 1);
        assert_eq!(s.tdiv_idx, TDIV_MIN_IDX);
        assert_eq!(s.trigger.source, 0);
        assert_eq!(s.trigger.level_v, 1.0);
        assert_eq!(Settings::default().sanitize(), Settings::default());
    }

    #[test]
    fn math_captures_hidden_channels() {
        let mut s = Settings::default();
        s.ch[1].on = false;
        assert!(!s.hardware().ch[1].on);
        s.math.on = true;
        assert!(s.captures(1));
        let hw = s.hardware();
        assert!(hw.ch[0].on && hw.ch[1].on);
        // Math-only changes don't change what the hardware sees.
        let mut t = s;
        t.math.vdiv_idx = 3;
        t.math.pos0 = 40;
        assert_eq!(t.hardware(), hw);
    }

    #[test]
    fn fft_captures_its_hidden_source() {
        let mut s = Settings::default();
        s.ch[1].on = false;
        s.fft.on = true;
        s.fft.source = 1;
        assert!(s.hardware().ch[1].on);
        // Spectrum-only changes don't change what the hardware sees.
        let mut t = s;
        t.fft.zoom = 3;
        t.fft.ref_dbv = -10;
        assert_eq!(t.hardware(), s.hardware());
        s.fft.source = 0;
        assert!(!s.hardware().ch[1].on);
    }

    #[test]
    fn run_modes() {
        let mut s = Settings::default();
        assert!(s.acquiring() && !s.waits_for_trigger());
        s.trigger.sweep = Sweep::Normal;
        assert!(s.waits_for_trigger());
        s.trigger.sweep = Sweep::Auto;
        s.run = RunMode::Single;
        assert!(s.acquiring() && s.waits_for_trigger());
        s.run = RunMode::Stopped;
        assert!(!s.acquiring());
    }

    #[test]
    fn external_trigger_has_no_level() {
        let mut s = Settings::default();
        assert_eq!(s.trigger_channel(), Some(0));
        assert!(s.trigger_level_code().is_some());
        s.trigger.source = EXT;
        assert_eq!(s.trigger_channel(), None);
        assert_eq!(s.trigger_level_code(), None);
        // A probe change never touches the level while triggering externally.
        s.set_probe(0, 10);
        assert_eq!(s.trigger.level_v, 1.0);
    }
}
