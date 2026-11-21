package com.owon.uppersoft.dso.source.comm;

import static com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo.Status_RT_ConnectErr;
import static com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo.Status_RT_OK;
import static com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo.Status_RT_ReadContentErr;
import static com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo.Status_RT_UnknownErr;
import static com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo.Status_RT_WriteContentErr;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.machine.aspect.IStopable;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.source.comm.detect.InitLinkRunner;
import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.ChartScreen;
import com.owon.uppersoft.dso.view.ITitleStatus;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.vds.core.comm.JobQueueDispatcher;
import com.owon.uppersoft.vds.core.usb.IDevice;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;
import com.owon.uppersoft.vds.util.Pref;

/**
 * Runs throughout the program's lifetime, supporting transfer-related matters such as detecting connections, receiving data, sending commands, and detecting disconnection
 * 
 * For now only calls the connection detection method
 * 
 */
public class InfiniteDaemon extends Flow {

	private final class PromptErrNDisConnectRunner implements Runnable {
		private String promptTxt;

		public String toString() {
			return "m_promptNDisConnectConnErr";
		}

		public void setPromptTxt(String t) {
			promptTxt = t;
		}

		public void run() {
			promptTxt(promptTxt);
			m_dislink.run();
		}

	}

	// private final class PromptErrRunner implements Runnable {
	// private String promptTxt;
	//
	// public String toString() {
	// return "m_promptConnErr";
	// }
	//
	// public void setPromptTxt(String t) {
	// promptTxt = t;
	// }
	//
	// public void run() {
	// promptTxt(promptTxt);
	// }
	//
	// }

	private MainWindow mw;
	protected ControlManager cm;

	private AbsInterCommunicator ic;

	private IStopable ca;

	public InfiniteDaemon(DataHouse dh, MainWindow mw, JobQueueDispatcher df,
			AbsInterCommunicator ic, IStopable ca, AbsGetDataRunner gdr) {
		super(dh.controlManager.sourceManager, gdr, df, dh.controlManager.getIRuntime());
		this.mw = mw;
		this.ic = ic;
		this.ca = ca;

		its = mw.getITitleStatus();
		cm = dh.controlManager;

		cs = mw.getChartScreen();

		midWait = 50;
		longWait = 1000;
		// DBG.errprintln("sp: " + getPeriod());
		m_initlink = new InitLinkRunner(mw, cm);
	}

