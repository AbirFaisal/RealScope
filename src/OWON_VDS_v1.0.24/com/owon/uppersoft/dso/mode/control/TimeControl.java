package com.owon.uppersoft.dso.mode.control;

import static java.math.BigDecimal.valueOf;

import java.math.BigDecimal;

import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.aspect.base.IOrgan;
import com.owon.uppersoft.vds.core.aspect.control.DeepProvider;
import com.owon.uppersoft.vds.core.aspect.control.FullScreenQuery;
import com.owon.uppersoft.vds.core.aspect.control.ITimeControl;
import com.owon.uppersoft.vds.core.aspect.control.TimeConfProvider;
import com.owon.uppersoft.vds.core.comm.effect.IPatchable;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.core.machine.MachineInfo;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.data.Range;
import com.owon.uppersoft.vds.util.Pref;
import com.owon.uppersoft.vds.util.format.UnitConversionUtil;

public class TimeControl implements IOrgan, IPatchable, ITimeControl {

	private int timebaseIdx = -1;

	/** KNOW used to draw UI positions, stored in the saved file, mainly based on this */
	private double horTrgPos = 0;// 10uS

	public void setHorTrgPos(double v) {
		horTrgPos = v;
	}

	public void resetPersistence() {
		tcd.nofiyResetPersistence();
	}

	public double getDBHorizontalTriggerPosition() {
		return horTrgPos;
	}

	public int getHorizontalTriggerPosition() {
		return (int) horTrgPos;
	}

	/**
	 * Number of points actually acquired within the screen range while running
	 */
	private int rtfls = 0;

	public int getRTfullScreenNumber() {
		return rtfls;
	}

	public void setRTfullScreenNumber(int rtfls) {
		this.rtfls = rtfls;
	}

	private Range horTrgRange = new Range(20000000, -20000000);

	public Range getHorTrgRange() {
		return horTrgRange;
	}

	/**
	 * position limit
	 * 
	 * @param p
	 */
	private void setHorTrgRange(Range p) {
		horTrgRange.left = p.left;
		horTrgRange.right = p.right;
	}

	public int restrictHorTrgPos() {
		int horTrgPos = getHorizontalTriggerPosition();
		int thtp = restrictHorTrgPos(horTrgPos);
		int del = thtp - horTrgPos;
		if (del != 0) {
			this.horTrgPos = (double) thtp;
			resetPersistence();
			dosubmit();
		}
		return thtp;
	}

	private int restrictHorTrgPos(int htp) {
		// System.out.println("in: " + htp);
		if (htp > horTrgRange.left) {
			htp = horTrgRange.left;
		} else if (htp < horTrgRange.right) {
			htp = horTrgRange.right;
		}
		// System.out.println("out: " + htp);
		return htp;
	}

	public TimeConfProvider getTimeConfProvider() {
		return tcp;
	}

	private DeepProvider dp;
	private TimeConfProvider tcp;
	private TimeControlDelegate tcd;
	private FullScreenQuery fsq;

	public TimeControl(MachineInfo machine, TimeConfProvider tcp,
			TimeControlDelegate tcd, DeepProvider dp, FullScreenQuery fsq) {
		this.dp = dp;
		this.tcp = tcp;
		this.tcd = tcd;
		this.fsq = fsq;
	}

	public void load(Pref p) {
		int tbidx = p.loadInt(TIMEBASE_INDEX);
		if (tbidx < 0 || tbidx >= tcp.getTimebaseNumber()) {
			tbidx = 0;
		}

		timebaseIdx = tbidx;
		tcd.notifyInitSlowMove(tcp.isOnSlowMoveTimebase(tbidx));

		updatePixel_mS_Sample_mS();

		int horTrgPos = p.loadInt(HOR_TRG_POS);

		this.horTrgPos = (double) horTrgPos;
	}

	public void persist(Pref p) {
		/** Note: the information written here will later be overwritten by the viewport extension module when the viewport extension starts */
		p.persistInt(TIMEBASE_INDEX, timebaseIdx);
		int v = getHorizontalTriggerPosition();
		p.persistInt(HOR_TRG_POS, v);
	}

	public void internalTimebaseIndex(int newidx) {
		internalTimebaseIndex(newidx, true);
	}

	public void internalTimebaseIndex(int newidx, boolean effect) {
		int oldidx = timebaseIdx;
		timebaseIdx = newidx;

		updateTimebase();
		tcd.onTimebaseChange(oldidx, newidx, effect);
	}

	public void setTimebaseIndexValue(int idx) {
		timebaseIdx = idx;
	}

	public void updateTimebase() {
		updatePixel_mS_Sample_mS();

		tcd.notifyUpdateCurrentSampleRate();
	}

	private void updatePixel_mS_Sample_mS() {
		/** Cache some related values for easy reuse */
		double dbv_mS = getBDTimebase().doubleValue() * GDefine.S2mS;// convert to mS
		pixelTime_mS = dbv_mS / GDefine.AREA_WIDTH_BLOCK_PIXES;
	}

