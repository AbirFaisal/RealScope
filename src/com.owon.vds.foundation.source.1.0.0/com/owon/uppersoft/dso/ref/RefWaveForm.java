package com.owon.uppersoft.dso.ref;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.IntBuffer;

import com.owon.uppersoft.dso.function.SoftwareControl;
import com.owon.uppersoft.dso.function.ref.IReferenceWaveForm;
import com.owon.uppersoft.dso.function.ref.ReferenceFile;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WFTimeScopeControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.util.ui.LineUtil;
import com.owon.uppersoft.vds.core.aspect.IView;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.core.control.MathControl;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.machine.VDS_Portable;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.core.rt.WFDrawRTUtil;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;
import com.owon.uppersoft.vds.core.wf.peak.PKDetectDrawUtil;
import com.owon.uppersoft.vds.util.StringPool;
import com.owon.uppersoft.vds.util.format.UnitConversionUtil;

public abstract class RefWaveForm implements IView, IReferenceWaveForm {
	public static final int REFPROTOCOL_VDS_INVERSE = 1;// 从1开始，0表示原型版本
	public static final int REFPROTOCOL_VDS_PK_EXTEND = 2;// 从2开始，加入峰值检测和扩展标识
	public static final int REFPROTOCOL_VDS_DM = 3;// 从3开始，加入深存储检测
	public static final int REFPROTOCOL_VDS_PROBE = 4;// 从4开始，加入探头比率检测
	public static final int REFPROTOCOL_VDS_CURRENT = REFPROTOCOL_VDS_PROBE;// 始终使用软件当前的版本

	private Color co = Color.PINK;

	/** 用来选择画图方法 */
	private byte statusType;// ?RT:DM
	private int offset; // pix
	// int width;//pix
	private int pos0;
	private int probeMultiIdx;

	/** 深存储时画出参考波形要用的参数 */
	private double gap;

	private int objIdx;
	private int waveType;
	protected int tbIdx;
	private int vbIdx;
	private int dataNums;
	protected int DMlen;

	public int getTbIdx() {
		return tbIdx;
	}

	/**
	 * 仅供包内访问
	 */
	protected RefWaveForm() {
	}

	private IntBuffer adcbuf;
	private IntBuffer pixbuf;

	public void setAdcbuf(IntBuffer adcbuf) {
		this.adcbuf = adcbuf;
		pixbuf = IntBuffer.allocate(adcbuf.remaining());
	}

	public IntBuffer getAdcbuf() {
		return adcbuf;
	}

	@Override
	public int getObjIndex() {
		return objIdx;
	}

	@Override
	public int getVBIndex() {
		return vbIdx;
	}

	@Override
	public void setObjIndex(int idx) {
		objIdx = idx;
	}

	/** math和ch的电压档位已经分化成两种 */
	public String getIntVoltageLabel_mV(VoltageProvider vp, MathControl mc) {
		if (waveType == ReferenceFile.RefFile_Math)
			return mc.MathVolt[vbIdx] + "V";
		return UnitConversionUtil.getIntVoltageLabel_mV(vp.getVoltage(
				probeMultiIdx, vbIdx));
	}

	public void setPos0(int p) {
		pos0 = p;
	}

	/** 运行时画出参考波形要用的参数 */
	protected int drawMode;

	public int getDrawMode() {
		return drawMode;
	}

	public void setDrawMode(int len) {
		drawMode = WFDrawRTUtil.getDrawModeFromLength(len);
	}

	public void setDrawMode2(int dm) {
		drawMode = dm;
	}

	// 本方法复制于WFTimeScopeControl
	protected abstract void setPK_detect_typeByDrawMode(boolean pk_detect, MachineType mt);

	public void resetRTIntBuf(int yb, boolean ScreenMode_3) {
		/**
		 * KNOW 临时缓冲区被直接拿来使用，在多次传输中反复使用，并且所有内容共用一个大的缓冲区方便分配，少量配置信息出于方便也存在其中
		 * 
		 * KNOW 数组包含了从下位机传上来的所有数据，但只有用initPos和screendatalen截取的才是画在屏幕上的
		 */
		IntBuffer buf = adcbuf;

		int p = buf.position();
		int l = buf.limit();
		int[] bb = buf.array();

		int[] pa = pixbuf.array();
		int i = 0, j;

		if (ScreenMode_3) {
			for (j = p; j < l; i++, j++) {
				/** 这个数组为固定adc数据，不在上面直接倍乘或偏移 */
				pa[i] = yb - bb[j];
			}
		} else {
			for (j = p; j < l; i++, j++) {
				/** 这个数组为固定adc数据，不在上面直接倍乘或偏移 */
				pa[i] = yb - (bb[j] << 1);
			}
		}
		// DBG.config("reset_" + String.valueOf(bb[p]) + ", yb: " + yb + "\n");
		// if (wf.wfi.ci.number == 1)
		// DBG.dbgArray(bb, l - 20, 20);
		pixbuf.limit(i);
		pixbuf.position(0);
	}

