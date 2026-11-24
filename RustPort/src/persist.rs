//! Saves the user's settings between launches, as JSON in the platform config
//! directory (`~/Library/Application Support/RealScope/settings.json` on macOS).

use std::fs;
use std::io;
use std::path::{Path, PathBuf};

use serde::{Deserialize, Serialize};

use crate::phosphor::Persistence;
use crate::settings::Settings;

/// Everything restored at startup: scope settings plus display state.
#[derive(Clone, Copy, Debug, PartialEq, Serialize, Deserialize)]
#[serde(default)]
pub struct Saved {
    pub scope: Settings,
    pub measurements_on: bool,
    pub phosphor_on: bool,
    pub persistence: Persistence,
}

impl Default for Saved {
    fn default() -> Self {
        Saved {
            scope: Settings::default(),
            measurements_on: true,
            phosphor_on: false,
            persistence: Persistence::Medium,
        }
    }
}

fn path() -> Option<PathBuf> {
    Some(dirs::config_dir()?.join("RealScope").join("settings.json"))
}

/// Loads the saved state. A missing or unreadable file gives the defaults, so
/// a bad file never stops the app from starting.
pub fn load() -> Saved {
    path().map(|p| load_from(&p)).unwrap_or_default()
}

/// Saves the state, reporting failures on stderr only.
pub fn save(saved: &Saved) {
    if let Some(p) = path()
        && let Err(e) = save_to(&p, saved)
    {
        eprintln!("saving settings to {}: {e}", p.display());
    }
}

fn load_from(path: &Path) -> Saved {
    let text = match fs::read_to_string(path) {
        Ok(t) => t,
        Err(e) if e.kind() == io::ErrorKind::NotFound => return Saved::default(),
        Err(e) => {
            eprintln!("reading settings from {}: {e}; using defaults", path.display());
            return Saved::default();
        }
    };
    match serde_json::from_str::<Saved>(&text) {
        Ok(mut saved) => {
            saved.scope = saved.scope.sanitize();
            saved
        }
        Err(e) => {
            eprintln!("parsing settings in {}: {e}; using defaults", path.display());
            Saved::default()
        }
    }
}

/// Writes to a temporary file and renames it over the old one, so a crash
/// mid-write can't leave a truncated settings file.
fn save_to(path: &Path, saved: &Saved) -> io::Result<()> {
    if let Some(dir) = path.parent() {
        fs::create_dir_all(dir)?;
    }
    let tmp = path.with_extension("json.tmp");
    fs::write(&tmp, serde_json::to_string_pretty(saved)?)?;
    fs::rename(&tmp, path)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::fft::{FftSettings, FftUnit, FftWindow};
    use crate::math::{MathOp, MathSettings};
    use crate::settings::{Coupling, EXT, EdgeSettings, RunMode, Slope, Sweep, TriggerMode};

    fn temp_path(name: &str) -> PathBuf {
        std::env::temp_dir().join(format!("realscope-test-{}-{name}", std::process::id())).join("settings.json")
    }

    #[test]
    fn round_trip() {
        let path = temp_path("round-trip");
        let mut saved = Saved::default();
        saved.scope.ch[0].vdiv_idx = 4;
        saved.scope.ch[1].coupling = Coupling::Ac;
        saved.scope.ch[1].probe = 10;
        saved.scope.tdiv_idx = 9;
        saved.scope.trigger.source = EXT;
        saved.scope.trigger.sweep = Sweep::Normal;
        saved.scope.trigger.slope = Slope::Falling;
        saved.scope.trigger.mode = TriggerMode::PerChannel;
        saved.scope.trigger.per_channel[1] = EdgeSettings { level_v: -0.3, slope: Slope::Falling };
        saved.scope.math = MathSettings { on: true, op: MathOp::Sub, vdiv_idx: 4, pos0: -30 };
        saved.scope.fft = FftSettings {
            on: true,
            source: 2,
            window: FftWindow::Blackman,
            unit: FftUnit::Vrms,
            db_div_idx: 1,
            ref_dbv: -10,
            zoom: 2,
        };
        saved.phosphor_on = true;
        saved.persistence = Persistence::Infinite;
        save_to(&path, &saved).unwrap();
        assert_eq!(load_from(&path), saved);
        fs::remove_dir_all(path.parent().unwrap()).unwrap();
    }

    #[test]
    fn run_mode_is_not_saved_and_loads_as_running() {
        let path = temp_path("running");
        let mut saved = Saved::default();
        saved.scope.run = RunMode::Single;
        save_to(&path, &saved).unwrap();
        assert!(!fs::read_to_string(&path).unwrap().contains("run\""));
        assert_eq!(load_from(&path).scope.run, RunMode::Running);
        fs::remove_dir_all(path.parent().unwrap()).unwrap();
    }

    #[test]
    fn missing_fields_use_defaults() {
        let path = temp_path("partial");
        fs::create_dir_all(path.parent().unwrap()).unwrap();
        fs::write(&path, r#"{"scope": {"tdiv_idx": 10, "ch": [{"vdiv_idx": 3}, {}]}, "phosphor_on": true}"#).unwrap();
        let loaded = load_from(&path);
        assert_eq!(loaded.scope.tdiv_idx, 10);
        assert_eq!(loaded.scope.ch[0].vdiv_idx, 3);
        assert_eq!(loaded.scope.ch[0].probe, 1);
        assert_eq!(loaded.scope.trigger, Settings::default().trigger);
        assert!(!loaded.scope.math.on, "a file from before the math trace loads with it off");
        assert!(!loaded.scope.fft.on);
        assert_eq!(loaded.scope.run, RunMode::Running);
        assert!(loaded.phosphor_on);
        assert!(loaded.measurements_on);
        fs::remove_dir_all(path.parent().unwrap()).unwrap();
    }

    #[test]
    fn bad_or_missing_file_gives_defaults() {
        let path = temp_path("bad");
        assert_eq!(load_from(&path), Saved::default());
        fs::create_dir_all(path.parent().unwrap()).unwrap();
        fs::write(&path, "not json {").unwrap();
        assert_eq!(load_from(&path), Saved::default());
        // Out-of-range values are clamped rather than rejected.
        fs::write(&path, r#"{"scope": {"tdiv_idx": 99}}"#).unwrap();
        assert_eq!(load_from(&path).scope.tdiv_idx, crate::tables::TDIV_MAX_IDX);
        fs::remove_dir_all(path.parent().unwrap()).unwrap();
    }
}
