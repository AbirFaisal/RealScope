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
	// trg_d_ADD.setAdd(0x2001, 1); // @@@ 触发状态地址
	// CHECK_STOP_ADD.setAdd(0x2062, 1);
	// VIDEOTRGD_ADD.setAdd(0x2002, 1); // 判断视频触发的地址
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
		/** 这里作针对硬件频率计的参数按采样率区分判断 */
		int div = FreqRef_125M;
		double v = (double) peroid_num * div / (double) time_sum;
		return v;
	}

	@Override
	public double getLimitFrequency() {
		return 60000000;// 50M的120%即30000000
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
				 * 观察DAC的补偿或步进调整相对波形adc值可以发现比例关系：
				 * 
				 * 电压档位: dac增量:adc增量,接近值
				 * 
				 * 5mV: [补偿]100:50,2000 ; [步进]100:50,100
				 * 
				 * 10mV: [补偿]200:25,2000 ; [步进]200:25,700
				 * 
				 * 其它大致: [补偿]200:25,2000 ; [步进]200:25,700
				 * 
				 * 是否可以缩小精度
				 * */
				// 询问这样做法背后的原理是否一致；验证实际校正过程中值的变化；考虑增加显示的进度可视
				switch (at) {
				case Compensation:
					df = 500;
				case Step:
					if (vb == 0)
						df = 20;//5mV参考值
					else
						df = 80;//5mv以上参考值
					break;
				}
				ICal ic = tran.getSimpleAdjustCMDType(at);
				return new BaselineArg(chl, vb, ic, dac_complementCode, df) {
					protected void computeNapply(int sign, int step, int vbidx) {
						// /** 最小1个电压档位底层作了特别缩小*/
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
