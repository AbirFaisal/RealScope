package com.owon.uppersoft.vds.auto;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.mode.control.SampleControl;
import com.owon.uppersoft.dso.mode.control.SysControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerDefine;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.wf.ON_WF_Iterator;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.auto.jobs.ArrangeChannelsJob;
import com.owon.uppersoft.vds.auto.jobs.JobRunner;
import com.owon.uppersoft.vds.auto.jobs.RecognizeJob;
import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.device.interpret.CMDResponser;
import com.owon.uppersoft.vds.device.interpret.DeviceAddressTable;
import com.owon.uppersoft.vds.source.comm.InterCommTiny;
import com.owon.uppersoft.vds.source.comm.Submitor2;

public class AutosetRunner2 implements Logable, JobRunner {
   
	private static final int TIMEBASE_10MS = 19;
	public static final int GRID_RANGE = 25;
	public static final int ADC_RANGE = 250;

	public static final int ADC_SCREEN_RANGE = (int) (ADC_RANGE * 1);
	public static final int ADC_LOCKTRG_RANGE = (int) (ADC_RANGE * 0.4);

	private WaveFormManager wfm;
	private ControlManager cm;
	private VoltageProvider vp;

	private Runnable donejob;
	private CoreControl cc;
	private InterCommTiny ict;

	public AutosetRunner2(Runnable donejob, InterCommTiny ict) {
		this.donejob = donejob;
		this.ict = ict;
		dh = Platform.getDataHouse();
		cm = dh.controlManager;
		cc = dh.controlManager.getCoreControl();
		wfm = dh.getWaveFormManager();
		vp = cc.getVoltageProvider();
		tc = cc.getTriggerControl();
		mw = Platform.getMainWindow();
	}

	@Override
	public void log(Object o) {
		System.out.print(o);
	}

	@Override
	public void logln(Object o) {
		System.out.println(o);
	}

	// private ChannelSets[] css;

	/**
	 * <code>进入自动设置后，其它的菜单关闭不可用真至自动设置完成
	若开pass  fail功能，则关闭
	设置为非停止状态
	开启两通道;
	关闭FFT;
	通道耦合方式切换至DC，反相关闭，带宽限制关闭;
	水平位置0(即水平居中);
	零点位置,CH1放0格位置,CH2放0格位置;
	水平时基初值(10ms)
	触发类型设置为交替. 触发位置设置0,模式＆释抑设置为自动(释抑100ns)
	(触发方式，如边沿视频或是单触交替之类可以不设置）
	采样方式为峰值检测(预先存好当前方式);
	电压档位初值(200mv);
	</code>
	 */
	protected void beforeAutoset() {
		dh.getWaveFormManager().hideDraw();
		// dataHouse.getWaveFormManager().forceFreshFreq();
		// if (true)
		// return;
		// css = cc.getWaveFormInfoControl().getAllChannelSets();

		// for (int i = 0; i < css.length; i++) {
		// ChannelSets cs = css[i];
		// if (cs.coupling == ChannelInfo.CouplingGroundIndex)// !cs.on ||
		// cs.coupling = ChannelInfo.CouplingACIndex;
		// }

		dh.closePersistence();

		cm.displayControl.setXYMode(false);
		mw.getToolPane().getButtonPane().switch_3in1_xy(false);
		mw.getChartScreen().updateXYView();

		cm.mathControl.mathon = false;

		cm.rc.forceStop();
		cm.ruleManager.getPFRuleControl().disableRule();

		cm.getFFTControl().c_setFFTon(false);
		cm.getWaveFormInfoControl().turnOnAllChannelsCouplingNotInverse();

		tc.c_setChannelMode(TriggerDefine.TrgModeAltIndex);
		tc.resetAltTrgSet();
		tc.resetSingleTrgset();

		mw.getToolPane().getTrgInfoPane().updateInfos(tc);

		cc.getSampleControl().c_setModelIdx(SampleControl.Sample_PKDetect);

		cc.getSysControl().c_setSyncOut(SysControl.SYNOUT_TrgOut);

		int tbi = TIMEBASE_10MS;
		cc.getTimeControl().c_setTimebase_HorTrgPos(tbi, 0);// 10ms
		cm.getZoomAssctr().switch2Main();

		ict.getValueFeeder().forceFeedMiddle();
	}

