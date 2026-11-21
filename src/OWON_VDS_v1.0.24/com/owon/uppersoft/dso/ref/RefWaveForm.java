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
	public static final int REFPROTOCOL_VDS_INVERSE = 1;// Starts from 1, 0 denotes the prototype version
	public static final int REFPROTOCOL_VDS_PK_EXTEND = 2;// Starts from 2, adds peak detect and extension flags
	public static final int REFPROTOCOL_VDS_DM = 3;// Starts from 3, adds deep memory detection
	public static final int REFPROTOCOL_VDS_PROBE = 4;// Starts from 4, adds probe ratio detection
	public static final int REFPROTOCOL_VDS_CURRENT = REFPROTOCOL_VDS_PROBE;// Always use the software's current version

	private Color co = Color.PINK;

	/** Used to select the drawing method */
	private byte statusType;// ?RT:DM
	private int offset; // pix
	// int width;//pix
	private int pos0;
	private int probeMultiIdx;

	/** Parameters needed to draw the reference waveform in deep memory */
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
	 * Package-private access only
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

	/** math and ch voltage scales have diverged into two kinds */
	public String getIntVoltageLabel_mV(VoltageProvider vp, MathControl mc) {
		if (waveType == ReferenceFile.RefFile_Math)
			return mc.MathVolt[vbIdx] + "V";
		return UnitConversionUtil.getIntVoltageLabel_mV(vp.getVoltage(
				probeMultiIdx, vbIdx));
	}

	public void setPos0(int p) {
		pos0 = p;
	}

	/** Parameters needed to draw the reference waveform at runtime */
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

	// This method is copied from WFTimeScopeControl
	protected abstract void setPK_detect_typeByDrawMode(boolean pk_detect, MachineType mt);

	public void resetRTIntBuf(int yb, boolean ScreenMode_3) {
		/**
		 * KNOW The temporary buffer is used directly and reused across multiple transfers; all contents share one large buffer for easy allocation, and a small amount of configuration info is also kept in it for convenience
		 * 
		 * KNOW The array holds all data uploaded from the device, but only the part cut out with initPos and screendatalen is drawn on the screen
		 */
		IntBuffer buf = adcbuf;

		int p = buf.position();
		int l = buf.limit();
		int[] bb = buf.array();

		int[] pa = pixbuf.array();
		int i = 0, j;

		if (ScreenMode_3) {
			for (j = p; j < l; i++, j++) {
				/** This array is fixed adc data and is not multiplied or offset in place */
				pa[i] = yb - bb[j];
			}
		} else {
			for (j = p; j < l; i++, j++) {
				/** This array is fixed adc data and is not multiplied or offset in place */
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
		/** For DM the drawing model (from the full-screen point count) must also be known, so that drawing works in the pk case */
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

	/** Get the letter with the same index as the target */
	public Character getLetter() {
		return (char) (objIdx + 97);
	}

	public void paintItem(Graphics2D g2d, ScreenContext pc, Rectangle r,
			boolean onFront) {
		/** Below: while connected to the device, fetch real-time data for drawing, fast refresh */

		Color tmp = g2d.getColor();
		g2d.setColor(co);

		int yb = getPos0onChart(pc);

		int y = r.y;
		int bottom = r.y + r.height;
		/** Draw the left ruler: top, bottom, middle in turn */
		LineUtil.paintChannelLabel(yb, y, bottom, g2d, String
				.valueOf(getLetter()), 2, onFront);

		g2d.setColor(tmp);
	}

	protected boolean pk_detect = false;
	protected int pk_detect_type = PKDetectDrawUtil.PK_DETECT_TYPE_NO;

	/**
	 * Package-private access only. Imports the internal state from a RefWaveForm object; returns null on error, otherwise returns a reference to itself
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

		/** Model info, 4 bytes */
		int intMachType = ba.nextInt();
		/** Version info, 4 bytes */
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

		/** File length, 4 bytes */
		int intFileSize = ba.nextInt();

		/** Data section marker 00 */
		byte dataMark = ba.nextByte();
		/** Section length */
		int dataLen = ba.nextInt();
		/** Section point data */
		ba.skip(dataLen);

		/** Waveform info section */
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

		/** A reference waveform may still be used across models within the same software; this problem is at the same level as file model matching and is solved together with it */
		setPK_detect_typeByDrawMode(pk_detect, cm.getMachine());

		int collectedDataP = ba.nextInt();

		ba.reset(collectedDataP);// Jump to the pointer position of the acquired point data;;

		IntBuffer adcbuf = IntBuffer.allocate(dataNums);
		setAdcbuf(adcbuf);
		int[] bs = adcbuf.array();// bs points to rwf.adcbuf
		ba.getIntArray(bs, 0, dataNums);// Read data from curPos, i.e. the acquired point data pointer position, to limit, and write it into the bs data
		adcbuf.position(0);
		adcbuf.limit(dataNums);

		ba.dispose();

		return this;
	}

	protected RefWaveForm createFromWF(ControlManager cm, IRefSource wf,
			WaveFormManager wfm) {
		/** Receive drawable data */
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

		/** Save the drawing mode (gap, drawMode) and offset */
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
		/** For DM the drawing model must also be known, so that drawing works in the pk case */
		drawMode = wftsc.getDrawMode();

		DMlen = cm.getDeepMemoryControl().getDeepDataLen();

		setPK_detect_typeByDrawMode(pk_detect, cm.getMachine());
		return this;
	}

	private int refFileVer = REFPROTOCOL_VDS_CURRENT;// File format version

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
			/** Read the file header in run and deep memory modes */
			byte[] fhb = sc.getMachineHeader().getBytes(StringPool.ASCIIString);

			raf.write(fhb); // File header (10 bytes, ASCII), ASCII)
			raf.writeInt(cm.getMachineTypeForSave()); // 4 bytes of model info 7102
			raf.writeInt(refFileVer); // 4 bytes of version info 1

			raf.writeByte(VDS_Portable.FILEFORMAT_REF);
			raf.writeByte(0);
			raf.writeByte(0);
			raf.writeByte(0);

			raf.writeInt(cm.getDeepMemoryControl().getDeepDataLen());

			fileLenPos = raf.getFilePointer();
			raf.writeInt(0);// File length

			/* Data section */
			raf.writeByte(0);// Data section entry marker is 0 (1 byte, byte)
			p1 = raf.getFilePointer();// Data section entry pointer
			raf.writeInt(0);// Data section length

			t1 = collectedDataP = raf.getFilePointer();// Pointer position of the acquired point data

			int[] dest = adcbuf.array();// dest points to rwf.adcbuf
			int p = adcbuf.position(), l = adcbuf.limit();
			for (int i = p; i < l; i++)
				raf.writeInt(dest[i]);// rwf.adcbuf's pos to limit, written to raf

			dataNums = l - p;
			t2 = raf.getFilePointer();

			raf.seek(p1);
			raf.writeInt((int) (t2 - t1));
			raf.seek(t2);

			/* Waveform info section */
			raf.writeByte(1);// Waveform channel section entry marker is 1 (1 byte, byte)
			p1 = raf.getFilePointer();
			raf.writeInt(0);// Waveform info section length

			t1 = raf.getFilePointer();

			/** Run or deep memory state */
			raf.writeByte(statusType);
			/** Reference waveform type: (4 bytes, int) */
			raf.writeInt(waveType);

			raf.writeBoolean(pk_detect);

			/** Zero position (4 bytes, int) */
			raf.writeInt(pos0);
			/** Timebase scale (4 bytes, int) */
			raf.writeInt(tbIdx);
			/** Voltage scale (4 bytes,int) */
			raf.writeInt(vbIdx);
			/** Probe ratio (4 bytes,int)add */
			raf.writeInt(probeMultiIdx);

			/** Number of transferred data points (4 bytes,int) */
			raf.writeInt(dataNums);
			/** Drawing start offset (4 bytes,int) */
			raf.writeInt(offset);
			/** Point interval needed for deep memory drawing; holds the full-screen point count when not in deep memory */
			if (statusType == 'M') {
				raf.writeDouble(gap);
			} else {
				/** This ensures that refs read from disk also have DrawMode info */
				raf.writeDouble(rtscreendatalen);
			}
			/** Pointer position of the acquired point data; (4 bytes, int) */
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
