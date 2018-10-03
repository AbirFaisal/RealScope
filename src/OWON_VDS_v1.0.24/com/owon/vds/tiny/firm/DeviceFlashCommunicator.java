package com.owon.vds.tiny.firm;

import java.util.zip.CRC32;

import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.core.comm.ICommunicateManager;
import com.owon.uppersoft.vds.device.interpret.TinyCommunicationProtocol;
import com.owon.uppersoft.vds.device.interpret.util.PackUtil2;
import com.owon.uppersoft.vds.util.ArrayLogger;
import com.owon.vds.firm.protocol.AddValueAttachCommand;

public class DeviceFlashCommunicator implements Logable {
	public static final int FLASH_CONTENT_SIZE = 2000;
	public static final int FLASH_SIZE = 2002;

	public static final int DEVPREF_CRCSKIPBYTES = 2;

	public static final byte DEVPREF_WBYTE1 = (byte) 0xaa;
	public static final byte DEVPREF_WBYTE0 = (byte) 0x55;

	public static final byte DEVPREF_RBYTE0 = (byte) 0xaa;
	public static final byte DEVPREF_RBYTE1 = (byte) 0x55;

	public static final AddValueAttachCommand write_flash = new AddValueAttachCommand(
			"write_flash", 0x1a0, 1, 1);// 写flash
	public static final AddValueAttachCommand read_flash = new AddValueAttachCommand(
			"read_flash", 0x1b0, 1, 1);// 读flash

	public DeviceFlashCommunicator() {
	}

	protected long updateCRC32(byte[] arr) {
		CRC32 c32 = new CRC32();
		c32.update(arr, DeviceFlashCommunicator.DEVPREF_CRCSKIPBYTES,
				FLASH_CONTENT_SIZE);
		long result = c32.getValue();
		return result;
	}

	@Override
	public void log(Object txt) {
		System.out.print(txt);// PrincipleTiny.dblog(txt);
	}

	@Override
	public void logln(Object txt) {
		System.out.println(txt); // PrincipleTiny.dblogln(txt);
	}

	public byte[] fetchPrefernce(ICommunicateManager ism,
			boolean beforeFPGADownload) {
		logln("beforeFPGADownload: " + beforeFPGADownload);
		// if (beforeFPGADownload) {
		// byte[] arr = PackUtil2.packCMD_5('R', FLASH_CONTENT_SIZE);
		// ism.write(arr, arr.length);
		// } else {
		writeCommmand(ism, read_flash);
		// }

		int len = FLASH_SIZE;
		byte[] arr = new byte[len];
		int rn = ism.acceptResponse(arr, len);
		logln("rn: " + rn);

		/** If you don't need fpga, you can't seem to read it? */
		// 0xAA 0x55
		// lg.log("0x");
		// lg.log(Integer.toHexString(arr[0]));
		// lg.log(", 0x");
		// lg.log(Integer.toHexString(arr[1]));
		ArrayLogger.outArray2Logable(this, arr, 0, rn);
		if (rn == len) {
			CRC32 c32 = new CRC32();
			c32.update(arr, DeviceFlashCommunicator.DEVPREF_CRCSKIPBYTES,
					FLASH_CONTENT_SIZE);
			final long result = c32.getValue();
			logln("fetch flash crc32: " + result);
			return arr;
		}

		return null;
	}

	public boolean sendPrefernce(ICommunicateManager ism, byte[] arr) {
		boolean success = true;
		writeCommmand(ism, write_flash);

		int len = TinyCommunicationProtocol.RESPONSE_LEN;
		byte[] res = new byte[len];
		int rn = ism.acceptResponse(res, len);
		// DBG.outArrayAlpha(res, 0, rn);

		len = FLASH_SIZE;
		arr[0] = DEVPREF_WBYTE0;
		arr[1] = DEVPREF_WBYTE1;
		logln("sendPrefernce");

		len = arr.length;
		int wn = ism.write(arr, len);
		logln("wn: " + wn);
		ArrayLogger.outArray2Logable(this, arr, 0, wn);

		final long result = updateCRC32(arr);
		logln("send flash crc32: " + result);
		if (wn != len)
			return false;

		len = TinyCommunicationProtocol.RESPONSE_LEN;
		res = new byte[len];
		ism.acceptResponse(res, len);
		// DBG.configArray(res, 0, len);
		return success;
	}

	protected int writeCommmand(ICommunicateManager ism,
			AddValueAttachCommand tca) {
		byte[] buf = PackUtil2.packCMD(tca);
		return ism.write(buf, buf.length);
	}
}