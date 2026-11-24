//! Serial protocol decoding (UART, I2C, SPI, 1-Wire) of the captured record.
//!
//! Each channel's record is turned into logic levels with an automatic
//! threshold, then decoded into [`Item`]s positioned in samples, so the UI can
//! line them up under the trace and list them with times.

mod i2c;
mod onewire;
mod spi;
mod uart;

use serde::{Deserialize, Serialize};

use crate::settings::Settings;
use crate::tables;

#[derive(Clone, Copy, Debug, PartialEq, Eq, Serialize, Deserialize, Default)]
pub enum Protocol {
    #[default]
    Uart,
    I2c,
    Spi,
    OneWire,
}

impl Protocol {
    pub const ALL: [Protocol; 4] = [Protocol::Uart, Protocol::I2c, Protocol::Spi, Protocol::OneWire];

    pub fn label(self) -> &'static str {
        match self {
            Protocol::Uart => "UART",
            Protocol::I2c => "I2C",
            Protocol::Spi => "SPI",
            Protocol::OneWire => "1-Wire",
        }
    }
}

/// How data values are labelled on the bus row.
#[derive(Clone, Copy, Debug, PartialEq, Eq, Serialize, Deserialize, Default)]
pub enum DataFormat {
    #[default]
    Hex,
    Ascii,
    Dec,
}

impl DataFormat {
    pub const ALL: [DataFormat; 3] = [DataFormat::Hex, DataFormat::Ascii, DataFormat::Dec];

    pub fn label(self) -> &'static str {
        match self {
            DataFormat::Hex => "Hex",
            DataFormat::Ascii => "ASCII",
            DataFormat::Dec => "Dec",
        }
    }
}

#[derive(Clone, Copy, Debug, PartialEq, Eq, Serialize, Deserialize, Default)]
pub enum UartLines {
    #[default]
    Ch1,
    Ch2,
    Both,
}

#[derive(Clone, Copy, Debug, PartialEq, Eq, Serialize, Deserialize, Default)]
pub enum Parity {
    #[default]
    None,
    Even,
    Odd,
}

/// Baud rates offered by the stepper.
pub const BAUDS: [u32; 13] =
    [300, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115_200, 230_400, 460_800, 921_600, 1_000_000];

#[derive(Clone, Copy, Debug, PartialEq, Serialize, Deserialize)]
#[serde(default)]
pub struct UartSettings {
    pub lines: UartLines,
    pub baud: u32,
    pub data_bits: u32,
    pub parity: Parity,
    /// Idle low (RS-232 levels) instead of idle high (TTL).
    pub inverted: bool,
}

impl Default for UartSettings {
    fn default() -> Self {
        UartSettings { lines: UartLines::Ch1, baud: 9600, data_bits: 8, parity: Parity::None, inverted: false }
    }
}

#[derive(Clone, Copy, Debug, PartialEq, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct I2cSettings {
    /// Channel carrying SCL; SDA is on the other one.
    pub scl: usize,
}

#[derive(Clone, Copy, Debug, PartialEq, Serialize, Deserialize)]
#[serde(default)]
pub struct SpiSettings {
    /// Channel carrying the clock; data is on the other one.
    pub clock: usize,
    /// SPI mode 0–3 (CPOL·2 + CPHA).
    pub mode: u8,
    pub bits: u32,
    pub lsb_first: bool,
}

impl Default for SpiSettings {
    fn default() -> Self {
        SpiSettings { clock: 0, mode: 0, bits: 8, lsb_first: false }
    }
}

#[derive(Clone, Copy, Debug, PartialEq, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct OneWireSettings {
    pub line: usize,
}

#[derive(Clone, Copy, Debug, PartialEq, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct DecodeSettings {
    pub on: bool,
    pub protocol: Protocol,
    pub format: DataFormat,
    pub uart: UartSettings,
    pub i2c: I2cSettings,
    pub spi: SpiSettings,
    pub onewire: OneWireSettings,
}

impl DecodeSettings {
    /// Whether decoding needs channel `ch` captured.
    pub fn uses(&self, ch: usize) -> bool {
        self.on
            && match self.protocol {
                Protocol::Uart => matches!(
                    (self.uart.lines, ch),
                    (UartLines::Both, _) | (UartLines::Ch1, 0) | (UartLines::Ch2, 1)
                ),
                Protocol::I2c | Protocol::Spi => true,
                Protocol::OneWire => self.onewire.line == ch,
            }
    }

