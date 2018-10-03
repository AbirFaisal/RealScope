package com.owon.uppersoft.dso.wf.dm2;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.core.deep.struct.RangeInfo4in1;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.plug.Plug10;
import com.owon.uppersoft.vds.core.plug.VDSource;
import com.owon.uppersoft.vds.core.rt.WFDrawRTUtil;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;
import com.owon.uppersoft.vds.core.wf.rt.PlugInfo;
import com.owon.uppersoft.vds.util.LoadArrayUtil;

public class Pluger2 {

	public static final int AREA_WIDTH = GDefine.AREA_WIDTH;

	private ByteBuffer insideBuffer = ByteBuffer.allocate(2000);

	private DiluteInfoUnitTiny2 diu;

	public Pluger2(DiluteInfoUnitTiny2 diu) {
		this.diu = diu;
		plug = false;

		int i = 0;
		tdis[i] = new TimebaseDiluteInfo(i, 10, 10, 10);
		i++;
		tdis[i] = new TimebaseDiluteInfo(i, 20, 10, 5);
		i++;
		tdis[i] = new TimebaseDiluteInfo(i, 40, 10, 5);
		i++;
		tdis[i] = new TimebaseDiluteInfo(i, 100, 10, 1);
		i++;
		tdis[i] = new TimebaseDiluteInfo(i, 200, 10, 1);
		i++;
		tdis[i] = new TimebaseDiluteInfo(i, 400, 1, 5);
		i++;
	}

	private TimebaseDiluteInfo[] tdis = new TimebaseDiluteInfo[6];

	protected int getTimebaseIndexFromScreenDataLen(int screendatalen) {
		// int len = tdis.length;
		// for (int i = 0; i < len; i++) {
		// if (tdis[i].getFullscreen() == screendatalen)
		// return i;
		// }
		// return -1;
		switch (screendatalen) {
		case 10:
			return 0;
		case 20:
			return 1;
		case 40:
			return 2;
		case 100:
			return 3;
		case 200:
			return 4;
		case 400:
			return 5;
		default:
			return -1;
		}
	}

	private double gap;

	VLog vl = new VLog();

	public double getGap() {
		return gap;
	}

	public int ppi2CPI() {
		return ppi / curr_tdi.getRates();
	}

	public int remainPPI2CPI() {
		return (ppi % curr_tdi.getRates())
				+ (int) (-curr_xoff / (curr_screendatalen / 1000));
	}

	/**
	 * 初始化载入时的插值
	 * 
	 */
	public void processPlugForInit(IntBuffer handlebuf) {
		int tbidx = getTimebaseIndexFromScreenDataLen(originalscreendatalen);
		if (tbidx < 0)
			return;

		// scr = -1;

		curr_tdi = tdis[tbidx];
		curr_tdi.setupInitPos_Pluged(initPos, pluggedTrgOffset);
		curr_tdi.logself();

		doPlug(handlebuf, initPos, plugDataLength);

		drawPoints = curr_points = curr_tdi.getPoints();

		ppi = curr_tdi.getInitPos_Pluged() + (curr_points >> 1);
		vl.logln("ppi:" + ppi);
		curr_xoff = 0;

		computeGap();

		handlebuf.position(pluggedTrgOffset);
		handlebuf.limit(pluggedTrgOffset + curr_points);
	}

	private void computeGap() {
		gap = AREA_WIDTH / (double) curr_points;
	}

	private int ppi;
	private double curr_xoff;

	private void doPlug_ext(IntBuffer handlebuf, int iPos, int len) {
		int ext = 10;

		insideBuffer.position(0);
		insideBuffer.limit(len + ext);
		diu.retrieveBuf(insideBuffer, iPos);
		// 可正弦插值，使用已经处理的extendRange计算正弦插值
		sinePlugForInit(handlebuf, sinePlugRate);
		linearPlug(handlebuf, linearPlugRate);

		// int rates = curr_tdi.getRates();
		// handlebuf.position(0);
		// handlebuf.limit(handlebuf.position() + len * rates);
		// vl.logln("handlebuf.remaining():" + handlebuf.remaining());
	}

