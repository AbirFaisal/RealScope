//! Acquisition thread: owns the USB device, applies settings, streams frames.

use std::sync::mpsc::{Receiver, RecvTimeoutError, Sender, TryRecvError};
use std::thread::{self, sleep};
use std::time::{Duration, Instant};

use anyhow::Result;

use crate::autoset;
use crate::device::Device;
use crate::protocol::{self, Block, Calibration, reg};
use crate::settings::{Coupling, EXT, RunMode, Settings};
use crate::tables::{ATTENUATION_VDIV_IDX, TDIV_MAX_IDX, TDIV_MIN_IDX, TIMEBASE_DIV};

/// One acquisition.
#[derive(Clone)]
pub struct Frame {
    /// Raw ADC samples per channel (screen position in counts, 0 = centre),
    /// `None` when the channel is off.
    pub samples: [Option<Vec<i8>>; 2],
    /// Hardware frequency counter per channel, kept only when that channel
    /// triggered (the counter is meaningless otherwise).
    pub counter_hz: [Option<f64>; 2],
    pub triggered: bool,
    /// Which channels' own triggers fired (both can in per-channel mode).
    pub channel_triggered: [bool; 2],
}

/// Requests from the UI to the acquisition thread.
pub enum Command {
    /// Use these settings from now on.
    Apply(Settings),
    /// Run auto-set; the result comes back as [`Event::AutosetDone`].
    Autoset,
}

pub enum Event {
    Status(String),
    Connected { serial: String, version: String },
    Frame(Frame),
    /// A single-shot capture completed (sent after its `Frame`); the worker has stopped.
    SingleDone,
    /// Auto-set finished; the worker is now running with `settings`.
    AutosetDone { settings: Settings, summary: String },
    Disconnected(String),
}

/// Spawns the worker, starting from `initial` settings. It keeps reconnecting
/// until `settings_rx` is closed.
pub fn spawn(initial: Settings, settings_rx: Receiver<Command>, events: Sender<Event>) {
    thread::Builder::new()
        .name("acquire".into())
        .spawn(move || worker(initial, settings_rx, events))
        .expect("spawning acquisition thread");
}

fn worker(mut settings: Settings, settings_rx: Receiver<Command>, events: Sender<Event>) {
    loop {
        let mut log = |msg: String| {
            let _ = events.send(Event::Status(msg));
        };
        match Device::connect(&mut log) {
            Ok((dev, cal)) => {
                // The USB serial descriptor is just "0" on these units; the board serial lives in flash.
                let _ = events.send(Event::Connected { serial: cal.serial.clone(), version: cal.version.clone() });
                let mut scope = Scope::new(dev, cal);
                match scope.run(&settings_rx, &events, &mut settings) {
                    Ok(()) => return, // UI went away
                    Err(e) => {
                        let _ = events.send(Event::Disconnected(format!("{e:#}")));
                    }
                }
            }
            Err(e) => {
                let _ = events.send(Event::Disconnected(format!("{e:#}")));
            }
        }
        // Wait before retrying, but keep tracking settings and notice shutdown.
        // Auto-set needs a scope, so a request made now is dropped.
        match settings_rx.recv_timeout(Duration::from_secs(1)) {
            Ok(Command::Apply(s)) => settings = s,
            Ok(Command::Autoset) => {}
            Err(RecvTimeoutError::Timeout) => {}
            Err(RecvTimeoutError::Disconnected) => return,
        }
    }
}

/// Takes a UI command: new settings replace the current ones. Returns whether
/// auto-set was requested.
fn receive(cmd: Command, settings: &mut Settings) -> bool {
    match cmd {
        Command::Apply(s) => {
            *settings = s;
            false
        }
        Command::Autoset => true,
    }
}

pub struct Scope {
    pub dev: Device,
    pub cal: Calibration,
    applied: Option<Settings>,
    /// When the frequency-counter thresholds were last written.
    counter_fed: Option<Instant>,
}

