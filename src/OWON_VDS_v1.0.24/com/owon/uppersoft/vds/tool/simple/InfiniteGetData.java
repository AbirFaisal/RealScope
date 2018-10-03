package com.owon.uppersoft.vds.tool.simple;

import java.nio.ByteBuffer;

public class InfiniteGetData implements Runnable {
	public static int LTimeOut = 500;
	// private int period = 200;
	public int shortsleep = 50;
	private ByteBuffer ordbuf = ByteBuffer.allocate(12);
	private UsbCommunicator uct;
	private UsbComControl ucc;

	private ByteBuffer sendCMDbuf;

	public InfiniteGetData(UsbCommunicator uct) {
		this.uct = uct;
		this.ucc = uct.getUsbComControl();
		sendCMDbuf = ucc.getSendCMDbuf();
	}

	public void sendCMD(int add, int bytes, int value) {
		ucc.addCMD(add, bytes, value, ordbuf);
		uct.write(ordbuf.array(), ordbuf.position());
		ordbuf.position(0);
	}

	public void run() {
		try {
			sendCMD(0x10A, 2, 0x2A0);// 垂直位置调整
			sendCMD(0x108, 2, 0x250);
			sendCMD(0xb, 1, 03);
			sendCMD(0x224, 1, 1);// os识别的批量设置
			sendCMD(0x2E, 2, 0xF100);// 触发位置设定0
			sendCMD(0x30, 2, 0xF100);
			sendCMD(0x24, 2, 0x2000);// ch1: 0x0, ch2: 0x2000单触边沿触发

			Thread.sleep(shortsleep);
			while (true) {
				if (!uct.isKeepGet())
					break;

				if (dealCommand()) {
					Thread.sleep(shortsleep);
				}

				ucc.addTextArea("[GET ch] ");
				sendCMD(0x1000, 2, 0x0101);// 5k 5ebusy
				ucc.acceptOneFrame(2);
				
				// long p1 = System.currentTimeMillis();
//				ucc.addTextArea("[GET ch1] ");
//				sendCMD(0x1000, 4, 1);// 64
//				ucc.acceptOneFrame();// ch0
//				ucc.addTextArea("[GET ch2] ");
//				sendCMD(0x1000, 4, 1 << 4);
//				ucc.acceptOneFrame();// ch1

				// long p2 = System.currentTimeMillis();
				// long t = p2 - p1;
				// uct.updateElapsedTimeLab(t);
				// Thread.sleep(period);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private boolean dealCommand() {
		synchronized (sendCMDbuf) {
			int num = sendCMDbuf.position();
			if (num == 0)
				return false;

			/** 没在加指令时才发送 */
			if (!uct.canAddcmd()) {
				ucc.dealCMDInteractive();
				System.out.println("dealCMD true");
				return true;
			}
		}
		return false;
	}
}