package com.owon.uppersoft.vds.source.comm.data;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormInfoControl;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.source.comm.TrgStatus;
import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.core.comm.ICommunicateManager;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;
import com.owon.uppersoft.vds.device.interpret.DeviceAddressTable;
import com.owon.uppersoft.vds.device.interpret.TinyCommunicationProtocol;
import com.owon.uppersoft.vds.device.interpret.util.Sendable;
import com.owon.uppersoft.vds.source.comm.data.GetDataRunner2.DataReceiveHandler;
import com.owon.uppersoft.vds.util.ArrayLogger;
import com.owon.uppersoft.vds.util.format.EndianUtil;
import com.owon.vds.firm.protocol.AddressAttachCommand;

/**
 * Get the trigger status, get the waveform according to the trigger mode setting, and update the display status
 * 
 */
public class TrgStatusInvestigator {

	private VLog el = new VLog();
	private ControlManager cm;
	private WaveFormInfoControl wfic;
	private DeviceAddressTable table;
	private DataReceiveHandler drh;

	public TrgStatusInvestigator(ControlManager cm, DeviceAddressTable table,
			DataReceiveHandler drh) {
		this.cm = cm;
		this.drh = drh;

		this.table = table;

		wfic = cm.getWaveFormInfoControl();

		el.on = false;
	}

	/** singleton array, optimized for frequent use */
	private byte[] RESPONSE_BUF = new byte[TinyCommunicationProtocol.RESPONSE_LEN];

	protected int getTrgStatus(ChannelsTransportInfo_Tiny ci,
			ICommunicateManager ism) {// if(1==1)return
		// 0xff;
		AddressAttachCommand tda = table.TRG_D_ADD;
		el.logln(tda);
		int trg_status = 0;
		int wn = Sendable.writeCommmand(ism, tda, 0);
		if (wn <= 0) {
			ci.status = ChannelsTransportInfo.Status_RT_WriteContentErr;
			return -1;
		}
		byte[] rep = RESPONSE_BUF;
		int rn = ism.acceptResponse(rep, rep.length);
		if (rn <= 0) {
			ci.status = ChannelsTransportInfo.Status_RT_WriteContentErr;
			return -1;
		}

		ArrayLogger.outArray2Logable(el, rep, 0, rn);
		trg_status = EndianUtil.nextIntL(rep, 1);
		el.logln("trg_d: " + trg_status);

		/** the trigger status zeroes the corresponding bits according to channel closure */
		WaveFormInfo[] wfinfo = wfic.getWaveFormInfos();
		int chllen = wfinfo.length;
		for (int i = 0; i < chllen; i++) {
			if (!wfinfo[i].ci.isOn()) {
				trg_status &= ~(1 << i);
			}
		}

		return trg_status;
	}

	/**
	 * @param ci
	 * @param ism
	 * @return -1 means error, greater than 0 means completed
	 */
	protected int getDataFinishedStatus(ChannelsTransportInfo_Tiny ci,
			ICommunicateManager ism) {
		AddressAttachCommand tda = table.datafinished_ADD;
		el.logln(tda);
		int flag = 0;
		int wn = Sendable.writeCommmand(ism, tda, 0);
		if (wn <= 0) {
			ci.status = ChannelsTransportInfo.Status_RT_WriteContentErr;
			return -1;
		}
		byte[] rep = RESPONSE_BUF;
		int rn = ism.acceptResponse(rep, rep.length);
		if (rn <= 0) {
			ci.status = ChannelsTransportInfo.Status_RT_WriteContentErr;
			return -1;
		}

		ArrayLogger.outArray2Logable(el, rep, 0, rn);
		flag = EndianUtil.nextIntL(rep, 1);
		el.logln("datafinished: " + flag);

		return Math.abs(flag);
	}

	// private static final int CHEKC_SINGLE_ONCE = 1, CHEKC_SINGLE_NORMAL = 2,
	// CHEKC_SINGLE_NONE = 0;

	// private int checkSingleOnce = CHEKC_SINGLE_NONE;

	// private boolean click_force = false;

