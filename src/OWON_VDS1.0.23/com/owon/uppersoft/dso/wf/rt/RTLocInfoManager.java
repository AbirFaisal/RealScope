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
 * 管理RT绘图
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
	 * 由于处理了反相，该方法只在载入数据时调用一次
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

		// System.out.println(wf.dataHouse.controlManager.sampleControl.avgon);
		/**
		 * KNOW 在暂不去保证混屏和平均值同步的情况下，判断平均值采样时就不再混屏
		 * 
		 * 停止后由于载入的波形不会是多次的，所以自动就关闭了混画
		 */

		BeyondMax = cdi.BeyondMax;
		BeyondMin = cdi.BeyondMin;
		// System.err.println(BeyondMax + ", " + BeyondMin);

		// KNOW 根据接收的波形幅数确定开辟的空间及混画幅数，而接收波形幅数在传输时指定了
		if (OneFramePop) {
			restrictFrames(1, chl);
			ByteBuffer bbuf = cdi.popAdcbuf();
			if (bbuf != null) {
				// 在这里对可能的慢扫产生的略过像素点进行略过
				bbuf.position(bbuf.position() + skipPoints);

				rtlis.get(0).resetRTIntBuf(bbuf, yb, ScreenMode_3, vbmulti);
			}
		} else {
			restrictFrames(size, chl);
			Iterator<RTLocInfo> rti = rtlis.iterator();
			for (ByteBuffer bbuf : buflist) {
				// System.err.println("1: "+bbuf.remaining());
				// 在这里对可能的慢扫产生的略过像素点进行略过
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
		/** KNOW 后采的帧靠后 */
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
		/** 每次计算前重置，如果获取到了，则被置为真 */
		init_min_max = false;
		for (RTLocInfo loc : rtlis) {
			MinMax mm = loc.collect_max_min();
			if (mm == null)
				continue;
			compare_setXY(mm);
		}

		/** 未初试成功，即未获得哪怕一帧的最值，这时用Integer.MAX_VALUE代表无效值 */
		if (!init_min_max)
			return Integer.MAX_VALUE;

		return min_max.getMiddle();
	}

	/**
	 * @return 最后一帧的中间值，如Integer.MAX_VALUE表示无帧
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
	 *            小的数值被设置
	 * @param mousey
	 *            大的数值被设置
	 */
	public void compare_setXY(MinMax mm) {
		/** 若还未获得任意一帧的最值，则这里获取并设置状态 */
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