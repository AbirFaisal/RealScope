//! 1-Wire at standard speed, from the widths of low pulses: a reset is at
//! least 480 µs low, the device's presence pulse follows 15–60 µs later and
//! lasts 60–240 µs, and in a bit slot a short low (< 15 µs) is a 1 and a long
//! one a 0. Bytes go LSB first.

use super::{Item, Kind, TOO_FEW};

const RESET_MIN: f64 = 400e-6;
const BIT_ONE_MAX: f64 = 15e-6;
/// Slack on the presence timing windows, which vary between devices.
const PRESENCE_GAP: std::ops::RangeInclusive<f64> = 10e-6..=80e-6;
const PRESENCE_LOW: std::ops::RangeInclusive<f64> = 50e-6..=300e-6;
/// A bit slot lasts at least 60 µs; used to size the byte boxes.
const SLOT: f64 = 60e-6;

pub fn decode(line: &[bool], dt: f64) -> Result<Vec<Item>, String> {
    // A 1 is a low of a few µs, which needs a few samples to see.
    if dt > 2e-6 {
        return Err(TOO_FEW.into());
    }
    let n = line.len();
    // Low pulses as (falling edge, rising edge); ones cut by the record edges are skipped.
    let mut pulses = Vec::new();
    let mut fall = None;
    for i in 1..n {
        if line[i - 1] && !line[i] {
            fall = Some(i);
        } else if !line[i - 1] && line[i]
            && let Some(f) = fall.take()
        {
            pulses.push((f, i));
        }
    }

    let mut items = Vec::new();
    let mut reset_end: Option<usize> = None;
    let mut bits: Vec<(usize, bool)> = Vec::new();
    let byte = |bits: &[(usize, bool)], error| Item {
        start: bits[0].0 as f64,
        end: bits[bits.len() - 1].0 as f64 + SLOT / dt,
        kind: Kind::Data,
        value: bits.iter().rev().fold(0, |v, &(_, b)| v << 1 | b as u32),
        bits: bits.len() as u32,
        error,
    };
    for (f, r) in pulses {
        let width = (r - f) as f64 * dt;
        if width >= RESET_MIN {
            if !bits.is_empty() {
                items.push(byte(&bits, Some("incomplete")));
                bits.clear();
            }
            items.push(Item { start: f as f64, end: r as f64, kind: Kind::Reset, value: 0, bits: 0, error: None });
            reset_end = Some(r);
            continue;
        }
        if let Some(re) = reset_end.take()
            && PRESENCE_GAP.contains(&((f - re) as f64 * dt))
            && PRESENCE_LOW.contains(&width)
        {
            items.push(Item { start: f as f64, end: r as f64, kind: Kind::Presence, value: 0, bits: 0, error: None });
            continue;
        }
        bits.push((f, width < BIT_ONE_MAX));
        if bits.len() == 8 {
            items.push(byte(&bits, None));
            bits.clear();
        }
    }
    if !bits.is_empty() {
        items.push(byte(&bits, Some("incomplete")));
    }
    Ok(items)
}

#[cfg(test)]
mod tests {
    use super::*;

    const DT: f64 = 1e-6;

    /// Bus at 1 µs per sample: reset, presence, then `bytes` LSB first.
    fn wave(bytes: &[u32]) -> Vec<bool> {
        let mut w = vec![true; 50];
        let mut seg = |level: bool, us: usize| w.extend(std::iter::repeat_n(level, us));
        seg(false, 480); // reset
        seg(true, 30);
        seg(false, 120); // presence
        seg(true, 330);
        for &b in bytes {
            for i in 0..8 {
                if b >> i & 1 == 1 {
                    seg(false, 6);
                    seg(true, 64);
                } else {
                    seg(false, 60);
                    seg(true, 10);
                }
            }
        }
        seg(true, 100);
        w
    }

    #[test]
    fn reset_presence_and_bytes() {
        // Skip ROM, then Convert T (DS18B20).
        let items = decode(&wave(&[0xcc, 0x44]), DT).unwrap();
        let summary: Vec<(Kind, u32)> = items.iter().map(|i| (i.kind, i.value)).collect();
        assert_eq!(summary, vec![(Kind::Reset, 0), (Kind::Presence, 0), (Kind::Data, 0xcc), (Kind::Data, 0x44)]);
        assert!(items.iter().all(|i| i.error.is_none()));
    }

    #[test]
    fn partial_byte_and_slow_sampling() {
        let mut w = wave(&[0xcc]);
        // Three more bits (1, 0, 1) with no end.
        for (low, high) in [(6, 64), (60, 10), (6, 64)] {
            w.extend(std::iter::repeat_n(false, low));
            w.extend(std::iter::repeat_n(true, high));
        }
        let items = decode(&w, DT).unwrap();
        let last = items.last().unwrap();
        assert_eq!((last.value, last.bits, last.error), (0b101, 3, Some("incomplete")));
        assert_eq!(decode(&w, 5e-6), Err(TOO_FEW.to_owned()));
    }
}