    pub fn sanitize(&mut self) {
        if !BAUDS.contains(&self.uart.baud) {
            self.uart.baud = UartSettings::default().baud;
        }
        self.uart.data_bits = self.uart.data_bits.clamp(5, 9);
        self.i2c.scl = self.i2c.scl.min(1);
        self.spi.clock = self.spi.clock.min(1);
        self.spi.mode = self.spi.mode.min(3);
        self.spi.bits = self.spi.bits.clamp(4, 16);
        self.onewire.line = self.onewire.line.min(1);
    }
}

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum Kind {
    Data,
    /// I2C address byte: `value` is the 7-bit address.
    Address { read: bool },
    Start,
    RepeatedStart,
    Stop,
    Ack,
    Nack,
    Reset,
    Presence,
}

/// One decoded element, spanning samples `start..end` of the record.
#[derive(Clone, Debug, PartialEq)]
pub struct Item {
    pub start: f64,
    pub end: f64,
    pub kind: Kind,
    pub value: u32,
    /// Width of `value` in bits, for formatting.
    pub bits: u32,
    /// Problem with this element (parity error, incomplete, …).
    pub error: Option<&'static str>,
}

impl Item {
    fn marker(at: f64, kind: Kind) -> Item {
        Item { start: at, end: at, kind, value: 0, bits: 0, error: None }
    }

    /// Short text for the bus row.
    pub fn text(&self, format: DataFormat) -> String {
        match self.kind {
            Kind::Data => format_value(self.value, self.bits, format),
            Kind::Address { read } => format!("{} {}", format_value(self.value, 7, format), if read { "R" } else { "W" }),
            Kind::Start => "S".into(),
            Kind::RepeatedStart => "Sr".into(),
            Kind::Stop => "P".into(),
            Kind::Ack => "A".into(),
            Kind::Nack => "N".into(),
            Kind::Reset => "Reset".into(),
            Kind::Presence => "Presence".into(),
        }
    }

    /// Longer text for the frame list: hex plus the ASCII character.
    pub fn describe(&self) -> String {
        let base = match self.kind {
            Kind::Data | Kind::Address { .. } => {
                let hex = format_value(self.value, self.bits.max(7), DataFormat::Hex);
                let ch = char::from_u32(self.value).filter(|c| c.is_ascii_graphic() || *c == ' ');
                let mut s = format!("0x{hex}");
                if let Kind::Address { read } = self.kind {
                    s += if read { " read" } else { " write" };
                } else if let Some(c) = ch {
                    s += &format!(" '{c}'");
                }
                s
            }
            Kind::Start => "Start".into(),
            Kind::RepeatedStart => "Repeated start".into(),
            Kind::Stop => "Stop".into(),
            Kind::Ack => "ACK".into(),
            Kind::Nack => "NACK".into(),
            Kind::Reset => "Reset".into(),
            Kind::Presence => "Presence".into(),
        };
        match self.error {
            Some(e) => format!("{base} — {e}"),
            None => base,
        }
    }
}

pub fn format_value(value: u32, bits: u32, format: DataFormat) -> String {
    match format {
        DataFormat::Hex => format!("{value:0w$X}", w = bits.div_ceil(4) as usize),
        DataFormat::Dec => value.to_string(),
        DataFormat::Ascii => match char::from_u32(value).filter(|c| bits <= 8 && (c.is_ascii_graphic() || *c == ' ')) {
            Some(c) => c.to_string(),
            None => format!("\\x{value:02X}"),
        },
    }
}

/// One bus row: a decoded line (or a message saying why there is nothing).
#[derive(Clone, Debug, Default)]
pub struct Row {
    pub label: String,
    pub items: Vec<Item>,
    pub message: Option<String>,
    /// Record length in samples, for placing items on the time axis.
    pub len: usize,
}

const TOO_FEW: &str = "Too few samples per bit: use a faster time/div";

/// Logic levels of a record: threshold halfway between its extremes, with
/// ±10 % hysteresis so noise on a level doesn't add edges. `None` when the
/// record is nearly flat (no activity to decode).
pub fn digitize(samples: &[i8]) -> Option<Vec<bool>> {
    let hi = *samples.iter().max()? as i32;
    let lo = *samples.iter().min()? as i32;
    if hi - lo < 8 {
        return None;
    }
    let mid = (hi + lo) as f64 / 2.0;
    let hyst = (hi - lo) as f64 / 10.0;
    let mut state = samples[0] as f64 > mid;
    Some(
        samples
            .iter()
            .map(|&s| {
                let s = s as f64;
                if s > mid + hyst {
                    state = true;
                } else if s < mid - hyst {
                    state = false;
                }
                state
            })
            .collect(),
    )
}

