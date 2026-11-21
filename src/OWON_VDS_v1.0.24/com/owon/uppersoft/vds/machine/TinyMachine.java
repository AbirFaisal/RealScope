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
	 * calculate the floating-point hardware frequency count from base and ref
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

	/** the above methods are temporary additions, quick and ineffective */
	public final int getPKType(int drawMode) {
		int pk_detect_type = PK_DETECT_TYPE_NO;
		/**
		 * peak detect uses the ordinary drawing method in dot mode, which means the adc array cannot be specially modified for peak detect
		 * 
		 * also for performance and ease of maintenance, so here it is simply converted to screen coordinates
		 * 
		 * with peak detect on or off, the number of points the adc captures within the screen range is the same, only the acquisition method differs,
		 * 
		 * even for deep memory there is no need for different full-screen point statistics for peak detect on/off, only the drawing method differs
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