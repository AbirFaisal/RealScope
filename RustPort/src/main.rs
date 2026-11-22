mod acquire;
mod device;
mod phosphor;
mod protocol;
mod settings;
mod tables;
mod ui;

use std::time::{Duration, Instant};

use anyhow::{Result, bail};

use acquire::Scope;
use device::Device;
use settings::Settings;

fn arg_value(flag: &str) -> Option<String> {
    let args: Vec<String> = std::env::args().collect();
    args.iter().position(|a| a == flag).and_then(|i| args.get(i + 1).cloned())
}

fn dump_path() -> Option<String> {
    arg_value("--dump")
}

fn main() -> Result<()> {
    let args: Vec<String> = std::env::args().skip(1).collect();
    if args.iter().any(|a| a == "--probe") {
        return probe(args.iter().any(|a| a == "--capture"));
    }
    ui::run();
    Ok(())
}

/// Command-line handshake check: connect, dump calibration, optionally grab one frame.
fn probe(capture: bool) -> Result<()> {
    let (dev, cal) = Device::connect(&mut |msg| println!("{msg}"))?;
    println!("Serial (USB): {}", dev.serial);
    println!("Board: version {:?}, serial {:?}, phase fine 0x{:04x}", cal.version, cal.serial, cal.phase_fine);
    for (name, table) in [("gain", &cal.gain), ("step", &cal.step), ("compensation", &cal.compensation)] {
        for (ch, row) in table.iter().enumerate() {
            println!("{name:>12} CH{}: {row:?}", ch + 1);
        }
    }
    if !capture {
        return Ok(());
    }

    let mut settings = Settings::default();
    if let Some(idx) = arg_value("--tdiv").and_then(|v| v.parse().ok()) {
        settings.tdiv_idx = usize::clamp(idx, tables::TDIV_MIN_IDX, tables::TDIV_MAX_IDX);
    }
    let mut scope = Scope::new(dev, cal);
    scope.apply(&settings)?;
    if let Some(path) = dump_path() {
        // Raw GETDATA blocks, for protocol debugging.
        let deadline = Instant::now() + Duration::from_secs(3);
        let blocks = loop {
            if let Some(b) = scope.dev.fetch([true, true])? {
                break b;
            }
            if Instant::now() > deadline {
                bail!("no data within 3 s");
            }
            std::thread::sleep(Duration::from_millis(5));
        };
        std::fs::write(&path, blocks.concat())?;
        println!("Wrote {} blocks to {path}", blocks.len());
        return Ok(());
    }
    let deadline = Instant::now() + Duration::from_secs(3);
    let frame = loop {
        if let Some(frame) = scope.acquire(&settings)? {
            break frame;
        }
        if Instant::now() > deadline {
            bail!("no data within 3 s");
        }
        std::thread::sleep(Duration::from_millis(5));
    };
    println!(
        "Captured at {}/div, {} (triggered: {})",
        tables::tdiv_label(settings.tdiv_idx),
        tables::format_si(tables::sample_rate_hz(settings.tdiv_idx), "Sa/s"),
        frame.triggered
    );
    for (i, samples) in frame.samples.iter().enumerate() {
        let Some(samples) = samples else { continue };
        let ch = &settings.ch[i];
        let volts: Vec<f64> = samples.iter().map(|&s| ch.sample_to_volts(s)).collect();
        let min = volts.iter().cloned().fold(f64::INFINITY, f64::min);
        let max = volts.iter().cloned().fold(f64::NEG_INFINITY, f64::max);
        let mean = volts.iter().sum::<f64>() / volts.len() as f64;
        println!(
            "CH{} @ {}/div: min {min:.3} V  max {max:.3} V  mean {mean:.3} V  ({} samples)",
            i + 1,
            tables::vdiv_label(ch.vdiv_idx),
            volts.len()
        );
    }
    Ok(())
}
