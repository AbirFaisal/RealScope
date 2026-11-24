//! I2C: START/STOP from SDA edges while SCL is high, bits sampled on SCL
//! rising edges, 8 bits MSB first plus ACK per byte. The first byte after a
//! (repeated) START is the 7-bit address and the R/W bit.

use super::{Item, Kind, TOO_FEW};

pub fn decode(scl: &[bool], sda: &[bool]) -> Result<Vec<Item>, String> {
    let n = scl.len().min(sda.len());
    let rises: Vec<usize> = (1..n).filter(|&i| scl[i] && !scl[i - 1]).collect();
    if let Some(spacing) = median_spacing(&rises)
        && spacing < 4.0
    {
        return Err(TOO_FEW.into());
    }

    let mut items = Vec::new();
    let mut in_transfer = false;
    let mut first_byte = false;
    // Rising-edge positions and SDA levels of the byte being read.
    let mut bits: Vec<(usize, bool)> = Vec::new();
    let incomplete = |bits: &[(usize, bool)], at: usize| Item {
        start: bits[0].0 as f64,
        end: at as f64,
        kind: Kind::Data,
        value: bits.iter().take(8).fold(0, |v, &(_, b)| v << 1 | b as u32),
        bits: bits.len().min(8) as u32,
        error: Some("incomplete"),
    };
    for i in 1..n {
        if scl[i] && scl[i - 1] && sda[i] != sda[i - 1] {
            // The SCL rise just before a STOP or repeated START is part of the
            // condition, not a data bit.
            bits.pop();
            if !bits.is_empty() {
                items.push(incomplete(&bits, i));
                bits.clear();
            }
            if !sda[i] {
                items.push(Item::marker(i as f64, if in_transfer { Kind::RepeatedStart } else { Kind::Start }));
                in_transfer = true;
                first_byte = true;
            } else {
                items.push(Item::marker(i as f64, Kind::Stop));
                in_transfer = false;
            }
            continue;
        }
        if !(in_transfer && scl[i] && !scl[i - 1]) {
            continue;
        }
        bits.push((i, sda[i]));
        if bits.len() < 9 {
            continue;
        }
        let byte = bits[..8].iter().fold(0, |v, &(_, b)| v << 1 | b as u32);
        let bit_w = (bits[8].0 - bits[0].0) as f64 / 8.0;
        let (start, ack_at) = (bits[0].0 as f64 - bit_w / 2.0, bits[8].0 as f64 - bit_w / 2.0);
        let (kind, value, width) = if first_byte {
            (Kind::Address { read: byte & 1 == 1 }, byte >> 1, 7)
        } else {
            (Kind::Data, byte, 8)
        };
        first_byte = false;
        items.push(Item { start, end: ack_at, kind, value, bits: width, error: None });
        let ack = if bits[8].1 { Kind::Nack } else { Kind::Ack };
        items.push(Item { start: ack_at, end: ack_at + bit_w, kind: ack, value: 0, bits: 0, error: None });
        bits.clear();
    }
    Ok(items)
}

/// Median distance between successive positions, in samples.
pub(super) fn median_spacing(at: &[usize]) -> Option<f64> {
    let mut d: Vec<usize> = at.windows(2).map(|w| w[1] - w[0]).collect();
    if d.is_empty() {
        return None;
    }
    d.sort_unstable();
    Some(d[d.len() / 2] as f64)
}

#[cfg(test)]
mod tests {
    use super::*;

    /// Builds SCL/SDA with `h` samples per half clock.
    struct Bus {
        scl: Vec<bool>,
        sda: Vec<bool>,
        h: usize,
    }

    impl Bus {
        fn new(h: usize) -> Bus {
            Bus { scl: vec![true; 4 * h], sda: vec![true; 4 * h], h }
        }
        fn hold(&mut self, scl: bool, sda: bool, len: usize) {
            self.scl.extend(std::iter::repeat_n(scl, len));
            self.sda.extend(std::iter::repeat_n(sda, len));
        }
        fn start(&mut self) {
            // From SCL low (or idle): release SDA, raise SCL, then pull SDA low.
            let h = self.h;
            self.hold(false, true, h);
            self.hold(true, true, h);
            self.hold(true, false, h);
            self.hold(false, false, h);
        }
        fn bit(&mut self, b: bool) {
            let h = self.h;
            self.hold(false, b, h);
            self.hold(true, b, h);
        }
        fn byte(&mut self, v: u32, ack: bool) {
            for i in (0..8).rev() {
                self.bit(v >> i & 1 == 1);
            }
            self.bit(!ack);
        }
        fn stop(&mut self) {
            let h = self.h;
            self.hold(false, false, h);
            self.hold(true, false, h);
            self.hold(true, true, 4 * h);
        }
    }

    #[test]
    fn write_then_read_with_repeated_start() {
        let mut b = Bus::new(5);
        b.start();
        b.byte(0x50 << 1, true);
        b.byte(0x00, true);
        b.byte(0x2a, true);
        b.start(); // repeated
        b.byte(0x50 << 1 | 1, true);
        b.byte(0x99, false); // master NACKs the last byte
        b.stop();
        let items = decode(&b.scl, &b.sda).unwrap();
        let summary: Vec<(Kind, u32)> = items.iter().map(|i| (i.kind, i.value)).collect();
        use Kind::*;
        assert_eq!(summary, vec![
            (Start, 0),
            (Address { read: false }, 0x50),
            (Ack, 0),
            (Data, 0x00),
            (Ack, 0),
            (Data, 0x2a),
            (Ack, 0),
            (RepeatedStart, 0),
            (Address { read: true }, 0x50),
            (Ack, 0),
            (Data, 0x99),
            (Nack, 0),
            (Stop, 0),
        ]);
        assert!(items.iter().all(|i| i.error.is_none() && i.start <= i.end));
    }

    #[test]
    fn byte_broken_by_stop_is_incomplete() {
        let mut b = Bus::new(5);
        b.start();
        b.byte(0x50 << 1, true);
        for _ in 0..3 {
            b.bit(true);
        }
        b.stop();
        let items = decode(&b.scl, &b.sda).unwrap();
        let broken = &items[items.len() - 2];
        assert_eq!((broken.kind, broken.error, broken.bits), (Kind::Data, Some("incomplete"), 3));
        assert_eq!(items.last().unwrap().kind, Kind::Stop);
    }

    #[test]
    fn too_fast() {
        let mut b = Bus::new(1);
        b.start();
        b.byte(0x12, true);
        b.stop();
        assert_eq!(decode(&b.scl, &b.sda), Err(TOO_FEW.to_owned()));
    }
}
