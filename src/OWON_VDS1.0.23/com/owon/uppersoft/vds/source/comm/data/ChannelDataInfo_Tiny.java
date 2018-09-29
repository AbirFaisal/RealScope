package com.owon.uppersoft.vds.source.comm.data;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;
import com.owon.uppersoft.vds.core.wf.rt.ChannelDataInfo;
import com.owon.uppersoft.vds.core.wf.rt.PlugInfo;
import com.owon.uppersoft.vds.source.front.PreHandler;

public class ChannelDataInfo_Tiny extends ChannelDataInfo {
	public PlugInfo pi = new PlugInfo();

	@Override
	public void computeFreq(BigDecimal currentSampleRateBD_kHz,
			MachineType machine) {
		// super.computeFreq(currentSampleRateBD_kHz, machine);
	}

	public void handleADCBoundNInverse(int pos0, boolean inverse) {
		handleADCBoundNInverseForFrame(pos0, inverse, alldmbuf);
	}

	private ByteBuffer alldmbuf;

	public void setAllDMBuffer(ByteBuffer bb) {
		alldmbuf = ByteBuffer.wrap(bb.array(), bb.position(), bb.remaining());
		// byte[] tmp = new byte[bb.remaining()];
		// System.arraycopy(bb.array(), bb.position(), tmp, 0, bb.remaining());
		// alldmbuf = ByteBuffer.wrap(tmp);
	}

	public void resetPi() {
		pi.reset();
	}

	public ByteBuffer getALLDMBuffer() {
		return alldmbuf;
	}

	@Override
	public void forceGround(int pos0) {
		gndByteBuffer(alldmbuf, pos0);
	}

	@Override
	public void reset() {
		super.reset();
		setFreq(-1);
	}

	/** 需要拉触发时才用的触发周围数据buf */
	private ByteBuffer trglocbuf;

	public void setTrgLocBuffer(ByteBuffer trgb) {
		this.trglocbuf = trgb;
	}

	public ByteBuffer getTrgLocBuffer() {
		return trglocbuf;
	}

	public int dmInitPos;
	public int dmfullscreen;
	public int dmslowMove;

	public void save2DMDataInfo(DMDataInfo di) {
		ByteBuffer bb = alldmbuf;

		di.datalen = bb.remaining();

		di.setFreq(getFrequencyFloat());

		di.screendatalen = dmfullscreen;

		PlugInfo dmpi = di.pi;
		dmpi.copyFrom(pi);
		di.initPos = dmInitPos;

		di.slowMove = dmslowMove;
	}

	public boolean trg_d;

	public void setResultRTBuf(ByteBuffer result) {
		initPos = 0;
		int p = result.position();
		int l = result.limit();
		byte[] arr = result.array();
		for (int i = p; i < l; i++) {
			arr[i] = PreHandler.limitValue2Byte(arr[i]);
		}
		setUniqueAdcbuf(result);
	}

	public ByteBuffer trimNgetADCByteBuffer() {
		ByteBuffer dmbuf = getALLDMBuffer();
		if (dmbuf == null)
			return dmbuf;

		int p = dmbuf.position();
		int l = dmbuf.limit();

		// logln("doTrimNgetADCByteBuffer: " + p + ", " + l);
		// logln("pre-50:");
		// DBG.outArrayHex(this, arr, p, pre);
		// logln("last-50:");
		// DBG.outArrayHex(this, arr, l - suf, suf);

		p += PreHandler.TRANBUFLEN_PREDM;
		l -= PreHandler.TRANBUFLEN_SUFDM;

		dmbuf.limit(l);
		dmbuf.position(p);

		return dmbuf;
	}
}
