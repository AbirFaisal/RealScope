//! Asynchronous serial: start bit, 5–9 data bits LSB first, optional parity,
//! stop bit. Bits are read at their centres, timed from each start edge.

use super::{Item, Kind, Parity, TOO_FEW, UartSettings};

pub fn decode(line: &[bool], dt: f64, cfg: &UartSettings) -> Result<Vec<Item>, String> {
    // Samples per bit.
    let p = 1.0 / (cfg.baud as f64 * dt);
    if p < 4.0 {
        return Err(TOO_FEW.into());
    }
    let n = line.len();
    // Idle (mark) is high unless inverted.
    let level = |at: f64| line[(at.round() as usize).min(n - 1)] != cfg.inverted;
    let parity_bits = (cfg.parity != Parity::None) as u32;
    let frame_bits = 1 + cfg.data_bits + parity_bits + 1;

    let mut items = Vec::new();
    let mut i = 1;
    while i < n {
        // A start bit begins with a mark-to-space edge.
        if !(level((i - 1) as f64) && !level(i as f64)) {
            i += 1;
            continue;
        }
        let s = i as f64 - 0.5;
        let end = s + p * frame_bits as f64;
        if end > n as f64 {
            break; // cut off by the end of the record
        }
        if level(s + p / 2.0) {
            i += 1; // glitch, not a start bit
            continue;
        }
        let centre = |bit: u32| s + p * (bit as f64 + 0.5);
        let value = (0..cfg.data_bits).filter(|&b| level(centre(1 + b))).fold(0u32, |v, b| v | 1 << b);
        let mut error = None;
        if cfg.parity != Parity::None {
            let ones = value.count_ones() + level(centre(1 + cfg.data_bits)) as u32;
            let ok = match cfg.parity {
                Parity::Even => ones.is_multiple_of(2),
                Parity::Odd => !ones.is_multiple_of(2),
                Parity::None => true,
            };
            if !ok {
                error = Some("parity error");
            }
        }
        let stop = centre(frame_bits - 1);
        if !level(stop) {
            error = error.or(Some("framing error"));
        }
        items.push(Item { start: s, end, kind: Kind::Data, value, bits: cfg.data_bits, error });
        // The next start edge can come right after the stop bit's centre.
        i = stop.ceil() as usize;
    }
    Ok(items)
}

#[cfg(test)]
pub(super) mod tests {
    use super::*;

    /// Idle-high line carrying `bytes`, `spb` samples per bit, with idle
    /// before and after. `flip` inverts data bit `b` of byte `k`; `bad_stop`
    /// makes byte `k`'s stop bit low.
    pub fn wave(bytes: &[u32], spb: f64, cfg: &UartSettings, idle: usize, flip: Option<(usize, u32)>, bad_stop: Option<usize>) -> Vec<bool> {
        let mut bits = vec![true; idle];
        let mut t = idle as f64;
        // Appends one bit lasting `spb` samples (fractional widths accumulate).
        let mut push = |b: bool, bits: &mut Vec<bool>| {
            t += spb;
            while (bits.len() as f64) < t {
                bits.push(b);
            }
        };
        for (k, &orig) in bytes.iter().enumerate() {
            push(false, &mut bits);
            let v = match flip {
                Some((fk, b)) if fk == k => orig ^ 1 << b,
                _ => orig,
            };
            for b in 0..cfg.data_bits {
                push(v >> b & 1 == 1, &mut bits);
            }
            match cfg.parity {
                Parity::None => {}
                Parity::Even => push(orig.count_ones() % 2 == 1, &mut bits),
                Parity::Odd => push(orig.count_ones() % 2 == 0, &mut bits),
            }
            push(bad_stop != Some(k), &mut bits);
            push(true, &mut bits); // a little idle between bytes
        }
        bits.extend(std::iter::repeat_n(true, idle));
        if cfg.inverted {
            bits.iter_mut().for_each(|b| *b = !*b);
        }
        bits
    }

    const DT: f64 = 1.0 / (9600.0 * 20.0); // 20 samples per bit at 9600 baud

    fn values(items: &[Item]) -> Vec<u32> {
        items.iter().map(|i| i.value).collect()
    }

    #[test]
    fn decodes_text() {
        let cfg = UartSettings::default();
        let w = wave(&[b'H' as u32, b'i' as u32, b'\r' as u32, b'\n' as u32], 20.0, &cfg, 50, None, None);
        let items = decode(&w, DT, &cfg).unwrap();
        assert_eq!(values(&items), vec![0x48, 0x69, 0x0d, 0x0a]);
        assert!(items.iter().all(|i| i.error.is_none()));
        // First start bit at sample 50, 10 bits per frame.
        assert!((items[0].start - 49.5).abs() < 1.0 && (items[0].end - 249.5).abs() < 1.0);
    }

    #[test]
    fn parity_and_framing_errors() {
        let cfg = UartSettings { parity: Parity::Even, ..Default::default() };
        let w = wave(&[0x41, 0x42, 0x43], 20.0, &cfg, 30, Some((1, 3)), Some(2));
        let items = decode(&w, DT, &cfg).unwrap();
        assert_eq!(values(&items), vec![0x41, 0x42 ^ 8, 0x43]);
        assert_eq!(items[0].error, None);
        assert_eq!(items[1].error, Some("parity error"));
        assert_eq!(items[2].error, Some("framing error"));
        let odd = UartSettings { parity: Parity::Odd, ..Default::default() };
        assert!(decode(&wave(&[0x41, 0x00], 20.0, &odd, 30, None, None), DT, &odd).unwrap().iter().all(|i| i.error.is_none()));
    }

    #[test]
    fn inverted_and_seven_bits() {
        let cfg = UartSettings { inverted: true, data_bits: 7, ..Default::default() };
        let w = wave(&[0x55, 0x2a, 0x7f], 13.7, &cfg, 30, None, None);
        assert_eq!(values(&decode(&w, 1.0 / (9600.0 * 13.7), &cfg).unwrap()), vec![0x55, 0x2a, 0x7f]);
    }

    #[test]
    fn frame_cut_by_record_end_is_dropped() {
        let cfg = UartSettings::default();
        let mut w = wave(&[0x31, 0x32], 20.0, &cfg, 30, None, None);
        w.truncate(30 + 11 * 20 + 100); // second frame half there
        assert_eq!(values(&decode(&w, DT, &cfg).unwrap()), vec![0x31]);
    }

    #[test]
    fn too_few_samples_per_bit() {
        let cfg = UartSettings::default();
        let w = wave(&[0x31], 3.0, &cfg, 10, None, None);
        assert_eq!(decode(&w, 1.0 / (9600.0 * 3.0), &cfg), Err(TOO_FEW.to_owned()));
    }
}
