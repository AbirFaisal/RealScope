package com.owon.uppersoft.dso.data;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.aspect.help.IExportableWF;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;

public class WFO implements IExportableWF {
	private CByteArrayInputStream ba;
	private ControlManager cm;

	public WFO(CByteArrayInputStream ba, ControlManager cm) {
		this.ba = ba;
		this.cm = cm;
	}

	private WaveFormInfo wfi;
	private WaveForm wf;
	private int initPos0, DMFilePointer;

	public WFO setWF(WaveForm wf) {
		this.wf = wf;
		wfi = wf.wfi;
		adcperpix = getVoltBetweenPix(); // * getProbeValue();
		initPos0 = wf.getFirstLoadPos0();
		DMFilePointer = wfi.getDMFilePointer();
		return this;
	}

	@Override
	public boolean isOn() {
		return wfi.ci.isOn();
	}

	/** 以下为导出数据到各种格式的方法，目前只支持直接导出深存储的内容，屏幕范围的后续有空再做 */
	@Override
	public int getDatalen() {
		return wfi.getDataLen();
	}

	public String getChannelLabel() {
		return wf.toString();
	}

	/**
	 * 随机获取数据点的电压值 TODO 每次获取要get运算，可以优化
	 * 
	 * @param i
	 * @return
	 */
	@Override
	public double voltAt(int i) {
		if (ba == null)
			return -1;
		if (i >= wfi.getDataLen())
			return 0;

		// System.out.println("FilePointer:" + wfi.getDMFilePointer()
		// + ",initPos:" + wfi.dmli.initPos + ",i:" + i);
		int v = ba.byteAt(DMFilePointer + i);
		// if(ci.number==1)
		// System.err.println(v + "," + initPos0);
		v -= initPos0;
		if (v == 0)
			return 0;
		else
			return v * adcperpix;
	}

	private double adcperpix;

	protected double getVoltBetweenPix() {
		double v = wfi.ci.getVoltValue() / (double)GDefine.PIXELS_PER_DIV;
		return v;
	}

	@Override
	public String getFreqLabel() {
		return cm.getFreqLabel(wfi.ci);
	}
}