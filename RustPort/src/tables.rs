//! VDS1022 parameter tables.
//!
//! Source: `com/owon/uppersoft/dso/model/machine/params/VDS1022ONE.txt` in the
//! Java reference (`src/OWON_VDS_v1.0.24`).

/// `[Voltbase]`, in millivolts per division.
pub const VDIV_MV: [u32; 10] = [5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000];

/// From this vdiv index on the input attenuator relay is switched in
/// (`VDS1022.java`: `DelayAttenuationVBIndex = 5`, relay engaged for `vb > 5`).
pub const ATTENUATION_VDIV_IDX: usize = 6;

/// `[Timebase]`, in seconds per division.
pub const TDIV_S: [f64; 32] = [
    5e-9, 10e-9, 20e-9, 50e-9, 100e-9, 200e-9, 500e-9, //
    1e-6, 2e-6, 5e-6, 10e-6, 20e-6, 50e-6, 100e-6, 200e-6, 500e-6, //
    1e-3, 2e-3, 5e-3, 10e-3, 20e-3, 50e-3, 100e-3, 200e-3, 500e-3, //
    1.0, 2.0, 5.0, 10.0, 20.0, 50.0, 100.0,
];

/// `[Tiny_SeperateFreq]`: value written to TIMEBASE (0x52); the ADC clock
/// (100 MS/s) is divided by it.
pub const TIMEBASE_DIV: [u32; 32] = [
    1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 4, 8, 20, 40, 80, 200, 400, 800, 2000, 4000, 8000, 20000, 40000,
    80000, 200000, 400000, 800000, 2000000, 4000000, 8000000, 20000000, 40000000,
];

pub const ADC_CLOCK_HZ: f64 = 100e6;

/// Timebases the app supports. From index 9 (5 µs) the 5000-point record fills
/// the screen; indices 6–8 run at the full 100 MS/s and show 1000–4000 real
/// points (`VDS1022.TB_1_4kFullScreen`). Below index 6 the vendor software
/// interpolates, and from `[SlowMoveTimebase]` (100 ms, index 22) the scope
/// switches to roll mode. Neither is ported yet.
pub const TDIV_MIN_IDX: usize = 6;
pub const TDIV_MAX_IDX: usize = 21;

/// Horizontal and vertical graticule (`GDefine.BlockNum`, `GDefine.PIXELS_PER_DIV`).
pub const H_DIVS: usize = 20;
pub const V_DIVS: usize = 10;
/// ADC counts per vertical division.
pub const COUNTS_PER_DIV: i32 = 25;
/// Samples are clamped to this range (`AbsPreHandler.ADC_MAX`).
pub const ADC_MAX: i32 = 125;
/// Channel position range in ADC counts (`MachineInfo_Tiny`).
pub const POS0_MAX: i32 = 250;

pub fn sample_rate_hz(tdiv_idx: usize) -> f64 {
    ADC_CLOCK_HZ / TIMEBASE_DIV[tdiv_idx] as f64
}

/// Samples spanning the 20-division screen: `min(5000, 20 × tdiv × 100 MS/s)`
/// (`MachineInfo.fillSampleRates`).
pub fn screen_points(tdiv_idx: usize) -> usize {
    (sample_rate_hz(tdiv_idx) * TDIV_S[tdiv_idx] * H_DIVS as f64).round() as usize
}

pub fn vdiv_label(idx: usize) -> String {
    let mv = VDIV_MV[idx];
    if mv >= 1000 {
        format!("{} V", mv / 1000)
    } else {
        format!("{mv} mV")
    }
}

pub fn tdiv_label(idx: usize) -> String {
    format_si(TDIV_S[idx], "s")
}

/// Time of vertical grid line `i` (0..=H_DIVS) relative to the trigger, which
/// sits at the centre of the record.
pub fn grid_time_s(tdiv_idx: usize, i: usize) -> f64 {
    (i as f64 - (H_DIVS / 2) as f64) * TDIV_S[tdiv_idx]
}

pub fn format_si(value: f64, unit: &str) -> String {
    // Snap float noise so the zero line reads "0 V", not "-0 V".
    let value = if value.abs() < 1e-12 { 0.0 } else { value };
    let abs = value.abs();
    let (scale, prefix) = if abs == 0.0 {
        (1.0, "")
    } else if abs < 1e-6 {
        (1e9, "n")
    } else if abs < 1e-3 {
        (1e6, "µ")
    } else if abs < 1.0 {
        (1e3, "m")
    } else if abs < 1e3 {
        (1.0, "")
    } else if abs < 1e6 {
        (1e-3, "k")
    } else {
        (1e-6, "M")
    };
    let v = value * scale;
    let text = format!("{v:.3}");
    let text = text.trim_end_matches('0').trim_end_matches('.');
    format!("{text} {prefix}{unit}")
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn screen_points_per_timebase() {
        // SR = min(100 MS/s, 5000 / (20 * tdiv)), MachineInfo.java:323-379
        assert_eq!(screen_points(6), 1000);
        assert_eq!(screen_points(7), 2000);
        assert_eq!(screen_points(8), 4000);
        for idx in 9..=TDIV_MAX_IDX {
            assert_eq!(screen_points(idx), 5000, "idx {idx}");
        }
        for idx in TDIV_MIN_IDX..=8 {
            assert_eq!(sample_rate_hz(idx), 100e6);
        }
        assert_eq!(sample_rate_hz(9), 50e6);
    }

    #[test]
    fn labels() {
        assert_eq!(vdiv_label(0), "5 mV");
        assert_eq!(vdiv_label(7), "1 V");
        assert_eq!(tdiv_label(9), "5 µs");
        assert_eq!(tdiv_label(16), "1 ms");
        assert_eq!(format_si(250e3, "Sa/s"), "250 kSa/s");
        assert_eq!(format_si(-0.0, "V"), "0 V");
        assert_eq!(format_si(-2.5e-3, "s"), "-2.5 ms");
    }

    #[test]
    fn grid_times() {
        assert_eq!(format_si(grid_time_s(16, 0), "s"), "-10 ms");
        assert_eq!(format_si(grid_time_s(16, H_DIVS / 2), "s"), "0 s");
        assert_eq!(format_si(grid_time_s(9, H_DIVS), "s"), "50 µs");
    }
}