	@Override
	public void adjustView(ScreenContext pc, Rectangle bound) {
		int yb = pc.getHcenter();
		resetRTIntBuf(yb, pc.isScreenMode_3());
	}

	@Override
	public void paintView(Graphics2D g2d, ScreenContext pc, Rectangle r) {
		// if (adcbuf == null || pixbuf == null)
		// return;
		g2d.setColor(co);

		int xb = offset + r.x;
		boolean linkline = Platform.getDataHouse().isLineLink();
		Shape tmp = g2d.getClip();
		g2d.setClip(r);

		// System.err.println(" dr ref" + pk_detect + ", " + pk_detect_type);
		/** 对DM的情况也需要知道画图模型(来自满屏点数信息)，才能在pk的情况下画图 */
		Platform.getControlManager().getMachine().getPaintOne().paintRef(g2d,
				drawMode, xb, linkline, r.y, r.height, pk_detect, pixbuf,
				pk_detect_type, statusType, gap);

		g2d.setClip(tmp);
	}

	public int getPos0onChart(ScreenContext pc) {
		int hc = pc.getHcenter();
		if (pc.isScreenMode_3())
			return hc - pos0;
		else
			return hc - (pos0 << 1);
	}

	/** 获得与目标相同序号的字母 */
	public Character getLetter() {
		return (char) (objIdx + 97);
	}

	public void paintItem(Graphics2D g2d, ScreenContext pc, Rectangle r,
			boolean onFront) {
		/** 以下为同下位机连接的情况下，获取实时数据绘图，快速刷新 */

		Color tmp = g2d.getColor();
		g2d.setColor(co);

		int yb = getPos0onChart(pc);

		int y = r.y;
		int bottom = r.y + r.height;
		/** 画左边的标尺 ,依次画顶 底 中 */
		LineUtil.paintChannelLabel(yb, y, bottom, g2d, String
				.valueOf(getLetter()), 2, onFront);

		g2d.setColor(tmp);
	}

	protected boolean pk_detect = false;
	protected int pk_detect_type = PKDetectDrawUtil.PK_DETECT_TYPE_NO;

