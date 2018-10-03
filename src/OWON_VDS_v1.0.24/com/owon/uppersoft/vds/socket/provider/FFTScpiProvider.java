package com.owon.uppersoft.vds.socket.provider;

import com.owon.uppersoft.dso.function.FFTView;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.mode.control.FFTControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.fft.WndType;
import com.owon.uppersoft.vds.socket.ScpiPool;

public class FFTScpiProvider {
	private ControlManager cm;
	private FFTControl fftc;
	private DataHouse dh;
	private FFTView fftv;

	public FFTScpiProvider(ControlManager ctrm) {
		this.cm = ctrm;
		dh = Platform.getDataHouse();
		fftc = cm.getFFTControl();
		fftv = dh.getWaveFormManager().getFFTView();
	}

	public String getIsDisplay() {
		return fftc.isFFTon() ? "ON" : "OFF";
	}

	public String setDisplay(String args) {
		boolean ffton = false;
		if (args.equalsIgnoreCase("ON"))
			ffton = true;
		else if (args.equalsIgnoreCase("OFF"))
			ffton = false;
		else
			return ScpiPool.Failed;

		fftc.c_setFFTon(ffton);
		dh.getMainWindow().getToolPane().getButtonPane().switch_3in1_fft(ffton);
		// 在运行时直接适用到下一幅，停止的话需要刷新
		if (!cm.sourceManager.isConnected() && ffton) {
			fftv.updateFFT();
		}
		cm.pcs.firePropertyChange(PropertiesItem.CHANGE_FFT, null, ffton);
		return ScpiPool.Success;
	}

	public String getSource() {
		return ScpiPool.CH + (fftc.getFFTchl() + 1);
	}

	public String setSource(String args) {
		String st = ScpiPool.CH;
		if (args.toUpperCase().startsWith(st))
			args = args.substring(st.length()).trim();
		int ch;
		try {
			ch = Integer.parseInt(args) - 1;
		} catch (Exception e) {
			return ScpiPool.Failed;
		}

		if (!fftc.isFFTon())
			return ScpiPool.WarnFFTOff;

		WaveFormManager wfm = dh.getWaveFormManager();
		WaveForm wf = wfm.getWaveForm(ch);
		if (wf == null)
			return ScpiPool.ErrCh;
		fftc.c_setFFTchl(ch);
		fftv.updateFFT();
		dh.getMainWindow().getToolPane().getInfoPane().repaint();
		cm.pcs.firePropertyChange(PropertiesItem.UPDATE_FFT, null, false);
		return ScpiPool.Success;
	}

	public String getWINDow() {
		WndType[] wnds = FFTView.wndTypes;
		return wnds[fftc.getFFTwnd()].toString();
	}

	public String setWINDow(String args) {
		if (!fftc.isFFTon())
			return ScpiPool.WarnFFTOff;
		WndType[] wnds = FFTView.wndTypes;
		for (int i = 0; i < wnds.length; i++) {
			if (args.equalsIgnoreCase(wnds[i].toString())) {
				fftc.setFFTwnd(i);
				fftv.updateFFT();
				cm.pcs.firePropertyChange(PropertiesItem.UPDATE_FFT, null,
						false);
				return ScpiPool.Success;
			}
		}
		return ScpiPool.Failed;
	}

	public String getFORMat() {
		return fftc.getFORMat();
	}

	public String setFORMat(String args) {
		if (!fftc.isFFTon())
			return ScpiPool.WarnFFTOff;

		int formatidx = -1;
		String[] format = FFTControl.format;
		for (int j = 0; j < format.length; j++) {
			String ftype = format[j].toUpperCase();
			if (args.startsWith(ftype)) {
				formatidx = j;
				break;
			}
		}
		if (formatidx < 0)
			return ScpiPool.Failed;

		int idx = -1;
		switch (formatidx) {
		case 0:// VRMS
			Double volt;
			args = args.substring(format[0].length()).trim();
			try {
				volt = Double.parseDouble(args);
				volt *= 1000;// V转mV
			} catch (Exception e) {
				return ScpiPool.Failed;
			}
			for (int i = 0; i < fftc.VOLTAGEs.length; i++) {
				if (volt == fftc.VOLTAGEs[i].getVolt()) {
					fftc.fftvaluetype = formatidx;
					idx = fftv.vrmsbaseidx = i;
					break;
				}
			}
			break;
		case 1:// DB
			args = args.substring(format[1].length()).trim();
			for (int i = 0; i < fftc.dBPerDiv.length; i++) {
				String ftype = fftc.dBPerDiv[i];
				if (args.equalsIgnoreCase(ftype)) {
					fftc.fftvaluetype = formatidx;
					idx = fftv.dBbaseidx = i;
					break;
				}
			}
			break;
		}
		if (idx < 0)
			return ScpiPool.Failed;

		fftv.setVrmsIdx(idx);
		fftv.updateFFT();
		cm.pcs.firePropertyChange(PropertiesItem.UPDATE_FFT, null, true);
		return ScpiPool.Success;
	}

	public String getZONE() {
		String[] scale = FFTControl.scale;
		return scale[fftc.fftscale];
	}

	public String setZONE(String args) {
		if (!fftc.isFFTon())
			return ScpiPool.WarnFFTOff;

		String[] scale = FFTControl.scale;
		for (int i = 0; i < scale.length; i++) {
			if (args.equalsIgnoreCase(scale[i])) {
				fftc.fftscale = i;
				fftv.scale();
				fftv.updateFFT();
				cm.pcs.firePropertyChange(PropertiesItem.UPDATE_FFT, null,
						false);
				return ScpiPool.Success;
			}
		}
		return ScpiPool.Failed;
	}

	public String getFREQbase() {
		int idx = cm.getTimeControl().getTimebaseIdx();
		return cm.getMachineInfo().FFTTimeBases[idx];
	}

	public String setFREQbase(String args) {
		if (!fftc.isFFTon())
			return ScpiPool.WarnFFTOff;
		// args = args.replaceAll(" +", "");
		String[] FreqBase = cm.getMachineInfo().FFTTimeBases;
		for (int i = 0; i < FreqBase.length; i++) {
			String fb = FreqBase[i].replaceAll("\\s*", "");
			if (args.equalsIgnoreCase(fb)) {
				Platform.getMainWindow().getToolPane().getDetailPane()
						.changeFFTTimeBase(i);
				fftv.updateFFT();
				cm.pcs.firePropertyChange(PropertiesItem.UPDATE_FFT, null,
						false);
				return ScpiPool.Success;
			}
		}
		return ScpiPool.Failed;
	}

}
