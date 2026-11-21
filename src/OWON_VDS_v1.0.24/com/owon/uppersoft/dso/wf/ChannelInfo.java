package com.owon.uppersoft.dso.wf;

import java.awt.Color;

import com.owon.uppersoft.dso.data.LObject;
import com.owon.uppersoft.dso.function.Markable;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.vds.core.aspect.base.IOrgan;
import com.owon.uppersoft.vds.core.aspect.control.Pos0_VBChangeInfluence;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.core.comm.effect.P_Channel;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;
import com.owon.uppersoft.vds.data.RGB;
import com.owon.uppersoft.vds.util.Pref;
import com.owon.uppersoft.vds.util.format.UnitConversionUtil;

public class ChannelInfo implements IOrgan, IChannelInfo, Markable {

	// public static final String[] SWITCH = { "ON", "OFF" };
	public static final LObject[] COUPLING = {
			new LObject("M.Channel.Coupling.DC"),
			new LObject("M.Channel.Coupling.AC"),
			new LObject("M.Channel.Coupling.Ground") };
	public static final String[] COUPLINGCHARS = { "DC", "AC", "Gnd" };

	public static final int CouplingDCIndex = 0;
	public static final int CouplingACIndex = 1;
	public static final int CouplingGroundIndex = 2;

	private boolean on = false;
	private int probeMultiIdx = 0;
	private int couplingIdx = 0;
	private int vbIdx;
	private int pos0;

	private int number;
	private String name;
	private final RGB rgb;
	private final Color color;

	private boolean bandlimit = false;
	private boolean inverse = false;
	public Volt[] VOLTAGEs;

	private float freq;
	private String freqtxt;

	public class Volt {
		public int idx;

		public Volt(int idx) {
			this.idx = idx;
		}

		public String getLabel(int pbrate) {
			return UnitConversionUtil.getIntVoltageLabel_mV(getValue(pbrate));
		}

		public int getValue(int pbrate) {
			return vp.getVoltage(pbrate, idx);
		}

		public int getValue() {
			return getValue(probeMultiIdx);
		}

		@Override
		public String toString() {
			return getLabel(probeMultiIdx);
		}
	}

	public VoltageProvider getVoltageProvider() {
		return vp;
	}

	private VoltageProvider vp;
	private Pos0_VBChangeInfluence pvi;

	public ChannelInfo(int number, VoltageProvider vp,
			Pos0_VBChangeInfluence pvi) {
		this.number = number;
		this.vp = vp;
		this.pvi = pvi;
		this.name = "CH" + (number + 1);

		int len = vp.getVoltageNumber();
		VOLTAGEs = new Volt[len];
		for (int m = 0; m < len; m++) {
			VOLTAGEs[m] = new Volt(m);
		}

		rgb = getChannelRGB(number);
		color = rgb.getColor();
		// load(p);
	}

	public int getValidVotageChangeIndex(int idx) {
		int change = idx;
		if (idx < 0) {
			change = 0;
		}
		if (idx >= vp.getVoltageNumber()) {
			change = vp.getVoltageNumber() - 1;
		}
		// avoid the vertical scale not being set again when calibrating gain in a closed loop
		return change;// (change == getVoltbaseIndex()) ? -1 :
	}

	private static RGB[] rgbs = new RGB[4];
	static {
		int i = 0;
		rgbs[i++] = new RGB("51B2E8");
		rgbs[i++] = new RGB("B2E851");
		rgbs[i++] = new RGB("66CCFF");
		rgbs[i] = new RGB("CC00FF");
	}

	public static RGB getChannelRGB(int wfidx) {
		return rgbs[wfidx];
	}

	public void load(Pref p) {
		// when no corresponding config can be found for the channel
		int vb = p.loadInt(name + ".vbIdx");
		if (vb < 0 || vb >= vp.getVoltageNumber()) {
			vb = 0;
		}
		this.pos0 = p.loadInt(name + ".pos0");
		// KNOW set pos0 first, so that getPos0() returns the correct value when the voltage scale is set
		setVoltbaseIndex(vb, true, true);
		this.on = p.loadBoolean(name + ".on");

		int idx = p.loadInt(name + ".probeMultiIdx");
		setProbeMultiIdx(idx);

		this.couplingIdx = p.loadInt(name + ".couplingIdx");
		this.bandlimit = p.loadBoolean(name + ".bandlimit");
		inverse = p.loadBoolean(name + ".inverse");
	}

