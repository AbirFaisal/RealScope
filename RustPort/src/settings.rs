//! User-facing scope configuration, shared between the UI and the acquisition thread.

use crate::tables::{ADC_MAX, COUNTS_PER_DIV, POS0_MAX, VDIV_MV};

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
    pub fn volts_per_div(&self) -> f64 {
        VDIV_MV[self.vdiv_idx] as f64 / 1000.0
    }

    /// Voltage at a screen position given in ADC counts (0 = centre, ±125 = edges),
    /// probe ×1 (`WFO.java:66-77`).
    pub fn code_to_volts(&self, code: i32) -> f64 {
        (code - self.pos0) as f64 * self.volts_per_div() / COUNTS_PER_DIV as f64
    }

    pub fn clamp_pos0(pos0: i32) -> i32 {
        pos0.clamp(-POS0_MAX, POS0_MAX)
    }
}

impl Settings {
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
        let ch = ChannelSettings { on: true, vdiv_idx: 7, coupling: Coupling::Dc, pos0: 0 };
        assert_eq!(ch.code_to_volts(125), 5.0);
        assert_eq!(ch.code_to_volts(-125), -5.0);
        // Zero line moved up 2 div: top of screen is now 3 div above it.
        let up = ChannelSettings { pos0: 50, ..ch };
        assert_eq!(up.code_to_volts(125), 3.0);
        assert_eq!(up.code_to_volts(50), 0.0);
        let mv = ChannelSettings { vdiv_idx: 4, ..ch }; // 100 mV/div
        assert!((mv.code_to_volts(25) - 0.1).abs() < 1e-12);
    }
}
