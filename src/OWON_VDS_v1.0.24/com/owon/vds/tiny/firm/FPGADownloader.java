package com.owon.vds.tiny.firm;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.core.comm.ICommunicateManager;
import com.owon.uppersoft.vds.device.interpret.util.Sendable;
import com.owon.uppersoft.vds.util.format.EndianUtil;
import com.owon.vds.firm.protocol.AddValueAttachCommand;
import com.owon.vds.firm.protocol.AddressAttachCommand;

public class FPGADownloader implements Logable {
	public static final String FPD_NOFILE = "FPD_NOFILE",
			FPD_SENDFILESIZE = "FPD_SENDFILESIZE",
			FPD_QUERY_BUFSIZE = "FPD_QUERY_BUFSIZE",
			FPD_QUERYSIZE_ERR = "FPD_QUERYSIZE_ERR",
			FPD_START_SEND = "FPD_START_SEND",
			FPD_SEND_LENGTH = "FPD_SEND_LENGTH", FPD_RESPONSE = "FPD_RESPONSE",
			FPD_DONE = "FPD_DONE", FPD_SENDSIZE_ERR = "FPD_SENDSIZE_ERR";

	public static final String fpgaPath = "fpga";

	public FPGADownloader() {
	}

	@Override
	public void log(Object txt) {
		System.out.print(txt);
	}

	@Override
	public void logln(Object txt) {
		System.out.println(txt);
	}

	public File checkFPGAAvailable(String machinetype) {
		File dir = new File(fpgaPath, machinetype);
		try {
			System.out.println(dir.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!dir.exists())
			return null;

		File[] fs = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toUpperCase().contains("FPGA");
			}
		});
		if (fs == null || fs.length <= 0)
			return null;

		File fpgafile = fs[0];

		if (fpgafile.exists() && fpgafile.isFile())
			return fpgafile;
		else
			return null;
	}

	/**
	 * Identify fpga running status: <0 error, ==0 not downloaded, >0 downloaded
	 * 
	 * @param sm
	 * @return Negative value is an error, positive value is not 0 means downloaded
	 * 
	 */
	public int queryFPGADownloaded(ICommunicateManager sm) {
		int rn;
		int len = 5;
		byte[] buf = new byte[len];

		logln("should FPGA re-download?");
		rn = Sendable.interCommmands(sm, FPGA_DOWNLOAD_QUERY_ADD, buf, this);
		if (rn < 0)
			return rn;
		int f = EndianUtil.nextIntL(buf, 1);
		logln("fpga running: " + f);
		return Math.abs(f);
	}

	public static final AddressAttachCommand FPGA_DOWNLOAD_ADD = new AddressAttachCommand(
			"FPGA_DOWNLOAD_ADD", 0x4000, 4);

	public static final AddValueAttachCommand FPGA_DOWNLOAD_QUERY_ADD = new AddValueAttachCommand(
			"FPGA_DOWNLOAD_QUERY_ADD", 0x223, 1, 0); // 判断fpga是否已经下载过了

	public boolean downloadFPGA(
			PropertyChangeListener pcl,
			ICommunicateManager sm,
			File fpgafile) {

		byte[] arr;
		int rn;

		int fsize = (int) fpgafile.length(), mcusize, orderLen = 5, FailLimt = 5;
		pcl.propertyChange(new PropertyChangeEvent(this, FPD_SENDFILESIZE, -1,
				fsize));
		/** 1. Send the FPGA file size to get the MCU cache size. */
		ByteBuffer bbuf = ByteBuffer.allocate(9).order(ByteOrder.LITTLE_ENDIAN);
		AddressAttachCommand fda = FPGA_DOWNLOAD_ADD;
		bbuf.putInt(fda.address);
		bbuf.put((byte) fda.bytes);
		bbuf.putInt(fsize);
		arr = bbuf.array();
		int len = bbuf.position();// 缓存put时position也跟着移动,这里不是0
		/** Send FPGA file F+'file size (4 bytes)' */
		int wrn = sm.write(arr, len);
		if (wrn <= 0) {
			pcl.propertyChange(new PropertyChangeEvent(this, FPD_SENDSIZE_ERR,
					null, null));
			return false;
		}

		/** MCU side reply buffer size D+'buffer size (4 bytes)' */
		pcl.propertyChange(new PropertyChangeEvent(this, FPD_QUERY_BUFSIZE,
				null, null));
		rn = sm.acceptResponse(arr, orderLen);

		if (rn <= 0) {
			pcl.propertyChange(new PropertyChangeEvent(this, FPD_QUERYSIZE_ERR,
					-1, rn));
			return false;
		}

		mcusize = EndianUtil.nextIntL(arr, 1);
		logln((char) arr[0] + " MCUbufferSize:" + mcusize);

		/** 2. After establishing 1, start the transmission of files and other packages. */
		int packetSize = 0;
		int frame = 0;// Frame number starts from 0
		int counter = 0;
		byte[] usbbuf = new byte[mcusize];
		int datasize = mcusize - 4;
		pcl.propertyChange(new PropertyChangeEvent(this, FPD_START_SEND, -1, fsize));
		logln("start sending package...");

		try {
			RandomAccessFile raf = new RandomAccessFile(fpgafile, "r");
			logln("[send FP buf " + datasize + " bytes >>]");
			int fpgaLen = fsize;
			while (packetSize < fpgaLen) {
				int sendsize = datasize;
				if (fpgaLen - packetSize < datasize) {
					sendsize = fpgaLen - packetSize;
					log(" +" + sendsize + " bytes");
				} else {
					log(".");
				}

				EndianUtil.writeIntL(usbbuf, 0, frame);// 写入帧数(4字节,开头)，验证对了
				/** Usbbuf fills the data */
				// ba.get(usbbuf, 4, sendsize);
				raf.read(usbbuf, 4, sendsize);
				// printSendWrapBufInfo(usbbuf, sendsize, frame);
				pcl.propertyChange(new PropertyChangeEvent(this,
						FPD_SEND_LENGTH, -1, sendsize));
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				/** Write includes frame number + one packet (4 bytes, beginning) */
				sm.write(usbbuf, usbbuf.length);
				// logln("write done,waiting MCUreply...");

				// bbuf.clear();
				Arrays.fill(arr, (byte) 0);
				/**
				 * MCU side reply: Receive externally correct: S+' frame number (4 bytes)'.
				 * Receive processing error: F+' frame number (4 bytes)'
				 */
				int r = sm.acceptResponse(arr, len);
				int resf = EndianUtil.nextIntL(arr, 1);

				if (r > 0 && arr[0] == 'S') {
					logln(" MCU:getFrame " + resf);
					// + " (succeed)" +arr[0]
					// + "," + arr[1] + "," + arr[2] + "," + arr[3] + ","
					// + arr[4]);
				} else {
					logln(" MCU:getFrame " + frame + " (fail) " + resf);
					counter++;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (counter >= FailLimt)
						break;
					continue;
				}

				packetSize += sendsize;
				frame++;
			}
			pcl.propertyChange(new PropertyChangeEvent(this, FPD_DONE, null,
					fpgaLen));
			logln("fpga done!");
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}