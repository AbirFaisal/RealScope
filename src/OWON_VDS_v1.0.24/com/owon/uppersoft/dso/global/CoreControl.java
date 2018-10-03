package com.owon.uppersoft.dso.global;

import java.beans.PropertyChangeSupport;
import java.math.BigDecimal;

import com.owon.uppersoft.dso.delegate.DefaultDeepProvider;
import com.owon.uppersoft.dso.delegate.DefaultFFTDelegate;
import com.owon.uppersoft.dso.delegate.DefaultFullScreenQuery;
import com.owon.uppersoft.dso.delegate.DefaultPVI;
import com.owon.uppersoft.dso.delegate.DefaultTimeConfProvider;
import com.owon.uppersoft.dso.delegate.DefaultTimeControlDelegate;
import com.owon.uppersoft.dso.delegate.DefaultTriggerExtendDelegate;
import com.owon.uppersoft.dso.delegate.DefaultVoltageProvider;
import com.owon.uppersoft.dso.function.measure.TimePerPointProvider;
import com.owon.uppersoft.dso.mode.control.DeepMemoryControl;
import com.owon.uppersoft.dso.mode.control.FFTControl;
import com.owon.uppersoft.dso.mode.control.SampleControl;
import com.owon.uppersoft.dso.mode.control.SysControl;
import com.owon.uppersoft.dso.mode.control.TimeControl;
import com.owon.uppersoft.dso.mode.control.TimeControlDelegate;
import com.owon.uppersoft.dso.model.WaveFormInfoControl;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerExtendDelegate;
import com.owon.uppersoft.dso.model.trigger.helper.TriggerLevelDelegate;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.aspect.control.DeepProvider;
import com.owon.uppersoft.vds.core.aspect.control.FullScreenQuery;
import com.owon.uppersoft.vds.core.aspect.control.ISupportChannelsNumber;
import com.owon.uppersoft.vds.core.aspect.control.MachineInfoProvider;
import com.owon.uppersoft.vds.core.aspect.control.Pos0_VBChangeInfluence;
import com.owon.uppersoft.vds.core.aspect.control.TimeConfProvider;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.core.machine.MachineInfo;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.pref.Config;
import com.owon.uppersoft.vds.core.sample.SampleRate;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;
import com.owon.uppersoft.vds.util.Pref;
import com.owon.uppersoft.vds.util.format.UnitConversionUtil;

