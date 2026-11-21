package com.owon.uppersoft.vds.machine;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.source.usb.USBPortsFilter;
import com.owon.uppersoft.vds.core.comm.JobQueueDispatcher;
import com.owon.uppersoft.vds.core.tune.ICal;
import com.owon.uppersoft.vds.device.interpret.DeviceAddressTable;
import com.owon.uppersoft.vds.device.interpret.LowerTranslator;
import com.owon.uppersoft.vds.m50.LowControlManager50M;
import com.owon.uppersoft.vds.m50.PreHandler50M;
import com.owon.uppersoft.vds.m50.Submitor2_50M;
import com.owon.uppersoft.vds.m50.TinyTuneFunctionWithPhaseFine;
import com.owon.uppersoft.vds.pen.DefaultTranslator2;
import com.owon.uppersoft.vds.source.comm.PortFilterTiny;
import com.owon.uppersoft.vds.source.comm.Submitor2;
import com.owon.uppersoft.vds.source.front.AbsPreHandler;
import com.owon.vds.calibration.stuff.BaselineArg;
import com.owon.vds.calibration.stuff.ArgCreator;
import com.owon.vds.calibration.stuff.CalArgTypeProvider;
import com.owon.vds.tiny.circle.GainArg;
import com.owon.vds.tiny.firm.pref.model.ArgType;
import com.owon.vds.tiny.tune.TinyTuneFunction;

public class VDS2052 extends TinyMachine {

	public static final int TB_5ns = 0;
	public static final int TB_1_4kFullScreen = 5;
	public static final int TB_5kFullScreen = 7;
	public static final int TB_50MSAMPLE = 9;

	public static final int VB_200mv = 5;

	public static final int FreqRef_125M = 125000000;

	public VDS2052() {
		setMaxSampleRateWhenChannelsOn(V_250M);
		table = new DeviceAddressTable_50M();
	}

	private DeviceAddressTable table;

	@Override
	public DeviceAddressTable getDeviceAddressTable() {
		return table;
	}

	// @Override
	// public void prepareCommAddress() {
	// SLOWMOVE_ADD.setAdd(0x0a, 1);
	// FORCETRG_ADD.setAdd(0x05, 1);
	// trg_d_ADD.setAdd(0x2001, 1); // @@@ trigger status address
	// CHECK_STOP_ADD.setAdd(0x2062, 1);
	// VIDEOTRGD_ADD.setAdd(0x2002, 1); // address for determining video trigger
	// }

	@Override
	public int saveID() {
		return 102;
	}

	@Override
	public TinyTuneFunction createTinyTuneFunction(int channelsNumber, int[] vbs) {
		TinyTuneFunction ttf = new TinyTuneFunctionWithPhaseFine(
				channelsNumber, vbs);
		return ttf;
	}

	@Override
	public String name() {
		return "VDS2052";// CoreControl.machTypIdMap.get(saveID());//
	}

	@Override
	public boolean isSupportMultiFreqArgument() {
		return true;
	}

	public AbsPreHandler createPreHandler(ControlManager cm,
			LowControlManger lcm) {
		PreHandler50M ph = new PreHandler50M(cm, lcm);
		return ph;
	}

	@Override
	public LowControlManger createLowControlManger(ControlManager cm) {
		return new LowControlManager50M(cm);
	}

	@Override
	public int getMultiFreqArgument(int tbidx) {
		if (tbidx <= 7)
			return 2;
		else
			return 1;
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
		int div = FreqRef_125M;
		double v = (double) peroid_num * div / (double) time_sum;
		return v;
	}

	@Override
	public double getLimitFrequency() {
		return 60000000;// 50M120% of 50M, i.e. 3000000030000000
	}

	public int getDelayAttenuationVBIndex() {
		return VB_200mv;
	}

	public static final int dac_complementCode = 4095;

	@Override
	public LowerTranslator createLowerTranslator(CalArgTypeProvider catp) {
		return new DefaultTranslator2(catp, dac_complementCode);
	}

	@Override
	public ArgCreator getArgCreator(final CalArgTypeProvider tran) {
		return new ArgCreator() {
			@Override
			public GainArg createGainArg(int chl, int vb) {
				ICal ic = tran.getSimpleAdjustCMDType(ArgType.Gain);
				return new GainArg(chl, vb, ic, dac_complementCode, 100);
			}

			@Override
			public BaselineArg createBaselineArg(int chl, int vb, ArgType at) {
				int df = 1000;

				/**
				 * observing the DAC compensation or step adjustment relative to the waveform adc value reveals a proportional relationship:
				 * 
				 * voltage scale: dac increment:adc increment, approximate value
				 * 
				 * 5mV: [compensation]100:50,2000 ; [step]100:50,100
				 * 
				 * 10mV: [compensation]200:25,2000 ; [step]200:25,700
				 * 
				 * others (approx.): [compensation]200:25,2000 ; [step]200:25,700
				 * 
				 * whether the precision can be reduced
				 * */
				// ask whether the principle behind this approach is consistent; verify how the values change during the actual calibration; consider adding a visible progress display
				switch (at) {
				case Compensation:
					df = 500;
				case Step:
					if (vb == 0)
						df = 20;//5mV reference value
					else
						df = 80;//5mv and above reference value
					break;
				}
				ICal ic = tran.getSimpleAdjustCMDType(at);
				return new BaselineArg(chl, vb, ic, dac_complementCode, df) {
					protected void computeNapply(int sign, int step, int vbidx) {
						// /** the lowest voltage scale is specially reduced in the lower layer*/
						if (vbidx < 1)
							step >>= 2;
						super.computeNapply(sign, step, vbidx);
					}

				};
			}
		};
	}

	public USBPortsFilter createPortFilter() {
		return new PortFilterTiny() {
			@Override
			protected String getMachineNameFromCode(int machineCode) {
				if (machineCode == 3)
					return "VDS2052";
				else
					return null;
			}
		};
	}

	@Override
	public Submitor2 createSubmitor(JobQueueDispatcher df, LowerTranslator lt,
			LowControlManger lcm, ControlManager cm) {
		return new Submitor2_50M(df, lt, lcm, cm);
	}
}