/// Decodes the capture per the decode settings. Rows come back even without
/// data, carrying a message.
pub fn decode(samples: [Option<&[i8]>; 2], s: &Settings) -> Vec<Row> {
    let d = &s.decode;
    let dt = 1.0 / tables::sample_rate_hz(s.tdiv_idx);
    let len = samples.iter().flatten().map(|x| x.len()).max().unwrap_or(0);
    let line = |ch: usize| -> Result<Vec<bool>, String> {
        let x = samples[ch].ok_or_else(|| "No data".to_owned())?;
        digitize(x).ok_or_else(|| format!("No activity on CH{}", ch + 1))
    };
    let row = |label: String, result: Result<Vec<Item>, String>| match result {
        Ok(items) => {
            let message = items.is_empty().then(|| "Nothing decoded".to_owned());
            Row { label, items, message, len }
        }
        Err(message) => Row { label, items: Vec::new(), message: Some(message), len },
    };
    match d.protocol {
        Protocol::Uart => {
            let chans: &[usize] = match d.uart.lines {
                UartLines::Ch1 => &[0],
                UartLines::Ch2 => &[1],
                UartLines::Both => &[0, 1],
            };
            chans
                .iter()
                .map(|&ch| row(format!("CH{}", ch + 1), line(ch).and_then(|l| uart::decode(&l, dt, &d.uart))))
                .collect()
        }
        Protocol::I2c => {
            let scl = d.i2c.scl;
            vec![row("I2C".into(), line(scl).and_then(|c| i2c::decode(&c, &line(1 - scl)?)))]
        }
        Protocol::Spi => {
            let clk = d.spi.clock;
            vec![row("SPI".into(), line(clk).and_then(|c| spi::decode(&c, &line(1 - clk)?, &d.spi)))]
        }
        Protocol::OneWire => {
            vec![row("1-Wire".into(), line(d.onewire.line).and_then(|l| onewire::decode(&l, dt)))]
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn digitize_thresholds_with_hysteresis() {
        assert_eq!(digitize(&[10; 100]), None);
        assert_eq!(digitize(&[10, 12, 9, 13]), None, "under 8 counts is no activity");
        // Levels 0 and 80 with ±3 counts of noise: only the real edges count.
        let noisy: Vec<i8> = (0..40).map(|i| if (10..30).contains(&i) { 80 } else { 0 } + [0, 3, -3, 2][i % 4]).collect();
        let d = digitize(&noisy).unwrap();
        let edges = d.windows(2).filter(|w| w[0] != w[1]).count();
        assert_eq!(edges, 2);
        assert!(d[15] && !d[5] && !d[35]);
    }

    #[test]
    fn formats() {
        assert_eq!(format_value(0x41, 8, DataFormat::Hex), "41");
        assert_eq!(format_value(0x41, 8, DataFormat::Ascii), "A");
        assert_eq!(format_value(0x0d, 8, DataFormat::Ascii), "\\x0D");
        assert_eq!(format_value(65, 8, DataFormat::Dec), "65");
        assert_eq!(format_value(0xabc, 12, DataFormat::Hex), "ABC");
        let addr = Item { start: 0.0, end: 1.0, kind: Kind::Address { read: true }, value: 0x50, bits: 7, error: None };
        assert_eq!(addr.text(DataFormat::Hex), "50 R");
        assert_eq!(addr.describe(), "0x50 read");
        let data = Item { kind: Kind::Data, value: 0x41, bits: 8, error: Some("parity error"), ..addr };
        assert_eq!(data.describe(), "0x41 'A' — parity error");
    }

    #[test]
    fn uses_and_sanitize() {
        let mut d = DecodeSettings { on: true, ..Default::default() };
        assert!(d.uses(0) && !d.uses(1));
        d.uart.lines = UartLines::Both;
        assert!(d.uses(1));
        d.protocol = Protocol::OneWire;
        d.onewire.line = 1;
        assert!(!d.uses(0) && d.uses(1));
        d.on = false;
        assert!(!d.uses(1));
        d.uart.baud = 12345;
        d.spi.bits = 99;
        d.spi.mode = 9;
        d.sanitize();
        assert_eq!((d.uart.baud, d.spi.bits, d.spi.mode), (9600, 16, 3));
    }
}
