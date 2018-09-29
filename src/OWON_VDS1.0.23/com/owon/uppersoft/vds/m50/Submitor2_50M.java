package com.owon.uppersoft.vds.m50;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.vds.core.comm.JobQueueDispatcher;
import com.owon.uppersoft.vds.device.interpret.LowerTranslator;
import com.owon.uppersoft.vds.machine.DeviceAddressTable_50M;
import com.owon.uppersoft.vds.machine.LowControlManger;
import com.owon.uppersoft.vds.machine.VDS2052;
import com.owon.uppersoft.vds.source.comm.Submitor2;
import com.owon.uppersoft.vds.source.comm.ext.IClockTimeAdjuster;

public class Submitor2_50M extends Submitor2 {
	public Submitor2_50M(JobQueueDispatcher jqd, LowerTranslator tran,
			LowControlManger lcm, ControlManager cm) {
		super(jqd, tran, lcm, cm);
	}

	protected DeviceAddressTable_50M getDeviceAddressTable() {
		return (DeviceAddressTable_50M) table;
	}

	@Override
	protected void pre_trg_add(int beforeTrg) {
		sendCMDbyBytes(table.PRE_TRG_ADD, beforeTrg);
	}

	@Override
	protected void suf_trg_add(int afterTrg) {
		sendCMDbyBytes(table.SUF_TRG_ADD, afterTrg);
	}

	private int vb_flag = 0;

	@Override
	public void wrVoltbase(final int chl, int vb) {
		if (vb == 0) {
			vb_flag |= (1 << chl);
		} else {
			vb_flag &= ~(1 << chl);
		}
		sendCMD(getDeviceAddressTable().FLAG_5mV, vb_flag);

		super.wrVoltbase(chl, vb);
	}

	@Override
	public void c_tb_htp(int tb, int htp) {
		// 50M的采样标志位
		sendCMD(getDeviceAddressTable().SAMPLE_50M_FLAG,
				tb == VDS2052.TB_50MSAMPLE ? 1 : 0);
		sendCMD(getDeviceAddressTable().READBACK_HTRG_OFFSET,
				tb <= VDS2052.TB_50MSAMPLE ? 13 : 2);
		super.c_tb_htp(tb, htp);
	}

	@Override
	protected IClockTimeAdjuster createClockTimeAdjuster(LowControlManger lcm) {
		return new ClockTimeAdjuster50M(lcm, this);
	}
}