package com.owon.uppersoft.vds.tool.simple;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.core.comm.ICommunicateManager;
import com.owon.vds.tiny.firm.FPGADownloader;

public class InteractiveBranch implements Runnable, Logable {

	private UsbCommunicator uct;
	private ICommunicateManager ucc;

	public InteractiveBranch(UsbCommunicator usbct, ICommunicateManager usbs) {
		uct = usbct;
		ucc = usbs;
	}

	public FPGADownloader fg = new FPGADownloader();

	@Override
	public void run() {
		boolean success = fg.downloadFPGA(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
			}
		}, ucc, new File("fpga", "FPGA.bin"));
		if (!success)
			return;
	}

	/** @deprecated */
	public Runnable comSend = new Runnable() {
		@Override
		public void run() {
			String inputtxt = uct.getMsgInputText();
			byte[] req;
			if (inputtxt.equals(FPGADownloader.fpgaPath)) {// ucc.sendFileName
				req = new byte[0]; // should be fpga file byte array ucc.
			} else {
				req = inputtxt.getBytes();
			}

			// long p1 = System.currentTimeMillis();
			int wn = ucc.write(req, req.length);// return -1，write failure
			if (wn <= 0) {
				addTextArea(" send( " + wn + " bytes):  " + "Error!");
				return;
			} else {
				addTextArea(" Send( " + wn + " bytes):  " + inputtxt);
			}

			byte[] rsp = new byte[4096];
			int rn = ucc.acceptResponse(rsp, rsp.length);
			// long p2 = System.currentTimeMillis();
			// elapsedTime = p2 - p1;
			// uct.updateElapsedTimeLab(elapsedTime);

			if (rn <= 0) {
				addTextArea("receive( " + rn + " bytes):  " + "Error!");
			} else {
				addTextArea("receive( " + rn + " bytes):  "
						+ new String(rsp, 0, rn));
			}
			// if (rsp != null && rsp.length < rspLen)
			// rsp = new byte[rspLen];
		}
	};

	public void setCycleAcceptOn(boolean b) {
		accepting = b;
	}

	private boolean accepting = false;
	/** @deprecated */
	public Runnable acp = new Runnable() {
		@Override
		public void run() {
			byte[] accept = new byte[4096];
			int t = 500;
			try {
				while (accepting) {
					Thread.sleep(t);
					long p1 = System.currentTimeMillis();
					int rn = ucc.acceptResponse(accept, accept.length);
					long p2 = System.currentTimeMillis();
					long elapsedTime = p2 - p1;
					uct.updateElapsedTimeLab(elapsedTime);
					DBG.outprintln("ElapsedTime:" + elapsedTime + ",rn:" + rn);
					if (rn <= 0) {
						if (t >= 2000) {
							String txt = "accept( " + rn + " bytes):  "
									+ "Error!" + ",elapsedTime:" + elapsedTime;
							addTextArea(txt);
						}
						if (t < 4000)
							t += 500;
						else {
							if (accepting)
								addTextArea(".....Try again after a few seconds");
						}

					} else {
						t = 500;

						String txt = "accept( " + rn + " bytes):  "
								+ new String(accept, 0, rn);
						addTextArea(txt);
					}
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

	};

	@Override
	public void logln(Object o) {
		addTextArea(o.toString());
	}

	@Override
	public void log(Object o) {
		appendTextArea(o.toString());
	}

	private void addTextArea(String txt) {
		if (uct == null)
			DBG.configln(txt);
		else
			uct.addTextArea(txt);
	}

	private void appendTextArea(String txt) {
		if (uct == null)
			DBG.configln(txt);
		else
			uct.appendTextArea(txt);
	}
}