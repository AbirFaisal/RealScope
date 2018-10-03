package com.owon.vds.tiny.firm.pref.model;

import static com.owon.uppersoft.vds.device.interpret.util.Arrays2DUtil.fillBytes;
import static com.owon.uppersoft.vds.device.interpret.util.Arrays2DUtil.loadFromBytes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.owon.uppersoft.vds.core.tune.Cending;
import com.owon.vds.tiny.firm.pref.DevicePref;
import com.owon.vds.tiny.tune.detail.AbsCalArgType;
import com.owon.vds.tiny.tune.detail.DefaultCalArgType;

public class TuneModel implements DevicePref {

	public List<DefaultCalArgType> cmdts;
	public Register reg = new Register();

	public TuneModel(int channelNumber, int vbNum) {
		cmdts = new ArrayList<DefaultCalArgType>();

		fillArgTypeList(cmdts, channelNumber, vbNum);
	}

	protected void fillArgTypeList(List<DefaultCalArgType> cmdts,
			int channelNumber, int vbNum) {
		// 粗调增益
		DefaultCalArgType coarseGain = new DefaultCalArgType(
				ArgType.Gain.ordinal(), Cending.ascending, channelNumber, vbNum);
		cmdts.add(coarseGain);

		// 零点步进
		DefaultCalArgType zeroAmplitude = new DefaultCalArgType(
				ArgType.Step.ordinal(), Cending.ascending, channelNumber, vbNum);
		cmdts.add(zeroAmplitude);

		// 零点补偿
		DefaultCalArgType zeroCompensation = new DefaultCalArgType(
				ArgType.Compensation.ordinal(), Cending.descending,
				channelNumber, vbNum);
		cmdts.add(zeroCompensation);
		// cmdts.add(new WindowTrigRaiseFall(this));// 触发上升&下降
		// cmdts.add(new CF_Multiple(this));// 触发移动倍率
		// cmdts.add(new WindowTrig(this));// 触发高低窗口
		// cmdts.add(new FullScale(this));// 满量程
		// cmdts.add(new FineGain(this));// 细调增益
		// cmdts.add(new ADCPhase(this));// ADC相位
	}

	public AbsCalArgType getSimpleAdjustCMDType(int type) {
		return cmdts.get(type);
	}

	public int getAbsCalArgTypeNumber() {
		return cmdts.size();
	}

	public Register getRegister() {
		return reg;
	}

	public void wrRegister(ByteBuffer bb) {
		reg.write(bb);
	}

	public void rdRegister(ByteBuffer bb) {
		reg.read(bb);
	}

	// public static String[] load(ResourceBundle bundle, String key) {
	// String tv = bundle.getString(key);
	// StringTokenizer st = new StringTokenizer(tv, "\r\n");
	// String[] types = new String[st.countTokens()];
	// int i = 0;
	// while (st.hasMoreTokens()) {
	// types[i] = st.nextToken();
	// i++;
	// }
	// return types;
	// }

	/**
	 * 在分区写入当前参数
	 * 
	 * @param bb
	 */
	public void writeArgsAtPartitionBuffer(ByteBuffer bb) {
		int p = bb.position();

		/** 10*6*2个byte的参数 */
		for (int i = 0; i < getAbsCalArgTypeNumber(); i++) {
			AbsCalArgType za = getSimpleAdjustCMDType(i);
			logln(za.getType());
			int[][] args = za.getArgs();
			fillBytes(bb, args);
		}

		int p2 = bb.position();
		logln(p + ", " + p2);
	}

	private void logln(String type) {
		System.out.println(type);
	}

	@Override
	public void setPropertyChangeListener(PropertyChangeListener pcl) {
		this.pcl = pcl;
	}

	private PropertyChangeListener pcl;

	/**
	 * 从分区读出当前参数
	 * 
	 * @param bb
	 */
	public void readArgsFromPartitionBuffer(ByteBuffer bb) {
		/** 10*6*2个byte的参数 */
		for (int i = 0; i < getAbsCalArgTypeNumber(); i++) {
			AbsCalArgType za = getSimpleAdjustCMDType(i);
			logln(za.getType());
			int[][] args = za.getArgs();
			loadFromBytes(args, bb, (short) 0);
		}
		if (pcl != null)
			pcl.propertyChange(new PropertyChangeEvent(this, ARGS_UPDATE, null,
					null));
	}
}