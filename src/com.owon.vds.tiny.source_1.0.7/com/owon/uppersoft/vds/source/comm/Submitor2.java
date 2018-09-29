package com.owon.uppersoft.vds.source.comm;

import java.beans.PropertyChangeSupport;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.trigger.TriggerDefine;
import com.owon.uppersoft.dso.source.comm.InfiniteDaemon;
import com.owon.uppersoft.dso.source.comm.TrgStatus;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.core.comm.BufferredSourceManager;
import com.owon.uppersoft.vds.core.comm.JobQueueDispatcher;
import com.owon.uppersoft.vds.core.comm.effect.AbsSubmitor;
import com.owon.uppersoft.vds.core.comm.effect.P_Channel;
import com.owon.uppersoft.vds.core.comm.job.JobUnit_I;
import com.owon.uppersoft.vds.core.comm.job.JobUnit_LightWeight;
import com.owon.uppersoft.vds.device.interpret.CMDResponser;
import com.owon.uppersoft.vds.device.interpret.DeviceAddressTable;
import com.owon.uppersoft.vds.device.interpret.LowerTranslator;
import com.owon.uppersoft.vds.device.interpret.TinyCommunicationProtocol;
import com.owon.uppersoft.vds.device.interpret.util.DefaultCMDResponser;
import com.owon.uppersoft.vds.device.interpret.util.Sendable;
import com.owon.uppersoft.vds.machine.LowControlManger;
import com.owon.uppersoft.vds.machine.TinyMachine;
import com.owon.uppersoft.vds.source.comm.ext.ClockTimeAdjuster;
import com.owon.uppersoft.vds.source.comm.ext.IClockTimeAdjuster;
import com.owon.uppersoft.vds.source.comm.ext.IntObject;
import com.owon.uppersoft.vds.util.PrimaryTypeUtil;
import com.owon.vds.firm.protocol.AddValueAttachCommand;
import com.owon.vds.firm.protocol.AddressAttachCommand;

/**
 * Submitor2，提供可支持的指令api
 * 
 */
