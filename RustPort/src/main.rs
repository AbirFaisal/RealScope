mod acquire;
mod autoset;
mod device;
mod math;
mod measure;
mod persist;
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
    if let Some(idx) = arg_value("--vdiv").and_then(|v| v.parse::<usize>().ok()) {
        for ch in &mut settings.ch {
            ch.vdiv_idx = idx.min(tables::VDIV_MV.len() - 1);
        }
    }
    if std::env::args().any(|a| a == "--ext") {
        settings.trigger.source = settings::EXT;
    }
    if std::env::args().any(|a| a == "--single") {
        settings.run = settings::RunMode::Single;
    }
    if std::env::args().any(|a| a == "--fall") {
        settings.trigger.slope = settings::Slope::Falling;
    }
    if let Some(level) = arg_value("--level").and_then(|v| v.parse().ok()) {
        settings.trigger.level_v = level;
    }
    if let Some(op) = arg_value("--math") {
        settings.math.on = true;
        settings.math.op = if op == "sub" { math::MathOp::Sub } else { math::MathOp::Add };
    }
    let mut scope = Scope::new(dev, cal);
    scope.apply(&settings)?;
    if std::env::args().any(|a| a == "--autoset") {
        let started = Instant::now();
        let (s, summary) = autoset::run(&mut scope, &settings, &mut |msg| println!("{msg}"))?;
        println!("{summary} ({:.1} s)", started.elapsed().as_secs_f64());
        settings = s;
        scope.apply(&settings)?;
    }
    settings.math.vdiv_idx = math::default_vdiv(&settings.ch);
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
    // Keep capturing briefly: the frequency counter is armed after the first
    // frame and reports in later ones.
    let start = Instant::now();
    let mut frame = None;
    while start.elapsed() < Duration::from_secs(3) {
        if let Some(f) = scope.acquire(&settings)? {
            let done = f.counter_hz.iter().any(Option::is_some) || start.elapsed() > Duration::from_millis(1500);
            frame = Some(f);
            if done {
                break;
            }
        }
        std::thread::sleep(Duration::from_millis(5));
    }
    let Some(frame) = frame else { bail!("no data within 3 s") };
    println!(
        "Captured at {}/div, {} (triggered: {})",
        tables::tdiv_label(settings.tdiv_idx),
        tables::format_si(tables::sample_rate_hz(settings.tdiv_idx), "Sa/s"),
        frame.triggered
    );
    let dt = 1.0 / tables::sample_rate_hz(settings.tdiv_idx);
    let print_table = |m: &measure::Measurements| {
        for row in measure::table(m).chunks(4) {
            let cells: Vec<String> = row.iter().map(|(_, label, value)| format!("{label:>9} {value:<11}")).collect();
            println!("  {}", cells.join(""));
        }
    };
    for (i, samples) in frame.samples.iter().enumerate() {
        let Some(samples) = samples else { continue };
        let ch = &settings.ch[i];
        let mut m = measure::measure(samples, ch, dt);
        m.counter = frame.counter_hz[i];
        println!("CH{} @ {}/div ({} samples):", i + 1, ch.vdiv_label(), samples.len());
        print_table(&m);
    }
    if let Some(t) = math::compute(frame.samples.each_ref().map(|s| s.as_deref()), &settings) {
        let mut m = measure::measure(&t.samples, &settings.math, dt);
        m.clipped |= t.sources_clipped;
        println!("Math {} @ {}/div ({} samples):", settings.math.op.label(), settings.math.vdiv_label(), t.samples.len());
        print_table(&m);
    }
    Ok(())
}
