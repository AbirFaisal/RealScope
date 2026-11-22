//! Acquisition thread: owns the USB device, applies settings, streams frames.

use std::sync::mpsc::{Receiver, RecvTimeoutError, Sender, TryRecvError};
use std::thread::{self, sleep};
use std::time::Duration;

use anyhow::Result;

use crate::device::Device;
use crate::protocol::{self, Block, Calibration, reg};
use crate::settings::{Coupling, Settings, Sweep};
use crate::tables::{ATTENUATION_VDIV_IDX, TDIV_MAX_IDX, TDIV_MIN_IDX, TIMEBASE_DIV};

/// One acquisition.
#[derive(Clone)]
pub struct Frame {
    /// Raw ADC samples per channel (screen position in counts, 0 = centre),
    /// `None` when the channel is off.
    pub samples: [Option<Vec<i8>>; 2],
    pub triggered: bool,
}

pub enum Event {
    Status(String),
    Connected { serial: String, version: String },
    Frame(Frame),
    Disconnected(String),
}

/// Spawns the worker. It keeps reconnecting until `settings_rx` is closed.
pub fn spawn(settings_rx: Receiver<Settings>, events: Sender<Event>) {
    thread::Builder::new()
        .name("acquire".into())
        .spawn(move || worker(settings_rx, events))
        .expect("spawning acquisition thread");
}

fn worker(settings_rx: Receiver<Settings>, events: Sender<Event>) {
    let mut settings = Settings::default();
    loop {
        let mut log = |msg: String| {
            let _ = events.send(Event::Status(msg));
        };
        match Device::connect(&mut log) {
            Ok((dev, cal)) => {
                // The USB serial descriptor is just "0" on these units; the board serial lives in flash.
                let _ = events.send(Event::Connected { serial: cal.serial.clone(), version: cal.version.clone() });
                let mut scope = Scope { dev, cal, applied: None };
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
        match settings_rx.recv_timeout(Duration::from_secs(1)) {
            Ok(s) => settings = s,
            Err(RecvTimeoutError::Timeout) => {}
            Err(RecvTimeoutError::Disconnected) => return,
        }
    }
}

pub struct Scope {
    pub dev: Device,
    pub cal: Calibration,
    applied: Option<Settings>,
}

impl Scope {
    pub fn new(dev: Device, cal: Calibration) -> Self {
        Scope { dev, cal, applied: None }
    }

    /// Main loop. Returns `Ok` when the UI closed the settings channel.
    fn run(&mut self, settings_rx: &Receiver<Settings>, events: &Sender<Event>, settings: &mut Settings) -> Result<()> {
        self.apply(settings)?;
        loop {
            loop {
                match settings_rx.try_recv() {
                    Ok(s) => *settings = s,
                    Err(TryRecvError::Empty) => break,
                    Err(TryRecvError::Disconnected) => return Ok(()),
                }
            }
            self.apply(settings)?;

            if !settings.running {
                match settings_rx.recv_timeout(Duration::from_millis(50)) {
                    Ok(s) => *settings = s,
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
                }
                None => sleep(Duration::from_millis(5)),
            }
        }
    }

    /// One trigger poll + fetch (`TrgStatusInvestigator.checkStatusForGetDataT`).
    pub fn acquire(&mut self, settings: &Settings) -> Result<Option<Frame>> {
        let ch_on = [settings.ch[0].on, settings.ch[1].on];
        if !ch_on[0] && !ch_on[1] {
            return Ok(None);
        }
        let trg_status = self.dev.write_reg(reg::TRG_D, 1, 0)? & protocol::chl_on_mask(ch_on) as i32;
        let finished = self.dev.write_reg(reg::DATAFINISHED, 1, 0)?;
        if settings.trigger.sweep == Sweep::Normal && trg_status == 0 && finished == 0 {
            return Ok(None);
        }

        let Some(blocks) = self.dev.fetch(ch_on)? else {
            return Ok(None);
        };
        let mut samples: [Option<Vec<i8>>; 2] = [None, None];
        for block in &blocks {
            if let Block::Data { channel, samples: s } = protocol::parse_block(block)? {
                let ch = &settings.ch[channel as usize];
                samples[channel as usize] = Some(if ch.coupling == Coupling::Gnd {
                    vec![ch.pos0 as i8; s.len()]
                } else {
                    s
                });
            }
        }
        Ok(Some(Frame { samples, triggered: trg_status != 0 }))
    }

    /// Writes whatever registers differ from the last applied settings.
    pub fn apply(&mut self, new: &Settings) -> Result<()> {
        let old = self.applied;
        if old.as_ref() == Some(new) {
            return Ok(());
        }
        let first = old.is_none();
        let was_running = old.map(|o| o.running).unwrap_or(false);

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

        let tdiv = new.tdiv_idx.clamp(TDIV_MIN_IDX, TDIV_MAX_IDX);
        if first || old.map(|o| o.tdiv_idx) != Some(new.tdiv_idx) {
            self.dev.write_reg(reg::TIMEBASE, 4, TIMEBASE_DIV[tdiv])?;
            self.dev.write_reg(reg::SAMPLE, 1, 0)?;
            self.dev.write_reg(reg::SLOWMOVE, 1, 0)?;
        }
        if first {
            self.dev.write_reg(reg::DM, 2, protocol::DM_LEN)?;
            let (pre, suf) = protocol::pre_suf_centered();
            self.dev.write_reg_bytewise(reg::PRE_TRG, 2, pre)?;
            self.dev.write_reg_bytewise(reg::SUF_TRG, 4, suf)?;
            self.dev.write_reg(reg::EMPTY, 1, 1)?;
        }

        let trg = new.trigger;
        let src = trg.source;
        if first || old.map(|o| (o.trigger.source, o.trigger.sweep)) != Some((src, trg.sweep)) {
            self.dev.write_reg(reg::TRG, 2, protocol::trigger(src, trg.sweep) as u32)?;
            // Holdoff goes high byte first (ClockTimeAdjuster.send), unlike cmd_bytewise.
            let h = protocol::HOLDOFF_DEFAULT;
            self.dev.write_reg(reg::HOLDOFF[src], 1, h >> 8)?;
            self.dev.write_reg(reg::HOLDOFF[src] + 1, 1, h & 0xff)?;
        }
        // The level is an absolute ADC code, so it moves with the source's vdiv and position.
        let level = new.trigger_level_code();
        if first || old.map(|o| (o.trigger.source, o.trigger_level_code())) != Some((src, level)) {
            self.dev.write_reg_bytewise(reg::EDGE_LEVEL[src], 2, protocol::edge_level(level))?;
        }

        if first || was_running != new.running {
            self.dev.command(&protocol::run_stop(new.running))?;
        }

        self.applied = Some(*new);
        Ok(())
    }
}
