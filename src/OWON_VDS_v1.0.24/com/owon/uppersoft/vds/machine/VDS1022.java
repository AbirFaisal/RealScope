package com.owon.uppersoft.vds.machine;

import java.util.List;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.source.usb.USBPortsFilter;
import com.owon.uppersoft.vds.core.comm.JobQueueDispatcher;
import com.owon.uppersoft.vds.core.tune.Cending;
import com.owon.uppersoft.vds.core.tune.ICal;
import com.owon.uppersoft.vds.device.interpret.DefaultTranslator;
import com.owon.uppersoft.vds.device.interpret.DeviceAddressTable;
import com.owon.uppersoft.vds.device.interpret.LowerTranslator;
import com.owon.uppersoft.vds.source.comm.PortFilterTiny;
import com.owon.uppersoft.vds.source.comm.Submitor2;
import com.owon.uppersoft.vds.source.front.AbsPreHandler;
import com.owon.uppersoft.vds.source.front.PreHandler;
import com.owon.vds.calibration.stuff.BaselineArg;
import com.owon.vds.calibration.stuff.ArgCreator;
import com.owon.vds.calibration.stuff.CalArgTypeProvider;
import com.owon.vds.tiny.circle.GainArg;
import com.owon.vds.tiny.firm.pref.model.ArgType;
import com.owon.vds.tiny.firm.pref.model.TuneModel;
import com.owon.vds.tiny.tune.TinyTuneFunction;
import com.owon.vds.tiny.tune.detail.DefaultCalArgType;

public class VDS1022 extends TinyMachine {

	public static final int TB_1_4kFullScreen = 6;// protected
	public static final int TB_5kFullScreen = 9;// protected

	private static final int VB_200mv = 5;

	private DeviceAddressTable table;

	public VDS1022() {
		setMaxSampleRateWhenChannelsOn(V_100M);
		table = new DeviceAddressTable();
	}

	@Override
	public DeviceAddressTable getDeviceAddressTable() {
		return table;
	}

	@Override
	public int saveID() {
		return 100;
	}

	@Override
	public boolean isSupportNetwork() {
		return 1 == 0;
	}

	@Override
	public String name() {
		return "VDS1022";// CoreControl.machTypIdMap.get(saveID());//
	}

	@Override
	public String series() {
		return "VDS_C2";
	}

	@Override
	public boolean bandLimit() {
		return false;
	}

	@Override
	public double doFrequencyCompute(int peroid_num, int time_sum) {
		/** here the parameters for the hardware frequency counter are distinguished by sampling rate */
		int div = FreqRef_100M;
		double v = peroid_num / (double) time_sum * div;
		return v;
	}

	@Override
	public double getLimitFrequency() {
		return 30000000;// 25M120% of 25M is 3000000030000000
	}

	public int getDelayAttenuationVBIndex() {
		return VB_200mv;
	}

	@Override
	public LowerTranslator createLowerTranslator(CalArgTypeProvider catp) {
		return new DefaultTranslator(catp);
	}

	@Override
	public TinyTuneFunction createTinyTuneFunction(int channelsNumber, int[] vbs) {
		TinyTuneFunction ttf = new TinyTuneFunction(channelsNumber, vbs) {
			@Override
			protected TuneModel createTuneModel(int channelsNumber, int vbNum) {
				return new TuneModel(channelsNumber, vbNum) {
					@Override
					protected void fillArgTypeList(
							List<DefaultCalArgType> cmdts, int channelNumber,
							int vbNum) {
						// coarse gain adjustment
						DefaultCalArgType coarseGain = new DefaultCalArgType(
								ArgType.Gain.ordinal(), Cending.ascending,
								channelNumber, vbNum);
						cmdts.add(coarseGain);

						// zero point step
						DefaultCalArgType zeroAmplitude = new DefaultCalArgType(
								ArgType.Step.ordinal(), Cending.ascending,
								channelNumber, vbNum);
						cmdts.add(zeroAmplitude);

						// zero point compensation
						DefaultCalArgType zeroCompensation = new DefaultCalArgType(
								ArgType.Compensation.ordinal(),
								Cending.descending, channelNumber, vbNum);
						cmdts.add(zeroCompensation);
					}
				};
			}
		};
		return ttf;
	}

	// also the maximum value
	public static final int dac_complementCode = 1023;
	public static final int dac_defaultCode = 512;

	@Override
	public ArgCreator getArgCreator(final CalArgTypeProvider tran) {
		return new ArgCreator() {
			@Override
			public GainArg createGainArg(int chl, int vb) {
				ICal ic = tran.getSimpleAdjustCMDType(ArgType.Gain);
				/**
				 * observing the DAC gain adjustment relative to the waveform adc value reveals a proportional relationship:
				 * 
				 * increasing relationship
				 * 
				 * voltage scale: dac increment:adc increment, approximate value
				 * 
				 * 5mV: [gain]100:50,630 ;
				 * 
				 * 50mV: [gain]100:50,630 ;
				 * 
				 * others (approx.): [gain]100:50,630 ;
				 * 
				 * 5V: [gain]100:50,630 ;
				 * 
				 * */
				return new GainArg(chl, vb, ic, 99999, 800) {
					@Override
					protected void computeNapply(int sign, int step, int vbidx) {
//						if (vbidx == 0 || vbidx == 10) {
//							step = step << 1;
//						}
						step = step >> 1;
						super.computeNapply(sign, step, vbidx);
					}
				};
			}

			@Override
			public BaselineArg createBaselineArg(int chl, int vb, ArgType at) {
				ICal ic = tran.getSimpleAdjustCMDType(at);

				BaselineArg arg = null;
				int df = dac_defaultCode;
				/**
				 * observing the DAC compensation or step adjustment relative to the waveform adc value reveals a proportional relationship:
				 * 
				 * compensation at the vertical center: the two are in a decreasing relationship; step at +4 divisions: the two are in an increasing relationship (decreasing at -4 divisions))
				 * 
				 * and the adjustable range is very narrow, so overflow occurs easily
				 * 
				 * voltage scale: dac increment:adc increment, approximate value
				 * 
				 * 5mV: [compensation]60:50,630 ; [step]60:50,110
				 * 
				 * 10mV: [compensation]60:50,630 ; [step]60:50,110
				 * 
				 * others (approx.): [compensation]60:50,630 ; [step]60:50,110
				 * 
				 * whether the precision can be reduced
				 * */
				// use the closest value; later it could be supported to use the default value as the starting value when the current value differs too much from the default
				switch (at) {
				case Compensation:
					df = 630;
					arg = new BaselineArg(chl, vb, ic, dac_complementCode, df);
					break;
				case Step:
					df = 111;
					arg = new BaselineArg(chl, vb, ic, dac_complementCode, df);
					break;
				}

				return arg;
			}
		};
	}

	public USBPortsFilter createPortFilter() {
		return new PortFilterTiny() {
			@Override
			protected String getMachineNameFromCode(int machineCode) {
				if (machineCode == 1)
					return "VDS1022";
				else
					return null;
			}
		};
	}

	@Override
	public Submitor2 createSubmitor(JobQueueDispatcher df, LowerTranslator lt,
			LowControlManger lcm, ControlManager cm) {
		return new Submitor2(df, lt, lcm, cm);
	}

	public AbsPreHandler createPreHandler(ControlManager cm,
			LowControlManger lcm) {
		return new PreHandler(cm, lcm);
	}

	public LowControlManger createLowControlManger(ControlManager cm) {
		return new LowControlManager20M(cm);
	}
}