	protected void afterAutoset() {
		// dataHouse.getWaveFormManager().resumeForceFreshFreq();

		ict.getValueFeeder().resumeFeedMiddle();

		// cc.getWaveFormInfoControl().turnChannelsDC(css);
		mw.getToolPane().getTrgInfoPane().updateInfos(tc);

		cc.getSampleControl().c_setModelIdx(SampleControl.Sample_Sampling);
		dh.getWaveFormManager().resumeDraw();
		donejob.run();
	}

	private VLog vl = new VLog();
	private TriggerControl tc;
	private MainWindow mw;
	private DataHouse dh;

	public static final double limitGridLitterVB = 1;

	public void getReady() {
		beforeAutoset();

		int vbnum = vp.getVoltageNumber();
		int wfnum = cc.getWaveFormInfoControl().getLowMachineChannels();

		List<WFAutoRoutine> wfrlist = new ArrayList<WFAutoRoutine>(wfnum);
		ON_WF_Iterator owi = wfm.on_wf_Iterator();

		// wfcs = new LinkedList<WFCheck>();

		while (owi.hasNext()) {
			WaveForm wf = owi.next();
			wf.setZeroYLoc(0, true, false);
			wf.setVoltBaseIndex(0, false);
			WFAutoRoutine wfr = new WFAutoRoutine(wf, vbnum, cc,
					limitGridLitterVB);
			wfr.getReady();
			wfrlist.add(wfr);

			// wfcs.add(new WFCheck(wf));
		}
		// sleepThread(200);
		vl.logln("...START AUTOSET...");

		isOver = false;

		replaceRunnable(new RecognizeJob(wfrlist, this, cc));

		ict.addPropertyChangeListener(pcl);
	}

	// private List<WFCheck> wfcs;
	private PropertyChangeListener pcl = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String n = evt.getPropertyName();
			if (n.equalsIgnoreCase(InterCommTiny.NEW_DATA_RECEIVE)) {
				call();
			}
		}
	};

	private Runnable run;
	private boolean isOver;

	public void replaceRunnable(Runnable run) {
		this.run = run;
	}

	public void tail() {
		afterAutoset();
		logln("...roll...");
		replaceRunnable(null);
		ict.removePropertyChangeListener(pcl);
		isOver = true;
	}

	public void resumeCustomize() {
		// cc.getWaveFormInfoControl().turnChannelsDC(css);
	}

	// public static final AddValueAttachCommand VIDEOTRGD_ADD = new
	// AddValueAttachCommand(
	// "VIDEOTRGD_ADD", 0x2, 1, 1); // 判断视频触发的地址

	public void queryVideoTrgd(CMDResponser responser) {
		DeviceAddressTable table = ict.getTinyMachine().getDeviceAddressTable();
		
		Submitor2 sb = (Submitor2) SubmitorFactory.reInit();
		sb.sendCMD(table.VIDEOTRGD_ADD, 1, responser);
	}

	public void doArrangeChannels(List<WFAutoRoutine> next_wfrlist,
			final int tbi) {
		logln("1st wf: " + next_wfrlist.get(0).getWaveForm().getChannelNumber());
		Collections.sort(next_wfrlist, new Comparator<WFAutoRoutine>() {
			@Override
			public int compare(WFAutoRoutine o1, WFAutoRoutine o2) {
				return o1.getWaveForm().getChannelNumber()
						- o2.getWaveForm().getChannelNumber();
			}
		});

		// resumeCustomize();
		replaceRunnable(new ArrangeChannelsJob(tbi, next_wfrlist, this, cc));
	}

	private void call() {
		if (isExit() || isOver) {
			return;
		}

		// for (WFCheck wfc : wfcs) {
		// wfc.collect();
		// }

		if (run != null) {
			run.run();
		}
	}

	private boolean isExit() {
		return cm.isExit();
	}

	// class WFCheck {
	// public WFCheck(WaveForm wf) {
	// this.wf = wf;
	// }
	//
	// private WaveForm wf;
	// private List<Double> freqs = new LinkedList<Double>();
	//
	// public void collect() {
	// freqs.add((double) wf.wfi.ci.getFreq());
	// }
	// }
}