impl Scope {
    pub fn new(dev: Device, cal: Calibration) -> Self {
        Scope { dev, cal, applied: None, counter_fed: None }
    }

    /// Main loop. Returns `Ok` when the UI closed the settings channel.
    fn run(&mut self, settings_rx: &Receiver<Command>, events: &Sender<Event>, settings: &mut Settings) -> Result<()> {
        self.apply(settings)?;
        let mut autoset_requested = false;
        loop {
            loop {
                match settings_rx.try_recv() {
                    Ok(cmd) => autoset_requested |= receive(cmd, settings),
                    Err(TryRecvError::Empty) => break,
                    Err(TryRecvError::Disconnected) => return Ok(()),
                }
            }
            if std::mem::take(&mut autoset_requested) {
                let mut log = |msg: String| {
                    let _ = events.send(Event::Status(msg));
                };
                let (new, summary) = autoset::run(self, settings, &mut log)?;
                *settings = new;
                let _ = events.send(Event::AutosetDone { settings: new, summary });
            }
            self.apply(settings)?;

            if !settings.acquiring() {
                match settings_rx.recv_timeout(Duration::from_millis(50)) {
                    Ok(cmd) => autoset_requested |= receive(cmd, settings),
                    Err(RecvTimeoutError::Timeout) => {}
                    Err(RecvTimeoutError::Disconnected) => return Ok(()),
                }
                continue;
            }

            match self.acquire(settings)? {
                Some(frame) => {
                    if events.send(Event::Frame(frame)).is_err() {
                        return Ok(());
                    }
                    if settings.run == RunMode::Single {
                        // One shot taken: the next apply() stops the scope and
                        // restores the user's sweep.
                        settings.run = RunMode::Stopped;
                        let _ = events.send(Event::SingleDone);
                    }
                }
                None => sleep(Duration::from_millis(5)),
            }
        }
    }

    /// One trigger poll + fetch (`TrgStatusInvestigator.checkStatusForGetDataT`).
    pub fn acquire(&mut self, settings: &Settings) -> Result<Option<Frame>> {
        let ch_on = [settings.captures(0), settings.captures(1)];
        if !ch_on[0] && !ch_on[1] {
            return Ok(None);
        }
        let trg_status = self.dev.write_reg(reg::TRG_D, 1, 0)? & protocol::chl_on_mask(ch_on) as i32;
        let finished = self.dev.write_reg(reg::DATAFINISHED, 1, 0)?;
        if settings.waits_for_trigger() && trg_status == 0 && finished == 0 {
            return Ok(None);
        }

        let Some(blocks) = self.fetch(ch_on, settings.run == RunMode::Single)? else {
            return Ok(None);
        };
        let mut samples: [Option<Vec<i8>>; 2] = [None, None];
        let mut counter_hz = [None, None];
        for block in &blocks {
            if let Block::Data { channel, samples: s, counter_hz: c } = protocol::parse_block(block, settings.tdiv_idx)? {
                if trg_status >> channel & 1 != 0 {
                    counter_hz[channel as usize] = c;
                }
                let ch = &settings.ch[channel as usize];
                samples[channel as usize] = Some(if ch.coupling == Coupling::Gnd {
                    vec![ch.pos0 as i8; s.len()]
                } else {
                    s
                });
            }
        }
        // The vendor app refreshes the counter thresholds every 2 s.
        if self.counter_fed.is_none_or(|t| t.elapsed() > Duration::from_secs(2)) {
            for (ch, s) in samples.iter().enumerate() {
                if let Some(mid) = s.as_deref().and_then(protocol::counter_threshold) {
                    self.dev.write_reg(reg::FREQ_REF[ch], 1, mid as u32)?;
                }
            }
            self.counter_fed = Some(Instant::now());
        }
        // TRG_D doesn't report the external input; in Normal sweep data only
        // arrives on a trigger, so a frame means it fired.
        let ext_fired = !settings.per_channel_trigger() && settings.trigger.source == EXT && settings.waits_for_trigger();
        let channel_triggered = [0, 1].map(|ch| trg_status >> ch & 1 != 0);
        Ok(Some(Frame { samples, counter_hz, triggered: trg_status != 0 || ext_fired, channel_triggered }))
    }

