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
		/** 这里作针对硬件频率计的参数按采样率区分判断 */
		int div = FreqRef_100M;
		double v = peroid_num / (double) time_sum * div;
		return v;
	}

	@Override
	public double getLimitFrequency() {
		return 30000000;// 25M的120%即30000000
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
						// 粗调增益
						DefaultCalArgType coarseGain = new DefaultCalArgType(
								ArgType.Gain.ordinal(), Cending.ascending,
								channelNumber, vbNum);
						cmdts.add(coarseGain);

						// 零点步进
						DefaultCalArgType zeroAmplitude = new DefaultCalArgType(
								ArgType.Step.ordinal(), Cending.ascending,
								channelNumber, vbNum);
						cmdts.add(zeroAmplitude);

						// 零点补偿
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

	// 也是最大值
	public static final int dac_complementCode = 1023;
	public static final int dac_defaultCode = 512;

	@Override
	public ArgCreator getArgCreator(final CalArgTypeProvider tran) {
		return new ArgCreator() {
			@Override
			public GainArg createGainArg(int chl, int vb) {
				ICal ic = tran.getSimpleAdjustCMDType(ArgType.Gain);
				/**
				 * 观察DAC的增益调整相对波形adc值可以发现比例关系：
				 * 
				 * 递增关系
				 * 
				 * 电压档位: dac增量:adc增量,接近值
				 * 
				 * 5mV: [增益]100:50,630 ;
				 * 
				 * 50mV: [增益]100:50,630 ;
				 * 
				 * 其它大致: [增益]100:50,630 ;
				 * 
				 * 5V: [增益]100:50,630 ;
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
				 * 观察DAC的补偿或步进调整相对波形adc值可以发现比例关系：
				 * 
				 * 补偿在垂直中心，二者是递减关系；步进在正4格时，二者是递增关系(负4格则递减)
				 * 
				 * 而且可调范围很窄，容易出现溢出
				 * 
				 * 电压档位: dac增量:adc增量,接近值
				 * 
				 * 5mV: [补偿]60:50,630 ; [步进]60:50,110
				 * 
				 * 10mV: [补偿]60:50,630 ; [步进]60:50,110
				 * 
				 * 其它大致: [补偿]60:50,630 ; [步进]60:50,110
				 * 
				 * 是否可以缩小精度
				 * */
				// 使用最接近的数值，后续可支持当当前值和默认值相差太大时，使用默认值作为起始数值
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