	public void persist(Pref p) {
		p.persistInt(name + ".vbIdx", vbIdx);
		p.persistInt(name + ".pos0", pos0);
		p.persistBoolean(name + ".on", on);
		p.persistInt(name + ".probeMultiIdx", probeMultiIdx);
		p.persistInt(name + ".couplingIdx", couplingIdx);
		p.persistRGB(name + ".rgb", rgb);

		p.persistBoolean(name + ".bandlimit", isBandlimit());
		p.persistBoolean(name + ".inverse", inverse);
	}

	public boolean isGround() {
		return couplingIdx == CouplingGroundIndex;
	}

	public Volt[] getVoltageLabels() {
		return VOLTAGEs;
	}

	public int getPos0() {
		return pos0;
	}

	public String getName() {
		return name;
	}

	/**
	 * Returns the value corresponding to pos0 within this half range, from top to bottom, from 0 to 2*hr,
	 * 
	 * @param hr
	 * @return
	 */
	public int getPos0byRange(int hr) {
		return hr - pos0;
	}

	/**
	 * When inversion is appended, used to get the "relative" value; when inversion is filtered, used to get the "absolute" value
	 * 
	 * @param v
	 * @return
	 */
	public int getInverseValue(int v) {
		if (inverse) {
			return getLevelFromPos0(v, pos0);
		}
		else {
			return v;
		}
	}

	private String probeLabel;

	public void setProbeMultiIdx(int probeMultiIdx) {
		this.probeMultiIdx = probeMultiIdx;
		// probeLabel = "x" + (int) Math.pow(10, probeMultiIdx);
		probeLabel = getProbeLabelForUse(probeMultiIdx);
	}

	public static String getProbeLabel(VoltageProvider vp, int probeMultiIdx) {
		return "x" + vp.getProbeMulties()[probeMultiIdx];
	}

	public String getProbeLabelForUse(int probeMultiIdx) {
		return getProbeLabel(vp, probeMultiIdx);
	}

	public void setNextProbeMultiIdx() {
		int idx = (this.probeMultiIdx + 1) % vp.getProbeMulties().length;
		setProbeMultiIdx(idx);
	}

	public int getProbeMultiIdx() {
		return probeMultiIdx;
	}

	public String getProbeLabel() {
		return probeLabel;
	}

	public Volt getVoltageLabel() {
		return getVoltageLabels()[vbIdx];
	}

	public int getVoltValue() {
		return getVoltageLabel().getValue();
	}

	/**
	 * computes what is logically the frequency counter value
	 * 
	 * @param freq
	 */
	public void updateFreqLabel(float freq) {
		// System.err.println(number+": "+freq);
		this.setFreq(freq);
		if (freq < 0) {
			this.freqtxt = "?";
		} else if (freq >= 0 && freq < 2) {
			this.freqtxt = "<2Hz";
		} else {
			MachineType mt = Platform.getControlManager().getMachine();
			this.freqtxt = UnitConversionUtil.getFrequencyLabel_Hz_withRestrict(
						freq, mt.getMaxFreqWhenChannelsOn());
		}// System.err.println(freqtxt);
	}

	public void c_forceBandLimit(boolean bw) {
		Submitable sbm = SubmitorFactory.reInit();
		sbm.c_chl(P_Channel.BANDLIMIT, number, bw ? 1 : 0);
		sbm.apply();
	}

	public void c_setBandLimit(boolean bl) {
		this.bandlimit = bl;
		c_forceBandLimit(isBandlimit());
	}

	public void setBandlimit(boolean bl) {
		this.bandlimit = bl;
	}

	public boolean isInverse() {
		return inverse;
	}

	/**
	 * Software inversion: when inverted the waveform zero point is pulled to the top, and because of the limit boundary a parallel clipping line appears at the bottom;
	 * 
	 * the trigger level or threshold slope setting also shows the opposite effect on the waveform; this approach is kept, same as SDS, no change needed
	 * 
	 * pulling the trigger, no change needed either
	 * 
	 * @param iv
	 */
	public void c_setInverse(boolean iv) {
		if (inverse == iv)
			return;

		inverse = iv;

		// changed to soft inversion
	}

