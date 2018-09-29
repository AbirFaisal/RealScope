package com.owon.uppersoft.vds.socket.provider;

import java.nio.ByteBuffer;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormInfoControl;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.aspect.IBoard;
import com.owon.uppersoft.vds.socket.ScpiPool;
import com.owon.uppersoft.vds.socket.command.CmdFactory;

public class MainProvider {

	protected ControlManager ctrm;
	private TrgScpiProvider trgPvd;
	private FFTScpiProvider fftPvd;
	private ChannelScpiProvider chlPvd;
	private MeasureScpiProvider measPvd;
	private AcquireScpiProvider acqPvd;
	private TimebaseScpiProvider tbPvd;
	private LanScpiProvider lanPvd;
	private DisplayScpiProvider dspPvd;

	public MainProvider() {
		this.ctrm = Platform.getControlManager();
	}

	public TrgScpiProvider getTrg() {
		if (trgPvd == null)
			trgPvd = new TrgScpiProvider(ctrm);
		return trgPvd;
	}

	public FFTScpiProvider getFFT() {
		if (fftPvd == null)
			fftPvd = new FFTScpiProvider(ctrm);
		return fftPvd;
	}

	public ChannelScpiProvider getChl() {
		if (chlPvd == null)
			chlPvd = new ChannelScpiProvider(ctrm);
		return chlPvd;
	}

	public AcquireScpiProvider getAcq() {
		if (acqPvd == null)
			acqPvd = new AcquireScpiProvider(ctrm);
		return acqPvd;
	}

	public TimebaseScpiProvider getTb() {
		if (tbPvd == null)
			tbPvd = new TimebaseScpiProvider(ctrm);
		return tbPvd;
	}

	public MeasureScpiProvider getMeas() {
		if (measPvd == null)
			measPvd = new MeasureScpiProvider(ctrm);
		return measPvd;
	}

	public LanScpiProvider getLan() {
		if (lanPvd == null)
			lanPvd = new LanScpiProvider(ctrm);
		return lanPvd;
	}

	public DisplayScpiProvider getDsp() {
		if (dspPvd == null)
			dspPvd = new DisplayScpiProvider(ctrm);
		return dspPvd;
	}

	public byte[] getData(String cmd) {
		int ch = -1;
		if (cmd.trim().equals(CmdFactory.ADC)) {
			ch = getDefaultSelectWaveform();
		} else {
			int len = CmdFactory.ADC.length();
			cmd = cmd.substring(len).trim();

			String st = ScpiPool.CH;
			if (cmd.toUpperCase().startsWith(st))
				cmd = cmd.substring(st.length()).trim();

			try {
				ch = Integer.parseInt(cmd) - 1;
			} catch (Exception e) {
				return null;// HandlerFactory.Failed;
			}
		}
		if (ch < 0 || ch > getMaxChannelIdx())
			return null;// HandlerFactory.ErrCh;

		WaveForm wf = Platform.getDataHouse().getWaveFormManager()
				.getWaveForm(ch);
		if (wf == null)
			return null;
		ByteBuffer bb = wf.getADC_Buffer();
		if (bb == null)
			return null;
		int p = bb.position();
		int remain = bb.remaining();
		byte[] b = new byte[remain];
		System.arraycopy(bb.array(), p, b, 0, remain);
		return b;
	}

	public int getMaxChannelIdx() {
		return ctrm.getSupportChannelsNumber() - 1;
	}

	public int getDefaultSelectWaveform() {
		WaveFormInfoControl wfic = ctrm.getWaveFormInfoControl();
		return wfic.getSelectedWF().wfi.ci.getNumber();
	}

	public String getIDN() {
		IBoard sc = ctrm.getSoftwareControl();

		String sb = ctrm.getConfig().getStaticPref().getManufacturerId() +
				',' +
				ctrm.getCoreControl().getMachine().name() +
				',' +
				sc.getBoardSeries() +
				',' +
				sc.getBoardVersion();
		return sb;
	}

	public String setFactoryDefault() {
		ctrm.factorySet();
		ctrm.pcs.firePropertyChange(PropertiesItem.UPDATE_FACTORY_VIEW, null,
				null);
		return ScpiPool.Success;
	}

	public String setAUTOset() {
		boolean rsp = Platform.getControlApps().interComm.autoset();
		return rsp ? ScpiPool.Success : ScpiPool.Failed;
	}

	public String getRunStop() {
		return ctrm.isRuntime() ? "RUN" : "STOP";
	}

	public String setRunStop() {
		if (!ctrm.sourceManager.isConnected())
			return "NOT CONNECT";

		boolean before = ctrm.isRuntime();
		Platform.getMainWindow().getTitlePane().askSwitchRS();
		return !before ? "SET RUN" : "SET STOP";
	}

	public String getTitleStatus() {
		return ctrm.getTitleStatusLabel();
	}
}
