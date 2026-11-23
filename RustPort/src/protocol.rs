//! VDS1022 wire protocol: packet builders, register encoders and response parsers.
//!
//! Everything here is pure so it can be unit tested without hardware. File
//! references point into `src/OWON_VDS_v1.0.24/com/owon/` of the Java reference.

use anyhow::{Result, bail, ensure};

use crate::settings::{ChannelSettings, Coupling, EXT, Sweep};
use crate::tables::{ADC_MAX, ATTENUATION_VDIV_IDX, screen_points};

pub const VENDOR_ID: u16 = 0x5345;
pub const PRODUCT_ID: u16 = 0x1234;

/// Every command is answered with `[status char][i32 LE]`.
pub const RESPONSE_LEN: usize = 5;

/// Register addresses (`uppersoft/vds/device/interpret/DeviceAddressTable.java`).
pub mod reg {
    pub const TRG_D: u32 = 0x01;
    pub const SAMPLE: u32 = 0x09;
    pub const SLOWMOVE: u32 = 0x0a;
    pub const CHL_ON: u32 = 0x0b;
    pub const PHASE_FINE: u32 = 0x18;
    pub const TRG: u32 = 0x24;
    /// Holdoff arg/index pairs per trigger source; EXT shares CH1's
    /// (`DeviceAddressTable.trg_holdoff`).
    pub const HOLDOFF: [u32; 3] = [0x26, 0x2a, 0x26];
    /// Function of the multi-purpose I/O connector (trigger in / trigger out / pass-fail).
    pub const MULTI_IO: u32 = 0x06;
    pub const EDGE_LEVEL: [u32; 2] = [0x2e, 0x30];
    /// Threshold the hardware frequency counter counts crossings of.
    pub const FREQ_REF: [u32; 2] = [0x4a, 0x4b];
    pub const TIMEBASE: u32 = 0x52;
    pub const SUF_TRG: u32 = 0x56;
    pub const PRE_TRG: u32 = 0x5a;
    pub const DM: u32 = 0x5c;
    pub const RUNSTOP: u32 = 0x61;
    pub const DATAFINISHED: u32 = 0x7a;
    pub const ZERO_OFFSET: [u32; 2] = [0x10a, 0x108];
    pub const EMPTY: u32 = 0x10c;
    pub const CHANNEL_SET: [u32; 2] = [0x111, 0x110];
    pub const VOLT_GAIN: [u32; 2] = [0x116, 0x114];
    pub const READ_FLASH: u32 = 0x1b0;
    pub const FPGA_QUERY: u32 = 0x223;
    pub const GETDATA: u32 = 0x1000;
    pub const FPGA_DOWNLOAD: u32 = 0x4000;
}

/// `[u32 LE addr][u8 len][value, len bytes LE]` (`PackUtil2.packCMD`).
pub fn cmd(addr: u32, len: u8, value: u32) -> Vec<u8> {
    let mut buf = Vec::with_capacity(5 + len as usize);
    buf.extend_from_slice(&addr.to_le_bytes());
    buf.push(len);
    buf.extend_from_slice(&value.to_le_bytes()[..len as usize]);
    buf
}

/// Writes a multi-byte register as one 1-byte command per byte at consecutive
/// addresses (`Sendable.sendCMDbyBytes`). Used for PRE/SUF_TRG, edge level and
/// PHASE_FINE.
pub fn cmd_bytewise(addr: u32, len: u8, value: u32) -> Vec<Vec<u8>> {
    (0..len as u32)
        .map(|i| cmd(addr + i, 1, (value >> (8 * i)) & 0xff))
        .collect()
}

pub fn run_stop(run: bool) -> Vec<u8> {
    cmd(reg::RUNSTOP, 1, if run { 0 } else { 1 })
}

/// GETDATA: one flag byte per channel, `5` = send, `4` = skip; CH1 in the low
/// byte (`GetDataRunner2.routineGetData`).
pub fn get_data(ch_on: [bool; 2]) -> Vec<u8> {
    let flag = |on: bool| if on { 5 } else { 4 };
    cmd(reg::GETDATA, 2, (flag(ch_on[1]) << 8) | flag(ch_on[0]))
}

