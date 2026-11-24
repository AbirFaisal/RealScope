//! Auto-set: finds the signals and picks V/div, time/div, positions and the
//! trigger level. Follows the vendor's rules (`vds/auto/`: `AutosetRunner2`,
//! `WFAutoRoutine`, `jobs/*`), run as one routine on the acquisition thread.

use std::thread::sleep;
use std::time::{Duration, Instant};

use anyhow::{Result, bail};

use crate::acquire::{Frame, Scope};
use crate::measure;
use crate::settings::{ChannelSettings, Coupling, RunMode, Settings, Slope, Sweep, TriggerMode, TriggerSettings};
use crate::tables::{self, ADC_MAX, COUNTS_PER_DIV, H_DIVS, TDIV_MAX_IDX, TDIV_MIN_IDX, TDIV_S, VDIV_MV};

/// The search runs at 10 ms/div (`AutosetRunner2.TIMEBASE_10MS`).
const START_TDIV_IDX: usize = 19;
/// Waveform height in counts: 0.8 of the screen for one channel, 0.4 each for
/// two (`ArrangeChannelsJob.ampsArray`).
const SINGLE_TARGET: i32 = 200;
const DUAL_TARGET: i32 = 100;
/// Zero lines for two channels: CH1 two divisions up, CH2 two down.
const DUAL_POS0: [i32; 2] = [50, -50];
const MAX_ROUNDS: usize = 8;
/// How long to wait for the hardware frequency counter to report.
const COUNTER_WAIT: Duration = Duration::from_secs(2);

/// Largest distance of the waveform from its zero line, in counts
/// (`WFAutoRoutine.getHalfAmp`). Includes any DC offset, so a waveform this
/// tall either side of zero fits.
pub fn half_amp_counts(samples: &[i8], pos0: i32) -> i32 {
    samples.iter().map(|&s| (s as i32 - pos0).abs()).max().unwrap_or(0)
}

/// Whether the waveform hits the ADC rails, so its real size is unknown.
pub fn clipped(samples: &[i8]) -> bool {
    samples.iter().any(|&s| (s as i32).abs() >= ADC_MAX)
}

/// Peak-to-peak size around the zero line, in volts at the probe tip.
fn amp_v(samples: &[i8], ch: &ChannelSettings) -> f64 {
    2.0 * half_amp_counts(samples, ch.pos0) as f64 * ch.volts_per_div() / COUNTS_PER_DIV as f64
}

/// Midpoint of the waveform as an ADC code, for a 50 % trigger level.
fn mid_code(samples: &[i8]) -> i32 {
    let max = samples.iter().copied().max().unwrap_or(0) as i32;
    let min = samples.iter().copied().min().unwrap_or(0) as i32;
    (max + min) / 2
}

/// Most sensitive V/div index at which `amp_v` fits in `target` counts
/// (`ArrangeChannelsJob.computeVBIndex`); the least sensitive if none does.
pub fn vdiv_for(amp_v: f64, probe: u32, target: i32) -> usize {
    let span = |i: usize| target as f64 / COUNTS_PER_DIV as f64 * VDIV_MV[i] as f64 / 1000.0 * probe as f64;
    (0..VDIV_MV.len()).find(|&i| amp_v <= span(i)).unwrap_or(VDIV_MV.len() - 1)
}

/// Fastest supported time/div that shows at least 3 periods across the screen
/// (`ArrangeTimeBaseJob.timebase`).
pub fn tdiv_for(period_s: f64) -> usize {
    let min = 3.0 * period_s / H_DIVS as f64;
    (TDIV_MIN_IDX..=TDIV_MAX_IDX).find(|&i| TDIV_S[i] >= min * (1.0 - 1e-9)).unwrap_or(TDIV_MAX_IDX)
}

