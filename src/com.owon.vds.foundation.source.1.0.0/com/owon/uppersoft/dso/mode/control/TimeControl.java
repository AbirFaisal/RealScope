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

	/** KNOW 用于画界面位置，存在保存文件中，主要以此 */
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
	 * 运行时真实的采集到的屏幕范围内的点数
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
	 * 限位
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
			setHorTrgPos(thtp);
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

		setTimebaseIndexValue(tbidx);
		tcd.notifyInitSlowMove(tcp.isOnSlowMoveTimebase(tbidx));

		updatePixel_mS_Sample_mS();

		int horTrgPos = p.loadInt(HOR_TRG_POS);

		setHorTrgPos(horTrgPos);
	}

	public void persist(Pref p) {
		/** 注意：这里写入的信息在视窗扩展开始时，会在后面被视窗扩展模块覆盖 */
		p.persistInt(TIMEBASE_INDEX, getTimebaseIdx());
		int v = getHorizontalTriggerPosition();
		p.persistInt(HOR_TRG_POS, v);
	}

	public void internalTimebaseIndex(int newidx) {
		internalTimebaseIndex(newidx, true);
	}

	public void internalTimebaseIndex(int newidx, boolean effect) {
		int oldidx = getTimebaseIdx();
		setTimebaseIndexValue(newidx);

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
		/** 缓存一些相关的数值，方便重复使用 */
		double dbv_mS = getBDTimebase().doubleValue() * GDefine.S2mS;// 转化为mS
		pixelTime_mS = dbv_mS / GDefine.AREA_WIDTH_BLOCK_PIXES;
	}

	public int getTimebaseIdx() {
		return timebaseIdx;
	}

	private double pixelTime_mS;

	/**
	 * @return 该时基档位每个像素对应的时间，单位为mS
	 */
	public double getPixelTime_mS() {
		return pixelTime_mS;
	}

	public boolean isOnSlowMoveTimebase() {
		return tcp.isOnSlowMoveTimebase(getTimebaseIdx());
	}

	public String getTimebaseLabel() {
		return tcp.getTimebaseLabel(getTimebaseIdx());
	}

	public BigDecimal getBDTimebase() {
		return tcp.getBDTimebase(getTimebaseIdx());
	}

	public int getValidTimebaseIndex(int idx) {
		int change = idx;
		if (idx < 0)
			change = 0;
		if (idx >= tcp.getTimebaseNumber())
			change = tcp.getTimebaseNumber() - 1;
		return (change == getTimebaseIdx() ? -1 : change);
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
		int tmp = getTimebaseIdx();
		internalTimebaseIndex(tbidx, effect);

		/** 这里还需要根据时基计算，以及从RT->DM, DM->RT时的转换，如何在时基切换之后重新锁定触发位置 */
		tcd.notifyUpdateHorTrgPosRange();

		/** 总是发指令改变当前 */
		double ohtp = getDBHorizontalTriggerPosition();
		double v = BigDecimal.valueOf(ohtp)
				.divide(tcp.ratio(getTimebaseIdx(), tmp)).doubleValue();
		setHorTrgPos(v);
		restrictHorTrgPos();
		dosubmit();

		tcd.onHorTrgPosChangedByTimebase(tbidx, getHorizontalTriggerPosition());
	}

	public void offline_setTimeBaseIdx(int tbidx) {
		// if (getTimebaseIdx() == tbidx)
		// return;

		internalTimebaseIndex(tbidx);
		/** 这里还需要根据时基计算，以及从RT->DM, DM->RT时的转换，如何在时基切换之后重新锁定触发位置 */
		tcd.notifyUpdateHorTrgPosRange();
		restrictHorTrgPos();
	}

	public void c_setTimebase_HorTrgPos(int tbidx, int htp) {
		if (getTimebaseIdx() != tbidx)
			internalTimebaseIndex(tbidx);
		tcd.notifyUpdateHorTrgPosRange();

		/** 变化范围随存储深度和时基(采样率)不同 */
		htp = restrictHorTrgPos(htp);
		setHorTrgPos(htp);
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
	 * 计算水平触发示数，依赖选择波形的信息进行计算，需在导入波形后进行
	 * 
	 * @return
	 */
	private String computeHorizontalTriggerLabel() {
		int htp;
		htp = getHorizontalTriggerPosition();

		return getHTPLabel(getTimebaseIdx(), htp);// 像素差转化为时间差
	}

	public String getHTPLabel(int tbidx, int htpPixsFromCenter) {
		BigDecimal bdtb = tcp.getBDTimebase(tbidx);
		double hv = bdtb.divide(valueOf(GDefine.AREA_WIDTH_BLOCK_PIXES))
				.multiply(valueOf(htpPixsFromCenter))
				.multiply(valueOf(GDefine.S2mS)).doubleValue();
		return UnitConversionUtil.getSimplifiedTimebaseLabel_mS(hv);
	}

	// ////////////////////////////////////////////////考虑去掉horTrgIdx
	/** 以存储深度起点为标记，往前为负，往后为正，用于深存储画图 */
	private int horTrgIdx;

	private void setHorTrgIdx(int v) {
		horTrgIdx = v;
	}

	public int getHorTrgIdx() {
		return horTrgIdx;
	}

	/**
	 * 在载入深存储后才有用途
	 * 
	 * 求触发前数据的方法
	 * 
	 * 先求总像素
	 * 
	 * 总像素 = 存储深度 / 压缩率
	 * 
	 * 左边的像素个数 = 总像素 / 2
	 * 
	 * 右边的像素个数 = - 左边的像素个数
	 * 
	 * 触发前数据个数 = （左边像素个数 - 触发位置）*压缩率
	 * 
	 * 
	 * 
	 * 
	 * 不使用传入的起始位置，按居中计算
	 * 
	 */
	public void computeHorTrgIdx(int dmidx, int htp) {
		double rate = (double) rtfls / GDefine.AREA_WIDTH;
		double allpixs = dp.getLength(dmidx) / rate;

		double num = (allpixs / 2 - htp) * rate;

		int hti = (int) Math.round(num);

		setHorTrgIdx(hti);
	}

	public void computeDMHorTrgIdx(int init, int screendatalen) {
		int htp = getHorizontalTriggerPosition();
		double rate = (double) screendatalen / GDefine.AREA_WIDTH;
		int hti = init + (int) (((GDefine.AREA_WIDTH >> 1) - htp) * rate);

		setHorTrgIdx(hti);
	}

	/**
	 * 看计算公式，似乎和采样率无关，只是人为限定的一个范围
	 * 
	 * 确定水平触发位置范围
	 * 
	 * 取决于: 通道开关、时基、存储深度
	 * 
	 * 水平位置限位，相应限制的像素个数为
	 * 
	 * 左边 = 50M数据/压缩率
	 * 
	 * 右边 = 存储深度/2/压缩率
	 */
	public void computeHorTrgRange(int dmidx, int cnf) {
		Range p = new Range();

		int rtfls = computeRTFullScreen(dmidx, cnf);
		if (rtfls == 0) {
			p.left = Integer.MAX_VALUE;
			p.right = Integer.MIN_VALUE;
		} else {
			double rate = rtfls / (double) 1000;

			// TODO压缩率小于1时需了解算法细节
			p.left = (int) (50000000 / rate);
			p.right = -(int) ((dp.getLength(dmidx) >> 1) / rate);
			// System.out.println(p);
		}

		setHorTrgRange(p);
	}

	/**
	 * 重新计算设置满屏数
	 * 
	 * 取决于: 通道开关、时基、存储深度
	 * 
	 * @return
	 */
	private int computeRTFullScreen(int dmidx, int cnf) {
		int i;
		if (cnf < 0) {
			i = 0;
		} else {
			i = fsq.getFullScreen(cnf, dmidx, getTimebaseIdx());
		}
		setRTfullScreenNumber(i);
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

		/** 变化范围随存储深度和时基(采样率)不同 */
		nhtp = restrictHorTrgPos(nhtp);
		del = nhtp - horTrgPos;
		setHorTrgPos(nhtp);
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
		sbm.c_tb_htp(getTimebaseIdx(), getHorizontalTriggerPosition());
	}

	public void applyTB_DM(int dmidx, int cnf) {
		hortrglbl = computeHorizontalTriggerLabel();
		computeHorTrgRange(dmidx, cnf);
	}

	public void dosubmit() {
		Submitable sbm = SubmitorFactory.reInit();
		sbm.recommendOptimize();
		sbm.c_tb_htp(getTimebaseIdx(), getHorizontalTriggerPosition());
		sbm.applyThen(tcd.getResetPersistenceRunnable());
	}

	public void setShouldEnableTrgByJudgeIsSlowMove() {
		tcd.notifyInitSlowMove(tcp.isOnSlowMoveTimebase(getTimebaseIdx()));
	}
}