/// channel_set register (`LowControlManger.java:98-140`): bit7 on, bits 6:5
/// coupling, bit1 attenuator relay.
pub fn channel_set(ch: &ChannelSettings) -> u8 {
    let coupling = match ch.coupling {
        Coupling::Dc => 0x20,
        Coupling::Ac => 0x00,
        Coupling::Gnd => 0x40,
    };
    let on = if ch.on { 0x80 } else { 0 };
    let atten = if ch.vdiv_idx >= ATTENUATION_VDIV_IDX { 0x02 } else { 0 };
    on | coupling | atten
}

pub fn chl_on_mask(on: [bool; 2]) -> u8 {
    on[0] as u8 | (on[1] as u8) << 1
}

/// zero_offset register value from calibration (`Submitor2.java:160-176`).
pub fn zero_offset(compensation: i32, step: i32, pos0: i32) -> i32 {
    // Java int arithmetic: `/` truncates toward zero, same as Rust.
    let mut v = compensation - pos0 * step / 100;
    if !(-225..=225).contains(&pos0) {
        v += 1;
    }
    v
}

/// MULTI_IO value that makes the connector the external trigger input
/// (`Submitor2.sync_out[SysControl.SYNOUT_TrgIn]`).
pub const MULTI_IO_TRIGGER_IN: u32 = 2;

/// TRG register for a single-source rising-edge trigger
/// (`TinyTrgSubmitHandler.java:61-111`): bit13 channel (0 = CH1, 1 = CH2),
/// bit0 external input, bit12 slope (0 = rise), bits 11:10 sweep.
pub fn trigger(source: usize, sweep: Sweep) -> u16 {
    let sweep = match sweep {
        Sweep::Auto => 0,
        Sweep::Normal => 1,
    };
    let source_bits = if source == EXT { 1 } else { (source as u16 & 1) << 13 };
    source_bits | (sweep << 10)
}

/// Edge level register for a rising edge (`AbsTranslator.getLevelArg`): low
/// byte = upper threshold = level, high byte = lower threshold = level - 10.
pub fn edge_level(level_code: i32) -> u32 {
    const LEVEL_MAX: i32 = 128;
    let mut up = level_code;
    let mut low = up - 10;
    if up > LEVEL_MAX {
        up = LEVEL_MAX;
        low = up - 10;
    }
    if low < -LEVEL_MAX {
        low = -LEVEL_MAX;
        up = low + 10;
    }
    ((up & 0xff) | ((low & 0xff) << 8)) as u32
}

/// Holdoff of 100 ns, the vendor default (`ClockTimeAdjuster.java:29-57`).
pub const HOLDOFF_DEFAULT: u32 = 0x0041;

/// Deep-memory length written to DM: 5000 screen points + 50 + 50 margin.
pub const DM_LEN: u32 = 5100;

/// Pre/post trigger lengths for a centred trigger (`Submitor2.c_htp`,
/// `LowControlManager20M.getHTPWithoutCompress` with htp = 0, incremented to 1):
/// `v = 5000 / 1000` for full-memory timebases, `screen_points / 1000` below.
pub fn pre_suf(tdiv_idx: usize) -> (u32, u32) {
    let half = DM_LEN / 2;
    let v = (screen_points(tdiv_idx) / 1000) as u32;
    (half - v, half + v)
}

/// Parses the 5-byte reply's i32 payload.
pub fn reply_value(reply: &[u8; RESPONSE_LEN]) -> i32 {
    i32::from_le_bytes([reply[1], reply[2], reply[3], reply[4]])
}

/// Per-channel, per-vdiv calibration read from the device flash.
#[derive(Clone, Debug, PartialEq)]
pub struct Calibration {
    pub gain: [[i32; 10]; 2],
    pub step: [[i32; 10]; 2],
    pub compensation: [[i32; 10]; 2],
    pub phase_fine: u16,
    pub version: String,
    pub serial: String,
}

pub const FLASH_LEN: usize = 2002;

