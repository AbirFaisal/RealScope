package com.owon.uppersoft.dso.wf.common;

import static com.owon.uppersoft.dso.global.DataHouse.Offline_DM;
import static com.owon.uppersoft.dso.global.DataHouse.RT_DM;
import static com.owon.uppersoft.dso.global.DataHouse.RT_Normal;

import java.math.BigDecimal;

import com.owon.uppersoft.dso.function.PersistentDisplay;
import com.owon.uppersoft.dso.function.RecordControl;
import com.owon.uppersoft.dso.function.record.OfflineChannelsInfo;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.machine.aspect.IMultiReceiver;
import com.owon.uppersoft.dso.mode.control.TimeControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.source.comm.TrgStatus;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.ToolPane;
import com.owon.uppersoft.vds.core.aspect.control.ITimeControl;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;
import com.owon.uppersoft.vds.print.PrinterPreviewFrame;

/**
 * MultiReceiver，为不同场合提供数据读入DataHouse的处理，同时更新controlManager中相应设置的状态
 * 
 */
public class MultiReceiver implements IMultiReceiver {
	private ControlManager cm;
	private WaveFormManager wfm;
	private ScreenContext pc;
	private TimeControl tc;

	public MultiReceiver(ControlManager cm, WaveFormManager wfm) {
		this.cm = cm;
		this.wfm = wfm;
		pc = cm.paintContext;
		tc = cm.getTimeControl();
	}

	protected void dataCompleteNIncrease(DataHouse dh) {
		dh.dataComplete();
		drawCount++;
	}

	private int drawCount = 0;

	protected void resetDrawCount() {
		drawCount = 0;
	}

	@Override
	public void receiveOfflineData(OfflineChannelsInfo oci, DataHouse dh,
			int status) {
		if (oci.getChannelCount() == 0)
			return;
		/** 0帧的情况返回，这样在平均值采样时避免计入空帧 */
		if (oci.getFrameCount() == 0)
			return;
		dh.setStatus(status);
		dh.dataComplete();

		// 时基
		MainWindow mw = Platform.getMainWindow();
		ToolPane tp = mw.getToolPane();

		boolean slow = cm.getCoreControl().isRunMode_slowMove();

		// System.err.println("receiveRTData: "+pc.pkdetect);

		// 深存储画图的预备：将LocInfo.cba指向dh.cba等.以免时基若变动，设置时崩溃.
		int dmidx = cm.getMachineInfo().getDMIndexFromLength(oci.DMlen);
		if (dmidx >= 0)
			cm.getDeepMemoryControl().setDeepIdx(dmidx);
		// 水平触发位置的设置和更新,须先设置深度
		tc.setHorTrgPos(oci.horTrgPos);
		cm.getCoreControl().updateHorTrgIdx4View();
		cm.getCoreControl().updateHorTrgPosRange();

		wfm.offAllWaveForms();
		wfm.receiveOfflineData(oci, pc, slow, oci.timebase);
		tc.offline_setTimeBaseIdx(oci.timebase);

		cm.measure(wfm);

		/** 依赖选择波形的信息进行计算，需在导入波形后进行 */
		tc.recomputeHorizontalTriggerLabel();
		mw.updateShow();
		tp.updateChannels();
		mw.getITitleStatus().updateView();
		cm.pcs.firePropertyChange(ITimeControl.onTimebaseUpdated, null, null);

		cm.pcs.firePropertyChange(PropertiesItem.APPLY_CHANNELS, null, null);

		PrinterPreviewFrame.selfRepaint();
	}

