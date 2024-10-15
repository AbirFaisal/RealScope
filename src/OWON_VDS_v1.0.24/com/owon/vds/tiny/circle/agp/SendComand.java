package com.owon.vds.tiny.circle.agp;

import java.util.List;

import javax.swing.JButton;

import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.source.comm.InfiniteDaemon;
import com.owon.uppersoft.dso.util.DBG;
import com.owon.vds.tiny.tune.TinyTuneFunction;

public class SendComand {
	/** 闭环 */
	// 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000
	String[] command = { //
	"$Amp,0.03,Vpp", "$Amp,0.06,Vpp", "$Amp,0.12,Vpp",//
			"$Amp,0.30,Vpp", "$Amp,0.6,Vpp", "$Amp,1.2,Vpp", //
			"$Amp,3,Vpp", "$Amp,6,Vpp", "$Amp,12,Vpp", //
			"$Amp,30,Vpp" };
	public static final String OUTPUT_OPEN = "$Output,1,,794#";
	public static final String OUTPUT_CLOSE = "$Output,0,,793#";
	public static final String WAVE_SAUARE = "$Wave,1,,540#";
	public static final String WAVE_FREQ = "$Freq,500,Hz,829#";

	private SerialComm sc;
	private JButton bp;
	private InfiniteDaemon daemon;
	TinyTuneFunction ttd;
	private WaveFormManager wfm;

	public SendComand(SerialComm sc) {
		this.sc = sc;
	}

	public void finish() {
		sc.release();
	}

	@Override
	public String toString() {
		return "m_ClcRunner";
	}

	public boolean check_usb_rs232() {
		/* Check if the serial port is connected */
		// sc.init();
		String res = sendCMD(WAVE_SAUARE);
		if (res.equals("")) {
			config("serical do not connected");
			return false;
		} else
			return true;
	}

	private String sendCMD(String s) {
		config("rs232 write: " + s + "\r\n{");
		String res = sc.comm(s.getBytes());
		config("}\r\n rs232 read: " + res);
		return res;
	}

	public void changeVmp(int i) {
		// System.out.println(checksum("$Freq,500,Hz"));
		String cmd = command[i];
		config("rs232 write: " + i + ":" + cmd + "\r\n{");

		String sa = cmd + "," + checksum(cmd) + "#";

		String res = sc.comm(sa.getBytes());
		config("}\r\n rs232 read: " + res);
	}

	public void turnChannels(boolean on) {
		config("turnChannels: " + on);
		if (on) {
			sendCMD(OUTPUT_OPEN);

			/* Add a small delay for serial communication */

			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			sendCMD(WAVE_FREQ);
		} else {
			sendCMD(OUTPUT_CLOSE);
		}
	}

	public int checksum(String d) {
		int s = 0;

		byte[] ss = d.getBytes();
		for (int i = 1; i < ss.length; i++) {
			s += (ss[i] & 0xff);
		}
		return s;
	}

	private void config(String msg) {
		System.out.println(msg + "\r\n");
	}

	public static void main(String[] args) {
		byte[] d = "\r\n".getBytes();
		System.out.println(d[0] + "," + d[1]);
		calibrate(0);
	}

	public static final void calibrate(int circleSerialPort) {

		List<String> list = SerialPortUtil.loadAvailablePort();

		if (list.size() <= 0 || list.size() < circleSerialPort + 1) {
			DBG.config("no this comm:" + circleSerialPort);
			return;
		}
		String sPort = list.get(circleSerialPort);
		SerialComm sc = new SerialComm();

		boolean b = sc.openPort(sPort);
		if (!b) {
			// Serial port is occupied
			System.out.println("serial busy");
			return;
		}
		SendComand sendComand = new SendComand(sc);
		sendComand.check_usb_rs232();
	}

}