	public void c_setOn(boolean on) {
		DataHouse dh = Platform.getDataHouse();
		ControlManager cm = dh.controlManager;

		setOnWithoutSync(on);

		Submitable sbm = SubmitorFactory.reInit();
		c_SyncChannel(sbm);

		sbm.apply();
		sbm = SubmitorFactory.reInit();

		cm.getTriggerControl().selfSubmit();
		sbm.apply_trgThen(cm.getResetPersistenceRunnable());

		cm.getCoreControl().updateCurrentSampleRate();
		cm.fire_RefreshMeasureResult();
	}

	public void c_SyncChannel(Submitable sbm) {
		int chl = number;

		sbm.recommendOptimize();
		/**
		 * KNOW try using a singleton object, try using synchronous sending
		 */
		if (on) {
			sbm.c_chl(P_Channel.ONOFF, chl, 1);
			sbm.c_chl(P_Channel.COUPLING, chl, couplingIdx);
			sbm.c_chl(P_Channel.VB, chl, vbIdx);
			sbm.c_chl(P_Channel.POS0, chl, pos0);
			sbm.c_chl(P_Channel.BANDLIMIT, chl, isBandlimit() ? 1 : 0);
		} else {
			sbm.c_chl(P_Channel.ONOFF, chl, 0);
		}
	}

	public void setOnWithoutSync(boolean on) {
		this.on = on;
	}

	public void c_setCoupling(int coupling) {
		this.couplingIdx = coupling;

		Submitable sbm = SubmitorFactory.reInit();
		sbm.c_chl(P_Channel.COUPLING, number, couplingIdx);
		sbm.apply();
	}

	public void c_setNextCoupling() {
		int idx = (couplingIdx + 1) % COUPLING.length;
		c_setCoupling(idx);
	}

	// @Deprecated
	// public void initbandlimit_forCorrectOSerr() {
	// int vbidx = getVoltbaseIndex();
	// if (shouldForcebandlimit(vbidx)) {
	// c_forceBandLimit(true);
	// } else {
	// c_forceBandLimit(isBandlimit());
	// }
	// }

	// @Deprecated
	// public void askReleaseBandlimit() {
	// if (!isBandlimit())
	// return;
	// setBandlimit(false);
	// int vbidx = getVoltbaseIndex();
	// if (shouldForcebandlimit(vbidx)) {
	// c_forceBandLimit(true);
	// } else {
	// c_forceBandLimit(false);
	// }
	// }

	public void c_setVoltage(int idx, final Runnable r) {

		int change = getValidVotageChangeIndex(idx);
		if (change < 0)
			return;

		// if (!validVoltageIndex(idx))
		// return;
		//
		final int vb0 = vbIdx;
		final int vbidx = change;
		// if (vb0 == vbidx)
		// return;

		/** The zero point is limited here, since it may have been set out of bounds at other voltage scales */
		setVoltbaseIndex(change, false, false);

		if (shouldForcebandlimit(vbidx)) {
			c_forceBandLimit(true);
			pvi.notifyChannelUpdate();
		} else if (shouldForcebandlimit(vb0)) {
			c_forceBandLimit(isBandlimit());
			pvi.notifyChannelUpdate();
		}

		Submitable sbm = SubmitorFactory.reInit();
		// sbm.recommendOptimize();

		final int num = number;
		final int pos0 = this.pos0;
		sbm.d_chl_vb(number, vbIdx, new Runnable() {
			@Override
			public void run() {
				// System.err.println("a t then");
				pvi.thredshold_voltsense_ByVoltBase(num, pos0, vb0, vbidx,
						new Runnable() {
							@Override
							public void run() {
								if (r != null)
									r.run();
							}
						}, pos0);
			}
		});
	}

