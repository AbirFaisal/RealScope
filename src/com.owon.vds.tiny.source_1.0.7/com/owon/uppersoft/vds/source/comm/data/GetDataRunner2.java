package com.owon.uppersoft.vds.source.comm.data;

import static com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo.Status_RT_ConnectErr;
import static com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo.Status_RT_OK;
import static com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo.Status_RT_UnknownErr;
import static com.owon.uppersoft.vds.source.front.AbsPreHandler.ReceiveSize;
import static com.owon.uppersoft.vds.source.front.AbsPreHandler.TRANBUFLEN_ALLDM;
import static com.owon.uppersoft.vds.source.front.AbsPreHandler.TRANBUFLEN_TRG;
import static com.owon.uppersoft.vds.source.front.AbsPreHandler.TRANBUFLOC_TRGBEG;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

import ch.ntb.usb.USBException;

import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.model.WaveFormInfoControl;
import com.owon.uppersoft.dso.source.comm.AbsGetDataRunner;
import com.owon.uppersoft.dso.source.usb.USBSourceManager;
import com.owon.uppersoft.vds.core.aspect.base.EchoLogger;
import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.core.comm.ICommunicateManager;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelsTransportInfo;
import com.owon.uppersoft.vds.device.interpret.util.Sendable;
import com.owon.uppersoft.vds.machine.TinyMachine;
import com.owon.uppersoft.vds.source.comm.SourceManagerTiny;
import com.owon.uppersoft.vds.source.front.AbsPreHandler;
import com.owon.uppersoft.vds.util.ArrayLogger;
import com.owon.vds.firm.protocol.AddValueAttachCommand;

public class GetDataRunner2 extends AbsGetDataRunner {

	interface Source {
		int acceptResponse(byte[] firstReceiveBuf, int receivesize);
	}

	public interface DataReceiveHandler {
		void onReceive();

		void onSweepOutAsOnce();
	}

	private AbsPreHandler ph;
	private ChannelsTransportInfo_Tiny ci;

	private Source src;

	@Override
	public ChannelsTransportInfo_Tiny getChannelsTransportInfo() {
		return ci;
	}

	public ValueFeeder getValueFeeder() {
		return vf;
	}

	private DataReceiveHandler dataReceiveListener;
	private WaveFormInfoControl wfic;

	public static AddValueAttachCommand GETDATA_ADD = new AddValueAttachCommand(
			"GETDATA_ADD", 0x1000, 2, 0);

	private TinyMachine tm;

	/**
	 * @param dh
	 * @param ism
	 * @param ph
	 *            为空则不处理
	 * @param dataReceiveListener
	 *            为空则不处理
	 * @param tm
	 *            为空则不处理
	 */
	public GetDataRunner2(DataHouse dh, final ICommunicateManager ism,
			AbsPreHandler ph, DataReceiveHandler dataReceiveListener,
			TinyMachine tm) {
		super(dh, ism);
		this.dataReceiveListener = dataReceiveListener;
		this.tm = tm;
		this.ph = ph;

		wfic = cm.getWaveFormInfoControl();

		adcm = new ADCMemory(cm.getAllChannelsNumber(), ReceiveSize);

		ci = new ChannelsTransportInfo_Tiny();

		vl.on = false;
		echo_vl.on = false;

		si = new TrgStatusInvestigator(cm, tm.getDeviceAddressTable(),
				dataReceiveListener);

		src = new Source() {
			public int acceptResponse(byte[] firstReceiveBuf, int receivesize) {
				SourceManagerTiny smt = (SourceManagerTiny) ism;
				if (smt.isUSBConnect()) {
					USBSourceManager usbsm = smt.getUSBSourceManager();
					return usbsm.acceptResponse(firstReceiveBuf, receivesize,
							timeout);
				} else {
					return smt.getChoice().acceptResponse(firstReceiveBuf,
							receivesize);
				}
			}
		};

		vf = new ValueFeeder(tm.getDeviceAddressTable().channel_freqref);
	}

