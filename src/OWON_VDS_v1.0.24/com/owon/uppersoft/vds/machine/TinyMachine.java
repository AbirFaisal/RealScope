package com.owon.uppersoft.vds.machine;

import static com.owon.uppersoft.vds.core.wf.peak.PKDetectDrawUtil.PK_DETECT_TYPE_1K_MINMAX_2PIX;
import static com.owon.uppersoft.vds.core.wf.peak.PKDetectDrawUtil.PK_DETECT_TYPE_2K_MINMAX_1PIX;
import static com.owon.uppersoft.vds.core.wf.peak.PKDetectDrawUtil.PK_DETECT_TYPE_NO;

import java.awt.Window;
import java.math.BigDecimal;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.model.machine.AbsMacType;
import com.owon.uppersoft.dso.source.comm.InfiniteDaemon;
import com.owon.uppersoft.dso.source.usb.USBPortsFilter;
import com.owon.uppersoft.vds.core.comm.JobQueueDispatcher;
import com.owon.uppersoft.vds.core.paint.IPaintOne;
import com.owon.uppersoft.vds.core.rt.WFDrawRTUtil;
import com.owon.uppersoft.vds.device.interpret.DeviceAddressTable;
import com.owon.uppersoft.vds.device.interpret.LowerTranslator;
import com.owon.uppersoft.vds.draw.PaintOne_Tiny;
import com.owon.uppersoft.vds.source.comm.Submitor2;
import com.owon.uppersoft.vds.source.front.AbsPreHandler;
import com.owon.vds.calibration.stuff.ArgCreator;
import com.owon.vds.calibration.stuff.CalArgTypeProvider;
import com.owon.vds.tiny.tune.TinyTuneFunction;

public abstract class TinyMachine extends AbsMacType {

	// public static final int FreqRef_100M = 100000000;
	// public static final int FreqRef_50M = 50000000;

	public abstract DeviceAddressTable getDeviceAddressTable();

	/**
	 * 根据base，ref，计算出浮点型的硬件频率计数
	 * 
	 * @param base
	 * @param ref
	 * @return
	 */
	public abstract double doFrequencyCompute(int base, int ref);

	public void showBallControl(InfiniteDaemon id, Window wnd) {
	}

	@Override
	public boolean isSupportNetwork() {
		return false;
	}

	@Override
	public double doHandleFrequencyCompute(int base, int ref,
			BigDecimal sampleRate_kHz) {
		return doFrequencyCompute(base, ref);
	}

	public TinyTuneFunction createTinyTuneFunction(int channelsNumber, int[] vbs) {
		return new TinyTuneFunction(channelsNumber, vbs);
	}

	@Override
	public final boolean passSlowOnNoneSlowTimebase() {
		return false;
	}

	@Override
	public boolean isTrgEdgeMiddleSupport() {
		return true;
	}

	public int getPKType(boolean dm, int drawMode) {
		return getPKType(drawMode);
	}

	/** 以上方法为临时添加，快速无效 */
	public final int getPKType(int drawMode) {
		int pk_detect_type = PK_DETECT_TYPE_NO;
		/**
		 * 峰值检测在点模式画图时是普通画法，这就决定了adc数组不能针对峰值检测做特别更改
		 * 
		 * 同时也是性能和维护的方便，故而这里只是简单转换到屏幕坐标
		 * 
		 * 开/关峰值检测的情况下，adc采到的屏幕范围内的点数是相同的，只是采集的方式不同，
		 * 
		 * 即便深存储也不用针对开/关峰值检测有不同的满屏点统计，只是画法不同而已
		 */
		if (drawMode == WFDrawRTUtil.DrawMode1p) {
			// if (pk_1kpix_250)
			// pk_detect_type = PK_DETECT_TYPE_1K_MINMAXMINMAX_4PIX;
			// else
			pk_detect_type = PK_DETECT_TYPE_1K_MINMAX_2PIX;
		} else if (drawMode == WFDrawRTUtil.DrawMode2p) {
			pk_detect_type = PK_DETECT_TYPE_2K_MINMAX_1PIX;
			// } else if (drawMode == WFDrawRTUtil.DrawMode4_5) {
			// pk_detect_type = PK_DETECT_TYPE_1250_4_5p;
			// } else if (drawMode == WFDrawRTUtil.DrawMode2_5p) {
			// pk_detect_type = PK_DETECT_TYPE_2500_2_5p;
			// } else if (drawMode == WFDrawRTUtil.DrawMode4in1) {
			// if (!dm)
			// pk_detect_type = PK_DETECT_TYPE_4K_MINMAXMINMAX_1PIX;
			// else
			// pk_detect_type = PK_DETECT_TYPE_4K_BEGMINMAXEND_1PIX;
		}
		return pk_detect_type;
	}

	private PaintOne_Tiny pot = new PaintOne_Tiny();

	@Override
	public IPaintOne getPaintOne() {
		return pot;
	}

	public boolean isFetchBallEvent() {
		return false;
	}

	public abstract int getDelayAttenuationVBIndex();

	public boolean isSupportMultiFreqArgument() {
		return false;
	}

	public int getMultiFreqArgument(int tbidx) {
		return -1;
	}

	public abstract LowControlManger createLowControlManger(ControlManager cm);

	public abstract LowerTranslator createLowerTranslator(
			CalArgTypeProvider catp);

	public abstract ArgCreator getArgCreator(CalArgTypeProvider tran);

	public abstract USBPortsFilter createPortFilter();

	public boolean isPhosphorOn() {
		return false;
	}

	public abstract AbsPreHandler createPreHandler(ControlManager cm,
			LowControlManger lcm);

	public abstract Submitor2 createSubmitor(JobQueueDispatcher df,
			LowerTranslator lt, LowControlManger lcm, ControlManager cm);
}