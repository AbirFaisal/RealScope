package com.owon.uppersoft.vds.source.front;

import java.nio.ByteBuffer;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.mode.control.DeepMemoryControl;
import com.owon.uppersoft.dso.mode.control.TimeControl;
import com.owon.uppersoft.dso.model.WaveFormInfoControl;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.core.data.MinMax;
import com.owon.uppersoft.vds.core.deep.struct.RangeInfo4in1;
import com.owon.uppersoft.vds.machine.LowControlManger;
import com.owon.uppersoft.vds.machine.MachineInfo_Tiny;
import com.owon.uppersoft.vds.source.comm.data.ChannelDataInfo_Tiny;
import com.owon.uppersoft.vds.source.comm.data.ChannelsTransportInfo_Tiny;
import com.owon.uppersoft.vds.util.LoadArrayUtil;

public abstract class AbsPreHandler {

	public static final int TRANBUFLOC_TRGBEG = 11;
	public static final int TRANBUFLEN_TRG = 100;
	public static final int TRANBUFLOC_DMBEG = TRANBUFLOC_TRGBEG
			+ TRANBUFLEN_TRG;

	public static final int TRANBUFLEN_PREDM = 50, TRANBUFLEN_SUFDM = 50,
			TRANBUFLEN_DM = 5000;
	public static final int TRANBUFLEN_ALLDM = TRANBUFLEN_DM + TRANBUFLEN_PREDM
			+ TRANBUFLEN_SUFDM;
	public static final int BufferSize = TRANBUFLEN_ALLDM + TRANBUFLOC_DMBEG;
	public static final int ReceiveSize = BufferSize;

	protected ControlManager cm;
	protected TimeControl tc;
	protected LowControlManger lcm;

	protected TrgLocateInfo tli = new TrgLocateInfo();
	protected SmallTimebasePreHandler stp;

	public AbsPreHandler(ControlManager cm, LowControlManger lcm) {
		this.cm = cm;
		this.lcm = lcm;
		tc = cm.getTimeControl();

		temp = new ByteBuffer[cm.getAllChannelsNumber()];

		stp = new SmallTimebasePreHandler(cm);
		
		vl.on = false;
	}

	protected DeepMemoryControl getDeepMemoryControl() {
		return cm.getDeepMemoryControl();
	}

	protected WaveFormInfoControl getWaveFormInfoControl() {
		return cm.getWaveFormInfoControl();
	}

	protected MachineInfo_Tiny getMachineInfo() {
		return (MachineInfo_Tiny) cm.getMachineInfo();
	}

	public static final int ADC_MAX = 125, ADC_MIN = -125;

	public static final byte limitValue2Byte(int v) {
		v = v > ADC_MAX ? ADC_MAX : v;
		v = v < ADC_MIN ? ADC_MIN : v;
		return (byte) v;
	}

	protected int getFullScreen() {
		return getMachineInfo().getFullScreen(
				LowControlManger.ChannelSampleConfig,
				getDeepMemoryControl().getDeepIdx())[tc.getTimebaseIdx()];
	}

	public abstract void preLoadReceiveData(ChannelsTransportInfo_Tiny ci);

	protected VLog vl = new VLog();

	protected void handelData_noplug(ByteBuffer dmbuf,
			ChannelDataInfo_Tiny cdi, ChannelsTransportInfo_Tiny ci, int tb,
			int fullscreen) {
		vl.logln("handelData_noplug");

		/** 可优化为使用最大长度的固定buffer，针对不同满屏点数通用 */
		ByteBuffer rtbuf = allocate(cdi.chl, fullscreen);
		byte[] rt_arr = rtbuf.array();

		byte[] dm_arr = dmbuf.array();
		int dm_beg = dmbuf.position();// begADC;//
		int dm_end = dmbuf.limit();

		vl.logln(cdi.chl + ", " + dm_beg + ", " + dm_end);
		// if (htp >= -500 && htp <= 500)
		int dm_screen_beg = ((dm_beg + dm_end) >> 1) - (fullscreen >> 1);
		// adcctr - (htp * chllen / screenWidth);
		int rt_ptr = 0;
		rtbuf.position(rt_ptr);
		System.arraycopy(dm_arr, dm_screen_beg, rt_arr, rt_ptr, fullscreen);
		rt_ptr += fullscreen;
		rtbuf.limit(rt_ptr);

		cdi.dmInitPos = dm_screen_beg - dm_beg;
		cdi.dmfullscreen = fullscreen;
		cdi.dmslowMove = 0;

		ci.tailhandle(cdi, rtbuf, 0);
	}

	protected void handelData_fullscreen(ByteBuffer dmbuf,
			ChannelDataInfo_Tiny cdi, ChannelsTransportInfo_Tiny ci,
			boolean isSlowMove, int points2pix) {
		vl.logln("handelData_fullscreen " + points2pix);

		int pixs = GDefine.AREA_WIDTH;
		int rt_len = pixs * points2pix;

		ByteBuffer rtbuf = allocate(cdi.chl, rt_len);
		byte[] rt_arr = rtbuf.array();

		byte[] dm_arr = dmbuf.array();
		int dm_beg = dmbuf.position();// begADC;//
		int dm_end = dmbuf.limit();
		int dmlen = dm_end - dm_beg;
		// DBG.outArrayAlpha(src, p + 50, 20);

		int div = dmlen / pixs;

		int dm_ptr = dm_beg;
		int rt_ptr = 0;

		rtbuf.position(rt_ptr);
		vl.logln(cdi.chl + ": " + dm_beg + " -> " + dm_end);

		if (points2pix == 4) {
			RangeInfo4in1 ri = new RangeInfo4in1();
			while (dm_ptr != dm_end) {
				LoadArrayUtil._4for1(ri, dm_arr, dm_ptr, div);
				dm_ptr += div;
				rt_ptr = ri.fillArray(rt_arr, rt_ptr);
			}
		} else if (points2pix == 2) {
			MinMax ri = new MinMax();
			while (dm_ptr != dm_end) {
				LoadArrayUtil._2for1(ri, dm_arr, dm_ptr, div);
				dm_ptr += div;
				rt_ptr = ri.fillArray(rt_arr, rt_ptr);
			}
		} else {
			System.err
					.println("err uncheck situation: PreHandler handelData_fullscreen");
		}
		// vl.logln(i);
		rtbuf.limit(rt_ptr);
		// ArrayLogger.configArray(rtbuf.array(), rtbuf.position(),
		// rtbuf.remaining());
		int dmslow = 0;
		int rt_slow = 0;
		// vl.logln(cdi.slowMove + "");

		/**
		 * 这里防止切换到非慢扫但正在解析的慢扫数据也被认成了非慢扫而导致画图出错
		 * 
		 * 处理的方法是捕捉慢扫移动数的不规则数值，过滤掉这样的波形帧
		 * 
		 * 如果不是不规则数值，则和非慢扫没差别
		 */
		if (isSlowMove || cdi.slowMove < dmlen) {
			dmslow = cdi.slowMove;

			if (dmslow > dmlen)
				dmslow = dmlen;
			rt_slow = dmslow / div * points2pix;
		}
		cdi.dmInitPos = 0;
		cdi.dmfullscreen = dmlen;
		cdi.dmslowMove = dmslow;

		ci.tailhandle(cdi, rtbuf, rt_slow);
	}

	private ByteBuffer[] temp;

	protected ByteBuffer allocate(int chl, int len) {
		ByteBuffer bb = temp[chl];
		if (bb == null)
			bb = ByteBuffer.allocate(TRANBUFLEN_DM);
		bb.position(0);
		bb.limit(len);
		return bb;
	}
}