	public void promptTxt(String txt) {
		final Dimension psz = new Dimension(300, 100);
		final JPanel p = new JPanel();
		p.add(new JLabel(txt + "   [ X ]"));
		p.setPreferredSize(psz);
		p.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				mw.promptUp();
			}
		});
		mw.prompt(p, null);
	}

	private long tbeg, tend, del;
	private int failure, longWait, midWait;

	public Runnable m_startGet = new Runnable() {
		@Override
		public String toString() {
			return "m_startGet";
		}

		public void run() {
			tbeg = System.currentTimeMillis();
			failure = 0;
			gdr.prepareToRun();

			ic.setPreStop(false);
			rewindAfterStop();

			mw.promptUp();
			its.enableBtns();

			// setPeriod(sm.getRecommendPeriod());
			ir.setKeepGet(true);
			its.updateView();
		}
	};

	public Runnable m_reInit = new Runnable() {
		@Override
		public void run() {
			ic.initMachine(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
				}
			});
		}

	};

	public void reInit() {
		addMission(m_reInit);
	}

	public Runnable m_afterStop = new Runnable() {
		@Override
		public String toString() {
			return "m_afterStop";
		}

		public void run() {
			gdr.release();
			tend = System.currentTimeMillis();
			del = tend - tbeg;
			int i = gdr.getChannelsTransportInfo().frameAllNum;
			if (i != 0)
				DBG.dbgln(i + " WaveForms on Rate: " + del / i + " ms/f");
			else {
				DBG.dbgln(i + " WaveForms for : " + del + " ms");
			}

			if (isAfterStop()) {
				try {
					Thread.sleep(longWait);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// If afterKeep is enabled and the flow requesting the deep memory file is started internally, the transfer thread is used here to fetch the file, which is equivalent to blocking
				// ca.setDMDataGotAlready(false);//Enabling it would fetch deep memory data multiple times
				ic.afterkeepload();
				its.confirmStopStatus();
			}
			/** Changed to restore the buttons separately at the end of the with-dm and without-dm branches */
			// mw.getTitlePane().confirmStopStatus();
		}
	};

	public PromptErrNDisConnectRunner pce = new PromptErrNDisConnectRunner();
	// public PromptErrRunner per = new PromptErrRunner();

	public InitLinkRunner m_initlink;

	public void initLink(IDevice id) {
		m_initlink.setDevice(id);
		// m_initlink.run();
		addMission(m_initlink);
	}

	protected ChartScreen cs;

	// Should be used to actively stop data fetching
	public void activeStopGet() {
		ca.stopkeep();
		addMission(m_afterStop);
	}

	public void askConnectUSB(IDevice id) {
		m_initlink.setDevice(id);
		addMission(m_initlink);
	}

	protected void afterTryGet(ChannelsTransportInfo ci) throws InterruptedException {
		int status = ci.status;

		if (status == Status_RT_OK) {
			// vl.logln("Status_RT_OK");
			onSucess(ci);

			// sleep(0);

			if (ci.getFrameCount() <= 0)
				sleep(40);
		} else {
			// vl.logln("Status_Fail");
			onFail(ci);
			vl.logln("sleep " + midWait + " ms on fail");
			// Wait on failure
			sleep(midWait);
		}
	}

	/**
	 * Performed after waveform loading; determines whether reception succeeded and changes behavior based on the call result
	 * 
	 * @param ci
	 */
	private final void onSucess(ChannelsTransportInfo ci) {
		TriggerControl triggerControl = cm.getTriggerControl();

		// Reset failure to zero as soon as one success occurs
		failure = 0;

		int trgs = ci.triggerStatus;
		TrgStatus ts = TrgStatus.VALUES[trgs];

		cm.curTrgStatus = trgs;
		int fc = ci.getFrameCount();
		if (ts == TrgStatus.Stop) {
			DBG.dbg("receive Stop TrgStatus!");
			if (ic.isPreStop()) {
				ci.checkReady();

				// At this point it was previously determined that dm is neededdm
				realStop();
			} else if (triggerControl.isSweepOnce()) {
				// Check for single trigger, set to stop
				// ic.askDM();
				markAfterStop();
				activeStopGet();
				its.applyStop();
			}
		}

		ci.frameAllNum += fc;
		if (ci.frameAllNum >= 100000000) {
			ci.frameAllNum = 0;
			tbeg = System.currentTimeMillis();
		}

		cm.pcs.firePropertyChange(PropertiesItem.updateFrameCounter,
				ci.frameAllNum, ci.points);
	}

	public void realStop() {
		ic.setPreStop(false);
		markAfterStop();
		activeStopGet();
		its.confirmStopStatus();
	}

	/**
	 * Judge the call result; after fail
	 * 
	 * @param ci
	 */
	private final void onFail(ChannelsTransportInfo ci) {// Failure handling
		ci.updateStatus(ci.status);
		// System.err.println(ci.status);
		failure++;

		if (failure >= cm.sourceManager.retryTimes()) {
			// int s = ci.getStatistics();

			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			DBG.seversln("tryRescue...");
			if (cm.sourceManager.tryRescue()) {
				ic.initMachine(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						DBG.outprintln(evt.getPropertyName() + ": "
								+ evt.getNewValue());
					}
				});
			} else {
				DBG.seversln("tryRescue fail");
				pce.setPromptTxt(I18nProvider.bundle().getString(
						"Label.WriteContentErr"));
				ca.stopkeepNForbidDM();
				addMission(pce);
			}
			failure = 0;
			return;
		}

		if (failure >= cm.maxFailureTime) {
			int s = ci.getStatistics();
			// System.err.println(s);
			switch (s) {
			case Status_RT_ConnectErr:
				pce.setPromptTxt(I18nProvider.bundle().getString(
						"Label.ConnectErr"));
				ca.stopkeepNForbidDM();
				addMission(pce);
				break;
			case Status_RT_WriteContentErr:
			case Status_RT_ReadContentErr:
				pce.setPromptTxt(I18nProvider.bundle().getString(
						"Label.ReadOrWriteContentErr"));
				ca.stopkeepNForbidDM();
				addMission(pce);
				break;
			case Status_RT_UnknownErr:
				pce.setPromptTxt(I18nProvider.bundle().getString(
						"Label.UnknownErr"));
				ca.stopkeepNForbidDM();
				addMission(pce);
				break;
			}

			// Connection error
			// Device error
			// Host error

			failure = 0;
		}
	}

	private boolean afterStop = false;
	private ITitleStatus its;

	public boolean isAfterStop() {
		return afterStop;
	}

	protected void markAfterStop() {
		afterStop = true;
	}

	protected void rewindAfterStop() {
		afterStop = false;
	}

	public void onClickStatus(int status_icon_xloc, int mouseBtn) {
	}

	public ChannelsTransportInfo getChannelsTransportInfo() {
		return gdr.getChannelsTransportInfo();
	}

	public AbsGetDataRunner getAbsGetDataRunner() {
		return gdr;
	}

	public void onExit(Pref p) {
	}
}