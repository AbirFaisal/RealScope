//! USB access to the VDS1022 and the connect-time handshake.

use std::thread::sleep;
use std::time::Duration;

use anyhow::{Context, Result, anyhow, bail, ensure};
use nusb::Interface;
use nusb::transfer::{Direction, EndpointType, Queue, RequestBuffer};

use crate::protocol::{self, Calibration, FLASH_LEN, RESPONSE_LEN, reg};

const REPLY_TIMEOUT: Duration = Duration::from_millis(200);
const LONG_TIMEOUT: Duration = Duration::from_millis(3000);
/// Data reads use a short timeout (`GetDataRunner2.timeout`), but the first
/// read after GETDATA can take longer on slow timebases.
const DATA_TIMEOUT: Duration = Duration::from_millis(500);

/// VDS1022 bitstream, uploaded on every power-up.
static FPGA_BITSTREAM: &[u8] = include_bytes!("../../res/fpga/vds1022/VDS1022_FPGA_V3.5.bin");

pub struct Device {
    _interface: Interface,
    out: Queue<Vec<u8>>,
    inp: Queue<RequestBuffer>,
    in_packet_size: usize,
    pub serial: String,
}

/// Waits for the single pending transfer on a queue, cancelling it after the
/// timeout. Evaluates to `Option<Completion<_>>`. (A macro because nusb's
/// `TransferRequest` bound is not public, so no generic fn can name it.)
macro_rules! complete {
    ($queue:expr, $timeout:expr) => {{
        let queue = &mut $queue;
        smol::block_on(async {
            let done = smol::future::or(async { Some(queue.next_complete().await) }, async {
                smol::Timer::after($timeout).await;
                None
            })
            .await;
            if done.is_none() {
                queue.cancel_all();
                let _ = queue.next_complete().await;
            }
            done
        })
    }};
}

impl Device {
    /// Finds the first VDS1022 and claims its interface 0.
    pub fn open() -> Result<Self> {
        let info = nusb::list_devices()
            .context("listing USB devices")?
            .find(|d| d.vendor_id() == protocol::VENDOR_ID && d.product_id() == protocol::PRODUCT_ID)
            .ok_or_else(|| anyhow!("no OWON scope (USB 5345:1234) found"))?;
        let serial = info.serial_number().unwrap_or("").to_owned();
        let device = info.open().context("opening USB device")?;
        let interface = device
            .claim_interface(0)
            .context("claiming interface 0 (is the Java app or another instance running?)")?;

        let (mut ep_in, mut ep_out) = (None, None);
        let alt = interface
            .descriptors()
            .find(|d| d.alternate_setting() == 0)
            .ok_or_else(|| anyhow!("interface 0 has no descriptor"))?;
        for ep in alt.endpoints() {
            if ep.transfer_type() != EndpointType::Bulk {
                continue;
            }
            match ep.direction() {
                Direction::In => ep_in = ep_in.or(Some((ep.address(), ep.max_packet_size()))),
                Direction::Out => ep_out = ep_out.or(Some(ep.address())),
            }
        }
        let (in_addr, in_packet_size) = ep_in.ok_or_else(|| anyhow!("no bulk IN endpoint"))?;
        let out_addr = ep_out.ok_or_else(|| anyhow!("no bulk OUT endpoint"))?;
        let inp = interface.bulk_in_queue(in_addr);
        let out = interface.bulk_out_queue(out_addr);

        Ok(Device { _interface: interface, out, inp, in_packet_size, serial })
    }

    fn write(&mut self, data: &[u8]) -> Result<()> {
        self.out.submit(data.to_vec());
        let c = complete!(self.out, LONG_TIMEOUT).ok_or_else(|| anyhow!("USB write timed out"))?;
        c.status.map_err(|e| anyhow!("USB write failed: {e}"))?;
        let n = c.data.actual_length();
        ensure!(n == data.len(), "short USB write: {n} of {}", data.len());
        Ok(())
    }

    /// Reads up to `len` bytes; the transfer ends early on a short packet,
    /// like the Java `CDevice.readBulk`.
    fn read(&mut self, len: usize, timeout: Duration) -> Result<Vec<u8>> {
        let mps = self.in_packet_size;
        self.inp.submit(RequestBuffer::new(len.div_ceil(mps) * mps));
        let c = complete!(self.inp, timeout).ok_or_else(|| anyhow!("USB read timed out"))?;
        c.status.map_err(|e| anyhow!("USB read failed: {e}"))?;
        let mut data = c.data;
        data.truncate(len);
        Ok(data)
    }

    /// Sends one command and returns the i32 payload of its 5-byte reply.
    pub fn command(&mut self, packet: &[u8]) -> Result<i32> {
        self.write(packet)?;
        let reply = self.read(RESPONSE_LEN, REPLY_TIMEOUT)?;
        let reply: [u8; RESPONSE_LEN] = reply
            .as_slice()
            .try_into()
            .map_err(|_| anyhow!("bad reply length {} to command {packet:02x?}", reply.len()))?;
        Ok(protocol::reply_value(&reply))
    }

