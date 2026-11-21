package com.owon.uppersoft.dso.wf.dm;

import java.nio.IntBuffer;

import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.plug.VDSource;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;
import com.owon.uppersoft.vds.core.wf.rt.PlugInfo;
import com.owon.uppersoft.vds.data.MultiplyorAndDivisor;
import com.owon.uppersoft.vds.device.interpolate.dynamic.DynamicPlugUtil;
import com.owon.uppersoft.vds.device.interpolate.dynamic.SineTables;
import com.owon.uppersoft.vds.util.BufferHandleUtil;

public class Pluger {

	private static byte[] bufarr = new byte[1000];// take an extra 1/2

	private DiluteInfoUnitTiny diu;

	public Pluger(DiluteInfoUnitTiny diu) {
		this.diu = diu;
		plug = false;
	}

	private double gap;

	/**
	 * @param screendatalen
	 * @param handlebuf
	 * @param sinePlugRate
	 * @return linear interpolation multiplier
	 */
	private MultiplyorAndDivisor linearPlug(int screendatalen,
			IntBuffer handlebuf, int sinePlugRate, int iPos) {
		MultiplyorAndDivisor md = new MultiplyorAndDivisor();

		int[] array = handlebuf.array();
		int pos = handlebuf.position();
		int limit = handlebuf.limit();

		int length = limit - pos;
		int[] tmp_array = new int[length];
		System.arraycopy(array, pos, tmp_array, 0, length);

		md.set(5, 2);
		VDSource
				.genSimulateLinearPlug_2_3(array, 0, tmp_array, 0, length, iPos);
		pos = 0;
		limit = length * 5 / 2;
		handlebuf.position(pos);
		handlebuf.limit(limit);
		return md;
	}

	VLog vl = new VLog();

	public double getGap() {
		return gap;
	}

	public void processPlug(int iPos, int len, int datalen, int screendatalen,
			int filePointer, boolean shouldReverse, int loadPos0,
			IntBuffer handlebuf, CByteArrayInputStream cba) {
		// System.err.println(screendatalen);
		boolean original = shouldPlugAsOriginal(screendatalen);
		if (original) {
			/** Here interpolate by the interpolation factor of the initial load, borrowing the non-interpolated way of drawing */
			doprocessPlug(iPos, len, datalen, this.screendatalen, filePointer,
					shouldReverse, loadPos0, handlebuf, cba);
			gap = this.screendatalen / (double) screendatalen;
		} else {
			doprocessPlug(iPos, len, datalen, screendatalen, filePointer,
					shouldReverse, loadPos0, handlebuf, cba);
			gap = 1;
		}
	}

	/**
	 * Interpolation when not at initial load
	 * 
	 * @param iPos
	 * @param len
	 * @param datalen
	 * @param screendatalen
	 * @param filePointer
	 * @param shouldReverse
	 * @param loadPos0
	 * @param handlebuf
	 * @param cba
	 */
	private void doprocessPlug(int iPos, int len, int datalen,
			int screendatalen, int filePointer, boolean shouldReverse,
			int loadPos0, IntBuffer handlebuf, CByteArrayInputStream cba) {
		int sine_plug_r;
		// System.err.println("doprocessPlug: "+screendatalen);
		// Perform interpolation
		// MultiplyorAndDivisor linear_plug_r;
		if (screendatalen != 400) {//
			sine_plug_r = CenterPointInfo.DEFINE_AREA_WIDTH / screendatalen;
			// Sine interpolation is possible; compute the sine interpolation using the already processed extendRange
			sinePlug(iPos, len, handlebuf, datalen, DefaultExtendRange,
					sine_plug_r);
			BufferHandleUtil.restrictForReverse(handlebuf, shouldReverse,
					loadPos0);
			// linear_plug_r = new MultiplyorAndDivisor(1, 1);
		} else {
			sine_plug_r = 1;
			// Sine interpolation is not possible
			diu.retrieveBuf(iPos, 0, len, handlebuf, filePointer, cba);
			BufferHandleUtil.restrictForReverse(handlebuf, shouldReverse,
					loadPos0);
			// MultiplyorAndDivisor linear_plug_r =
			linearPlug(screendatalen, handlebuf, sine_plug_r, iPos);
		}
		// sinePlugRate = sine_plug_r;
		// linearPlugRate = linear_plug_r.getDoubleValue();
	}

	public int processPlugForInit(int filePointer, boolean shouldReverse,
			int loadPos0, IntBuffer handlebuf, CByteArrayInputStream cba,
			int halfpixs) {
		return doprocessPlugForInit(initPos, plugDataLength, filePointer,
				shouldReverse, loadPos0, handlebuf, cba, halfpixs);
	}