/// One step of the per-channel V/div search.
#[derive(Debug, PartialEq)]
pub enum Step {
    /// The current V/div is right; the waveform measured `amp_v` volts.
    Stay { amp_v: f64 },
    Move(usize),
    /// Less than one division at the most sensitive setting
    /// (`WFAutoRoutine.checkOff`).
    NoSignal,
}

pub fn next_vdiv(samples: &[i8], ch: &ChannelSettings, target: i32) -> Step {
    let idx = ch.vdiv_idx;
    let amp_v = amp_v(samples, ch);
    if clipped(samples) {
        return if idx + 1 < VDIV_MV.len() { Step::Move(idx + 1) } else { Step::Stay { amp_v } };
    }
    match vdiv_for(amp_v, ch.probe, target) {
        want if want != idx => Step::Move(want),
        _ if idx == 0 && half_amp_counts(samples, ch.pos0) < COUNTS_PER_DIV => Step::NoSignal,
        _ => Step::Stay { amp_v },
    }
}

#[derive(Clone, Copy, PartialEq)]
enum Found {
    Searching,
    Signal,
    Nothing,
}

/// Runs auto-set from `start` and returns the new settings with a one-line
/// summary. If no signal is found, `start` comes back unchanged.
pub fn run(scope: &mut Scope, start: &Settings, log: &mut dyn FnMut(String)) -> Result<(Settings, String)> {
    let last_vdiv = VDIV_MV.len() - 1;
    let mut s = *start;
    s.run = RunMode::Running;
    s.tdiv_idx = START_TDIV_IDX;
    // Back to one shared trigger (stored per-channel edges are kept).
    s.trigger =
        TriggerSettings { source: 0, level_v: 0.0, slope: Slope::Rising, sweep: Sweep::Auto, mode: TriggerMode::Shared, ..s.trigger };
    for ch in &mut s.ch {
        // The probe setting is kept: it describes the probe actually attached.
        *ch = ChannelSettings { on: true, coupling: Coupling::Dc, pos0: 0, vdiv_idx: last_vdiv, ..*ch };
    }

    // 1. V/div search on both channels at once, from the least sensitive setting down.
    log("Auto-set: looking for signals…".into());
    let mut found = [Found::Searching; 2];
    let mut amp = [0.0; 2];
    let mut visited = [0u32; 2];
    for _ in 0..MAX_ROUNDS {
        if !found.contains(&Found::Searching) {
            break;
        }
        let frame = capture(scope, &s)?;
        for c in 0..2 {
            if found[c] != Found::Searching {
                continue;
            }
            let Some(samples) = frame.samples[c].as_deref() else {
                found[c] = Found::Nothing;
                continue;
            };
            let idx = s.ch[c].vdiv_idx;
            visited[c] |= 1 << idx;
            amp[c] = amp_v(samples, &s.ch[c]);
            match next_vdiv(samples, &s.ch[c], SINGLE_TARGET) {
                // Going back down to a setting that already proved too small: stop here.
                Step::Move(want) if want < idx && visited[c] & (1 << want) != 0 => found[c] = Found::Signal,
                Step::Move(want) => s.ch[c].vdiv_idx = want,
                Step::Stay { .. } => found[c] = Found::Signal,
                Step::NoSignal => found[c] = Found::Nothing,
            }
        }
    }
    for f in &mut found {
        if *f == Found::Searching {
            *f = Found::Signal;
        }
    }
    let signals: Vec<usize> = (0..2).filter(|&c| found[c] == Found::Signal).collect();
    if signals.is_empty() {
        return Ok((*start, "Auto-set: no signal found on CH1 or CH2".into()));
    }

    // 2. Time/div from the slowest signal, which also becomes the trigger source.
    log("Auto-set: measuring frequency…".into());
    let mut slowest: Option<(usize, f64)> = None;
    for &c in &signals {
        if let Some(p) = measure_period(scope, &mut s, c)?
            && slowest.is_none_or(|(_, q)| p > q)
        {
            slowest = Some((c, p));
        }
    }
    let (trig_ch, tdiv) = match slowest {
        Some((c, p)) => (c, tdiv_for(p)),
        // DC only: nothing to fit to the screen, keep the user's timebase.
        None => (signals[0], start.tdiv_idx.clamp(TDIV_MIN_IDX, TDIV_MAX_IDX)),
    };
    s.tdiv_idx = tdiv;
    s.trigger.source = trig_ch;

    // 3. Final layout: one channel fills 8 divisions, two share the screen.
    log("Auto-set: arranging…".into());
    let target = if signals.len() == 1 { SINGLE_TARGET } else { DUAL_TARGET };
    for c in 0..2 {
        if found[c] != Found::Signal {
            s.ch[c].on = false;
            continue;
        }
        s.ch[c].pos0 = if signals.len() == 1 { 0 } else { DUAL_POS0[c] };
        s.ch[c].vdiv_idx = vdiv_for(amp[c], s.ch[c].probe, target);
    }
    // Peaks can read a little higher at the new timebase; widen anything that spills.
    let mut frame = capture(scope, &s)?;
    for _ in 0..3 {
        let mut changed = false;
        for &c in &signals {
            let Some(samples) = frame.samples[c].as_deref() else { continue };
            let spills = clipped(samples) || half_amp_counts(samples, s.ch[c].pos0) > target / 2 + COUNTS_PER_DIV / 2;
            if spills && s.ch[c].vdiv_idx < last_vdiv {
                s.ch[c].vdiv_idx += 1;
                changed = true;
            }
        }
        if !changed {
            break;
        }
        frame = capture(scope, &s)?;
    }

    // 4. Trigger at 50 % of the trigger channel (`WaveForm.setTrg50Percent`).
    if let Some(samples) = frame.samples[trig_ch].as_deref() {
        s.trigger.level_v = s.ch[trig_ch].code_to_volts(mid_code(samples));
    }

    let describe = |c: usize| {
        if s.ch[c].on { format!("CH{} {}/div", c + 1, s.ch[c].vdiv_label()) } else { format!("CH{} off", c + 1) }
    };
    let mut summary = format!(
        "Auto-set: {}, {}, {}/div, trigger CH{} ↑ {}",
        describe(0),
        describe(1),
        tables::tdiv_label(s.tdiv_idx),
        trig_ch + 1,
        tables::format_si(s.trigger.level_v, "V")
    );
    if slowest.is_none() {
        summary.push_str(" (no periodic signal; time/div unchanged)");
    }
    Ok((s, summary))
}