public class Submitor2 extends AbsSubmitor implements
		TinyCommunicationProtocol, Logable {

	private static final int VB_500mV = 6;

	private Sendable sd;
	private LowerTranslator pref_tran;
	protected LowControlManger lcm;
	private PropertyChangeSupport pcs;
	protected DeviceAddressTable table;

	public Submitor2(JobQueueDispatcher jqd, LowerTranslator tran,
			LowControlManger lcm, ControlManager cm) {
		super(jqd);
		this.pref_tran = tran;
		this.lcm = lcm;
		this.pcs = cm.pcs;

		table = lcm.getMachineType().getDeviceAddressTable();

		sd = new Sendable(this, this);// new VLog()

		cta = createClockTimeAdjuster(lcm);
		VBrelay = VB_500mV;// 500mV的索引值
	}

	private int insideVB = -1;
	private int VBrelay;

	@Override
	public void d_chl_pos0(int chl, int pos0, final Runnable r) {
		int d = pos0;
		wrPos0(chl, d);
		addJobUnit(new JobUnit_LightWeight() {
			@Override
			public void doJob(BufferredSourceManager sm) {
				if (r != null)
					r.run();
			}
		});
	}

	@Override
	public void d_chl_vb(int chl, int vbIndex, final Runnable r) {
		int vb = vbIndex;
		wrChannel(chl, vb);
		wrVoltbase(chl, vb);
		wrPos0(chl);
		addJobUnit(new JobUnit_LightWeight() {
			@Override
			public void doJob(BufferredSourceManager sm) {
				if (r != null)
					r.run();
			}
		});
	}

	@Override
	public void c_chl(final P_Channel mode, final int chl, final int... arr) {
		switch (mode) {
		case ONOFF:
			/** 信道开关 */
			wrChannel_onoff(chl);
			break;
		case VB: {
			int vb = arr[0];
			// 由于电压档位改变可能引起通道前端衰减变化，所以也需要同步通道设置
			wrChannel(chl, vb);
			wrVoltbase(chl, vb);
			//
			wrPos0(chl);
			break;
		}
		case POS0: {
			int d = arr[0];
			wrPos0(chl, d);
			break;
		}
		case COUPLING: {
			wrChannel(chl, lcm.getChannelVBIndex(chl));
			break;
		}
		default:
		}
	}

	protected void afterWrVoltbase(int vb) {
	}

	public void wrVoltbase(final int chl, int vb) {
		int v = pref_tran.translate2VBValue(chl, vb);
		// System.out.println("vb: " + chl + ", " + v);
		sendCMD(table.volt_gain[chl], v);

		int oldvb = insideVB;
		insideVB = vb;

		afterWrVoltbase(vb);

		/** 在引起继电器切换的电压档位调节时作延时 */
		if (oldvb >= 0 && (oldvb >= VBrelay && vb < VBrelay)
				|| (oldvb < VBrelay && vb >= VBrelay)) {
			addJobUnit(new JobUnit_LightWeight() {
				@Override
				public void doJob(BufferredSourceManager sm) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
		}

	}

	public int wrPos0(final int chl) {
		return wrPos0(chl, lcm.getChannelPos0(chl));
	}

	private int tempV1, tempV2;

	public int wrPos0(final int chl, int pos0) {
		int v = pref_tran.translate2PosValue(chl, pos0,
				lcm.getChannelVBIndex(chl));
		if (pos0 > 225 || pos0 < -225) {
			v = v + 1;
		}
		// if (chl == 0) {
		// tempV1 = v;
		// sendCMD(table.zero_offset[1], tempV2);
		// System.out.println("pos0: " + 1 + ", " + tempV2);
		// } else {
		// tempV2 = v;
		// sendCMD(table.zero_offset[0], tempV1);
		// System.out.println("pos0: " + 0 + ", " + tempV1);
		// }

		sendCMD(table.zero_offset[chl], v);
		// System.out.println("pos0: " + chl + ", " + v);

		return v;
	}

	protected void wrChannel(int chl, int vb) {
		int v = lcm.getChannelsSetFlag(chl, vb);

		// String t = PrimaryTypeUtil.toBytesString_2(v);
		// System.err.println("chl: " + chl + ", " +t);
		// System.out.println("chl: " + chl + ", " + v);
		sendCMD(table.channel_set[chl], v);
	}

	protected void wrChannel_onoff(int chl) {
		int chlflag = lcm.getChannelsFlag();
		sendCMD(table.CHL_ON_ADD, chlflag);

		wrChannel(chl, lcm.getChannelVBIndex(chl));
		wrEmpty();
		// syncPK();
	}

	public void wrTrg(int v) {
		logln("trg set 16 bits:");
		dblogBytes_2(v);
		sendCMD(table.TRG_ADD, v);
	}

	public void wrEmpty() {
		logln("wrEmpty");
		sendCMD(table.EMPTY_ADD, 0);
	}

	@Override
	public void apply_trg() {
	}

	@Override
	public void apply_trgThen(Runnable r) {
	}

	@Override
	public void apply_trgSweep(int sweepIndex) {
		/** 指令发送完毕，扫频的控制由上位机进行，这里预留，实际的判断可以放到gdr */
		switch (sweepIndex) {
		case TriggerDefine.TrigSweepAutoIndex:
			break;
		case TriggerDefine.TrigNormalIndex:
			break;
		case TriggerDefine.TrigOnceIndex:
			break;
		default:
			break;
		}
	}

	@Override
	public void apply() {
	}

	@Override
	public void applyThen(Runnable r) {
	}

	@Override
	public void c_dm(int deepIdx) {
		sendCMD(table.DM_ADD, lcm.getDeepMemoryDataLen());

		syncPK();
	}

	@Override
	public void c_fft(boolean ffton, int fftchl) {
		/** 通道开关及分频系数其它地方设置，这里只改变采集深度 */
		// c_dm(lcm.getDeepMemoryIndex());
		syncPK();
	}

	private int beforeTrg, afterTrg;

	public String getLogTrg() {
		return "beforeTrg(0x5a): " + beforeTrg + " ;" + "afterTrg(0x5b): "
				+ afterTrg;
	}

	@Override
	public void c_htp(int htp) {
		int dmlen = lcm.getDeepMemoryDataLen();
		int half = dmlen >> 1;
		IntObject io = new IntObject();
		int v = lcm.getHTPWithoutCompress(htp, io);
		// System.err.println(v);
		beforeTrg = half - v;

		/** 预触发数据超出存储的做法，预触发设置为0，过触发设置为预触发的绝对值加上存储深度 */
		if (beforeTrg < 0) {
			afterTrg = dmlen - beforeTrg;
			beforeTrg = 0;
		} else {
			afterTrg = half + v;
		}
		// System.err.println(beforeTrg+", "+afterTrg);
		/** 触发前数据 */
		pre_trg_add(beforeTrg);
		/** 触发后数据 */
		suf_trg_add(afterTrg);
		final int iov = io.value;
		sendCMD(table.EMPTY_ADD, 1, new DefaultCMDResponser() {
			@Override
			protected void handleResponse(int res) {
				// if (0 == 1) {
				// try {
				// Thread.sleep(200);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
				// }
				lcm.commitRemainHtp(iov);
			}
		});
	}

	protected void pre_trg_add(int beforeTrg) {
		sendCMDbyBytes(table.PRE_TRG_ADD, beforeTrg);
	}

	protected void suf_trg_add(int afterTrg) {
		sendCMDbyBytes(table.SUF_TRG_ADD, afterTrg);
	}

	@Override
	public void c_network_off() {
	}

	@Override
	public void c_network(boolean netOn, byte[] ipaddress, int port,
			byte[] gwaddress, byte[] smaddress, byte[] macaddress) {
	}

	private boolean select_pk;

	@Override
	public void c_sample(int modelIdx) {
		select_pk = (modelIdx == 1);
		syncPK();
	}

	protected void syncPK() {
		int v = 0;
		if (select_pk) {
			if (lcm.alsoSupportPeakDetect()) {
				v = 1;
			}
		}
		sendCMD(table.SAMPLE_ADD, v);
	}

	private static final int[] sync_out = { 2, 0, 1 };

	@Override
	public void c_sync_output(int sync_output) {
		// System.err.println("dd: " + sync_output);

		if (lcm.getMachineType().isMultiIOSupport())
			sendCMD(table.SYNCOUTPUT_ADD, sync_out[sync_output]);
	}

	@Override
	public void c_tb_htp(int tb, int htp) {
		int v;
		int slowmoveMode;

		boolean slow = false;
		if (lcm.isFFTOn()) {
			v = lcm.getFFTSeperateFreq(tb);
			htp = 0;
			slowmoveMode = 0;
		} else {
			v = lcm.getSeperateFreq(tb);
			slow = lcm.isSlowMoveTimebase(tb);
			slowmoveMode = slow ? 1 : 0;
		}

		TinyMachine tm = lcm.getMachineType();
		if (tm.isSupportMultiFreqArgument()) {
			int vv = tm.getMultiFreqArgument(tb);
			sendCMD(table.MULTIFREQ_ADD, vv);
			// System.out.println("ddddddddddd: " + vv);
		}

		sendCMD(table.TIMEBASE_ADD, v);
		syncPK();
		sendCMD(table.SLOWMOVE_ADD, slowmoveMode);

		c_htp(htp);
	}

	protected IClockTimeAdjuster cta;

	protected IClockTimeAdjuster createClockTimeAdjuster(LowControlManger lcm) {
		return new ClockTimeAdjuster(lcm, this);
	}

	public IClockTimeAdjuster getClockTimeAdjuster() {
		return cta;
	}

	@Override
	public void c_trg_edge(int mode, int chl, byte item, int... arr) {
		// System.out.println("trg_cur_chl: "+trg_cur_chl);
		int pcc = chl;
		switch (item) {
		case trg_voltsense:
			// logln(arr[0]+","+ arr[1]);
			sendCMDbyBytes(table.edge_level[pcc],
					pref_tran.getLevelArg(arr[0], arr[1]));
			break;
		case trg_edge_holdoff:
			cta.c_trg_holdoffArg(chl, arr[1], arr[2]);
			break;
		default:
		}
	}

	@Override
	public void c_trg_pulse(int mode, int chl, byte item, int... arr) {
		IClockTimeAdjuster cta = this.cta;
		int pcc = chl;
		switch (item) {
		case trg_pulse_condition:
			cta.c_trg_condtionArg(chl, arr[0], arr[2], arr[3]);
			break;
		case trg_voltsense:
			sendCMD(table.pulse_level[pcc],
					pref_tran.getLevelArg(arr[0], arr[1]));
			break;
		case trg_pulse_holdoff:
			cta.c_trg_holdoffArg(chl, arr[1], arr[2]);
			break;
		default:
		}
	}

	@Override
	public void c_trg_slope(int mode, int chl, byte item, int... arr) {
		IClockTimeAdjuster cta = this.cta;
		int pcc = chl;
		switch (item) {
		case trg_slope_condition:
			cta.c_trg_condtionArg(chl, arr[0], arr[2], arr[3]);
			break;
		case trg_slope_uppest:
		case trg_slope_lowest:
			sendCMD(table.slope_thredshold[pcc], lcm.getThredsholdPairArg(pcc));
			break;
		case trg_slope_holdoff:
			cta.c_trg_holdoffArg(chl, arr[1], arr[2]);
			break;
		default:
		}
	}

	@Override
	public void c_trg_video(int mode, int chl, byte item, int... arr) {
		IClockTimeAdjuster cta = this.cta;
		switch (item) {
		case trg_video_holdoff:
			cta.c_trg_holdoffArg(chl, arr[1], arr[2]);
			break;
		case trg_video_sync:
			int syncValue = arr[1];
			sendCMD(table.VIDEOLINE_ADD, syncValue << 6);
			break;
		default:
		}
	}

	@Override
	public void forceTrg() {
		// /** 不能在指令发送后再改变强制状态，因为停止后的指令会累积到下次运行时，那时已经运行起来了难以改变；故而要及时更改强制状态 */
		// GetDataRunner2 gdr2 = (GetDataRunner2)
		// lcm.getControlApps().getDaemon()
		// .getGetDataRunner();
		// gdr2.forceTrgOnce();
		sendCMD(table.FORCETRG_ADD, lcm.getTrgChannles(),
				new DefaultCMDResponser() {
					@Override
					protected void handleResponse(int res) {
					}
				});
	}

	@Override
	public void sendRun() {
		// /** 恢复到运行未强制的状态 */
		// GetDataRunner2 gdr2 = (GetDataRunner2)
		// lcm.getControlApps().getDaemon()
		// .getGetDataRunner();
		// gdr2.resetforceTrgOnce();

		sendCMD(table.RUNSTOP_ADD, 0);
	}

	@Override
	public void sendStopThen(final Runnable r) {
		sendCMD(table.RUNSTOP_ADD, 1, new DefaultCMDResponser() {
			@Override
			protected void handleResponse(int res) {
				// try {
				// Thread.sleep(200);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
				checkStop();
			}
		});
	}

	private void checkStop() {
		JobUnit_I jui = sd.prepareCMD(table.CHECK_STOP_ADD, 1,
				"loop-check stop?", new CheckStopPCL());
		// 暂时用默认的pcl，可以打印应答
		jui.doJob(lcm.getSourceManager());
	}

	/**
	 * 如何停止；如何单次、正常触发，如何相应停下，如何恢复运行
	 */
	private final class CheckStopPCL extends DefaultCMDResponser {
		@Override
		protected void handleResponse(int res) {
			boolean b = res != 0;
			if (b) {
				onCheckOutAsStop();
			} else {
				checkStop();
			}
		}
	}

	protected void onCheckOutAsStop() {
		InfiniteDaemon id = lcm.getControlApps().getDaemon();

		/** 主动改变触发状态为stop */
		TrgStatus ts = TrgStatus.Stop;
		id.getChannelsTransportInfo().triggerStatus = ts.ordinal();
		Platform.getMainWindow().updateStatus(ts);

		id.realStop();
	}

	@Override
	public void sync_pf(int pf) {
		sendCMD(table.PF_ADD, pf);
	}

	private void dblogBytes_2(int v) {
		logln(PrimaryTypeUtil.toBytesString_2(v));
	}

	VLog vl = new VLog();

	public void logln(Object o) {
		vl.logln(o);
		// pcs.firePropertyChange(PropertiesItem.APPEND_TXTLINE, null, o);
	}

	@Override
	public void log(Object o) {
		vl.log(o);
		// pcs.firePropertyChange(PropertiesItem.APPEND_TXT, null, o);
	}

	private DefaultCMDResponser pcl = new DefaultCMDResponser();

	@Deprecated
	public void sendCMD(int add, int bytes, int value) {
		sd.sendCMD(add, bytes, value, pcl);
	}

	@Deprecated
	private void EMIFWriteData(int add, int value, int bytes) {
		sendCMD(add, bytes, value);
	}

	@Deprecated
	private void sendCMD2(int bytes, int value, CMDResponser pcl) {
		sd.sendCMD2(bytes, value, pcl);
	}

	public void sendCMD(AddValueAttachCommand aac, CMDResponser pcl) {
		sd.sendCMD(aac, pcl);
	}

	public void sendCMD(AddressAttachCommand aac, int value, CMDResponser pcl) {
		sd.sendCMD(aac, value, pcl);
	}

	public void sendCMDbyBytes(AddressAttachCommand tca, int value,
			CMDResponser pcl) {
		sd.sendCMDbyBytes(tca, value, pcl);
	}

	public void sendCMDbyBytes(AddressAttachCommand tca, int value) {
		sd.sendCMDbyBytes(tca, value, pcl);
	}

	public void sendCMD(AddressAttachCommand aac, int value) {
		sd.sendCMD(aac, value, pcl);
	}
}
