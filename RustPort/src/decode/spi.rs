//! SPI with clock and one data line (no chip-select: only two channels).
//! Data is read on the mode's sampling edge; words are framed by gaps in the
//! clock longer than four typical bit times.

use super::i2c::median_spacing;
use super::{Item, Kind, SpiSettings, TOO_FEW};

pub fn decode(clk: &[bool], data: &[bool], cfg: &SpiSettings) -> Result<Vec<Item>, String> {
    let n = clk.len().min(data.len());
    // Modes 0 and 3 sample on the rising edge, 1 and 2 on the falling edge.
    let rising = matches!(cfg.mode, 0 | 3);
    let edges: Vec<usize> = (1..n).filter(|&i| clk[i] != clk[i - 1] && clk[i] == rising).collect();
    let Some(bit_w) = median_spacing(&edges) else { return Ok(Vec::new()) };
    if bit_w < 4.0 {
        return Err(TOO_FEW.into());
    }
    let gap = 4.0 * bit_w;

    let mut items = Vec::new();
    let mut word: Vec<(usize, bool)> = Vec::new();
    let flush = |word: &[(usize, bool)], error: Option<&'static str>| {
        let value = if cfg.lsb_first {
            word.iter().rev().fold(0, |v, &(_, b)| v << 1 | b as u32)
        } else {
            word.iter().fold(0, |v, &(_, b)| v << 1 | b as u32)
        };
        Item {
            start: word[0].0 as f64 - bit_w / 2.0,
            end: word[word.len() - 1].0 as f64 + bit_w / 2.0,
            kind: Kind::Data,
            value,
            bits: word.len() as u32,
            error,
        }
    };
    for &e in &edges {
        if let Some(&(last, _)) = word.last()
            && (e - last) as f64 > gap
        {
            items.push(flush(&word, Some("incomplete")));
            word.clear();
        }
        word.push((e, data[e]));
        if word.len() == cfg.bits as usize {
            items.push(flush(&word, None));
            word.clear();
        }
    }
    if !word.is_empty() {
        items.push(flush(&word, Some("incomplete")));
    }
    Ok(items)
}

#[cfg(test)]
mod tests {
    use super::*;

    /// Clock and data for `words` in the given mode, `h` samples per half
    /// clock, `idle` samples between bursts. Data changes on the edge
    /// opposite to the sampling one.
    fn wave(bursts: &[&[u32]], cfg: &SpiSettings, h: usize, idle: usize) -> (Vec<bool>, Vec<bool>) {
        let cpol = cfg.mode >= 2;
        let cpha = cfg.mode % 2 == 1;
        let (mut clk, mut dat) = (vec![cpol; idle], vec![false; idle]);
        for burst in bursts {
            for &w in *burst {
                for i in 0..cfg.bits {
                    let bit = if cfg.lsb_first { w >> i & 1 == 1 } else { w >> (cfg.bits - 1 - i) & 1 == 1 };
                    // CPHA 0: data valid before the first edge; CPHA 1: set on the first edge.
                    let halves = if cpha { [(!cpol, bit), (cpol, bit)] } else { [(cpol, bit), (!cpol, bit)] };
                    for (c, d) in halves {
                        clk.extend(std::iter::repeat_n(c, h));
                        dat.extend(std::iter::repeat_n(d, h));
                    }
                }
            }
            clk.extend(std::iter::repeat_n(cpol, idle));
            dat.extend(std::iter::repeat_n(false, idle));
        }
        (clk, dat)
    }

    fn values(items: &[Item]) -> Vec<(u32, Option<&'static str>)> {
        items.iter().map(|i| (i.value, i.error)).collect()
    }

    #[test]
    fn all_modes_decode_the_same_bytes() {
        for mode in 0..4 {
            let cfg = SpiSettings { mode, ..Default::default() };
            let (c, d) = wave(&[&[0xa5, 0x3c], &[0x01]], &cfg, 5, 100);
            assert_eq!(values(&decode(&c, &d, &cfg).unwrap()), vec![(0xa5, None), (0x3c, None), (0x01, None)], "mode {mode}");
        }
    }

    #[test]
    fn lsb_first_and_12_bit_words() {
        let cfg = SpiSettings { lsb_first: true, bits: 12, ..Default::default() };
        let (c, d) = wave(&[&[0xabc, 0x123]], &cfg, 4, 50);
        assert_eq!(values(&decode(&c, &d, &cfg).unwrap()), vec![(0xabc, None), (0x123, None)]);
    }

    #[test]
    fn gap_ends_a_partial_word() {
        // A 5-bit fragment then a full byte after a gap.
        let short = SpiSettings { bits: 5, ..Default::default() };
        let (mut c, mut d) = wave(&[&[0x15]], &short, 5, 100);
        let cfg = SpiSettings::default();
        let (c2, d2) = wave(&[&[0x42]], &cfg, 5, 100);
        c.extend(c2);
        d.extend(d2);
        let items = decode(&c, &d, &cfg).unwrap();
        assert_eq!(values(&items), vec![(0x15, Some("incomplete")), (0x42, None)]);
        assert_eq!(items[0].bits, 5);
    }

    #[test]
    fn too_fast() {
        let cfg = SpiSettings::default();
        let (c, d) = wave(&[&[0x42]], &cfg, 1, 10);
        assert_eq!(decode(&c, &d, &cfg), Err(TOO_FEW.to_owned()));
    }
}