/// Period of channel `c` in seconds: from the hardware counter when it reports
/// (it runs at the full 100 MHz, so slow sampling can't alias it), otherwise
/// from the samples. Leaves the trigger on `c` at 50 %.
fn measure_period(scope: &mut Scope, s: &mut Settings, c: usize) -> Result<Option<f64>> {
    s.trigger.source = c;
    let frame = capture(scope, s)?;
    let Some(samples) = frame.samples[c].as_deref() else { return Ok(None) };
    s.trigger.level_v = s.ch[c].code_to_volts(mid_code(samples));
    let dt = 1.0 / tables::sample_rate_hz(s.tdiv_idx);
    let software = measure::measure(samples, &s.ch[c], dt).freq;

    // The counter is only valid once the channel triggers at the new level,
    // and only after acquire() has fed it the threshold.
    scope.apply(s)?;
    let deadline = Instant::now() + COUNTER_WAIT;
    while Instant::now() < deadline {
        match scope.acquire(s)? {
            Some(f) => {
                if let Some(hz) = f.counter_hz[c].filter(|hz| *hz > 0.0) {
                    return Ok(Some(1.0 / hz));
                }
            }
            None => sleep(Duration::from_millis(5)),
        }
    }
    Ok(software.filter(|f| *f > 0.0).map(|f| 1.0 / f))
}