	private void doPlug(IntBuffer handlebuf, int iPos, int len) {
		insideBuffer.position(0);
		insideBuffer.limit(len);
		diu.retrieveBuf(insideBuffer, iPos);
		// 可正弦插值，使用已经处理的extendRange计算正弦插值
		sinePlugForInit(handlebuf, sinePlugRate);
		linearPlug(handlebuf, linearPlugRate);
		// vl.logln("handlebuf.remaining():" + handlebuf.remaining());
	}

	/**
	 * @param len
	 * @param handlebuf
	 */
	private void sinePlugForInit(IntBuffer handlebuf, int sine_rate) {
		int[] handlearr = handlebuf.array();
		byte[] iba = insideBuffer.array();
		int p = insideBuffer.position();
		int l = insideBuffer.limit();
		int len = l - p;
		if (sine_rate <= 1) {
			int i = 0;
			while (p < l) {
				handlearr[i] = iba[p];
				p++;
				i++;
			}
			handlebuf.position(0);
			handlebuf.limit(i);
			return;
		}

		Plug10.sine_interpolate(handlearr, iba, len, 0, p);
		/** 偏移还是从extra对应的位置开始，长度为插plugrate之后的长度 */
		handlebuf.position(0);
		handlebuf.limit(len * sine_rate);
	}

	/**
	 * @return 线性插值的倍速
	 */
	private void linearPlug(IntBuffer handlebuf, int line_rate) {
		if (line_rate <= 1)
			return;

		int[] array = handlebuf.array();
		int pos = handlebuf.position();
		int limit = handlebuf.limit();

		int length = limit - pos;
		int[] tmp_array = new int[length];
		System.arraycopy(array, pos, tmp_array, 0, length);

		VDSource.genSimulateLinearPlug(array, 0, tmp_array, 0, length,
				line_rate);
		handlebuf.position(0);
		handlebuf.limit(length * line_rate);
	}

	public TimebaseDiluteInfo getCurrentTimebaseDiluteInfo() {
		return curr_tdi;
	}

	public int getDrawMode() {

		// if (drawPoints == 5000) {
		// return WFDrawRTUtil.DrawModeDilute;
		// }

		if (drawPoints < 1000)
			return WFDrawRTUtil.DrawModeDilute;

		switch (drawPoints) {
		case 1000:
			return WFDrawRTUtil.DrawMode1p;
		case 2000:
			return WFDrawRTUtil.DrawMode2p;
		case 4000:
			return WFDrawRTUtil.DrawMode4in1;
		default:
			System.err.println("");
			return -1;
		}
	}

	private boolean plug = false;

	public boolean isPlug() {
		return plug;
	}

	public void setPlug(boolean plug) {
		this.plug = plug;
	}

	public int initPos;
	private int curr_screendatalen, originalscreendatalen;
	public int plugDataLength;
	private int sinePlugRate;
	private int linearPlugRate;
	private int pluggedTrgOffset;

	public static final int DefaultExtendRange = 50;
	public static final int DefaultPlugPointsThredsold = 400;

	private TimebaseDiluteInfo curr_tdi;

	private void logln(Object o) {
	}

	/**
	 * 提取插值信息，返回取出拉触发多拿数据以外，正确的屏幕起始位置
	 * 
	 * @param cdi
	 * @param li
	 * @return 没有多拿数据情况下的起始位置
	 */
	public void initPlugInfo(DMDataInfo cdi, MachineType mt) {
		/** 载入深存储时为无需插值的时基，则后续也不进行插值 */
		if (!plug) {
			logln("screendatalen >= 1k err in Pluger.java");
		}
		originalscreendatalen = curr_screendatalen = cdi.screendatalen;
		initPos = cdi.initPos;
		PlugInfo pi = cdi.pi;
		plugDataLength = pi.plugDataLength;
		sinePlugRate = pi.sinePlugRate;
		linearPlugRate = (int) pi.linearPlugRate;
		pluggedTrgOffset = pi.pluggedTrgOffset;
	}

	public void setGap(double i) {
		gap = i;
	}

	private int curr_points;
	private int drawPoints;