	/**
	 * Interpolation at initial load
	 * 
	 * @param datalen
	 * @param screendatalen
	 * @param filePointer
	 * @param shouldReverse
	 * @param loadPos0
	 * @param handlebuf
	 * @param cba
	 * @param halfpixs
	 * @return full-screen point count after interpolation
	 */
	private int doprocessPlugForInit(final int iPos, final int len,
			int filePointer, boolean shouldReverse, int loadPos0,
			IntBuffer handlebuf, CByteArrayInputStream cba, int halfpixs) {
		int sine_plug_r;

		MultiplyorAndDivisor linear_plug_r;
		// Perform interpolation
		if (sinePlugRate > 1) {
			sine_plug_r = sinePlugRate;
			linear_plug_r = new MultiplyorAndDivisor(1, 1);
			// Sine interpolation is possible; compute the sine interpolation using the already processed extendRange
			sinePlugForInit(iPos, len, handlebuf, sine_plug_r);

			BufferHandleUtil.restrictForReverse(handlebuf, shouldReverse,
					loadPos0);
		} else {
			sine_plug_r = 1;
			// Sine interpolation is not possible
			diu.retrieveBuf(iPos, 0, len, handlebuf, filePointer, cba);

			BufferHandleUtil.restrictForReverse(handlebuf, shouldReverse,
					loadPos0);

			linear_plug_r = linearPlug(screendatalen, handlebuf, sine_plug_r,
					iPos);
		}
		gap = 1;
		int length = linear_plug_r.multiplyByInt(screendatalen * sine_plug_r);
		transform2InsideInfo(length, handlebuf, halfpixs, pluggedTrgOffset);
		return length;
	}

	private void transform2InsideInfo(int length, IntBuffer handlebuf,
			int halfpixs, int pluggedTrgOffset) {
		handlebuf.position(pluggedTrgOffset);
		handlebuf.limit(pluggedTrgOffset + length);

		diu.setCenterPoint(initPos, -pluggedTrgOffset - halfpixs);
	}

	/**
	 * @param pos
	 * @param len
	 * @param handlebuf
	 */
	private void sinePlugForInit(int pos, int len, IntBuffer handlebuf,
			int plugrate) {
		int pPos = pos, pLen = len;

		int[] handlearr = handlebuf.array();

		doSinePlug(pPos, pLen, bufarr, handlearr, plugrate);

		/** The offset still starts from the position corresponding to extra; the length is the length after interpolating by plugrate */
		handlebuf.position(0);
		handlebuf.limit(len * plugrate);
	}

	private void sinePlug(int pos, int len, IntBuffer handlebuf, int datalen,
			int extendRange, int plugrate) {
		int orglen = len;
		int pPos = pos, pLen = len;
		int ex = 0;

		/** Extra points are taken at both the front and back for trigger pull; setting extendRange to 0 gives the situation after the extra points have already been taken */
		if (extendRange > 0) {
			/** Smallest among the three positive values */
			ex = Math.min(Math.min(pPos, extendRange), datalen - (pPos + pLen));
			if (ex < 0)
				ex = 0;

			pPos = pPos - ex;
			pLen = pLen + (ex << 1);
		}

		int[] array = handlebuf.array();

		doSinePlug(pPos, pLen, bufarr, array, plugrate);

		/** The offset still starts from the position corresponding to extra; the length is the length after interpolating by plugrate */
		int iPos, iLimit;
		iPos = ex * plugrate;
		iLimit = iPos + orglen * plugrate;

		handlebuf.position(iPos);
		handlebuf.limit(iLimit);
	}

	private void doSinePlug(int pPos, int pLen, byte[] bufarr, int[] handlearr,
			int plugrate) {
		diu.retrieveBuf(bufarr, pPos, 0, pLen);
		int[][] sincTable = SineTables.sincTabSp(plugrate);
		DynamicPlugUtil.sinc_interps(bufarr, 0, pLen, handlearr, plugrate,
				sincTable);
	}

	public boolean canPaintSinePlug(int screendatalen) {
		return plug;
	}

	public int getScreendatalen() {
		return screendatalen;
	}

	private boolean plug = false;

	public boolean isPlug() {
		return plug;
	}

	public void setPlug(boolean plug) {
		this.plug = plug;
	}

	/**
	 * Whether to interpolate with the interpolation factor from load time; if no, it means interpolation at load time
	 * 
	 * @param screendatalen
	 * @return
	 */
	public boolean shouldPlugAsOriginal(int screendatalen) {
		if (this.screendatalen > screendatalen) {
			return true;
		} else
			return false;
	}

	public int initPos;
	private int screendatalen;
	public int plugDataLength;
	private int sinePlugRate;
	private double linearPlugRate;
	private int pluggedTrgOffset;

	public static final int DefaultExtendRange = 50;
	public static final int DefaultPlugPointsThredsold = 400;

	private void logln(Object o) {
	}

	/**
	 * Extract interpolation info and return the correct screen start position, excluding the extra data taken for trigger pull
	 * 
	 * @param cdi
	 * @param li
	 * @return start position when there is no extra data
	 */
	public int initPlugInfo(DMDataInfo cdi, MachineType mt) {
		/** When loading deep memory, if the timebase does not need interpolation, no interpolation is done afterwards either */
		if (!plug) {
			logln("screendatalen >= 1k err in Pluger.java");
			return -1;
		}
		screendatalen = cdi.screendatalen;
		initPos = cdi.initPos;
		PlugInfo pi = cdi.pi;
		plugDataLength = pi.plugDataLength;
		sinePlugRate = pi.sinePlugRate;
		linearPlugRate = pi.linearPlugRate;
		pluggedTrgOffset = pi.pluggedTrgOffset;
		return initPos;
	}

	// public int getDrawModeFromLength(int screenADCLen) {
	// return WFDrawRTUtil.DrawMode1p;
	// }

	public void setGap(double i) {
		gap = i;
	}

}