/// Parses the 2002-byte flash image (`vds/tiny/firm/FlashMirror.java`,
/// `DefaultPrefControl.java:78-112`, `pref/model/Register.java`).
pub fn parse_flash(flash: &[u8]) -> Result<Calibration> {
    ensure!(flash.len() == FLASH_LEN, "flash image is {} bytes, expected {FLASH_LEN}", flash.len());
    ensure!(flash[0] == 0xaa && flash[1] == 0x55, "flash magic missing (got {:02x} {:02x})", flash[0], flash[1]);
    let version = i32::from_le_bytes(flash[2..6].try_into().unwrap());
    ensure!(version == 2, "unsupported flash layout version {version}");

    // Device partition starts after magic (2) + version (4) + factory partition (1000):
    // three i16 LE tables [2 ch][10 vdiv], order Gain, Step, Compensation (ArgType.java).
    let table = |n: usize| {
        let mut t = [[0i32; 10]; 2];
        let base = 1006 + n * 40;
        for (ch, row) in t.iter_mut().enumerate() {
            for (vb, v) in row.iter_mut().enumerate() {
                let o = base + (ch * 10 + vb) * 2;
                let raw = i16::from_le_bytes([flash[o], flash[o + 1]]) as i32;
                *v = raw.max(0);
            }
        }
        t
    };

    // Register at factory partition + 200: oem byte, version\0, serial\0,
    // then 100 bytes of locale flags, then phase-fine i16.
    let mut p = 6 + 200 + 1;
    let cstr = |p: &mut usize| -> Result<String> {
        let rest = &flash[*p..];
        let Some(end) = rest.iter().position(|&b| b == 0) else {
            bail!("unterminated string in flash register");
        };
        let s = String::from_utf8_lossy(&rest[..end]).into_owned();
        *p += end + 1;
        Ok(s)
    };
    let version_str = cstr(&mut p)?;
    let serial = cstr(&mut p)?;
    let pf = p + 100;
    ensure!(pf + 2 <= 1006, "flash register overflows factory partition");
    let phase_fine = u16::from_le_bytes([flash[pf], flash[pf + 1]]);

    Ok(Calibration {
        gain: table(0),
        step: table(1),
        compensation: table(2),
        phase_fine,
        version: version_str,
        serial,
    })
}

/// Size of one channel's GETDATA block (`AbsPreHandler.ReceiveSize`):
/// 11 header + 100 trigger-locate + 5100 deep memory. The header is
/// `[u8 channel][u32 time_sum][u32 period_num][u16 slow]`; the Java reads it
/// with absolute `ByteBuffer.get(index)`, so it starts at byte 0 even though
/// that buffer is positioned at the deep memory (`GetDataRunner2.java:310-354`).
pub const BLOCK_LEN: usize = 5211;
/// The 5000 deep-memory samples, after the 50-sample pre-roll; 50 post-roll follow.
const DM_BEGIN: usize = 111 + 50;
pub const MAX_SCREEN_POINTS: usize = 5000;

/// Where the on-screen samples sit in a block (`PreHandler.preLoadReceiveData`).
fn screen_window(tdiv_idx: usize) -> (usize, usize) {
    let points = screen_points(tdiv_idx);
    if points >= MAX_SCREEN_POINTS {
        // Whole record, shifted by one sample to remove edge jitter (PreHandler.java:56-66).
        (DM_BEGIN + 1, MAX_SCREEN_POINTS)
    } else {
        // Centred window around the trigger (AbsPreHandler.handelData_noplug).
        (DM_BEGIN + MAX_SCREEN_POINTS / 2 - points / 2, points)
    }
}

#[derive(Debug, PartialEq)]
pub enum Block {
    /// Device has no data yet (`EBUSY`).
    Busy,
    /// On-screen samples, clamped to the ADC range, plus the hardware
    /// frequency counter reading if it has one.
    Data { channel: u8, samples: Vec<i8>, counter_hz: Option<f64> },
}

/// Threshold to feed the hardware frequency counter: the midpoint of the
/// on-screen record in raw ADC codes (`ValueFeeder.syncChannelsMiddle`,
/// `MinMax.computeMiddle`).
pub fn counter_threshold(samples: &[i8]) -> Option<u8> {
    let max = *samples.iter().max()? as i32;
    let min = *samples.iter().min()? as i32;
    Some(((min + max) >> 1) as i8 as u8)
}

/// Hardware frequency counter from the block header: `period_num` periods of
/// the trigger source took `time_sum` ticks of the 100 MHz reference
/// (`GetDataRunner2.acceptChannelDataT2`, `VDS1022.doFrequencyCompute`).
fn counter_hz(buf: &[u8]) -> Option<f64> {
    let time_sum = u32::from_le_bytes(buf[1..5].try_into().unwrap());
    let period_num = u32::from_le_bytes(buf[5..9].try_into().unwrap());
    (time_sum > 0 && period_num > 0).then(|| period_num as f64 * 100e6 / time_sum as f64)
}