    /// Fetches the data blocks. A single shot has already triggered and the
    /// next poll may no longer say so, so keep retrying through EBUSY rather
    /// than lose the capture (`ChannelsTransportInfo_Tiny.fetchUntilData`).
    fn fetch(&mut self, ch_on: [bool; 2], until_data: bool) -> Result<Option<Vec<Vec<u8>>>> {
        let deadline = Instant::now() + Duration::from_secs(1);
        loop {
            let blocks = self.dev.fetch(ch_on)?;
            if blocks.is_some() || !until_data || Instant::now() > deadline {
                return Ok(blocks);
            }
            sleep(Duration::from_millis(2));
        }
    }

    /// Writes whatever registers differ from the last applied settings.
    pub fn apply(&mut self, new: &Settings) -> Result<()> {
        // Compare and store the hardware view, so math-only changes write nothing.
        let new = &new.hardware();
        let old = self.applied;
        if old.as_ref() == Some(new) {
            return Ok(());
        }
        let first = old.is_none();
        let was_acquiring = old.is_some_and(|o| o.acquiring());

        let on = [new.ch[0].on, new.ch[1].on];
        if first || old.map(|o| [o.ch[0].on, o.ch[1].on]) != Some(on) {
            self.dev.write_reg(reg::CHL_ON, 1, protocol::chl_on_mask(on) as u32)?;
        }

        for i in 0..2 {
            let ch = new.ch[i];
            let prev = old.map(|o| o.ch[i]);
            if prev == Some(ch) {
                continue;
            }
            let vb = ch.vdiv_idx;
            let vdiv_changed = prev.map(|p| p.vdiv_idx) != Some(vb);
            if first || vdiv_changed || prev.map(|p| (p.on, p.coupling)) != Some((ch.on, ch.coupling)) {
                self.dev.write_reg(reg::CHANNEL_SET[i], 1, protocol::channel_set(&ch) as u32)?;
            }
            if first || vdiv_changed {
                self.dev.write_reg(reg::VOLT_GAIN[i], 2, self.cal.gain[i][vb] as u32)?;
                let crossed = prev.is_some_and(|p| (p.vdiv_idx >= ATTENUATION_VDIV_IDX) != (vb >= ATTENUATION_VDIV_IDX));
                if crossed {
                    // Let the attenuator relay settle (Submitor2.java:137-150).
                    sleep(Duration::from_millis(200));
                }
            }
            if first || vdiv_changed || prev.map(|p| p.pos0) != Some(ch.pos0) {
                let v = protocol::zero_offset(self.cal.compensation[i][vb], self.cal.step[i][vb], ch.pos0);
                self.dev.write_reg(reg::ZERO_OFFSET[i], 2, v as u32 & 0xffff)?;
            }
        }

        if first {
            self.dev.write_reg(reg::DM, 2, protocol::DM_LEN)?;
        }
        let tdiv = new.tdiv_idx.clamp(TDIV_MIN_IDX, TDIV_MAX_IDX);
        if first || old.map(|o| o.tdiv_idx) != Some(new.tdiv_idx) {
            self.dev.write_reg(reg::TIMEBASE, 4, TIMEBASE_DIV[tdiv])?;
            self.dev.write_reg(reg::SAMPLE, 1, 0)?;
            self.dev.write_reg(reg::SLOWMOVE, 1, 0)?;
            // The trigger window depends on how many points fill the screen.
            let (pre, suf) = protocol::pre_suf(tdiv);
            self.dev.write_reg_bytewise(reg::PRE_TRG, 2, pre)?;
            self.dev.write_reg_bytewise(reg::SUF_TRG, 4, suf)?;
            self.dev.write_reg(reg::EMPTY, 1, 1)?;
        }

        if new.per_channel_trigger() {
            // Each channel gets its own TRG word, holdoff and level, followed by
            // an EMPTY write, in the vendor's order (TinyTrgSubmitHandler.selfSubmit_Tiny).
            let key = |s: &Settings| (s.trigger.mode, [0, 1].map(|c| (s.edge(c).map(|e| e.slope), s.level_code(c))));
            if first || old.as_ref().map(key) != Some(key(new)) {
                for ch in 0..2 {
                    let (Some(edge), Some(code)) = (new.edge(ch), new.level_code(ch)) else { continue };
                    self.dev.write_reg(reg::TRG, 2, protocol::trigger_alt(ch, edge.slope) as u32)?;
                    self.write_holdoff(ch)?;
                    self.dev.write_reg_bytewise(reg::EDGE_LEVEL[ch], 2, protocol::edge_level(code, edge.slope))?;
                    self.dev.write_reg(reg::EMPTY, 1, 0)?;
                }
            }
        } else {
            self.apply_shared_trigger(old, new, first)?;
        }

        // Stopped -> Single sends Run, which re-arms the trigger (TRG was written above).
        if first || was_acquiring != new.acquiring() {
            self.dev.command(&protocol::run_stop(new.acquiring()))?;
        }

        self.applied = Some(*new);
        // Scale or position changed, so the counter threshold moved: refresh it on the next frame.
        self.counter_fed = None;
        Ok(())
    }

