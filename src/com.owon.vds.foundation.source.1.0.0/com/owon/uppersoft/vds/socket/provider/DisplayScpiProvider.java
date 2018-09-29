package com.owon.uppersoft.vds.socket.provider;

import com.owon.uppersoft.dso.function.FFTCursorControl;
import com.owon.uppersoft.dso.function.MarkCursorControl;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.socket.ScpiPool;

public class DisplayScpiProvider {

	private ControlManager cm;
	private MarkCursorControl mcc;
	private FFTCursorControl fcc;

	private final String[] cursors = { "TIME", "VOLT", "FREQuency", "AMPLitude" };
	private final String VECTOR = "VECTOR", DOT = "DOT";
	private final String[] persistence = { "Off", "0.5s", "1s", "2s", "5s",
			"Infinite", "Phosphor" };

	public DisplayScpiProvider(ControlManager ctrm) {
		this.cm = ctrm;
		this.mcc = cm.mcctr;
		this.fcc = cm.fftctr;
	}

	public String getCursor() {
		StringBuilder sb = new StringBuilder();
		if (mcc.getOnTimebaseM())
			sb.append(cursors[0] + " ");
		if (mcc.getOnVoltbaseM())
			sb.append(cursors[1] + " ");
		if (fcc.getOnFrebaseMark())
			sb.append(cursors[2] + " ");
		if (fcc.getOnVoltbaseMark())
			sb.append(cursors[3]);
		if (sb.length() <= 0)
			sb.append("CURSOR ALL CLOSED");
		return sb.toString();
	}

	public String setCursor(String args, boolean b) {
		for (int i = 0; i < cursors.length; i++) {
			if (args.equalsIgnoreCase(cursors[i])) {
				switch (i) {
				case 0:
					mcc.setOnTimebaseM(b);
					break;
				case 1:
					mcc.setOnVoltbaseM(b);
					break;
				case 2:
					// if (!cm.getFFTControl().isFFTon())
					// return "FFT IS CLOSE";
					fcc.setOnFrebaseMark(b);
					break;
				case 3:
					// if (!cm.getFFTControl().isFFTon())
					// return "FFT IS CLOSE";
					fcc.setOnVoltbaseMark(b);
					break;
				}
				Platform.getMainWindow().re_paint();
				cm.pcs.firePropertyChange(PropertiesItem.UPDATE_CURSOR, null,
						null);
				return ScpiPool.Success;
			}
		}
		return ScpiPool.Failed;
	}

	public String getDraw() {
		return cm.displayControl.linelink ? VECTOR : DOT;
	}

	public String setDraw(String args) {
		if (args.equalsIgnoreCase(VECTOR)) {
			cm.displayControl.linelink = true;
		} else if (args.equalsIgnoreCase(DOT)) {
			cm.displayControl.linelink = false;
		} else
			return ScpiPool.Failed;
		Platform.getMainWindow().updateShow();
		cm.pcs.firePropertyChange(PropertiesItem.UPDATE_LINELINK, null, null);
		return ScpiPool.Success;
	}

	public String getPersistence() {
		int idx = cm.displayControl.getPersistenceIndex();
		return persistence[idx];
	}

	public String setPersistence(String args) {
		int len = persistence.length;
		for (int i = 0; i < len; i++) {
			if (args.equalsIgnoreCase(persistence[i])) {
				if (args.equalsIgnoreCase("Phosphor")
						&& !cm.displayControl.isPhosphorOn())
					return "THIS MACHINE NOT SUPPORT PHOSPHOR";
				int idx = cm.displayControl.setPersistenceIndex(i);
				Platform.getDataHouse().getPersistentDisplay()
						.fadeThdOn_Off_UI(idx);
				cm.pcs.firePropertyChange(
						PropertiesItem.UPDATE_PERSISTENCE_INDEX, null, null);
				return ScpiPool.Success;
			}
		}
		return ScpiPool.Failed;
	}
}
