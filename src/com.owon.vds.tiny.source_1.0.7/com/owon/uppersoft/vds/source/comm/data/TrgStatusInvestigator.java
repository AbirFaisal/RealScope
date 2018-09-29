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
 * 获取触发状态、配合触发模式设置获取波形、更新显示状态
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

	/** 单例数组优化频繁使用 */
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

		/** 触发状态会根据通道的关闭置零对应的位 */
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
	 * @return -1代表出错，大于0代表完成
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
		/** 使用此时机作为触发状态的读取，决定是否获取数据 */
		ci.channel_trg_status = 0;
		// checkSingleOnce = CHEKC_SINGLE_NONE;

		TriggerControl tc = cm.getCoreControl().getTriggerControl();
		/** 指令发送完毕，扫频的控制由上位机进行 */
		int trg_status = getTrgStatus(ci, ism);
		if (trg_status < 0)
			return false;

		int datafinished = getDataFinishedStatus(ci, ism);
		// 笔式暂时读不到，所以注释；出于兼容旧机型的考虑，可以注释之，否则还是限制完成标志更恰当
		if (datafinished < 0)
			return false;

		// 还有强制触发等..

		int ciTrgStatus;

		ci.resetTrg_d();
		if (cm.getTimeControl().isOnSlowMoveTimebase()) {
			ciTrgStatus = TrgStatus.Scan.ordinal();
		} else if (tc.isSingleTrg()) {

			// 针对双通道特定的代码
			// int chl = tc.getSingleTrgChannel();
			// trg_status &= (1 << chl);

			if (tc.isSweepAuto()) {
				// mcu先判断完成标志，如果为真则发数据；发数据后fpga会清空完成标志
				// 反之再判断触发状态，无则强制触发，最后返回EBUSY

				/** 这里外部触发也可能触发到，而触发状态反应不出来，所以只有其非零就认为有触发 */
				if (trg_status != 0) {
					// (& lcm.getTrgChannles())
					ciTrgStatus = TrgStatus.Trg_d.ordinal();
				} else {
					ciTrgStatus = TrgStatus.Auto.ordinal();
				}
			} else if (tc.isSweepOnce()) {
				// 单次触发，发强制时触发的指令触发状态不会被设置
				ciTrgStatus = TrgStatus.Ready.ordinal();
				// System.out.println("trg_status: " + trg_status + ", "
				// + datafinished);
				if (trg_status == 0) {
					if (datafinished > 0) {
						// 无触发判断完成标志，真则为强制触发，可以拿数据
						ciTrgStatus = TrgStatus.Stop.ordinal();// 有触发就Stop
						// checkSingleOnce = CHEKC_SINGLE_ONCE;// 如有数据即有触发就Stop

						onSweepOutAsOnce();
					} else {
						ci.status = ChannelsTransportInfo_Tiny.Status_RT_OK;
						ci.setFrameCount(0);
						/** 使当前状态可以刷新 */
						ci.channel_trg_status = trg_status;
						ci.triggerStatus = ciTrgStatus;
						dealTrgStatus(ci);
						return false;
					}
				} else {
					// 有触发则跳过完成标志去拿数据
					ciTrgStatus = TrgStatus.Stop.ordinal();// 有触发就Stop

					// 在大于10ms的情况下，cpu搬移数据要比较久，可能出现ebusy的情况，需要持续读取直到有数据
					ci.fetchUntilData = true;
				}
			} else {
				ciTrgStatus = TrgStatus.Ready.ordinal();
				// Normal
				if (trg_status == 0) {
					if (datafinished > 0) {
						// 无触发判断完成标志，真则为强制触发，可以拿数据
						ciTrgStatus = TrgStatus.Trg_d.ordinal();// 总是Ready
						// checkSingleOnce = CHEKC_SINGLE_NORMAL;// 总是Ready
					} else {
						ci.status = ChannelsTransportInfo_Tiny.Status_RT_OK;
						ci.setFrameCount(0);
						/** 使当前状态可以刷新 */
						ci.channel_trg_status = trg_status;
						ci.triggerStatus = ciTrgStatus;
						dealTrgStatus(ci);
						return false;
					}
				} else {
					// 有触发则跳过完成标志去拿数据
					ciTrgStatus = TrgStatus.Trg_d.ordinal();// 总是Ready
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
		/** 恢复点单次触发的状态 */
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