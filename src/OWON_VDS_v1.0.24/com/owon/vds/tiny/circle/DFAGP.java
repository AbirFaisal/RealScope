package com.owon.vds.tiny.circle;

import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.vds.tiny.circle.agp.SendComand;
import com.owon.vds.tiny.circle.agp.SerialComm;

public class DFAGP implements AGPControl {
	private SendComand sendComand;
	private VoltageProvider vp;

	public DFAGP(VoltageProvider vp) {
		this.vp = vp;
	}

	@Override
	public void init(String sPort) {
		SerialComm sc = new SerialComm();

		boolean b = sc.openPort(sPort);
		if (!b) {
			// 串口被占用
			System.out.println("serial busy");
			return;
		}
		sendComand = new SendComand(sc);
		sendComand.check_usb_rs232();
	}

	@Override
	public void turnChannels(boolean on) {
		sendComand.turnChannels(on);
	}

	@Override
	public void genWFwithVB(int vbidx) {
		System.out.println("genWFwithVB " + vbidx);
		// int vbbase = vp.getVoltage(0, vbidx);// mv 探头倍率x1

		sendComand.changeVmp(vbidx);
		// gen: ac, quare, low, up, freq(1us)
	}

	@Override
	public void finish() {
		sendComand.finish();
	}
}