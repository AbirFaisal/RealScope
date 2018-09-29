package com.owon.uppersoft.vds.socket.provider;

import static com.owon.uppersoft.vds.socket.ScpiPool.ErrNum;
import static com.owon.uppersoft.vds.socket.ScpiPool.Failed;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormInfoControl;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.sub.InfoBlock;
import com.owon.uppersoft.dso.view.sub.InfoBlockAction;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.ChannelInfo.Volt;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.socket.NB;
import com.owon.uppersoft.vds.socket.ScpiPool;

public class ChannelScpiProvider {

	private ControlManager cm;

	public ChannelScpiProvider(ControlManager ctrm) {
		this.cm = ctrm;
	}

	/** :CHANnel命令子系统 */
	public boolean getIsChannelDisplay(int chlidx) {
		return cm.getCoreControl().getWaveFormInfo(chlidx).ci.isOn();
	}

	public String setChannelDisplay(int chidx, String args) {
		ChannelInfo ci = cm.getCoreControl().getWaveFormInfo(chidx).ci;
		if (args.equalsIgnoreCase("ON")) {
			ci.c_setOn(true);// setOn(b);
		} else if (args.equalsIgnoreCase("OFF")) {
			ci.c_setOn(false);
		} else
			return ScpiPool.Failed;

		Platform.getMainWindow().updateDefaultAll();
		return ScpiPool.Success;
	}

	public String getChannelCoupling(int chidx) {
		WaveForm wf = cm.getWaveFormInfoControl().getWaveForm(chidx);
		if (wf == null)
			return ScpiPool.ErrCh;
		return ChannelInfo.COUPLINGCHARS[wf.wfi.ci.getCouplingIdx()];
	}

	public String setChannelCoupling(int chidx, String rearcmd) {
		int cpidx = -1;
		String[] coupArr = ChannelInfo.COUPLINGCHARS;
		for (int i = 0; i < coupArr.length; i++) {
			if (rearcmd.equalsIgnoreCase(coupArr[i])) {
				cpidx = i;
				break;
			}
		}
		if (cpidx < 0)
			return ScpiPool.Failed;

		WaveForm wf = cm.getWaveFormInfoControl().getWaveForm(chidx);
		if (wf == null)
			return ScpiPool.ErrCh;
		wf.wfi.ci.c_setCoupling(cpidx);
		cm.pcs.firePropertyChange(PropertiesItem.COUPLING_OPTION, null, null);
		Platform.getMainWindow().updateDefaultAll();
		return ScpiPool.Success;
	}

	public String getChannelProbe(int chidx) {
		WaveFormInfoControl wfic = cm.getWaveFormInfoControl();
		WaveForm wf = wfic.getWaveForm(chidx);
		if (wf == null)
			return ScpiPool.ErrCh;
		return wf.wfi.ci.getProbeLabel();
	}

	public String setChannelProbe(int chidx, String rearcmd) {
		int probidx = -1;
		String[] probArr = cm.getMachineInfo().ProbeTexts;
		for (int i = 0; i < probArr.length; i++) {
			if (rearcmd.equalsIgnoreCase(probArr[i])) {
				probidx = i;
				break;
			}
		}
		if (probidx < 0)
			return ScpiPool.Failed;

		WaveFormInfoControl wfic = cm.getWaveFormInfoControl();
		WaveForm wf = wfic.getWaveForm(chidx);
		if (wf == null)
			return ScpiPool.ErrCh;
		wf.wfi.ci.setProbeMultiIdx(probidx);
		cm.updateChannelVoltValueEverywhere(chidx);
		return ScpiPool.Success;
	}

	public double getChannelSCALe(int chlidx) {
		// KNOW 原为mV，要求返回单位为V的值，故除以1000
		double vt = cm.getCoreControl().getWaveFormInfo(chlidx).ci
				.getVoltValue();
		return vt / 1000;
	}

	public String setChannelSCALe(int chlidx, String dv) {
		// 从字符得到设置电压
		if (!NB.isNum(dv))
			return ErrNum;
		double vt;
		try {
			vt = Double.parseDouble(dv);
		} catch (Exception e) {
			return Failed;
		}
		vt *= 1000;// V转mV

		// 遍历电压档位找到相同电压，设置进去
		Volt[] vs = cm.getCoreControl().getWaveFormInfo(chlidx).ci
				.getVoltageLabels();
		int vtidx = -1;
		for (int i = 0, len = vs.length; i < len; i++) {
			if (vs[i].getValue() == vt) {
				vtidx = i;
				break;
			}
		}
		if (vtidx < 0)
			return ScpiPool.Failed;// ErrVScale;
		InfoBlock ib = Platform.getMainWindow().getToolPane().getInfoPane()
				.get(chlidx);
		InfoBlockAction ibd = ib.getInfoBlockDelegate();
		ibd.removeOwnedComboBox();
		ibd.selected(vtidx);
		ib.repaint();
		return ScpiPool.Success;
	}

	public String getChannelOFFSet(int chidx) {
		WaveFormInfoControl wfic = cm.getWaveFormInfoControl();
		WaveForm wf = wfic.getWaveForm(chidx);
		if (wf == null)
			return ScpiPool.ErrCh;
		return String.valueOf(wf.wfi.ci.getPos0());
	}

	public String setChannelOFFSet(int chlidx, String args) {
		if (!NB.isNum(args))
			return ErrNum;
		int pixs = -1;
		try {
			pixs = Integer.parseInt(args);
		} catch (Exception e) {
			return Failed;
		}

		InfoBlock ib = Platform.getMainWindow().getToolPane().getInfoPane()
				.get(chlidx);
		if (ib == null)
			return ScpiPool.ErrCh;
		InfoBlockAction ibd = ib.getInfoBlockDelegate();
		pixs = ibd.valueChanged_Directly(pixs);// 自带有限定offsset范围
		return ScpiPool.Success + ",Valid Set:" + String.valueOf(pixs);
	}

	public String getHardwareFrequency(int idx) {
		WaveFormInfoControl wfic = cm.getWaveFormInfoControl();
		WaveForm wf = wfic.getWaveForm(idx);
		if (wf == null)
			return ScpiPool.ErrCh;
		return cm.getFreqLabel(wf.wfi.ci);
	}

	public String getINVerseState(int chidx) {
		WaveFormInfoControl wfic = cm.getWaveFormInfoControl();
		WaveForm wf = wfic.getWaveForm(chidx);
		if (wf == null)
			return ScpiPool.ErrCh;
		return wf.wfi.ci.isInverse() ? "ON" : "OFF";
	}

	public String setINVerseState(int chidx, String args) {
		boolean iv = false;
		if (args.equalsIgnoreCase("ON"))
			iv = true;
		else if (args.equalsIgnoreCase("OFF"))
			iv = false;
		else
			return ScpiPool.Failed;

		WaveFormInfoControl wfic = cm.getWaveFormInfoControl();
		WaveForm wf = wfic.getWaveForm(chidx);
		if (wf == null)
			return ScpiPool.ErrCh;

		wf.wfi.ci.c_setInverse(iv);
		cm.pcs.firePropertyChange(PropertiesItem.CHANNEL_OPPOSITE, null, null);
		Platform.getMainWindow().updateShow();
		return ScpiPool.Success;
	}
}