    /// Holdoff goes high byte first (ClockTimeAdjuster.send), unlike cmd_bytewise.
    fn write_holdoff(&mut self, ch: usize) -> Result<()> {
        let h = protocol::HOLDOFF_DEFAULT;
        self.dev.write_reg(reg::HOLDOFF[ch], 1, h >> 8)?;
        self.dev.write_reg(reg::HOLDOFF[ch] + 1, 1, h & 0xff)?;
        Ok(())
    }

    /// One trigger source for the whole capture. The keys include the mode,
    /// so coming back from per-channel mode rewrites everything.
    fn apply_shared_trigger(&mut self, old: Option<Settings>, new: &Settings, first: bool) -> Result<()> {
        let trg = new.trigger;
        let src = trg.source;
        let single = new.run == RunMode::Single;
        let trg_key =
            |s: &Settings| (s.trigger.mode, s.trigger.source, s.trigger.slope, s.trigger.sweep, s.run == RunMode::Single);
        if first || old.as_ref().map(trg_key) != Some(trg_key(new)) {
            if src == EXT && (first || old.map(|o| o.trigger.source) != Some(EXT)) {
                // Make the shared I/O connector an input. Like the vendor app, it is
                // not switched back to an output afterwards, so it never drives a
                // signal into a source that is still attached.
                self.dev.write_reg(reg::MULTI_IO, 1, protocol::MULTI_IO_TRIGGER_IN)?;
            }
            self.dev.write_reg(reg::TRG, 2, protocol::trigger(src, trg.slope, trg.sweep, single) as u32)?;
            self.write_holdoff(src)?;
        }
        // The level is an absolute ADC code, so it moves with the source's vdiv and position.
        // (The external input has a fixed threshold, so there's nothing to write.)
        let level = new.trigger_level_code();
        if let Some(code) = level
            && (first
                || old.map(|o| (o.trigger.mode, o.trigger.source, o.trigger.slope, o.trigger_level_code()))
                    != Some((trg.mode, src, trg.slope, level)))
        {
            self.dev.write_reg_bytewise(reg::EDGE_LEVEL[src], 2, protocol::edge_level(code, trg.slope))?;
        }
        Ok(())
    }
}