	public int getTimebaseIdx() {
		return timebaseIdx;
	}

	private double pixelTime_mS;

	/**
	 * @return time corresponding to each pixel at this timebase scale, in mS
	 */
	public double getPixelTime_mS() {
		return pixelTime_mS;
	}

	public boolean isOnSlowMoveTimebase() {
		return tcp.isOnSlowMoveTimebase(timebaseIdx);
	}

	public String getTimebaseLabel() {
		return tcp.getTimebaseLabel(timebaseIdx);
	}

	public BigDecimal getBDTimebase() {
		return tcp.getBDTimebase(timebaseIdx);
	}

	public int getValidTimebaseIndex(int idx) {
		int change = idx;
		if (idx < 0)
			change = 0;
		if (idx >= tcp.getTimebaseNumber())
			change = tcp.getTimebaseNumber() - 1;
		return (change == timebaseIdx ? -1 : change);
	}

	public void c_setTimebaseIdx(int tbidx, boolean effect) {
		// System.out.println("c_setTimebaseIdx "+tbidx);
		// boolean b = tbidx >= 0 && tbidx < tcp.getTimebaseNumber();
		// if (!b)
		// return;
		//
		// if (getTimebaseIdx() == tbidx)
		// return;

		int change = getValidTimebaseIndex(tbidx);
		if (change < 0)
			return;

		tbidx = change;
		// System.out.println("c_setTimebaseIdx");
		int tmp = timebaseIdx;
		internalTimebaseIndex(tbidx, effect);

		/** This also needs to be calculated from the timebase, along with the RT->DM, DM->RT conversion, and how to re-lock the trigger position after the timebase switch */
		tcd.notifyUpdateHorTrgPosRange();

		/** Always send a command to change the current */
		double ohtp = horTrgPos;
		double v = BigDecimal.valueOf(ohtp)
				.divide(tcp.ratio(timebaseIdx, tmp)).doubleValue();
		horTrgPos = v;
		restrictHorTrgPos();
		dosubmit();

		tcd.onHorTrgPosChangedByTimebase(tbidx, getHorizontalTriggerPosition());
	}

	public void offline_setTimeBaseIdx(int tbidx) {
		// if (getTimebaseIdx() == tbidx)
		// return;

		internalTimebaseIndex(tbidx);
		/** This also needs to be calculated from the timebase, along with the RT->DM, DM->RT conversion, and how to re-lock the trigger position after the timebase switch */
		tcd.notifyUpdateHorTrgPosRange();
		restrictHorTrgPos();
	}

	public void c_setTimebase_HorTrgPos(int tbidx, int htp) {
		if (timebaseIdx != tbidx)
			internalTimebaseIndex(tbidx);
		tcd.notifyUpdateHorTrgPosRange();

		/** The variation range differs with memory depth and timebase (sampling rate) */
		htp = restrictHorTrgPos(htp);
		horTrgPos = (double) htp;
		resetPersistence();
		dosubmit();
		Platform.getMainWindow().update_HorTrg();
	}

	/**
	 * @param dh
	 * @return
	 */
	public String getHorizontalTriggerLabel() {
		hortrglbl = computeHorizontalTriggerLabel();
		return hortrglbl;
	}

	public void recomputeHorizontalTriggerLabel() {
		hortrglbl = computeHorizontalTriggerLabel();
	}

	/**
	 * Calculate the horizontal trigger reading, which depends on the selected waveform's information and must be done after the waveform is imported
	 * 
	 * @return
	 */
	private String computeHorizontalTriggerLabel() {
		int htp;
		htp = getHorizontalTriggerPosition();

		return getHTPLabel(timebaseIdx, htp);// convert the pixel difference to a time difference
	}

	public String getHTPLabel(int tbidx, int htpPixsFromCenter) {
		BigDecimal bdtb = tcp.getBDTimebase(tbidx);
		double hv = bdtb.divide(valueOf(GDefine.AREA_WIDTH_BLOCK_PIXES))
				.multiply(valueOf(htpPixsFromCenter))
				.multiply(valueOf(GDefine.S2mS)).doubleValue();
		return UnitConversionUtil.getSimplifiedTimebaseLabel_mS(hv);
	}

	// ////////////////////////////////////////////////consider removing horTrgIdx
	/** Marked from the memory depth start point, negative toward the front, positive toward the back, used for deep memory drawing */
	private int horTrgIdx;

	private void setHorTrgIdx(int v) {
		horTrgIdx = v;
	}

	public int getHorTrgIdx() {
		return horTrgIdx;
	}