    pub fn write_reg(&mut self, addr: u32, len: u8, value: u32) -> Result<i32> {
        self.command(&protocol::cmd(addr, len, value))
    }

    pub fn write_reg_bytewise(&mut self, addr: u32, len: u8, value: u32) -> Result<()> {
        for packet in protocol::cmd_bytewise(addr, len, value) {
            self.command(&packet)?;
        }
        Ok(())
    }

    /// Sends GETDATA and reads one block per enabled channel.
    /// Returns `None` if the device answered EBUSY.
    pub fn fetch(&mut self, ch_on: [bool; 2]) -> Result<Option<Vec<Vec<u8>>>> {
        self.write(&protocol::get_data(ch_on))?;
        let n = ch_on.iter().filter(|&&on| on).count();
        let mut blocks = Vec::with_capacity(n);
        for i in 0..n {
            let block = self.read(protocol::BLOCK_LEN, DATA_TIMEOUT)?;
            if i == 0 && block.first() == Some(&b'E') {
                return Ok(None);
            }
            ensure!(block.len() == protocol::BLOCK_LEN, "short data block: {} bytes", block.len());
            blocks.push(block);
        }
        Ok(Some(blocks))
    }

    fn read_flash(&mut self) -> Result<Vec<u8>> {
        self.write(&protocol::cmd(reg::READ_FLASH, 1, 1))?;
        let flash = self.read(FLASH_LEN, LONG_TIMEOUT)?;
        ensure!(flash.len() == FLASH_LEN, "flash read returned {} bytes", flash.len());
        Ok(flash)
    }

    /// `FPGADownloader.downloadFPGA`: announce the size, get the MCU buffer
    /// size, then send numbered frames of exactly that size.
    fn upload_fpga(&mut self, log: &mut dyn FnMut(String)) -> Result<()> {
        let size = FPGA_BITSTREAM.len();
        let buf_size = self.command(&protocol::cmd(reg::FPGA_DOWNLOAD, 4, size as u32))?;
        ensure!(buf_size > 4, "MCU reported FPGA buffer size {buf_size}");
        let buf_size = buf_size as usize;
        let chunk = buf_size - 4;
        let frames = size.div_ceil(chunk);
        log(format!("Uploading FPGA bitstream ({size} bytes, {frames} frames)…"));

        let mut failures = 0;
        let mut frame = 0usize;
        while frame * chunk < size {
            let data = &FPGA_BITSTREAM[frame * chunk..((frame + 1) * chunk).min(size)];
            let mut packet = vec![0u8; buf_size];
            packet[..4].copy_from_slice(&(frame as u32).to_le_bytes());
            packet[4..4 + data.len()].copy_from_slice(data);
            sleep(Duration::from_millis(50));
            self.write(&packet)?;
            let reply = self.read(RESPONSE_LEN, LONG_TIMEOUT)?;
            if reply.first() == Some(&b'S') {
                frame += 1;
                if frame.is_multiple_of(10) || frame == frames {
                    log(format!("FPGA upload {frame}/{frames}"));
                }
            } else {
                failures += 1;
                if failures >= 5 {
                    bail!("FPGA upload failed at frame {frame}");
                }
                sleep(Duration::from_millis(100));
            }
        }
        Ok(())
    }

    /// Opens the scope and brings it to a state where registers can be
    /// written (`InterCommTiny.initMachine`).
    /// Discards anything the scope still had queued for the previous session
    /// (e.g. half of a data block if that process quit mid-transfer), which
    /// would otherwise be read as the reply to our first commands.
    fn drain(&mut self) -> usize {
        let mut drained = 0;
        for _ in 0..64 {
            match self.read(protocol::BLOCK_LEN, Duration::from_millis(50)) {
                Ok(data) if !data.is_empty() => drained += data.len(),
                _ => break,
            }
        }
        drained
    }

    pub fn connect(log: &mut dyn FnMut(String)) -> Result<(Self, Calibration)> {
        let mut dev = Self::open()?;
        let stale = dev.drain();
        if stale > 0 {
            log(format!("Discarded {stale} stale bytes from a previous session"));
        }

        let fpga_loaded = dev.write_reg(reg::FPGA_QUERY, 1, 0)? != 0;
        let cal = protocol::parse_flash(&dev.read_flash()?).context("reading calibration from flash")?;
        if !fpga_loaded {
            dev.upload_fpga(log)?;
            dev.write_reg_bytewise(reg::PHASE_FINE, 2, cal.phase_fine as u32)?;
        }
        sleep(Duration::from_millis(500));
        Ok((dev, cal))
    }
}
