package com.owon.vds.tiny.circle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.wf.ON_WF_Iterator;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.source.comm.InterCommTiny;
import com.owon.uppersoft.vds.ui.dialog.ProgressObserver;
import com.owon.vds.calibration.stuff.ArgCreator;

public abstract class CalRunner2 implements Logable {
	private WaveFormManager wfm;
	private ControlManager cm;
	private ProgressObserver po;
	private VoltageProvider vp;

	private Runnable finishedJob;
	private CoreControl cc;
	private int maximum;
	private int progress;
	private PropertyChangeSupport ict;

	private ArgCreator ac;
	private AGPControl agp;

	public CalRunner2(ArgCreator ac, ProgressObserver po, Runnable finishedJob,
			PropertyChangeSupport ict, AGPControl agp) {
		this.ac = ac;
		this.po = po;
		this.ict = ict;
		this.agp = agp;
		this.finishedJob = finishedJob;

		DataHouse dh = Platform.getDataHouse();
		cm = dh.controlManager;
		cc = dh.controlManager.getCoreControl();
		wfm = dh.getWaveFormManager();
		vp = cc.getVoltageProvider();
	}

	protected abstract int computeMaximum(int chlnum, int vbnum);

	protected abstract IWFCalRoutine2 createWFCalRoutine(int vbnum,
			WaveForm wf, ArgCreator ac);

	public void getReady() {
		logln("parallel");

		vbnum = vp.getVoltageNumber();
		int wfnum = cc.getWaveFormInfoControl().getLowMachineChannels();// 1;//
		maximum = computeMaximum(wfnum, vbnum);

		po.setMaximum(maximum);

		cc.getTimeControl().c_setTimebase_HorTrgPos(
				cc.getMachineInfo().getTimebaseIndex("1ms"), 0);

		wfrlist = new ArrayList<IWFCalRoutine2>(wfnum);
		ON_WF_Iterator owi = wfm.on_wf_Iterator();
		vbidx = 0;
		while (owi.hasNext()) {
			WaveForm wf = owi.next();

			// if (wf.getChannelNumber() == 1)continue;

			IWFCalRoutine2 wfr = createWFCalRoutine(vbnum, wf, ac);
			wfr.getReady();
			wfr.forVB(vbidx);
			wfrlist.add(wfr);
		}
		agp.genWFwithVB(vbidx);
		progress = 0;
		isOver = false;
		isCancel = false;
		afterCancelJob = null;
		ict.addPropertyChangeListener(pcl);
	}

	private int vbidx;

	private PropertyChangeListener pcl = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String n = evt.getPropertyName();
			if (n.equalsIgnoreCase(InterCommTiny.NEW_DATA_RECEIVE)) {
				call();
			}
		}
	};

	private boolean isOver;
	private List<IWFCalRoutine2> wfrlist;

	private boolean isExit() {
		return cm.isExit();
	}

	private boolean isCancel;

	public boolean isCancel() {
		return isCancel;
	}

	private Runnable afterCancelJob;
	private int vbnum;

	public void cancel(Runnable afterJob) {
		this.isCancel = true;
		this.afterCancelJob = afterJob;
	}

	private void call() {
		if (isExit() || isOver) {
			return;
		}

		if (progress < maximum && !isCancel) {
			// System.out.println("call");
			Iterator<IWFCalRoutine2> wfri = wfrlist.iterator();

			boolean nextvb = true;
			while (wfri.hasNext()) {
				if (isExit())
					return;

				/** 校正终结的通道 */
				IWFCalRoutine2 wfr = wfri.next();
				if (wfr.getRoutCalType() < 0)
					continue;

				nextvb = false;
				if (isCancel) {
					jobOnFinished();
					break;
				}

				/** 校正 */
				int v = wfr.routOut();
				if (v > 0) {
					progress += v;
					po.increaseValue(v);
				}
			}

			// System.out.println("nextvb: " + nextvb);
			if (nextvb) {
				vbidx++;
				if (vbidx < vbnum) {
					wfri = wfrlist.iterator();
					while (wfri.hasNext()) {
						if (isExit())
							return;

						// 也可以判断cancel
						/** 校正终结的通道 */
						IWFCalRoutine2 wfr = wfri.next();
						wfr.forVB(vbidx);
					}
					agp.genWFwithVB(vbidx);
				}
			}
			logln("...roll...");
		} else {
			jobOnFinished();
		}

		if (isCancel) {
			if (afterCancelJob != null)
				afterCancelJob.run();
		}
	}

	private void jobOnFinished() {
		isOver = true;
		ict.removePropertyChangeListener(pcl);
		if (finishedJob != null)
			finishedJob.run();
	}

	@Override
	public void log(Object o) {
		System.err.print(o);
	}

	@Override
	public void logln(Object o) {
		System.err.println(o);
	}

}