package com.owon.uppersoft.vds.auto;

import java.nio.ByteBuffer;

import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.vds.calibration.stuff.CalculatUtil;
import com.owon.vds.calibration.stuff.PKCalResult;

enum ArrangeStatus {
	Init, POS_LEVEL, DONE
}

public class WFAutoArrange {
	private static final int HALF_ADC = 120;
	public static final int GRID_RANGE = 25;
	public static final int ADC_RANGE = 250;

	public static final int ADC_SCREEN_RANGE = (int) (ADC_RANGE * 1);
	public static final int ADC_LOCKTRG_RANGE = (int) (ADC_RANGE * 0.4);

	private ArrangeStatus as;
	private WaveForm wf;
	private CoreControl cc;
	private int lastvb;

	public WFAutoArrange(WaveForm wf, int vbnum, CoreControl cc) {
		this.wf = wf;
		this.cc = cc;
		lastvb = vbnum - 1;
		// setVoltBaseIndex(0);
	}

	private boolean setVoltBaseIndex(int vb) {
		boolean b = wf.setVoltBaseIndex(vb, false);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return b;
	}

	private ByteBuffer getADCBuffer() {
		return wf.getADC_Buffer();
	}

	private String getChlName() {
		return wf.toString();
	}

	public WaveForm getWaveForm() {
		return wf;
	}

	private VLog vl = new VLog();

	public void getReady2(int pos0, int preferHeight, int vbidx) {
		this.pos0 = pos0;

		as = ArrangeStatus.Init;
		chlAmpMax = preferHeight;
		chlAmpMin = (int) (preferHeight * 0.8);
		vl.logln("getReady2 " + getChlName() + ", " + pos0 + ", "
				+ preferHeight);
		vl.logln("chlAmpMax: " + chlAmpMax + ", chlAmpMin: " + chlAmpMin);
		wf.setZeroYLoc(pos0, true, false);
		as = ArrangeStatus.Init;
		
		setVoltBaseIndex(vbidx);
	}

	private int pos0;

	public boolean routOut() {
		vl.logln(".......routOut..... >> " + getChlName() + ", ArrangeStatus: "
				+ as);

		switch (as) {
		case Init:
			return checkVBChangeTrend();
		case POS_LEVEL:
			return continuePosLevel();
		default:
			return false;
		}
	}

	private int getAMP() {
		vl.logln("getAMP");
		ByteBuffer bb = getADCBuffer();
		PKCalResult pkcr = CalculatUtil.computePK(bb);
		pkcr.withoutPos0(pos0);
		vl.logln(pkcr);
		double avg = CalculatUtil.computeAverage(bb) - pos0;
		vl.logln("avg: " + avg);

		int amp = getHalfAmp(pkcr, avg) << 1;
		vl.logln("amp " + amp);
		return amp;
	}

	private int getHalfAmp(PKCalResult pkcr, double avg) {
		int abs_high = Math.abs(pkcr.max), abs_low = Math.abs(pkcr.min);
		int pk = pkcr.pk;

		double abs_avg = Math.abs(avg);

		if (abs_avg > pk && pk < 5) {
			if (abs_high > HALF_ADC && abs_low > HALF_ADC) {
				return abs_low;
			} else {
				return (int) abs_avg;
			}
		} else {
			return abs_high > abs_low ? abs_high : abs_low;
		}
	}

	private int chlAmpMax, chlAmpMin;

	/**
	 * 旨在将波形拉到屏幕内并且是比较容易锁住触发的位置
	 * 
	 * @return
	 */
	private boolean checkVBChangeTrend() {
		if (1 == 1)
			return toPosLevel();

		int vb = wf.getVoltbaseIndex();
		int amp = getAMP();
		vl.logln("vb: " + cc.getVoltageProvider().getVoltageLabel(vb)
				+ ", amp:" + amp);

		/** 操作电压档位，只拉小波形，不拉大波形 */
		if (amp < chlAmpMin) {
			return toPosLevel();
		} else if (amp >= chlAmpMax) {
			if (vb + 1 <= lastvb) {
				vl.logln("vb + 1 ");
				setVoltBaseIndex(vb + 1);
				return false;
			} else {
				// 电压幅度太大，不再尝试
				return toPosLevel();
			}
		} else {
			// 下一阶段
			return toPosLevel();
		}
	}

	private boolean continuePosLevel() {
		as = ArrangeStatus.DONE;
		wf.setTrg50Percent(cc.getTriggerControl());
		return true;
	}

	private boolean toPosLevel() {
		as = ArrangeStatus.POS_LEVEL;
		return false;
	}
}