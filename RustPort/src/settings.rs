//! User-facing scope configuration, shared between the UI and the acquisition thread.

use crate::tables::{ADC_MAX, COUNTS_PER_DIV, POS0_MAX, VDIV_MV, format_si};

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
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

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum Sweep {
    Auto,
    Normal,
}

#[derive(Clone, Copy, Debug, PartialEq)]
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

#[derive(Clone, Copy, Debug, PartialEq)]
pub struct TriggerSettings {
    /// 0 = CH1, 1 = CH2.
    pub source: usize,
    /// Level in volts at the probe tip of the source channel.
    pub level_v: f64,
    pub sweep: Sweep,
}

#[derive(Clone, Copy, Debug, PartialEq)]
pub struct Settings {
    pub ch: [ChannelSettings; 2],
    pub tdiv_idx: usize,
    pub trigger: TriggerSettings,
    pub running: bool,
}

impl Default for Settings {
    fn default() -> Self {
        let ch = ChannelSettings {
            on: true,
            vdiv_idx: 7, // 1 V/div
            coupling: Coupling::Dc,
            pos0: 0,
            probe: 1,
        };
        Settings {
            ch: [
                ChannelSettings { pos0: 50, ..ch },
                ChannelSettings { pos0: -75, ..ch },
            ],
            tdiv_idx: 15, // 500 µs/div
            trigger: TriggerSettings {
                source: 0,
                level_v: 1.0,
                sweep: Sweep::Auto,
            },
            running: true,
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

    /// Trigger level as an absolute ADC code, including the source channel's
    /// position (`EdgeTrigger.java`, `VoltsensableTrigger.java:69-70`).
    pub fn trigger_level_code(&self) -> i32 {
        let ch = &self.ch[self.trigger.source];
        let counts = (self.trigger.level_v / ch.volts_per_div() * COUNTS_PER_DIV as f64).round();
        (ch.pos0 + counts as i32).clamp(-ADC_MAX, ADC_MAX)
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
}
