package com.owon.uppersoft.vds.auto;

import java.nio.ByteBuffer;

import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.vds.calibration.stuff.CalculatUtil;
import com.owon.vds.calibration.stuff.PKCalResult;

enum VBAim {
	bigger, smaller, both;
}

/**
 * WFAutoRoutine，自校正要准确才能自动设置，否则遵循的波形规律就失效了
 * 
 */
public class WFAutoRoutine {
	public enum AutoStatus {
		Init, ContinueCheckVB, NoInput, POS_LEVEL, DONE
	}

	private static final int HALF_ADC = 120;
	public static final int GRID_RANGE = 25;
	public static final int ADC_RANGE = 250;

	public static final int ADC_SCREEN_RANGE = (int) (ADC_RANGE * 1);
	public static final int ADC_LOCKTRG_RANGE = (int) (ADC_RANGE * 0.2);
	// 1 / 2.5

	private AutoStatus as = AutoStatus.Init;
	private WaveForm wf;
	private int lastvb;
	private CoreControl cc;

	public AutoStatus getAutoStatus() {
		return as;
	}

	public WaveForm getWaveForm() {
		return wf;
	}

	private int midvb;
	private int limitAMP;

	public WFAutoRoutine(WaveForm wf, int vbnum, CoreControl cc,
			double limitGridLitterVB) {
		this.wf = wf;
		this.cc = cc;
		limitAMP = (int) (GRID_RANGE * limitGridLitterVB);
		lastvb = vbnum - 1;
		midvb = vbnum >> 1;
	}

	public void getReady() {
		as = AutoStatus.Init;
		checkOffCount = 0;
		allCheckCount = 0;
		vbaim = VBAim.both;
	}

	private VLog vl = new VLog();

	/**
	 * @return 是否结束了该通道的所有设置
	 */
	public boolean routOut() {
		vl.logln(".......routOut..... >> " + getChlName() + ", AutoStatus: "
				+ as);

		switch (as) {
		case Init:
			return checkOff();
		case ContinueCheckVB:
			return checkVBChangeTrend();
		case POS_LEVEL:
			return continuePosLevel();
		default:
			return true;
		}
	}

	private int checkOffCount, allCheckCount;

	private boolean checkOff() {
		vl.logln("checkOff >> " + getChlName() + ", checkOffCount: "
				+ checkOffCount + ", allCheckCount: " + allCheckCount);
		ByteBuffer bb = getADCBuffer();
		if (bb == null) {
			vl.logln("ByteBuffer bb null?");
			return false;
		}

		PKCalResult pkcr = CalculatUtil.computePK(bb);
		vl.logln(pkcr);
		int max = Math.abs(pkcr.max), min = Math.abs(pkcr.min);

		/** 当超过界限值，可以一次认定为开启 */
		if (max > limitAMP || min > limitAMP) {
			detemineOn();
			return false;
		}

		int del = (max < limitAMP && min < limitAMP) ? 1 : -1;
		checkOffCount += del;
		allCheckCount++;

		if (allCheckCount > 6) {
			as = AutoStatus.NoInput;
			vl.logln("checkOut NoInput >> " + getChlName());
			return true;
		}

		if (checkOffCount > 2) {
			as = AutoStatus.NoInput;
			vl.logln("checkOut NoInput >> " + getChlName());
			return true;
		} else if (checkOffCount < -2) {
			detemineOn();
			return false;
		}
		return false;
	}

	private void detemineOn() {
		vl.logln("checkOut Input >> " + getChlName());
		setVoltBaseIndex(midvb);
		// 设置了，需要在下一次再取数据判断
		as = AutoStatus.ContinueCheckVB;
		vbaim = VBAim.both;
	}

	private boolean setVoltBaseIndex(int vb) {
		boolean b = wf.setVoltBaseIndex(vb, false);
//		try {
//			Thread.sleep(50);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		return b;
	}

	private ByteBuffer getADCBuffer() {
		return wf.getADC_Buffer();
	}

	private String getChlName() {
		return wf.toString();
	}

	private VBAim vbaim = VBAim.both;

	public int getAMP() {
		vl.logln("getAMP");
		ByteBuffer bb = getADCBuffer();
		PKCalResult pkcr = CalculatUtil.computePK(bb);
		vl.logln(pkcr);
		double avg = CalculatUtil.computeAverage(bb);
		vl.logln("avg: " + avg);

		int amp = getHalfAmp(pkcr, avg) << 1;
		vl.logln("amp " + amp);
		return this.amp = amp;
	}

	private int getHalfAmp(PKCalResult pkcr, double abs_avg) {
		int abs_high = Math.abs(pkcr.max), abs_low = Math.abs(pkcr.min);
		int pk = pkcr.pk;

		abs_avg = Math.abs(abs_avg);

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

	/**
	 * 旨在将波形拉到屏幕内并且是比较容易锁住触发的位置
	 * 
	 * @return
	 */
	private boolean checkVBChangeTrend() {
		if (vbaim == null) {
			System.err.println("vbTrend(VBAim vbaim) err");
			return true;
		}

		int vb = wf.getVoltbaseIndex();
		vl.logln("vb: " + cc.getVoltageProvider().getVoltageLabel(vb));
		int amp = getAMP();
		if (vbaim != VBAim.smaller && amp >= ADC_SCREEN_RANGE) {
			if (vb + 1 <= lastvb) {
				vbaim = VBAim.bigger;
				vl.logln("vb + 1 ");
				setVoltBaseIndex(vb + 1);
				return false;
			} else {
				// 电压幅度太大，不再尝试
				return toPosLevel();
			}
		} else if (vbaim != VBAim.bigger && amp < ADC_LOCKTRG_RANGE) {
			if (vb - 1 >= 0) {
				vbaim = VBAim.smaller;
				vl.logln("vb - 1 ");
				setVoltBaseIndex(vb - 1);
				return false;
			} else {
				// vl.logln("continue as = AutoStatus.NoInput;");
				// as = AutoStatus.NoInput;
				return toPosLevel();
			}
		} else {
			// 下一阶段
			return toPosLevel();
		}
	}

	private boolean toPosLevel() {
		vbaim = VBAim.both;
		as = AutoStatus.POS_LEVEL;
		return false;
	}

	public int amp = -1;

	private boolean continuePosLevel() {
		as = AutoStatus.DONE;
		wf.setTrg50Percent(cc.getTriggerControl());
		return true;
	}

}