package com.owon.uppersoft.vds.source.comm.ext;

import com.owon.uppersoft.vds.device.interpret.DeviceAddressTable;
import com.owon.uppersoft.vds.device.interpret.TinyCommunicationProtocol;
import com.owon.uppersoft.vds.machine.LowControlManger;
import com.owon.uppersoft.vds.source.comm.Submitor2;
import com.owon.vds.firm.protocol.AddressAttachCommand;

public class ClockTimeAdjuster implements TinyCommunicationProtocol,
		IClockTimeAdjuster {

	protected LowControlManger lcm;
	protected Submitor2 sbm;
	protected DeviceAddressTable table;

	public ClockTimeAdjuster(LowControlManger lcm, Submitor2 sbm) {
		this.lcm = lcm;
		this.sbm = sbm;
		table = lcm.getMachineType().getDeviceAddressTable();
	}

	protected Arg_En getArg_En(int arg, int en) {
		// 多出来的虚拟档位换算到协议
		// if (en == 6) {
		// arg *= 10;
		// en = 5;
		// }

		// arg是以ns为单位的，协议要求把值除以10，所以en-1
		en -= 1;

		return new Arg_En(arg, en);
	}

	@Override
	public void c_trg_holdoffArg(int chl, int arg, int en) {
		// 这里只是简单拷贝，等值传递，后续可添加方法，实现arg和en的转化
		Arg_En ae = getArg_En(arg, en);
		arg = ae.arg;
		en = ae.en;

		logln("[trg holdoff arg[9~0]" + arg + ", index[2~0]:]" + en);
		int tmp = ((arg << 6) | (en & 0x7));// Submitor2 sbm,

		send(chl, tmp);
	}

	protected void send(int chl, int tmp) {
		int v;
		v = (tmp >>> 8) & 0xff;
		logln("arg[9~2] >> 0x" + Integer.toHexString(v));
		sendCMD(table.trg_holdoff[chl][0], v);

		v = tmp & 0xff;
		logln("arg[1~0] 000 index[2~0] >> 0x" + Integer.toHexString(v));
		sendCMD(table.trg_holdoff[chl][1], v);
	}

	protected void sendCMD(AddressAttachCommand tca, int value) {
		sbm.sendCMD(tca, value);
	}

	protected void logln(String string) {
		sbm.logln(string);
	}

	@Override
	public void c_trg_condtionArg(int chl, int cdt, int arg, int en) {
		// 这里只是简单拷贝，等值传递，后续可添加方法，实现arg和en的转化
		Arg_En ae = getArg_En(arg, en);
		arg = ae.arg;
		en = ae.en;

		logln("[trg condition time:]");
		boolean eql = lcm.isPulse_Equal(cdt);
		int v;
		int pcc = chl;

		AddressAttachCommand h_add = table.trg_condtion_equal_hl[pcc][0];
		if (eql) {
			AddressAttachCommand l_add = table.trg_condtion_equal_hl[pcc][1];
			logln("arg: " + arg);

			int hv = (int) (arg * 1.05);
			v = (hv << 6) | (en & 0x7);
			logln("h:[15~6]" + hv + ", [2~0]" + en);
			sendCMD(h_add, v);

			int lv = (int) (arg * 0.95);
			logln("l:[15~0]" + lv);
			sendCMD(l_add, lv);
		} else {
			v = en & 0x7;
			logln("h:[2~0]" + en);
			sendCMD(h_add, v);

			v = arg;
			logln("[15~0]" + arg);
			sendCMD(table.trg_condtion_gl[pcc], v);
		}

	}
}