	/**
	 * -...+
	 * 
	 * @param m
	 */
	public void computeMove(int m) {
		// if (curr_tdi.getFullscreen() == curr_screendatalen) {
		// return;
		// }
		if (gap < 1) {
			ppi -= m / gap;
		} else {
			vl.logln("computeMove");
			vl.logln("m:" + m);
			vl.logln("gap:" + gap);

			int num = (int) (m / gap);
			ppi -= num;
			curr_xoff += (m - gap * num);

			num = (int) (curr_xoff / gap);
			ppi -= num;
			curr_xoff = (curr_xoff - gap * num);

			vl.logln("ppi:" + ppi);
			vl.logln("curr_xoff:" + curr_xoff);
		}
	}

	public void translate2ScreenDatalenWithoutLoad(int screendatalen2,
			CenterPointInfo2 cpi) {
		translate2ScreenDatalen(screendatalen2, null, cpi);
	}

	public void translate2ScreenDatalen(int screendatalen2,
			IntBuffer handlebuf, CenterPointInfo2 cpi) {
		double r = screendatalen2 / (double) curr_screendatalen;
		// double ro = screendatalen2 / (double) originalscreendatalen;

		vl.logln("r: " + r);

		curr_screendatalen = screendatalen2;

		curr_points = (int) (curr_points * r);
		drawPoints = curr_points;

		curr_xoff = curr_xoff * r;

		computeGap();
		vl.logln("gap:" + gap);
		vl.logln("ppi:" + ppi);

		after0(handlebuf, cpi);
	}

	private void after0(IntBuffer handlebuf, CenterPointInfo2 cpi) {
		double xoff = curr_xoff;
		int i0 = (int) (ppi - (AREA_WIDTH >> 1) / gap);

		int i1 = i0 + curr_points;

		vl.logln("i0: " + i0 + ", i1: " + i1);
		if (xoff < 0) {
			int more = (int) (-xoff / gap) + 1;
			i1 += more * gap;
		} else if (xoff > 0) {
			int more = (int) (xoff / gap);

			i0 -= more * gap;
			xoff -= more;

			i1 -= more * gap;
		}
		vl.logln("then i0: " + i0 + ", i1: " + i1);
		cpi.setXBloc((int) xoff);
		vl.logln("xoff:" + xoff);

		if (handlebuf == null)
			return;

		afterTranslateCompress(i0, i1, handlebuf, xoff);
	}

	private void afterTranslateCompress(int i0, int i1, IntBuffer handlebuf,
			double xoff) {
		int rates = curr_tdi.getRates();
		int iPos = i0 / rates;
		int len = (i1 - i0) / rates + 1;
		int remain = i0 % rates;
		vl.logln("iPos: " + iPos);
		vl.logln("len: " + len);
		vl.logln("remain: " + remain);
		doPlug_ext(handlebuf, iPos, len);

		int hlen = handlebuf.remaining();
		/** 精简插值后实际需要画到屏幕上的点 */
		int plen = (int) ((1000 - xoff + 1) * curr_points / 1000);
		if (hlen > plen)
			hlen = plen;

		handlebuf.position(handlebuf.position() + remain);
		handlebuf.limit(handlebuf.position() + hlen);

		vl.logln("curr_points: " + curr_points);
		vl.logln("hlen: " + hlen);

		int i_xoff = (int) xoff;
		if (curr_points > 4000) {// && false
			int[] rt_arr = new int[4100];
			int p = handlebuf.position();
			int l = handlebuf.limit();
			int[] arr = handlebuf.array();
			int div = (int) (curr_points / AREA_WIDTH);
			vl.logln("div: " + div);
			RangeInfo4in1 ri = new RangeInfo4in1();
			int rt_ptr = 0;
			int ptr0 = rt_ptr;
			while (p < l && (rt_ptr - ptr0 + i_xoff) <= 4000) {
				LoadArrayUtil._4for1(ri, arr, p, div);
				p += div;
				rt_ptr = ri.fillArray(rt_arr, rt_ptr);
			}
			System.arraycopy(rt_arr, 0, arr, 0, rt_ptr);
			handlebuf.position(0);
			handlebuf.limit(rt_ptr);
			vl.logln("rt_ptr: " + rt_ptr);
			// gap = 0.25;
			drawPoints = 4000;
		}
	}

}