	protected ChannelsTransportInfo getData() {
		if (si.checkStatusForGetDataT(ci, ism)) {

			if (ci.fetchUntilData) {
				// 在单次触发且有触发的情况下，持续尝试读取数据，跳过每次的EBUSY情况，直到读到数据
				do {
					routineGetData(GETDATA_ADD);
				} while (ci.getFrameCount() <= 0);
				ci.fetchUntilData = false;
			} else
				routineGetData(GETDATA_ADD);

			// 获取多种数据存入 ci(单纯拿数据)，接收复合指令数据 return 数据中的通道信息（下位机根据收到的指令 返回相应数据）
			boolean ffton = cm.getFFTControl().isFFTon();
			if (ci.status == ChannelsTransportInfo.Status_RT_OK
					&& ci.getFrameCount() > 0) {
				vl.logln("ok");
				handleGroundNInverse(ci);
				if (ffton) {
					/** 在fft时，保持和原来一样的时基档位(分频系数)，则时域波形的画图不变，频域fft计算取出2048个点 */
					if (ph != null)
						ph.preLoadReceiveData(ci);
					// 避免在100ms以上档位fft碰上慢扫的一些限制
					ci.slowMove = 0;
				} else {
					if (ph != null)
						ph.preLoadReceiveData(ci);
					handleAverage(ci);
				}

				dh.receiveRTData(ci);

				/** 反馈中间值给硬件频率计 */
				vf.syncChannelsMiddle(ism, dh.getWaveFormManager()
						.on_wf_Iterator());
				if (dataReceiveListener != null)
					dataReceiveListener.onReceive();
				checkTrgEdgeMiddle();
			} else {
				vl.logln("bad");
			}
			// 载入波形datahouse 画图用的
		}
		return ci;
	}

	protected Source getSource() {
		return src;
	}

	private void routineGetData(AddValueAttachCommand gda) {
		// cm.pcs.firePropertyChange(Tune2.APPEND_TXTLINE2, null,
		// ((Submitor2) (SubmitorFactory.getSubmitable())).getLogTrg());

		// vl.logln("Loop: " + ct);
		// vl.logln(Integer.toHexString(chlflag));
		WaveFormInfo[] wfinfo = wfic.getWaveFormInfos();
		int chlflag = 0;
		int len = wfinfo.length;
		for (int i = len - 1, v; i >= 0; i--) {
			v = wfinfo[i].ci.isOn() ? 5 : 4;
			chlflag = (chlflag << 8) | v;
		}

		gda.value = chlflag;
		int wn = Sendable.writeCommmand(ism, gda);
		// vl.logln("wn: " + wn);
		if (wn <= 0) {
			ci.status = ChannelsTransportInfo.Status_RT_WriteContentErr;
		}

		Source src = getSource();

		int ons = wfic.getOnWaveFormNumber();
		// vl.logln("ons: " + ons);

		acceptNMData(ci, ons, src);
		updateFrame(1);
		si.dealTrgStatus(ci);
	}

	private ValueFeeder vf;

	private TrgStatusInvestigator si;

	public static final int timeout = 50;

	public static final int DefaultNoDataTimes = 15;
	private int noDataTimes = 0;

	private ADCMemory adcm;

