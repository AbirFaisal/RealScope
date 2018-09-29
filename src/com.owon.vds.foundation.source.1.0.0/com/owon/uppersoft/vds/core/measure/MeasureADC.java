package com.owon.uppersoft.vds.core.measure;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.core.aspect.help.IWF;

public class MeasureADC {

	public int[] array = new int[10000];

	/** 建立一个与MeasureT枚举一一对应的数组来存放计算信息 */
	public VR[] vrs = VR.createVRs();

	public int p;
	public int l;

	public VerticalValueMeasure vvc = new VerticalValueMeasure();

	public float raiseTime;
	public float fallTime;
	public int raiseStart;
	public int fallStart;
	public int raiseFlag;
	public int fallFlag;
	public float nWidth;
	public float pWidth;

	public MeasureADC() {
	}

	private static final double V_DIV_PER_POINT = 10 / (double) 250;

	public void doVerticalMeasure(IWF wf, boolean isvideo, double freLimit,
			VoltageProvider vp) {
		int voltbase = vp.getVoltage(wf.getProbeMultiIdx(), wf
				.getVoltbaseIndex());
		double Vconst = V_DIV_PER_POINT * voltbase;
		ByteBuffer bb = wf.getADC_Buffer();
		vvc.doMeasure(vrs, wf, isvideo, freLimit, Vconst, bb);
	}

}