	public boolean checkStatusForGetDataT(ChannelsTransportInfo_Tiny ci,
			ICommunicateManager ism) {
		/** use this timing to read the trigger status and decide whether to fetch data */
		ci.channel_trg_status = 0;
		// checkSingleOnce = CHEKC_SINGLE_NONE;

		TriggerControl tc = cm.getCoreControl().getTriggerControl();
		/** commands are all sent; sweep control is done by the host */
		int trg_status = getTrgStatus(ci, ism);
		if (trg_status < 0)
			return false;

		int datafinished = getDataFinishedStatus(ci, ism);
		// the pen model cannot read it for now, so it is commented out; for compatibility with older models it can be commented out, otherwise restricting by the completion flag is more appropriate
		if (datafinished < 0)
			return false;

		// also force trigger, etc..

		int ciTrgStatus;

		ci.resetTrg_d();
		if (cm.getTimeControl().isOnSlowMoveTimebase()) {
			ciTrgStatus = TrgStatus.Scan.ordinal();
		} else if (tc.isSingleTrg()) {

			// code specific to dual channel
			// int chl = tc.getSingleTrgChannel();
			// trg_status &= (1 << chl);

			if (tc.isSweepAuto()) {
				// mcuthe mcu first checks the completion flag and sends data if it is true; after sending data the fpga clears the completion flag
				// otherwise it checks the trigger status, forces a trigger if there is none, and finally returns EBUSYEBUSY

				/** external trigger may also fire here while the trigger status does not reflect it, so any non-zero value is taken to mean there is a trigger */
				if (trg_status != 0) {
					// (& lcm.getTrgChannles())
					ciTrgStatus = TrgStatus.Trg_d.ordinal();
				} else {
					ciTrgStatus = TrgStatus.Auto.ordinal();
				}
			} else if (tc.isSweepOnce()) {
				// single trigger: when the force-trigger command is sent, the trigger status is not set
				ciTrgStatus = TrgStatus.Ready.ordinal();
				// System.out.println("trg_status: " + trg_status + ", "
				// + datafinished);
				if (trg_status == 0) {
					if (datafinished > 0) {
						// with no trigger, check the completion flag; if true it is a forced trigger and data can be fetched
						ciTrgStatus = TrgStatus.Stop.ordinal();// Stop if there is a triggerStop
						// checkSingleOnce = CHEKC_SINGLE_ONCE;// Stop if there is data, i.e. a triggerStop

						onSweepOutAsOnce();
					} else {
						ci.status = ChannelsTransportInfo_Tiny.Status_RT_OK;
						ci.setFrameCount(0);
						/** allow the current status to be refreshed */
						ci.channel_trg_status = trg_status;
						ci.triggerStatus = ciTrgStatus;
						dealTrgStatus(ci);
						return false;
					}
				} else {
					// if triggered, skip the completion flag and fetch data
					ciTrgStatus = TrgStatus.Stop.ordinal();// Stop if there is a triggerStop

					// above 10ms, the cpu takes a relatively long time to move data and ebusy may occur, so reading must continue until there is data
					ci.fetchUntilData = true;
				}
			} else {
				ciTrgStatus = TrgStatus.Ready.ordinal();
				// Normal
				if (trg_status == 0) {
					if (datafinished > 0) {
						// with no trigger, check the completion flag; if true it is a forced trigger and data can be fetched
						ciTrgStatus = TrgStatus.Trg_d.ordinal();// always ReadyReady
						// checkSingleOnce = CHEKC_SINGLE_NORMAL;// always ReadyReady
					} else {
						ci.status = ChannelsTransportInfo_Tiny.Status_RT_OK;
						ci.setFrameCount(0);
						/** allow the current status to be refreshed */
						ci.channel_trg_status = trg_status;
						ci.triggerStatus = ciTrgStatus;
						dealTrgStatus(ci);
						return false;
					}
				} else {
					// if triggered, skip the completion flag and fetch data
					ciTrgStatus = TrgStatus.Trg_d.ordinal();// always ReadyReady
				}

			}
			boolean ext = tc.isOnExtTrgMode();
			if (ext)
				trg_status = 0;
		} else {
			if (trg_status != 0) {
				ciTrgStatus = TrgStatus.Trg_d.ordinal();
			} else {
				ciTrgStatus = TrgStatus.Auto.ordinal();
			}
		}
		ci.channel_trg_status = trg_status;
		el.logln("trg_status: " + trg_status);

		ci.triggerStatus = ciTrgStatus;

		return true;
	}

	protected void onSweepOutAsOnce() {
		drh.onSweepOutAsOnce();
	}

	public void dealTrgStatus(ChannelsTransportInfo_Tiny ci) {
		int trgs = ci.triggerStatus;
		TrgStatus ts = TrgStatus.Scan;

		boolean flag = (trgs < TrgStatus.VALUES.length && trgs >= 0);
		if (!flag) {
			ci.triggerStatus = trgs = TrgStatus.Error.ordinal();
		}

		ts = TrgStatus.VALUES[trgs];
		// logln(ci.dataComplete + "; " + ts);

		Platform.getMainWindow().updateStatus(ts);
	}

	public void onDataReceive(ChannelsTransportInfo_Tiny ci) {
		/** restore the state after clicking single trigger */
		// if (checkSingleOnce == CHEKC_SINGLE_ONCE) {
		// ci.triggerStatus = TrgStatus.Stop.ordinal();
		// setClickForce(false);
		// } else if (checkSingleOnce == CHEKC_SINGLE_NORMAL) {
		// ci.triggerStatus = TrgStatus.Ready.ordinal();
		// setClickForce(false);
		// }
	}

	// public boolean isClickForce() {
	// return click_force;
	// }
	//
	// public void setClickForce(boolean click_force) {
	// this.click_force = click_force;
	// }
}