	private void acceptNMData(ChannelsTransportInfo_Tiny ci, int chlnum,
			Source usbs) {
		int rn;
		ci.status = Status_RT_UnknownErr;
		// PropertyChangeListener pcl = ci.pcl;
		try {
			vl.logln("---------------:");

			// long t1 = Calendar.getInstance().getTimeInMillis();

			byte[] firstReceiveBuf = adcm.firstReceiveBuf;
			byte[][] usbbufs = adcm.usbbufs;

			rn = usbs.acceptResponse(firstReceiveBuf, ReceiveSize);
			vl.logln("receive 1st: " + rn);

			if (firstReceiveBuf[0] == 'E') {
				String ebusy = new String(firstReceiveBuf, 0, 5);
				vl.logln("receive :" + ebusy);
				ci.status = Status_RT_OK;
				ci.setFrameCount(0);
				noDataTimes++;
				return;
			} else if (rn < ReceiveSize) {
				// ArrayLogger.outArray2Logable(vl, firstReceiveBuf, 0, 5);
				ci.status = Status_RT_OK;
				ci.setFrameCount(0);
				noDataTimes++;
				return;
			}
			noDataTimes = 0;

			si.onDataReceive(ci);

			ci.reset('T');
			/** 待到真的有通道数据时，再清空上一幅，这样在深存储的时候才始终有上次的内容 */
			ci.gatherEnoughChannelDataInfos(chlnum);

			ci.setDataComplete(1);

			Iterator<ChannelDataInfo_Tiny> cdit_pool_itr = ci
					.iterator_ChannelDataInfo();

			// System.arraycopy(firstReceiveBuf, 0, usbbuf, 0,
			// firstReceiveBuf.length);

			adcm.firstReceiveBuf = usbbufs[0];
			byte[] usbbuf = usbbufs[0] = firstReceiveBuf;

			int chl_pool_idx = 0;

			ByteBuffer trgbuf = ByteBuffer.wrap(usbbuf, TRANBUFLOC_TRGBEG,
					TRANBUFLEN_TRG).order(ByteOrder.LITTLE_ENDIAN);

			ByteBuffer alldmbuf = ByteBuffer.wrap(usbbuf, trgbuf.limit(),// TRANBUFLOC_DMBEG,
					TRANBUFLEN_ALLDM).order(ByteOrder.LITTLE_ENDIAN);

			acceptChannelDataT2(cdit_pool_itr.next(), chl_pool_idx, alldmbuf,
					trgbuf);
			// pcl.propertyChange(new PropertyChangeEvent(this,
			// PropertiesItem.CHLNEXT_DONE, 0, 0));

			chl_pool_idx++;

			while (chl_pool_idx < chlnum) {
				usbbuf = usbbufs[chl_pool_idx];
				rn = usbs.acceptResponse(usbbuf, ReceiveSize);
				vl.logln("receive next: " + rn);
				if (rn < ReceiveSize) {
					throw new Exception("receive size < " + ReceiveSize
							+ ", on pool_idx" + chl_pool_idx);
				}

				trgbuf = ByteBuffer.wrap(usbbuf, TRANBUFLOC_TRGBEG,
						TRANBUFLEN_TRG).order(ByteOrder.LITTLE_ENDIAN);

				alldmbuf = ByteBuffer.wrap(usbbuf, trgbuf.limit(),// TRANBUFLOC_DMBEG,
						TRANBUFLEN_ALLDM).order(ByteOrder.LITTLE_ENDIAN);

				acceptChannelDataT2(cdit_pool_itr.next(), chl_pool_idx,
						alldmbuf, trgbuf);
				// pcl.propertyChange(new PropertyChangeEvent(this,
				// PropertiesItem.CHLNEXT_DONE, 0, cnt));

				chl_pool_idx++;
			}
			// pcl.propertyChange(new PropertyChangeEvent(this,
			// PropertiesItem.TRANS_DONE, 0, chlnum));
			vl.logln("*********************!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			ci.status = Status_RT_OK;
			ci.setFrameCount(1);
			return;
		} catch (USBException e) {
			ci.status = Status_RT_ConnectErr;
			vl.logln(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			vl.logln(e.getMessage());
			e.printStackTrace();
		}
		// pcl.propertyChange(new PropertyChangeEvent(this,
		// PropertiesItem.TRANS_FAIL, null, null));
		return;
	}

	protected void acceptChannelDataT2(ChannelDataInfo_Tiny cdi, int idx,
			ByteBuffer alldmbuf, ByteBuffer trgbuf) throws USBException {
		/** 这里对alldmbuf中取值必须使用get(index)，不破坏当前的position位置 */
		int ptr = 0;
		int v = alldmbuf.get(ptr);
		ptr++;
		// logln("v: " + v);

		cdi.chl = v;
		vl.logln("cdi.chl: " + v);
		/** 更新有效的触发状态 */
		boolean trg_d = cdi.trg_d = ((ci.channel_trg_status >>> v & 1) != 0);

		int time_sum = alldmbuf.getInt(ptr);
		ptr += 4;
		int peroid_num = alldmbuf.getInt(ptr);

		vl.logln(v + "trg_d: " + (trg_d ? 1 : 0) + " time_sum: " + time_sum
				+ ", peroid_num: " + peroid_num);
		echo_vl.logln("ch" + (v + 1));

		float freq;
		if (!trg_d || time_sum <= 0 || peroid_num <= 0 || tm == null) {
			freq = -1;
		} else {
			freq = (float) tm.doFrequencyCompute(peroid_num, time_sum);
			// (float) (100000000 / (double) time_sum * peroid_num);
			// 10ns * time_sum / peroid_num * 10E8
			vl.logln("freq: " + freq);
		}
		/** 当有触发时硬件频率计数值才有效 */
		cdi.setFreq(freq);

		ptr += 4;
		int slow = alldmbuf.getShort(ptr);
		ptr += 2;
		vl.logln("slow number: " + slow);

		cdi.slowMove = Math.abs(slow);

		// vl.logByteBuffer(bb);
		// cdi.setUniqueAdcbuf(alldmbuf);
		cdi.resetPi();
		/** 原本把raw也放在uniquebuf里，后来改动为不影响原来的，只是多保存在rawbuf里 */
		cdi.setAllDMBuffer(alldmbuf);

		echo_vl.logln("screen center data:");
		ArrayLogger.outArray2Logable(echo_vl, alldmbuf.array(),
				alldmbuf.position() + 2500, 100);

		cdi.setTrgLocBuffer(trgbuf);

		echo_vl.logln("pull trg data:");
		ArrayLogger.outArray2Logable(echo_vl, trgbuf.array(),
				trgbuf.position(), trgbuf.remaining());
	}

	private EchoLogger echo_vl = new EchoLogger();// Logable new EchoLogger();//

	private VLog vl = new VLog();

}