pub fn parse_block(buf: &[u8], tdiv_idx: usize) -> Result<Block> {
    if buf.first() == Some(&b'E') {
        return Ok(Block::Busy);
    }
    ensure!(buf.len() >= BLOCK_LEN, "short data block: {} bytes", buf.len());
    let channel = buf[0];
    ensure!(channel < 2, "bad channel byte {channel} in data block");
    let (begin, points) = screen_window(tdiv_idx);
    let samples = buf[begin..begin + points]
        .iter()
        .map(|&b| (b as i8 as i32).clamp(-ADC_MAX, ADC_MAX) as i8)
        .collect();
    Ok(Block::Data { channel, samples, counter_hz: counter_hz(buf) })
}

#[cfg(test)]
mod tests {
    use super::*;

    fn ch(on: bool, vdiv_idx: usize, coupling: Coupling) -> ChannelSettings {
        ChannelSettings { on, vdiv_idx, coupling, pos0: 0, probe: 1 }
    }

    #[test]
    fn golden_packets() {
        assert_eq!(run_stop(true), [0x61, 0, 0, 0, 1, 0]);
        assert_eq!(run_stop(false), [0x61, 0, 0, 0, 1, 1]);
        assert_eq!(cmd(reg::FPGA_QUERY, 1, 0), [0x23, 0x02, 0, 0, 1, 0]);
        assert_eq!(cmd(reg::READ_FLASH, 1, 1), [0xb0, 0x01, 0, 0, 1, 1]);
        assert_eq!(get_data([true, true]), [0x00, 0x10, 0, 0, 2, 5, 5]);
        assert_eq!(get_data([true, false]), [0x00, 0x10, 0, 0, 2, 5, 4]);
        assert_eq!(cmd(reg::DM, 2, DM_LEN), [0x5c, 0, 0, 0, 2, 0xec, 0x13]);
        assert_eq!(cmd(reg::TIMEBASE, 4, 400), [0x52, 0, 0, 0, 4, 0x90, 0x01, 0, 0]);
    }

    #[test]
    fn bytewise() {
        let (pre, suf) = pre_suf(9);
        assert_eq!((pre, suf), (2545, 2555));
        assert_eq!(pre_suf(21), (2545, 2555));
        assert_eq!(pre_suf(6), (2549, 2551));
        assert_eq!(pre_suf(8), (2546, 2554));
        assert_eq!(cmd_bytewise(reg::PRE_TRG, 2, pre), vec![
            vec![0x5a, 0, 0, 0, 1, 0xf1],
            vec![0x5b, 0, 0, 0, 1, 0x09],
        ]);
        let suf_cmds = cmd_bytewise(reg::SUF_TRG, 4, suf);
        assert_eq!(suf_cmds.len(), 4);
        assert_eq!(suf_cmds[0], [0x56, 0, 0, 0, 1, 0xfb]);
        assert_eq!(suf_cmds[3], [0x59, 0, 0, 0, 1, 0x00]);
    }

    #[test]
    fn channel_set_bits() {
        assert_eq!(channel_set(&ch(true, 7, Coupling::Dc)), 0xa2);
        assert_eq!(channel_set(&ch(true, 4, Coupling::Dc)), 0xa0);
        assert_eq!(channel_set(&ch(true, 7, Coupling::Ac)), 0x82);
        assert_eq!(channel_set(&ch(true, 7, Coupling::Gnd)), 0xc2);
        assert_eq!(channel_set(&ch(false, 5, Coupling::Dc)), 0x20);
        assert_eq!(chl_on_mask([true, true]), 3);
        assert_eq!(chl_on_mask([false, true]), 2);
    }

    #[test]
    fn trigger_bits() {
        assert_eq!(trigger(0, Sweep::Auto), 0x0000);
        assert_eq!(trigger(1, Sweep::Auto), 0x2000);
        assert_eq!(trigger(0, Sweep::Normal), 0x0400);
        assert_eq!(trigger(EXT, Sweep::Auto), 0x0001);
        assert_eq!(trigger(EXT, Sweep::Normal), 0x0401);
    }

    #[test]
    fn edge_levels() {
        let cmds = cmd_bytewise(reg::EDGE_LEVEL[0], 2, edge_level(0));
        assert_eq!(cmds[0], [0x2e, 0, 0, 0, 1, 0x00]);
        assert_eq!(cmds[1], [0x2f, 0, 0, 0, 1, 0xf6]);
        // Clamped at the bottom: low = -128, up = -118.
        assert_eq!(edge_level(-125), (((-118i32) & 0xff) | ((-128i32 & 0xff) << 8)) as u32);
    }

