//! Digital-phosphor style display buffer.
//!
//! Each capture is drawn into a per-channel time × voltage grid the way a CRT
//! beam would draw it; the grid fades over time, and each cell is shown with a
//! brightness that grows with how often the signal passes through it.

use std::time::Duration;

use crate::tables::ADC_MAX;

/// Horizontal resolution, matching the vendor screen width (`GDefine.AREA_WIDTH`),
/// independent of the window size.
pub const COLS: usize = 1000;
/// One row per ADC code, -125..=125.
pub const ROWS: usize = 2 * ADC_MAX as usize + 1;
/// Brightness steps used for drawing.
pub const LEVELS: u8 = 16;
/// Hits at which a cell reaches ~63% of full intensity before the gamma lift.
const SATURATION_HITS: f32 = 1.0;

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum Persistence {
    Short,
    Medium,
    Infinite,
}

impl Persistence {
    pub const ALL: [Persistence; 3] = [Persistence::Short, Persistence::Medium, Persistence::Infinite];

    pub fn label(self) -> &'static str {
        match self {
            Persistence::Short => "Short",
            Persistence::Medium => "Medium",
            Persistence::Infinite => "Infinite",
        }
    }

    /// Multiplier for the hit grid after `elapsed` time.
    pub fn decay_factor(self, elapsed: Duration) -> f32 {
        let half_life = match self {
            Persistence::Short => 0.15,
            Persistence::Medium => 1.0,
            Persistence::Infinite => return 1.0,
        };
        0.5f32.powf(elapsed.as_secs_f32() / half_life)
    }
}

/// A vertical run of cells in one column that share a brightness level.
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub struct Run {
    pub col: u16,
    /// Row 0 is ADC code -125.
    pub row_lo: u8,
    pub row_hi: u8,
    pub level: u8,
}

impl Run {
    pub fn code_lo(&self) -> i32 {
        self.row_lo as i32 - ADC_MAX
    }

    pub fn code_hi(&self) -> i32 {
        self.row_hi as i32 - ADC_MAX
    }
}

pub struct Phosphor {
    /// Column-major hit grids, `col * ROWS + row`.
    grid: [Vec<f32>; 2],
}

impl Default for Phosphor {
    fn default() -> Self {
        Phosphor { grid: [vec![0.0; COLS * ROWS], vec![0.0; COLS * ROWS]] }
    }
}

fn row_of(code: i8) -> usize {
    (code as i32 + ADC_MAX).clamp(0, ROWS as i32 - 1) as usize
}

/// Brightness level for a hit count: saturating, with a square-root lift so
/// rare paths stay visible; any non-zero cell gets at least level 1.
pub fn level(hits: f32) -> u8 {
    if hits <= 0.0 {
        return 0;
    }
    let intensity = (1.0 - (-hits / SATURATION_HITS).exp()).sqrt();
    ((intensity * LEVELS as f32).ceil() as u8).clamp(1, LEVELS)
}

impl Phosphor {
    pub fn clear(&mut self) {
        for g in &mut self.grid {
            g.fill(0.0);
        }
    }

    pub fn decay(&mut self, factor: f32) {
        if factor >= 1.0 {
            return;
        }
        for g in &mut self.grid {
            for v in g.iter_mut() {
                *v *= factor;
            }
        }
    }

    /// Draws one capture. Each column receives one unit of beam time spread
    /// over the rows the trace crosses in it (extended to the previous column's
    /// last sample so edges are continuous): flat parts glow brightly, fast
    /// edges dimly, like a CRT.
    pub fn accumulate(&mut self, ch: usize, samples: &[i8]) {
        let n = samples.len();
        if n == 0 {
            return;
        }
        let grid = &mut self.grid[ch];
        let mut prev_last: Option<i8> = None;
        for col in 0..COLS {
            let a = (col * n / COLS).min(n - 1);
            let b = ((col + 1) * n / COLS).clamp(a + 1, n);
            let chunk = &samples[a..b];
            let (mut lo, mut hi) = chunk.iter().fold((i8::MAX, i8::MIN), |(lo, hi), &s| (lo.min(s), hi.max(s)));
            if let Some(p) = prev_last {
                lo = lo.min(p);
                hi = hi.max(p);
            }
            prev_last = chunk.last().copied();
            let (r0, r1) = (row_of(lo), row_of(hi));
            let weight = 1.0 / (r1 - r0 + 1) as f32;
            for cell in &mut grid[col * ROWS + r0..=col * ROWS + r1] {
                *cell += weight;
            }
        }
    }