	public void receiveOfflineDMData(DMInfo ci, DataHouse dh) {
		if (ci.channelNum == 0)
			return;
		dh.dataNotComplete();
		dh.setStatus(Offline_DM);

		MainWindow mw = Platform.getMainWindow();
		ToolPane tp = mw.getToolPane();
		CoreControl cc = cm.getCoreControl();

		// pc.slow = tc.isSlowMove();
		// System.err.println(ci.datalen);
		int dmidx = cm.getMachineInfo().getDMIndexFromLength(ci.datalen);
		dh.setupDeepMemoryStorage(ci.file);

		int tbIdx = ci.timebase;
		BigDecimal tbd = cc.getTimeConfProvider().getBDTimebase(tbIdx);

		// 存储深度 TODO 测试用暂定如此，与屏幕中心点的像素差转化为读入时的触发点位置，在中心对称的运行时情况下适用
		cm.getDeepMemoryControl().setDeepIdx(dmidx);

		// 水平触发位置的设置和更新,须先设置深度
		tc.setHorTrgPos(ci.horTrgPos);

		int initPos = ci.initPos;
		int screenlen = ci.getScreendatalen();
		cc.updateDMHorTrgIdx4View(initPos, screenlen);

		cc.updateHorTrgPosRange();

		tc.setTimebaseIndexValue(tbIdx);
		wfm.receiveOfflineDMData(ci, pc, tbd, tbIdx);
		dh.dataComplete();

		// 将LocInfo.cba指向dh.cba等.以免时基若变动，设置时崩溃.
		tc.offline_setTimeBaseIdx(tbIdx);
		// 时基调动，因LocInfo->CompressInfoUnit.tbTranslate()要拿buf数据,故LocInfo的cba不能空,要先设。

		cm.measure(wfm);

		/** 依赖选择波形的信息进行计算，需在导入波形后进行 */
		tc.recomputeHorizontalTriggerLabel();

		cm.pcs.firePropertyChange(PropertiesItem.APPLY_CHANNELS, null, null);

		mw.updateShow();
		tp.updateChannels();
		mw.getITitleStatus().updateView();
		cm.pcs.firePropertyChange(ITimeControl.onTimebaseUpdated, null, null);

		PrinterPreviewFrame.selfRepaint();
	}

	public void receiveRTData(ChannelsTransportInfo cti, DataHouse dh) {
		dh.dataNotComplete();
		// DBG.dbgln(ci.channels.size() + ", " + ci.dataComplete);
		if (cti.getChannelCount() == 0 || cti.getDataComplete() == 0)
			return;

		/** 0帧的情况返回，这样在平均值采样时避免计入空帧 */
		if (cti.getFrameCount() == 0)
			return;

		dh.setStatus(RT_Normal);// also dm
		dataCompleteNIncrease(dh);

		MainWindow mw = Platform.getMainWindow();
		ToolPane tp = mw.getToolPane();

		boolean slow = cm.getCoreControl().isRunMode_slowMove();
		// System.err.println("slow: "+slow);
		/**
		 * 做额外判断，防止从慢扫切到非慢扫时，可能还拿到几个慢扫的帧
		 * 
		 * 在非慢扫时，cti.slowMove不会拿来用，只是照例画波形，但这几帧还是慢扫的数据需要被过滤掉
		 */

		if (!slow) {
			if (cti.slowMove > 0) {
				MachineType mt = cm.getMachine();
				// LATER
				boolean pass = mt.passSlowOnNoneSlowTimebase();
				if (pass) {
					cti.slowMove = 0;
				} else {
					return;
				}
			}
		}
		// System.err.println("receiveRTData: "+pc.pkdetect);

		cm.getCoreControl().updateHorTrgIdx4View();

		/** 当慢扫的情况下进行单次触发，停止的那一帧是特例，因为它不能以慢扫的方式画图 */
		boolean sweepOnceStop = cm.getTriggerControl().isSweepOnce()
				&& cti.triggerStatus == TrgStatus.Stop.ordinal();
		slow = slow && !sweepOnceStop;
		wfm.receiveRTData(cti, pc, slow);

		cm.updateMeasure(wfm);

		/** 依赖选择波形的信息进行计算，需在导入波形后进行 */

		PersistentDisplay pd = dh.getPersistentDisplay();
		// RT时余辉
		if (pd.isUseCanvasBuffer()) {
			pd.bufferWaveForms(pc, wfm, mw);
		} else {
			mw.updateShow();
		}

		tp.updateAfterData();

		RecordControl rc = cm.rc;
		rc.recordOnce(wfm, cti);
	}

	public void receiveRTDMData(DMInfo ci, DataHouse dh) {
		// DBG.dbgln(ci.channels.size() + ", " + ci.dataComplete);
		if (ci.channelNum == 0 || ci.dataComplete == 0)
			return;
		dh.setStatus(RT_DM);
		dh.dataComplete();

		MainWindow mw = Platform.getMainWindow();
		ToolPane tp = mw.getToolPane();

		dh.setupDeepMemoryStorage(ci.file);

		int initPos = ci.initPos;
		int screenlen = ci.getScreendatalen();
		cm.getCoreControl().updateDMHorTrgIdx4View(initPos, screenlen);

		wfm.receiveRTDMData(ci, pc, tc.getBDTimebase(), tc.getTimebaseIdx());

		// 停下来载入深存储不再计算一次测量值
		/** 依赖选择波形的信息进行计算，需在导入波形后进行 */
		mw.updateShow();
		tp.updateAfterData();
	}

}