	/**
	 * Only useful after deep memory is loaded
	 * 
	 * Method for computing the data before the trigger
	 * 
	 * First compute the total pixels
	 * 
	 * Total pixels = memory depth / compression ratio
	 * 
	 * Pixel count on the left = total pixels / 2
	 * 
	 * Pixel count on the right = - pixel count on the left
	 * 
	 * Data count before trigger = (left pixel count - trigger position) * compression ratio
	 * 
	 * 
	 * 
	 * 
	 * Does not use the passed-in start position, calculates as centered
	 * 
	 */
	public void computeHorTrgIdx(int dmidx, int htp) {
		double rate = (double) rtfls / GDefine.AREA_WIDTH;
		double allpixs = dp.getLength(dmidx) / rate;

		double num = (allpixs / 2 - htp) * rate;

		int hti = (int) Math.round(num);

		horTrgIdx = hti;
	}

	public void computeDMHorTrgIdx(int init, int screendatalen) {
		int htp = getHorizontalTriggerPosition();
		double rate = (double) screendatalen / GDefine.AREA_WIDTH;
		int hti = init + (int) (((GDefine.AREA_WIDTH >> 1) - htp) * rate);

		horTrgIdx = hti;
	}

	/**
	 * Judging from the formula, it seems unrelated to the sampling rate and is just an artificially limited range
	 * 
	 * Determine the horizontal trigger position range
	 * 
	 * Depends on: channel switches, timebase, memory depth
	 * 
	 * Horizontal position limit; the corresponding limited pixel counts are
	 * 
	 * Left = 50M data / compression ratio
	 * 
	 * Right = memory depth / 2 / compression ratio
	 */
	public void computeHorTrgRange(int dmidx, int cnf) {
		Range p = new Range();

		int rtfls = computeRTFullScreen(dmidx, cnf);
		if (rtfls == 0) {
			p.left = Integer.MAX_VALUE;
			p.right = Integer.MIN_VALUE;
		} else {
			double rate = rtfls / (double) 1000;

			// TODO the algorithm details need to be understood when the compression ratio is below 1
			p.left = (int) (50000000 / rate);
			p.right = -(int) ((dp.getLength(dmidx) >> 1) / rate);
			// System.out.println(p);
		}

		setHorTrgRange(p);
	}

	/**
	 * Recalculate and set the full-screen count
	 * 
	 * Depends on: channel switches, timebase, memory depth
	 * 
	 * @return
	 */
	private int computeRTFullScreen(int dmidx, int cnf) {
		int i;
		if (cnf < 0) {
			i = 0;
		} else {
			i = fsq.getFullScreen(cnf, dmidx, timebaseIdx);
		}
		this.rtfls = i;
		return i;
	}

	private String hortrglbl;

	public void c_setHorizontalTriggerPosition(int nhtp) {
		c_setHorizontalTriggerPosition(nhtp, true);
	}

	public void c_addHorizontalTriggerPosition(int del) {
		c_addHorizontalTriggerPosition(del, true);
	}

	/**
	 * +...-
	 * 
	 * @param horTrgPos
	 */
	public void c_addHorizontalTriggerPosition(int del, boolean commit) {
		int v = getHorizontalTriggerPosition();
		c_setHorizontalTriggerPosition(v + del, commit);
	}

	/**
	 * +...-
	 * 
	 * @param horTrgPos
	 */
	private void c_setHorizontalTriggerPosition(int nhtp, boolean commit) {
		DataHouse dh = Platform.getDataHouse();
		MainWindow mw = dh.getMainWindow();
		ScreenContext pc = mw.getChartScreen().getPaintContext();
		WaveFormManager wfm = dh.getWaveFormManager();
		int horTrgPos = getHorizontalTriggerPosition();
		int del = nhtp - horTrgPos;

		/** The variation range differs with memory depth and timebase (sampling rate) */
		nhtp = restrictHorTrgPos(nhtp);
		del = nhtp - horTrgPos;
		this.horTrgPos = (double) nhtp;
		resetPersistence();

		if (commit) {
			Submitable sbm = SubmitorFactory.reInit();
			sbm.recommendOptimize();
			sbm.c_htp(getHorizontalTriggerPosition());
			sbm.apply();
		}

		hortrglbl = computeHorizontalTriggerLabel();

		if (dh.isDMLoad()) {
			wfm.addWaveFormsDMXloc(del, pc);
		} else {
			wfm.addWaveFormsRTXloc(del, pc);
		}
		mw.update_HorTrg();
	}

	public void selfSubmit(Submitable sbm) {
		sbm.c_tb_htp(timebaseIdx, getHorizontalTriggerPosition());
	}

	public void applyTB_DM(int dmidx, int cnf) {
		hortrglbl = computeHorizontalTriggerLabel();
		computeHorTrgRange(dmidx, cnf);
	}

	public void dosubmit() {
		Submitable sbm = SubmitorFactory.reInit();
		sbm.recommendOptimize();
		sbm.c_tb_htp(timebaseIdx, getHorizontalTriggerPosition());
		sbm.applyThen(tcd.getResetPersistenceRunnable());
	}

	public void setShouldEnableTrgByJudgeIsSlowMove() {
		tcd.notifyInitSlowMove(tcp.isOnSlowMoveTimebase(timebaseIdx));
	}
}