    #[cfg(test)]
    pub fn hits(&self, ch: usize, col: usize, code: i8) -> f32 {
        self.grid[ch][col * ROWS + row_of(code)]
    }

    /// Lit cells as vertical same-level runs, ready to paint as quads.
    pub fn runs(&self, ch: usize) -> Vec<Run> {
        let grid = &self.grid[ch];
        let mut runs = Vec::new();
        for col in 0..COLS {
            let mut current: Option<Run> = None;
            for row in 0..ROWS {
                let l = level(grid[col * ROWS + row]);
                match &mut current {
                    Some(run) if run.level == l => run.row_hi = row as u8,
                    _ => {
                        if let Some(run) = current.take() {
                            runs.push(run);
                        }
                        if l > 0 {
                            current = Some(Run { col: col as u16, row_lo: row as u8, row_hi: row as u8, level: l });
                        }
                    }
                }
            }
            runs.extend(current);
        }
        runs
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn lit_rows(p: &Phosphor, ch: usize, col: usize) -> Vec<i32> {
        (-ADC_MAX..=ADC_MAX).filter(|&c| p.hits(ch, col, c as i8) > 0.0).collect()
    }

    #[test]
    fn flat_line_lights_one_row_per_column() {
        let mut p = Phosphor::default();
        p.accumulate(0, &[10; 5000]);
        for col in [0, 1, 500, COLS - 1] {
            assert_eq!(lit_rows(&p, 0, col), vec![10]);
            assert_eq!(p.hits(0, col, 10), 1.0);
        }
        assert!(lit_rows(&p, 1, 0).is_empty(), "other channel untouched");
    }

    #[test]
    fn step_draws_a_continuous_dim_edge() {
        let mut p = Phosphor::default();
        let mut s = vec![-50i8; 5000];
        s[2500..].fill(50);
        p.accumulate(0, &s);
        // The edge lands at column 500: every row from -50 to 50 is lit, each dimly.
        assert_eq!(lit_rows(&p, 0, 500), (-50..=50).collect::<Vec<_>>());
        assert!((p.hits(0, 500, 0) - 1.0 / 101.0).abs() < 1e-6);
        assert_eq!(lit_rows(&p, 0, 499), vec![-50]);
        assert_eq!(lit_rows(&p, 0, 501), vec![50]);
    }

    #[test]
    fn short_records_fill_every_column() {
        let mut p = Phosphor::default();
        p.accumulate(1, &[3; 1000]);
        assert!((0..COLS).all(|c| p.hits(1, c, 3) == 1.0));
    }

    #[test]
    fn accumulate_decay_clear() {
        let mut p = Phosphor::default();
        p.accumulate(0, &[0; 5000]);
        p.accumulate(0, &[0; 5000]);
        assert_eq!(p.hits(0, 7, 0), 2.0);
        p.decay(0.5);
        assert_eq!(p.hits(0, 7, 0), 1.0);
        p.clear();
        assert_eq!(p.hits(0, 7, 0), 0.0);
    }

    #[test]
    fn persistence_decay() {
        assert_eq!(Persistence::Infinite.decay_factor(Duration::from_secs(10)), 1.0);
        assert!((Persistence::Medium.decay_factor(Duration::from_secs(1)) - 0.5).abs() < 1e-6);
        assert!(Persistence::Short.decay_factor(Duration::from_millis(150)) < 0.51);
    }

    #[test]
    fn levels_are_monotonic_and_saturate() {
        assert_eq!(level(0.0), 0);
        assert_eq!(level(1e-4), 1);
        let mut prev = 0;
        for i in 1..200 {
            let l = level(i as f32 * 0.05);
            assert!(l >= prev);
            prev = l;
        }
        assert_eq!(level(100.0), LEVELS);
    }

    #[test]
    fn runs_merge_and_skip_empty() {
        let mut p = Phosphor::default();
        let mut s = vec![-50i8; 5000];
        s[2500..].fill(50);
        p.accumulate(0, &s);
        let runs = p.runs(0);
        // One run per flat column, plus the edge column as a single run.
        assert_eq!(runs.len(), COLS);
        let edge = runs.iter().find(|r| r.col == 500).unwrap();
        assert_eq!((edge.code_lo(), edge.code_hi()), (-50, 50));
        assert!(edge.level < LEVELS);
        let flat = runs.iter().find(|r| r.col == 10).unwrap();
        assert_eq!((flat.code_lo(), flat.code_hi(), flat.level), (-50, -50, level(1.0)));
        assert!(p.runs(1).is_empty());
    }
}