	/**
	 * 仅供包内访问，由RefWaveForm对象导入内部状态，如出错，则返回null，否则返回自身引用
	 * 
	 * @param f
	 * @param cm
	 * @return
	 */
	protected RefWaveForm loadFromFile(File f, ControlManager cm) {
		CByteArrayInputStream ba = new CByteArrayInputStream(f);
		final int byteFileHeadLen = 10;
		byte[] fhb = new byte[byteFileHeadLen];
		ba.get(fhb, 0, byteFileHeadLen);
		String strFileHead = "";
		try {
			strFileHead = new String(fhb, 0, byteFileHeadLen,
					StringPool.ASCIIString);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (!strFileHead.startsWith(VDS_Portable.FileHeader)) {
			return null;
		}

		/** 机型信息 4 字节 */
		int intMachType = ba.nextInt();
		/** 版本信息 4 字节 */
		int refFileVer = ba.nextInt();

		if (refFileVer >= REFPROTOCOL_VDS_PK_EXTEND) {
			int extend = ba.nextInt();

			int v = (extend >>> 24);
			if (v != VDS_Portable.FILEFORMAT_REF) {
				return null;
			}
		}

		DMlen = -1;
		if (refFileVer >= REFPROTOCOL_VDS_DM) {
			DMlen = ba.nextInt();
		}

		/** 文件长度 4 字节 */
		int intFileSize = ba.nextInt();

		/** 数据区标记0 */
		byte dataMark = ba.nextByte();
		/** 区长度 */
		int dataLen = ba.nextInt();
		/** 区点数据 */
		ba.skip(dataLen);

		/** 波形信息区 */
		byte waveMark = ba.nextByte();

		int waveLen = ba.nextInt();

		statusType = ba.nextByte();

		waveType = ba.nextInt();

		pk_detect = ba.nextBoolean();

		pos0 = ba.nextInt();

		tbIdx = ba.nextInt();

		vbIdx = ba.nextInt();

		if (refFileVer >= REFPROTOCOL_VDS_PROBE) {
			probeMultiIdx = ba.nextInt();
		}

		dataNums = ba.nextInt();

		offset = ba.nextInt();

		gap = ba.nextDouble();

		if (statusType == 'T')
			setDrawMode((int) gap);

		/** 参考波形在同一软件中仍可能跨机型使用，这里的问题和文件机型匹配同一级别，一同解决 */
		setPK_detect_typeByDrawMode(pk_detect, cm.getMachine());

		int collectedDataP = ba.nextInt();

		ba.reset(collectedDataP);// 跳到采集点数据的指针位置;

		IntBuffer adcbuf = IntBuffer.allocate(dataNums);
		setAdcbuf(adcbuf);
		int[] bs = adcbuf.array();// bs指向rwf.adcbuf
		ba.getIntArray(bs, 0, dataNums);// 读从curPos,即采集点数据指针位置到limit数据，写入bs数据中
		adcbuf.position(0);
		adcbuf.limit(dataNums);

		ba.dispose();

		return this;
	}

	protected RefWaveForm createFromWF(ControlManager cm, IRefSource wf,
			WaveFormManager wfm) {
		/** 接收可画数据 */
		IntBuffer adcbuf = wf.save2RefIntBuffer();
		setAdcbuf(adcbuf);

		int p = adcbuf.position(), l = adcbuf.limit();
		dataNums = l - p;
		tbIdx = cm.getTimeControl().getTimebaseIdx();

		waveType = wf.getWaveType();
		pos0 = wf.getPos0();
		vbIdx = wf.getVoltbaseIndex();
		probeMultiIdx = wf.getProbeMultiIdx();
		DataHouse dh = Platform.getDataHouse();
		WFTimeScopeControl wftsc = wfm.getWFTimeScopeControl();
		pk_detect = wftsc.isPK_Detect();

		boolean isDM = dh.isDMLoad();

		/** 保存画图模式(gap, drawMode)和offset */
		if (isDM) {
			WaveFormInfo dmli = cm.getWaveFormInfoControl()
					.getWaveFormInfoForDM();
			statusType = 'M';
			offset = dmli.getXOffset_DM();
			gap = dmli.getDMGap();
		} else {
			statusType = 'T';

			offset = wftsc.getSlowMoveOffset(wftsc.getDrawMode());
		}
		/** 对DM的情况也需要知道画图模型，才能在pk的情况下画图 */
		setDrawMode2(wftsc.getDrawMode());

		DMlen = cm.getDeepMemoryControl().getDeepDataLen();

		setPK_detect_typeByDrawMode(pk_detect, cm.getMachine());
		return this;
	}

	private int refFileVer = REFPROTOCOL_VDS_CURRENT;// 文件格式版本

	/**
	 * @param cm
	 * @param f
	 */
	public void persistRefFile(ControlManager cm, File f, int rtscreendatalen) {
		long fileLenPos, t1, t2, p1, collectedDataP;
		int dataNums;
		try {
			RandomAccessFile raf = new RandomAccessFile(f, "rw");
			final SoftwareControl sc = cm.getSoftwareControl();
			/** 运行和深存储下读取文件头 */
			byte[] fhb = sc.getMachineHeader().getBytes(StringPool.ASCIIString);

			raf.write(fhb); // 文件头(10 字节, ASCII)
			raf.writeInt(cm.getMachineTypeForSave()); // 4个字节机型信息 7102
			raf.writeInt(refFileVer); // 4个字节版本信息 1

			raf.writeByte(VDS_Portable.FILEFORMAT_REF);
			raf.writeByte(0);
			raf.writeByte(0);
			raf.writeByte(0);

			raf.writeInt(cm.getDeepMemoryControl().getDeepDataLen());

			fileLenPos = raf.getFilePointer();
			raf.writeInt(0);// 文件长度

			/* 数据区 */
			raf.writeByte(0);// 数据区入口标记为0(1字节，byte)
			p1 = raf.getFilePointer();// 数据区入口指针
			raf.writeInt(0);// 数据区长度

			t1 = collectedDataP = raf.getFilePointer();// 采集点数据的指针位置

			int[] dest = adcbuf.array();// dest指向rwf.adcbuf
			int p = adcbuf.position(), l = adcbuf.limit();
			for (int i = p; i < l; i++)
				raf.writeInt(dest[i]);// rwf.adcbuf的pos to limit,写入raf

			dataNums = l - p;
			t2 = raf.getFilePointer();

			raf.seek(p1);
			raf.writeInt((int) (t2 - t1));
			raf.seek(t2);

			/* 波形信息区 */
			raf.writeByte(1);// 波形通道区入口标记为1(1字节，byte)
			p1 = raf.getFilePointer();
			raf.writeInt(0);// 波形信息区长度

			t1 = raf.getFilePointer();

			/** 运行或深存储的状态 */
			raf.writeByte(statusType);
			/** 参考波形类型: (4字节, int) */
			raf.writeInt(waveType);

			raf.writeBoolean(pk_detect);

			/** 零点位置 (4字节, int) */
			raf.writeInt(pos0);
			/** 时基档位 (4字节, int) */
			raf.writeInt(tbIdx);
			/** 电压档位(4字节,int) */
			raf.writeInt(vbIdx);
			/** 探头比率(4字节,int)add */
			raf.writeInt(probeMultiIdx);

			/** 传输的数据点个数(4字节,int) */
			raf.writeInt(dataNums);
			/** 画图起始偏移量(4字节,int) */
			raf.writeInt(offset);
			/** 深存储画图需要的点间隔, 非深存储时存放满屏点数 */
			if (statusType == 'M') {
				raf.writeDouble(gap);
			} else {
				/** 以此确保存盘读入的ref也有DrawMode信息 */
				raf.writeDouble(rtscreendatalen);
			}
			/** 采集点数据的指针位置; (4字节, int) */
			raf.writeInt((int) collectedDataP);

			t2 = raf.getFilePointer();

			raf.seek(p1);
			raf.writeInt((int) (t2 - t1));

			raf.seek(fileLenPos);
			raf.writeInt((int) raf.length());

			raf.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