    #[test]
    fn zero_offset_formula() {
        assert_eq!(zero_offset(630, 111, 0), 630);
        // 630 - (50*111)/100 = 630 - 55 (5550/100 truncated)
        assert_eq!(zero_offset(630, 111, 50), 575);
        // Truncation toward zero for negative positions: -5550/100 = -55
        assert_eq!(zero_offset(630, 111, -50), 685);
        // |pos0| > 225 gets +1
        assert_eq!(zero_offset(630, 111, 226), 630 - 250 + 1);
        assert_eq!(zero_offset(630, 111, -250), 630 + 277 + 1);
    }

    fn synthetic_flash() -> Vec<u8> {
        let mut f = vec![0u8; FLASH_LEN];
        f[0] = 0xaa;
        f[1] = 0x55;
        f[2..6].copy_from_slice(&2i32.to_le_bytes());
        let mut p = 206;
        f[p] = 0;
        p += 1;
        for s in [b"V2.5".as_slice(), b"VDS1022I1234"] {
            f[p..p + s.len()].copy_from_slice(s);
            p += s.len() + 1;
        }
        f[p + 100..p + 102].copy_from_slice(&0x8012u16.to_le_bytes());
        for t in 0..3 {
            for i in 0..20 {
                let v = (100 * (t + 1) + i) as i16;
                let o = 1006 + t * 40 + i * 2;
                f[o..o + 2].copy_from_slice(&v.to_le_bytes());
            }
        }
        // A negative entry is replaced by 0.
        f[1006..1008].copy_from_slice(&(-5i16).to_le_bytes());
        f
    }

    #[test]
    fn flash_parse() {
        let cal = parse_flash(&synthetic_flash()).unwrap();
        assert_eq!(cal.version, "V2.5");
        assert_eq!(cal.serial, "VDS1022I1234");
        assert_eq!(cal.phase_fine, 0x8012);
        assert_eq!(cal.gain[0][0], 0);
        assert_eq!(cal.gain[0][1], 101);
        assert_eq!(cal.gain[1][0], 110);
        assert_eq!(cal.step[0][3], 203);
        assert_eq!(cal.compensation[1][9], 319);

        let mut bad = synthetic_flash();
        bad[0] = 0;
        assert!(parse_flash(&bad).is_err());
    }

    #[test]
    fn block_parse() {
        assert_eq!(parse_block(b"EBUSY", 9).unwrap(), Block::Busy);
        let mut buf = vec![0u8; BLOCK_LEN];
        buf[0] = 1;
        buf[162] = 0x80; // -128
        buf[162 + 4999] = 42;
        match parse_block(&buf, 9).unwrap() {
            Block::Data { channel, samples, counter_hz } => {
                assert_eq!(channel, 1);
                assert_eq!(counter_hz, None);
                assert_eq!(samples.len(), MAX_SCREEN_POINTS);
                assert_eq!(samples[0], -125); // -128 clamped
                assert_eq!(samples[4999], 42);
            }
            Block::Busy => panic!("expected data"),
        }
        assert!(parse_block(&buf[..100], 9).is_err());

        assert_eq!(counter_threshold(&[49, 62, 55]), Some(55));
        assert_eq!(counter_threshold(&[-10, -3]), Some((-7i8) as u8)); // floor, like Java >> 1
        assert_eq!(counter_threshold(&[]), None);

        // Counter: 3 periods in 300_000 ticks of 100 MHz = 1 kHz.
        buf[1..5].copy_from_slice(&300_000u32.to_le_bytes());
        buf[5..9].copy_from_slice(&3u32.to_le_bytes());
        match parse_block(&buf, 9).unwrap() {
            Block::Data { counter_hz, .. } => assert_eq!(counter_hz, Some(1000.0)),
            Block::Busy => panic!("expected data"),
        }

        // 2 µs/div: 4000 points centred in the 5000-point record.
        buf[661] = 7;
        buf[661 + 3999] = 9;
        match parse_block(&buf, 8).unwrap() {
            Block::Data { samples, .. } => {
                assert_eq!(samples.len(), 4000);
                assert_eq!(samples[0], 7);
                assert_eq!(samples[3999], 9);
            }
            Block::Busy => panic!("expected data"),
        }
    }
}
