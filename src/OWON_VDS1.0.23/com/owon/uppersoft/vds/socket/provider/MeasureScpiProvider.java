package com.owon.uppersoft.vds.socket.provider;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormInfoControl;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.measure.MeasureT;
import com.owon.uppersoft.vds.core.measure.VR;
import com.owon.uppersoft.vds.socket.ScpiPool;
import com.owon.uppersoft.vds.socket.command.CmdFactory;

public class MeasureScpiProvider {
	private ControlManager cm;

	public MeasureScpiProvider(ControlManager ctrm) {
		this.cm = ctrm;
	}

	public String getRecVamp(int index) {
		ByteBuffer bb = Platform.getDataHouse().getWaveFormManager()
				.getWaveForm(index).getADC_Buffer();

		if (bb == null)
			return "?";
		int p = bb.position();
		int remain = bb.remaining();
		byte[] b = new byte[remain];
		System.arraycopy(bb.array(), p, b, 0, remain);

		Arrays.sort(b);
		int sum = 0;
		int ps = (int) (b.length * 0.06);
		int len = (b.length / 2) - ps;

		for (int i = ps; i < len; i++) {
			sum += b[b.length - i] - b[i];
		}
		// System.out.println(b.length + "  ps:" + ps + "  sum:" + sum);
		return Double.toString(1.0 * sum / (len - ps));
	}

	/** :MEASure命令子系统 */
	public String getMeasureValue(int chidx, int typidx, String[] msts) {
		if (!cm.getMeasureManager().ison())
			return ScpiPool.WarnMeasureOff;

		WaveFormInfoControl wfic = cm.getWaveFormInfoControl();
		WaveForm wf = wfic.getWaveForm(chidx);
		if (wf == null)
			return ScpiPool.ErrCh;
		String cmd = msts[typidx];
		if (cmd.equalsIgnoreCase(CmdFactory.RECVamp))
			return getRecVamp(chidx);

		/** accquire RiseDelay,FallDelay */
		boolean b = isDelayType(cmd);
		if (b) {
			double dv = cm.measMod.getDelayValue(cmd, chidx);
			return dv < 0 ? "?" : String.valueOf(dv);
		}
		/** accquire ChannelMeasureValue */
		VR[] vrs = wf.getMeasureADC().vrs;
		double v = vrs[typidx].v;
		// System.out.println("Value:" + vrs[typidx].vu + ", v:  " +v);
		/* 以下测量类型的值要从mV转为V */
		MeasureT mt = MeasureT.VALUES[typidx];
		switch (mt) {
		case PKPK:
		case MAX:
		case MIN:
		case VAMP:
		case VTOP:
		case VBASe:
		case AVERage:
		case CYCRms:
			v = v / 1000;// mV转V;
			break;
		}
		b = vrs[typidx].vu == "?";
		return b ? "?" : String.valueOf(v);
	}

	public String getMeasureSource() {
		return cm.measMod.getCHlinkElem();
	}

	public String setMeasureSource(String cmd) {
		String st = ScpiPool.CH;
		if (cmd.toUpperCase().startsWith(st))
			cmd = cmd.substring(st.length()).trim();
		int ch = -1;
		try {
			ch = Integer.parseInt(cmd) - 1;
		} catch (Exception e) {
			return ScpiPool.Failed;
		}
		if (ch < 0 || ch > cm.getAllChannelsNumber() - 1)
			return ScpiPool.ErrCh;
		cm.measMod.addChElem(ch);
		cm.getMeasureManager().setMeasureOn_enforce();
		return ScpiPool.Success;
	}

	public String addMeasureElem(String cmd) {
		boolean b = isDelayType(cmd);
		if (b) {
			b = cm.measMod.addOrDelDelay(true, cmd);
			cm.getMeasureManager().setMeasureOn_enforce();
			return b ? ScpiPool.Success : ScpiPool.Failed;
		}

		MeasureT[] ts = MeasureT.VALUES;
		int len = MeasureT.Count;// CmdFactory.MeasValLen;
		for (int i = 0; i < len; i++) {
			// System.out.println(cmd + "," + ts[i].toString());
			if (cmd.equalsIgnoreCase(ts[i].toString())) {
				cm.measMod.addMtElem(i);
				cm.getMeasureManager().setMeasureOn_enforce();
				return ScpiPool.Success;
			}
		}
		return ScpiPool.Failed;
	}

	public String delMeasureElem(String cmd) {
		boolean b = isDelayType(cmd);
		if (b) {
			b = cm.measMod.addOrDelDelay(false, cmd);
			cm.getMeasureManager().setMeasureOn_enforce();
			return b ? ScpiPool.Success : ScpiPool.Failed;
		}

		if (cmd.equalsIgnoreCase("all")) {
			cm.measMod.removeAllMeasures();
			cm.getMeasureManager().setMeasureOn_enforce();
			return ScpiPool.Success;
		}

		MeasureT[] ts = MeasureT.VALUES;
		int len = MeasureT.Count;// CmdFactory.MeasValLen;
		for (int i = 0; i < len; i++) {
			if (cmd.equalsIgnoreCase(ts[i].toString())) {
				boolean rs = cm.measMod.delMtElem(i);
				cm.getMeasureManager().setMeasureOn_enforce();
				return rs ? ScpiPool.Success : ScpiPool.Failed;
			}
		}
		return ScpiPool.Failed;
	}

	private boolean isDelayType(String cmd) {
		boolean b = cmd.equalsIgnoreCase(CmdFactory.RDELay)
				|| cmd.equalsIgnoreCase(CmdFactory.FDELay);
		return b;
	}

}
