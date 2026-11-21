package com.owon.uppersoft.dso.wf.rt;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.owon.uppersoft.dso.model.WFTimeScopeControl;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.data.MinMax;
import com.owon.uppersoft.vds.core.paint.IPaintOne;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.core.wf.RTLocInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelDataInfo;
import com.owon.uppersoft.vds.core.wf.rt.LoadMedia;

/**
 * Manage RT drawing
 * 
 * @author Matt
 * 
 */
public class RTLocInfoManager {

	private List<RTLocInfo> rtlis;
	private WaveForm wf;

	public RTLocInfoManager(WaveForm wf) {
		this.wf = wf;
		rtlis = new LinkedList<RTLocInfo>();// Collections.synchronizedList();
	}

	public void restrictFrames(int num, int chl) {
		while (rtlis.size() < num) {
			rtlis.add(new RTLocInfo(chl));
		}
		while (rtlis.size() > num) {
			rtlis.remove(0);
		}
	}

	public static final boolean OneFramePop = 1 == 0;

	/**
	 * Since invert is handled, this method is called only once when loading data
	 * 
	 * @param cdi
	 * @param pc
	 * @param reverse
	 * @param pos0
	 * @param skipPoints
	 * @param pk_detect
	 */
	public void resetRTIntBuf(ChannelDataInfo cdi, ScreenContext pc,
			int skipPoints, int yb, LoadMedia cti, BigDecimal vbmulti) {
		int chl = cdi.chl;

		LinkedList<ByteBuffer> buflist = cdi.getBuflist();
		int size = buflist.size();
		// System.err.println(size);
		if (size <= 0) {
			return;
		}
		boolean ScreenMode_3 = pc.isScreenMode_3();
		// int yb = pc.getHcenter();

		// System.out.println(wf.dh.controlManager.sampleControl.avgon);
		/**
		 * KNOW Without guaranteeing frame mixing and average synchronization for now, frame mixing is no longer done when judging average sampling
		 * 
		 * After stopping, the loaded waveform is not multi-frame, so frame mixing is turned off automatically
		 */

		BeyondMax = cdi.BeyondMax;
		BeyondMin = cdi.BeyondMin;
		// System.err.println(BeyondMax + ", " + BeyondMin);

		// KNOW The space allocated and the number of frames mixed are determined by the number of waveform frames received, and the received frame count is specified at transmission
		if (OneFramePop) {
			restrictFrames(1, chl);
			ByteBuffer bbuf = cdi.popAdcbuf();
			if (bbuf != null) {
				// Skip here the pixel points that may be skipped due to slow sweep
				bbuf.position(bbuf.position() + skipPoints);

				rtlis.get(0).resetRTIntBuf(bbuf, yb, ScreenMode_3, vbmulti);
			}
		} else {
			restrictFrames(size, chl);
			Iterator<RTLocInfo> rti = rtlis.iterator();
			for (ByteBuffer bbuf : buflist) {
				// System.err.println("1: "+bbuf.remaining());
				// Skip here the pixel points that may be skipped due to slow sweep
				bbuf.position(bbuf.position() + skipPoints);
				// System.err.println("1: "+bbuf.remaining());
				rti.next().resetRTIntBuf(bbuf, yb, ScreenMode_3, vbmulti);
			}
		}

		// System.out.println("on reset: " + rtlis.size());
	}

	public void releaseRTLI() {
		rtlis.clear();
	}

	public void resetIntBuf(BigDecimal vbmulti, int yb, boolean screenMode_3) {
		// xboff = 0;
		if (rtlis.size() > 0) {
			RTLocInfo rtli = rtlis.get(0);
			rtli.resetIntBuf(vbmulti, yb, screenMode_3);
		}
	}

	public ByteBuffer getNextFrameADCBuffer(int i) {
		int sz = rtlis.size();
		if (sz == 0 || i >= sz)
			return null;
		return rtlis.get(i).adcbuf;
	}

	public ByteBuffer rtliADC_Buffer() {
		int sz = rtlis.size();
		if (sz == 0) {
			System.err
					.println("RTLocInfoManager.rtliADC_Buffer().rtlis.size()==null");
			return null;
		}
		/** KNOW later-acquired frames come later */
		return rtlis.get(sz - 1).adcbuf;
	}

	private boolean BeyondMax = false, BeyondMin = false;

	public boolean isRtADC_MaxBeyond() {
		return BeyondMax;
	}

	public boolean isRtADC_MinBeyond() {
		return BeyondMin;
	}

	/** x: min, y: max */
	private MinMax min_max = new MinMax();
	private boolean init_min_max;

	public int getTrg50percentValue() {
		/** Reset before each computation; set to true if obtained */
		init_min_max = false;
		for (RTLocInfo loc : rtlis) {
			MinMax mm = loc.collect_max_min();
			if (mm == null)
				continue;
			compare_setXY(mm);
		}

		/** Initialization not yet successful, i.e. not even one frame's extreme values have been obtained; Integer.MAX_VALUE then represents an invalid value */
		if (!init_min_max)
			return Integer.MAX_VALUE;

		return min_max.getMiddle();
	}

	/**
	 * @return Middle value of the last frame; Integer.MAX_VALUE means no frame
	 */
	public int computeMiddle() {
		int sz = rtlis.size();
		if (sz == 0)
			return Integer.MAX_VALUE;

		RTLocInfo rtli = rtlis.get(sz - 1);
		MinMax mm = rtli.collect_max_min();
		if (mm == null)
			return Integer.MAX_VALUE;
		return mm.computeMiddle();
	}

	/**
	 * @param x
	 *            smaller value is set
	 * @param mousey
	 *            larger value is set
	 */
	public void compare_setXY(MinMax mm) {
		/** If no frame's extreme values have been obtained yet, they are fetched and the state is set here */
		if (!init_min_max) {
			min_max.set(mm);
			init_min_max = true;
			return;
		}
		min_max.mergeMinMax(mm);
	}

	public IntBuffer save2RefIntBuffer() {
		int sz = rtlis.size();
		if (sz == 0)
			return IntBuffer.allocate(0);

		ByteBuffer buf = rtlis.get(sz - 1).adcbuf;
		byte[] src = buf.array();
		int limit = buf.limit();
		int pos = buf.position();

		IntBuffer adcbuf = IntBuffer.allocate(limit - pos);
		int[] dest = adcbuf.array();
		int j = 0;

		for (int i = pos; i < limit; i++, j++) {
			dest[j] = src[i];
		}
		adcbuf.limit(j);
		adcbuf.position(0);
		return adcbuf;
	}

	public RTLocInfo getFirst() {
		try {
			return rtlis.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void paintKinds(Graphics2D g2d, int drawMode, int xb,
			boolean linkline, Rectangle r, boolean pkdetect,
			WFTimeScopeControl wftsc, ScreenContext pc) {
		if (rtlis.size() <= 0)
			return;

		try {
			IPaintOne ipo = pc.getIPaintOne();
			for (RTLocInfo rtlid : rtlis) {
				ipo.paintONE(g2d, drawMode, xb, linkline, r.y, r.height,
						pkdetect, wftsc, rtlid.pixbuf, wftsc
								.getPK_detect_type());
			}
		} catch (Exception e) {
			// e.printStackTrace();
			// DBG.seversln("RTLocInfoManager.paintKinds:" + e.getMessage());
		}

	}

}