public abstract class CoreControl implements TimePerPointProvider,
		MachineInfoProvider, ISupportChannelsNumber {

	private final int channelsNumber;

	private Principle principle;

	public Principle getPrinciple() {
		return principle;
	}

	private PropertyChangeSupport pcs;

	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcs;
	}

	private MachineInfoProvider mip;

	public CoreControl(Config conf, final Principle principle) {
		pcs = new PropertyChangeSupport(this);
		this.principle = principle;

		mip = new MachineInfoProvider() {
			@Override
			public MachineInfo getMachineInfo() {
				return principle.getMachine();
			}
		};

		Pref p = conf.getSessionProperties();

		/** 装入机型类别 */
		principle.prepareTuneFunction(p);

		MachineInfo machine = principle.getMachine();
		channelsNumber = machine.getChannelNumbers();

		vp = new DefaultVoltageProvider(mip);
		tcp = new DefaultTimeConfProvider(mip);
		dp = new DefaultDeepProvider(mip);
		tcd = new DefaultTimeControlDelegate(this, pcs);
		fsq = new DefaultFullScreenQuery(mip);
		fftd = new DefaultFFTDelegate(this);
		pvi = new DefaultPVI(this, pcs);

		initCore(machine);
		productParam = null;
	}

	private DefaultFFTDelegate fftd;
	private VoltageProvider vp;
	private TimeConfProvider tcp;
	private DeepProvider dp;
	private TimeControlDelegate tcd;
	private TriggerExtendDelegate ted;
	private FullScreenQuery fsq;

	public VoltageProvider getVoltageProvider() {
		return vp;
	}

	public TimeConfProvider getTimeConfProvider() {
		return tcp;
	}

	public DeepProvider getDeepProvider() {
		return dp;
	}

	public TriggerLevelDelegate getTriggerLevelDelegate() {
		return tld;
	}

	private TriggerLevelDelegate tld;

	private DefaultPVI pvi;

	public Pos0_VBChangeInfluence getPos0_VBChangeInfluence() {
		return pvi;
	}

	private void initCore(MachineInfo machine) {
		wfic = createWaveFormInfoControl(vp, channelsNumber, pvi);
		ted = new DefaultTriggerExtendDelegate(this, pcs, wfic);

		triggerControl = createTriggerControl(channelsNumber, ted);
		tld = new TriggerLevelDelegate(triggerControl);

		sysControl = new SysControl();
		sampleControl = new SampleControl();
		deepMemoryControl = new DeepMemoryControl(dp);

		fftControl = new FFTControl(fftd, vp);
		timeControl = new TimeControl(machine, tcp, tcd, dp, fsq);
	}

	protected TriggerControl createTriggerControl(int channelsNumber,
			TriggerExtendDelegate ted) {
		return new TriggerControl(channelsNumber, ted);
	}

	protected abstract WaveFormInfoControl createWaveFormInfoControl(
			VoltageProvider vp, int channelsNumber, Pos0_VBChangeInfluence pvi);

	public int getAllChannelsNumber() {
		return wfic.getLowMachineChannels();
	}

	public int getSupportChannelsNumber() {
		return wfic.getLowMachineChannels();
	}

	public MachineInfo getMachineInfo() {
		return principle.getMachine();
	}

	public MachineType getMachine() {
		return principle.getMachineType();
	}

	public int getMachineTypeForSave() {
		return getMachine().saveID();
	}

	private String productParam = null;

	public void setProductParam(String pp) {
		productParam = pp;
	}

	public void persist(Pref p) {
		if (productParam == null)
			productParam = getMachine().name();
		p.setProperty("productParam", productParam + "ONE");

		principle.persist(p);
		triggerControl.persist(p);
		wfic.persist(p);
		sampleControl.persist(p);
		timeControl.persist(p);
		deepMemoryControl.persist(p);
		sysControl.persist(p);
	}

	public void factoryset(Pref p) {
		triggerControl.load(p);
		wfic.load(p);
		sampleControl.load(p);
		deepMemoryControl.load(p);
		timeControl.load(p);
		fftControl.load(p, channelsNumber);
		sysControl.load(p, false);

		currentSampleRate = computeSampleRate();
	}

	public void load(Pref p) {
		triggerControl.load(p);
		wfic.load(p);
		sampleControl.load(p);
		deepMemoryControl.load(p);
		timeControl.load(p);
		fftControl.load(p, channelsNumber);
		sysControl.load(p, true);

		currentSampleRate = computeSampleRate();
	}

	/**
	 * 初始同步，在任何模式下都发完整信息进行匹配
	 * 
	 * @param bbuf
	 */
	public void initDetail(Submitable sbm) {
		triggerControl.selfSubmit();
		wfic.selfSubmit(sbm);
		sampleControl.selfSubmit(sbm);
		deepMemoryControl.selfSubmit(sbm);
		sysControl.selfSubmit(sbm);
		fftControl.selfSubmit(sbm);
		timeControl.selfSubmit(sbm);
	}

	/**
	 * 在运行时同步，可在切入fft时只发必要信息
	 * 
	 * @param bbuf
	 */
	public void syncDetail(Submitable sbm) {
		if (fftControl.isFFTon()) {
			wfic.selfSubmit(sbm);
			fftControl.selfSubmit(sbm);
			timeControl.selfSubmit(sbm);
		} else {
			initDetail(sbm);
		}
	}

	/**
	 * 当无通道打开时返回-1
	 * 
	 * @return
	 */
	public int getChannelSampleConfig() {
		return getMachineInfo()
				.getChannelSampleType(wfic.getOnWaveFormNumber());
	}

	/**
	 * 数据点：min,max,min,max 可在改变采样率时判断，减少测试次数，但测试内容比较简单，这里就不优化了
	 * 
	 * 但是界面下方的采样率显示区不需要针对打开峰值检测而更改为实际的采样率
	 * 
	 * @return 峰值检测实际是否开启
	 */
	public boolean isPeakDetectWork() {
		if (fftControl.isFFTon())
			return false;

		boolean pdon = sampleControl.isPeakDetect(); // 开启
		boolean b = pdon && checkPeakDetectWork();

		/** TODO fire可再优化 */
		pcs.firePropertyChange(PropertiesItem.UPDATE_PK, null, b);
		return b;
	}

	protected abstract boolean checkPeakDetectWork();

	public boolean isSamplePromtion() {
		int cnf = getChannelSampleConfig();
		if (cnf < 0) {
			return false;
		}
		SampleRate cur = getSampleRateWithChannelNumFlag(cnf);
		SampleRate th = getChannelsSampleRate(cnf);
		// System.err.println(cur + ", " + th);
		/**
		 * 峰值检测的判断是：在未开启峰值检测的情况下，如果屏幕采到的点数是屏幕像素的1倍以上就不用提升采样率来做；
		 * 
		 * 如果达不到，就看看采样率能否提升，能够提升也可以做
		 */

		boolean samplePromtion = (!cur.equals(th));// 采样率可以提升，其实需要碰到是否可以提升到拿4:1的数据
		return samplePromtion;
	}

	public boolean isFullPointsLargerThenScreen() {
		int fullscreenlen = timeControl.getRTfullScreenNumber();
		int screenpixs = GDefine.AREA_WIDTH;

		boolean larger = (fullscreenlen > screenpixs); // 比屏幕数大
		return larger;
	}

	/**
	 * @return 实时采样率
	 */
	private SampleRate getSampleRateWithChannelNumFlag(int cnf) {
		if (cnf < 0) {
			return null;
		}
		SampleRate sr = getMachineInfo().getSampleRate(cnf,
				deepMemoryControl.getDeepIdx())[timeControl.getTimebaseIdx()];
		return sr;
	}

	private SampleRate currentSampleRate;

	public void updateCurrentSampleRate() {
		if (fftControl.isFFTon()) {
			// mathControl.getFFTSampleRateBD();
		} else {
			SampleRate last = currentSampleRate;
			SampleRate current_sr = computeSampleRate();
			if (current_sr == null) {
				return;
			}

			if (!current_sr.equals(last)) {
				currentSampleRate = current_sr;
				pcs.firePropertyChange(PropertiesItem.SampleRateChange, last,
						current_sr);
			}
		}

	}

	public BigDecimal getCurrentSampleRateBD_kHz() {
		if (fftControl.isFFTon()) {
			BigDecimal bd = fftControl.getFFTSampleRateBD();
			if (bd == null)
				return null;
			else
				return bd.divide(BigDecimal.valueOf(1000));
		} else {
			if (currentSampleRate == null)
				return null;
			else
				return currentSampleRate.getBDValue_kHz();
		}
	}

	public String getCurrentSampleRate_Text() {
		if (fftControl.isFFTon()) {
			BigDecimal bd = fftControl.getFFTSampleRateBD();
			if (bd == null)
				return "";
			else
				return UnitConversionUtil.getSimplifiedFrequencyLabel_Hz(bd
						.doubleValue());
		} else {
			if (currentSampleRate == null)
				return "";
			else
				return currentSampleRate.getSampleRateTxt();
		}
	}

	public boolean isTrgLevelDisable() {
		boolean disable = !triggerControl.isTrgEnable()
				|| triggerControl.isOnExtTrgMode()
				|| fftControl.isFFTon()
				|| timeControl.isOnSlowMoveTimebase();
		return disable;
	}

	public boolean mayDelayInvalid() {
		return !triggerControl.isSingleTrg()
				|| triggerControl.getSingleTriggerSet()
						.isCurrentTrigger_Video();
	}

	public boolean shouldSkipBeforeFrames() {
		return triggerControl.isSweepNormal()
				|| triggerControl.isSweepOnce()
				|| fftControl.isFFTon() || isPeakDetectWork()
				|| sampleControl.avgon
				|| timeControl.isOnSlowMoveTimebase();
	}

	/**
	 * 通道都已关闭，此时如何处理需要采样率的计算？
	 * 
	 * @return 实时采样率
	 */
	private SampleRate computeSampleRate() {
		return getSampleRateWithChannelNumFlag(getChannelSampleConfig());
	}

	/**
	 * @return 当前通道个数下的最高采样率
	 */
	private SampleRate getChannelsSampleRate(int cnf) {
		MachineInfo machine = getMachineInfo();
		/** 找对应通道的最高采样率，这里是算法是使用最小时基档位的采样率，存储深度为最大的情况下 */
		SampleRate sr = machine
				.getSampleRate(cnf, machine.DEEPValue.length - 1)[0];
		return sr;
	}

	protected TriggerControl triggerControl;
	protected WaveFormInfoControl wfic;
	protected TimeControl timeControl;
	protected SampleControl sampleControl;
	protected DeepMemoryControl deepMemoryControl;
	protected SysControl sysControl;
	protected FFTControl fftControl;

	public TimeControl getTimeControl() {
		return timeControl;
	}

	public SampleControl getSampleControl() {
		return sampleControl;
	}

	public DeepMemoryControl getDeepMemoryControl() {
		return deepMemoryControl;
	}

	public SysControl getSysControl() {
		return sysControl;
	}

	public FFTControl getFFTControl() {
		return fftControl;
	}

	public TriggerControl getTriggerControl() {
		return triggerControl;
	}

	public WaveFormInfoControl getWaveFormInfoControl() {
		return wfic;
	}

	public WaveFormInfo[] getWaveFormInfos() {
		return wfic.getWaveFormInfos();
	}

	public WaveFormInfo getWaveFormInfo(int idx) {
		return wfic.getWaveFormInfo(idx);
	}

	/**
	 * 计算满屏数
	 * 
	 * 提供在改变通道开关、时基、存储深度时限制水平触发位置
	 * 
	 * @param controlManager
	 *            TODO
	 */
	public void updateHorTrgPosRange() {
		int dmidx = deepMemoryControl.getDeepIdx();
		int cnf = getChannelSampleConfig();
		timeControl.computeHorTrgRange(dmidx, cnf);
	}

	public void updateHorTrgIdx4View() {
		int dmidx = deepMemoryControl.getDeepIdx();
		int htp = timeControl.getHorizontalTriggerPosition();
		timeControl.computeHorTrgIdx(dmidx, htp);
	}

	/**
	 * @param init
	 *            TODO
	 * @param screendatalen
	 *            TODO
	 */
	public void updateDMHorTrgIdx4View(int init, int screendatalen) {
		timeControl.computeDMHorTrgIdx(init, screendatalen);
	}

	public boolean isRunMode_slowMove() {
		return (timeControl.isOnSlowMoveTimebase() && !fftControl.isFFTon());
	}

	public abstract double getTimePerPoint(int points);

	/**
	 * @return FFT下采集传输过来的点
	 */
	public int fftAvailablePoints() {
		return getMachineInfo().fftAvailablePoints(
				fftControl.getFFTTimebaseIndex());
	}

	public void ontFFTSwitch(boolean ffton, int fftchl) {
		String n;
		if (ffton) {
			wfic.storeChannelsDisplaybeforeFFT();
			wfic.preSetChannelsForFFT(fftchl);
			sampleControl.c_setModelIdx(0);
			// 改变界面状态；发fft指令，接收数据
			n = PropertiesItem.FFT_ON;
			// System.err.println("syncFFTStatus "+ ffton);
		} else {
			wfic.recoverChannelsDisplayfromNonFFT();// wfic.turnOnAllChannelsWitouSync();//
			// 改变界面状态；发匹配指令
			n = PropertiesItem.FFT_OFF;
		}
		timeControl.updateTimebase();

		Platform.getControlApps().interComm.syncDetail(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		pcs.firePropertyChange(n, null, null);
	}

}