/// Applies `s` and returns a frame captured entirely under it: the first one
/// after a change may have started before it, so it is skipped.
fn capture(scope: &mut Scope, s: &Settings) -> Result<Frame> {
    scope.apply(s)?;
    let deadline = Instant::now() + Duration::from_secs(4);
    let mut skipped = false;
    loop {
        if let Some(f) = scope.acquire(s)? {
            if skipped {
                return Ok(f);
            }
            skipped = true;
        } else {
            sleep(Duration::from_millis(5));
        }
        if Instant::now() > deadline {
            bail!("Auto-set: no data from the scope");
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn ch(vdiv_idx: usize, probe: u32) -> ChannelSettings {
        ChannelSettings { vdiv_idx, probe, ..ChannelSettings::default() }
    }

    #[test]
    fn vdiv_fits_target() {
        // 1 V peak-to-peak in 200 counts (8 div) needs >= 125 mV/div: 200 mV.
        assert_eq!(VDIV_MV[vdiv_for(1.0, 1, 200)], 200);
        // Exactly 8 div at 100 mV/div fits.
        assert_eq!(VDIV_MV[vdiv_for(0.8, 1, 200)], 100);
        // Two-channel target is half the height.
        assert_eq!(VDIV_MV[vdiv_for(0.8, 1, 100)], 200);
        // x10 probe: same signal, 10x the V/div at the tip for the same index.
        assert_eq!(VDIV_MV[vdiv_for(10.0, 10, 200)], 200);
        assert_eq!(vdiv_for(0.0, 1, 200), 0);
        assert_eq!(vdiv_for(1000.0, 1, 200), VDIV_MV.len() - 1);
    }

    #[test]
    fn tdiv_shows_three_periods() {
        // 1 kHz: 3 ms over 20 div needs >= 150 µs/div: 200 µs.
        assert_eq!(TDIV_S[tdiv_for(1e-3)], 200e-6);
        // ~147 Hz: 3 × 6.8 ms / 20 = 1.02 ms: 2 ms.
        assert_eq!(TDIV_S[tdiv_for(6.8e-3)], 2e-3);
        // Exact fit: 3 × 6.67 µs / 20 = 1 µs/div.
        assert_eq!(TDIV_S[tdiv_for(20e-6 / 3.0)], 1e-6);
        assert_eq!(tdiv_for(1e-9), TDIV_MIN_IDX);
        assert_eq!(tdiv_for(100.0), TDIV_MAX_IDX);
    }

    #[test]
    fn amplitude_includes_dc_offset() {
        assert_eq!(half_amp_counts(&[10, 20, 30], 0), 30);
        assert_eq!(half_amp_counts(&[-40, 0, 10], 0), 40);
        assert_eq!(half_amp_counts(&[40, 60], 50), 10);
        assert!(clipped(&[0, 125]) && clipped(&[-126, 0]) && !clipped(&[124, -124]));
        assert_eq!(mid_code(&[-20, 0, 60]), 20);
    }

    #[test]
    fn search_steps() {
        // Clipped: one step less sensitive.
        assert_eq!(next_vdiv(&[125, -125], &ch(5, 1), 200), Step::Move(6));
        // 0.5 V peaks at 5 V/div read as 3 counts (0.6 V pk-pk estimate): jump to 200 mV/div.
        assert_eq!(next_vdiv(&[3, -1], &ch(9, 1), 200), Step::Move(5));
        // At 200 mV/div the same signal is 65 counts: settled.
        match next_vdiv(&[65, -10], &ch(5, 1), 200) {
            Step::Stay { amp_v } => assert!((amp_v - 1.04).abs() < 1e-9),
            other => panic!("{other:?}"),
        }
        // Noise at the most sensitive setting: no signal.
        assert_eq!(next_vdiv(&[3, -2], &ch(0, 1), 200), Step::NoSignal);
        // A small but real signal at 5 mV/div stays.
        assert!(matches!(next_vdiv(&[40, -40], &ch(0, 1), 200), Step::Stay { .. }));
    }
}
