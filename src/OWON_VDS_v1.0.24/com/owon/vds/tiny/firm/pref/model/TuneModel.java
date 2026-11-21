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
		// Coarse gain
		DefaultCalArgType coarseGain = new DefaultCalArgType(
				ArgType.Gain.ordinal(), Cending.ascending, channelNumber, vbNum);
		cmdts.add(coarseGain);

		// Zero-point step
		DefaultCalArgType zeroAmplitude = new DefaultCalArgType(
				ArgType.Step.ordinal(), Cending.ascending, channelNumber, vbNum);
		cmdts.add(zeroAmplitude);

		// Zero-point compensation
		DefaultCalArgType zeroCompensation = new DefaultCalArgType(
				ArgType.Compensation.ordinal(), Cending.descending,
				channelNumber, vbNum);
		cmdts.add(zeroCompensation);
		// cmdts.add(new WindowTrigRaiseFall(this));// Trigger rise & fall
		// cmdts.add(new CF_Multiple(this));// Trigger move multiplier
		// cmdts.add(new WindowTrig(this));// Trigger high/low window
		// cmdts.add(new FullScale(this));// Full scale
		// cmdts.add(new FineGain(this));// Fine gain
		// cmdts.add(new ADCPhase(this));// ADC phase
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
	 * Write the current parameters into the partition
	 * 
	 * @param bb
	 */
	public void writeArgsAtPartitionBuffer(ByteBuffer bb) {
		int p = bb.position();

		/** 10*6*2 bytes of parameters */
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
	 * Read the current parameters from the partition
	 * 
	 * @param bb
	 */
	public void readArgsFromPartitionBuffer(ByteBuffer bb) {
		/** 10*6*2 bytes of parameters */
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