	/**
	 * @param zero
	 * @param sjus
	 */
	public void c_setZero(int zero, final boolean commit) {
		final int p0 = pos0;

		// System.err.println(zero+", "+pos0);
		/** In later versions halfPosRange may change to a quantity related to vbIdx */
		int hpr = pos0HalfRange;
		if (zero < -hpr) {
			zero = -hpr;
		} else if (zero > hpr) {
			zero = hpr;
		}

		// if (zero == getPos0())
		// return;
		int p1 = zero;
		final int dp = p1 - p0;
		this.pos0 = zero;

		// causes the trigger level to change
		final int chlidx = number;
		final Runnable r = pvi.thredshold_voltsense_ByPos0(chlidx, dp, commit);

		if (commit) {
			/**
			 * KNOW
			 * Currently on the ui thread; once syncJob is set, re-entry skips adding the command task, and after the continuous operation ends (e.g. mouseRelease)
			 * 
			 * this state value is reset; the reset happens on another thread, so re-entering handlers may not detect it
			 */
			Submitable sbm = SubmitorFactory.reInit();
			sbm.d_chl_pos0(number, pos0, new Runnable() {
				@Override
				public void run() {
					if (r != null)
						r.run();
					pvi.resetPersistence();
				}
			});
		}

	}

	public static final int getLevelFromPos0(int v, int pos0) {
		return pos0 - (v - pos0);
	}

	public int getLevelOnScreen(int v) {
		if (inverse) {
			return getLevelFromPos0(v, pos0);
		} else {
			return v;
		}
	}

	// protected void snapShot2ChannelDataInfo(OfflineInfo oi) {
	// oi.pos0 = getPos0();
	// oi.vbIdx = getVoltbaseIndex();
	// oi.probeMultiIdx = getProbeMultiIdx();
	//
	// oi.frequency = (float) getFreq();
	// oi.cycle = (float) (1 / getFreq());
	// }

	public void snapShot2ChannelDataInfo(DMDataInfo cd, WaveForm wf) {
		/** The saved zero point is the one from the last run, so that it matches the dm obtained on stop; waveform offsets from later zero point moves are not saved */
		cd.pos0 = wf.getFirstLoadPos0();
		cd.vbIdx = vbIdx;
		cd.probeMultiIdx = probeMultiIdx;

		cd.setFreq(freq);
	}

	public int getHalfPosRange() {
		return pos0HalfRange;
	}

	private int pos0HalfRange;

	public int getVoltbaseIndex() {
		return vbIdx;
	}

	public void setVoltbaseIndex(int vbIdx, boolean restricePos0,
			boolean isfirstload) {
		this.vbIdx = vbIdx;

		/** vbIdx-dependent pos0HalfRange */
		Volt volt = getVoltageLabel();

		// Use the underlying timebase voltage scale value for the check, not counting the probe multiplier
		int currentVolt = volt.getValue(0);
		pos0HalfRange = vp.getPos0HalfRange(currentVolt);

		if (!restricePos0)
			return;
		/** Limit the zero point while changing the voltage scale; may affect the trigger level */
		DataHouse dh = Platform.getDataHouse();
		if (dh == null || isfirstload) {
			// System.out.println("datahouse IS NULL");
			return;
		}
		// else
		// System.out.println("datahouse exist");
		WaveFormManager wfm = dh.getWaveFormManager();
		WaveForm wf = wfm.getWaveForm(number);
		MainWindow mw = dh.getWorkBench().getMainWindow();
		// System.err.println("ci.pos0:" + getPos0());
		wfm.setZeroYLoc(wf, pos0, true);
		mw.getToolPane().getInfoPane().updatePos0(number);
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	public boolean isOn() {
		return on;
	}

	public void setCouplingIdx(int couplingIdx) {
		this.couplingIdx = couplingIdx;
	}

	public int getCouplingIdx() {
		return couplingIdx;
	}

	public void setPos0(int pos0) {
		this.pos0 = pos0;
	}

	public boolean isForcebandlimit() {
		return shouldForcebandlimit(vbIdx);
	}

	private boolean shouldForcebandlimit(int vbIdx) {
		return vbIdx == 0;
	}

	public Color getColor() {
		return color;
	}

	void setName(String name) {
		this.name = name;
	}

	void setNumber(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	double period;// us

	public double getPeriod() {
		return period;
	}

	void setFreq(float freq) {
		this.freq = freq;

		period = 1000000 / freq;
	}

	public float getFreq() {
		return freq;
	}

	void setFreqtxt(String freqtxt) {
		this.freqtxt = freqtxt;
	}

	public String getFreqtxt() {
		return freqtxt;
	}

	public boolean isBandlimit() {
		if (isForcebandlimit())
//			setBandlimit(true);// enforce bandwidth limit at every scale
		 return true;// enabled only at the lowest scale; restores the original state when going back to other scales
		return bandlimit;
	}

}
