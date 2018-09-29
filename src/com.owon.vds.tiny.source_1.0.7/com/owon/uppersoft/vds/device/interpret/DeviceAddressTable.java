package com.owon.uppersoft.vds.device.interpret;

//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.ADD_CH1_FreqRef;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.ADD_CH2_FreqRef;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.channel_ch1_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.channel_ch2_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.edge_level_ch1_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.edge_level_ch2_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.edge_level_ext_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.pulse_level_ch1_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.pulse_level_ch2_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.slope_thred_ch1_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.slope_thred_ch2_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.trg_cdt_equal_h_ch1_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.trg_cdt_equal_h_ch2_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.trg_cdt_equal_l_ch1_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.trg_cdt_equal_l_ch2_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.trg_cdt_gl_ch1_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.trg_cdt_gl_ch2_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.trg_holdoff_arg_ch1_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.trg_holdoff_arg_ch2_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.trg_holdoff_arg_ext_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.trg_holdoff_index_ch1_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.trg_holdoff_index_ch2_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.trg_holdoff_index_ext_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.volt_gain_ch1_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.volt_gain_ch2_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.zero_off_ch1_ADD;
//import static com.owon.uppersoft.vds.device.interpret.TinyCommAddress.zero_off_ch2_ADD;

import com.owon.vds.firm.protocol.AddressAttachCommand;

public class DeviceAddressTable {
	public DeviceAddressTable() {
		// 从全局改为对象
		// 根据机型划分
		// 从枚举改为类
	}

	public AddressAttachCommand TRG_D_ADD = new AddressAttachCommand(
			"TRG_D_ADD", 0x01, 1);

	public AddressAttachCommand datafinished_ADD = new AddressAttachCommand(
			"datafinished_ADD", 0x7a, 1);

	public AddressAttachCommand VIDEOTRGD_ADD = new AddressAttachCommand(
			"VIDEOTRGD_ADD", 0x2, 1); // 判断视频触发的地址

	/** 频率参考地址: ch1, ch2 */
	public AddressAttachCommand channel_freqref[] = {
			new AddressAttachCommand("ADD_CH1_FreqRef", 0x4a, 1),
			new AddressAttachCommand("ADD_CH2_FreqRef", 0x4b, 1) };

	/** 通道设置参数地址: ch1, ch2 */
	public AddressAttachCommand channel_set[] = {
			new AddressAttachCommand("channel_ch1_ADD", 0x111, 1),
			new AddressAttachCommand("channel_ch2_ADD", 0x110, 1) };
	/** 增益参数地址: ch1, ch2 */
	public AddressAttachCommand volt_gain[] = {
			new AddressAttachCommand("volt_gain_ch1_ADD", 0x116, 2),
			new AddressAttachCommand("volt_gain_ch2_ADD", 0x114, 2) };
	/** 零点偏移参数地址: ch1, ch2 */
	public AddressAttachCommand zero_offset[] = {
			new AddressAttachCommand("zero_off_ch1_ADD", 0x10a, 2),
			new AddressAttachCommand("zero_off_ch2_ADD", 0x108, 2) };
	/** 边沿触发电平参数地址: ch1, ch2 */
	public AddressAttachCommand edge_level[] = {
			new AddressAttachCommand("edge_level_ch1_ADD", 0x2e, 2),
			new AddressAttachCommand("edge_level_ch2_ADD", 0x30, 2), //
			new AddressAttachCommand("edge_level_ext_ADD", 0x10c, 1) };
	/** 脉宽触发电平参数地址: ch1, ch2 */
	public AddressAttachCommand pulse_level[] = {
			new AddressAttachCommand("pulse_level_ch1_ADD", 0x2e, 2),
			new AddressAttachCommand("pulse_level_ch2_ADD", 0x30, 2) };
	/** 斜率触发阈值参数地址: ch1, ch2 */
	public AddressAttachCommand slope_thredshold[] = {
			new AddressAttachCommand("slope_thred_ch1_ADD", 0x10, 2),
			new AddressAttachCommand("slope_thred_ch2_ADD", 0x12, 2) };

	/** 脉宽、斜率触发时间条件为=时的参数地址: ch1(h/l), ch2(h/l) */
	public AddressAttachCommand trg_condtion_equal_hl[][] = {
			{
					new AddressAttachCommand("trg_cdt_equal_h_ch1_ADD", 0x32, 2),
					new AddressAttachCommand("trg_cdt_equal_l_ch1_ADD", 0x36, 2) },
			{
					new AddressAttachCommand("trg_cdt_equal_h_ch2_ADD", 0x3a, 2),
					new AddressAttachCommand("trg_cdt_equal_l_ch2_ADD", 0x3e, 2) } };

	/** 脉宽、斜率触发时间条件为>、<时的参数地址: ch1/ch2 */
	public AddressAttachCommand trg_condtion_gl[] = {
			new AddressAttachCommand("trg_cdt_gl_ch1_ADD", 0x42, 2),
			new AddressAttachCommand("trg_cdt_gl_ch2_ADD", 0x46, 2) };

	/** 触发释抑的参数地址: ch1(h/l), ch2(h/l) */
	public AddressAttachCommand trg_holdoff[][] = {
			{
					new AddressAttachCommand("trg_holdoff_arg_ch1_ADD", 0x26, 1),
					new AddressAttachCommand("trg_holdoff_index_ch1_ADD", 0x27,
							1) },
			{
					new AddressAttachCommand("trg_holdoff_arg_ch2_ADD", 0x2a, 1),
					new AddressAttachCommand("trg_holdoff_index_ch2_ADD", 0x2b,
							1) },
			//
			{
					new AddressAttachCommand("trg_holdoff_arg_ext_ADD", 0x26, 1),
					new AddressAttachCommand("trg_holdoff_index_ext_ADD", 0x27,
							1) } };

	public AddressAttachCommand EMPTY_ADD = new AddressAttachCommand(
			"EMPTY_ADD", 0x10c, 1); //

	public AddressAttachCommand GETDATA2_ADD = new AddressAttachCommand(
			"GETDATA2_ADD", 0x2000, 2); // 读取地址

	public AddressAttachCommand FORCETRG_ADD = new AddressAttachCommand(
			"FORCETRG_ADD", 0x0c, 1); // !!! 只用低2位，其它情况cpu会在慢扫时发值

	public AddressAttachCommand VIDEOLINE_ADD = new AddressAttachCommand(
			"VIDEOLINE_ADD", 0x32, 2); //

	public AddressAttachCommand CHECK_STOP_ADD = new AddressAttachCommand(
			"CHECK_STOP_ADD", 0xB1, 1); // @@@
	public AddressAttachCommand RUNSTOP_ADD = new AddressAttachCommand(
			"RUNSTOP_ADD", 0x61, 1); // @@@

	public AddressAttachCommand TIMEBASE_ADD = new AddressAttachCommand(
			"TIMEBASE_ADD", 0x52, 4); // 分频系数
	public AddressAttachCommand MULTIFREQ_ADD = new AddressAttachCommand(
			"MULTIFREQ_ADD", 0x50, 1); // 倍频系数

	public AddressAttachCommand SLOWMOVE_ADD = new AddressAttachCommand(
			"SLOWMOVE_ADD", 0xa, 1); // @@@
	public AddressAttachCommand SAMPLE_ADD = new AddressAttachCommand(
			"SAMPLE_ADD", 0x09, 1); // @@@
	public AddressAttachCommand PF_ADD = new AddressAttachCommand("PF_ADD",
			0x07, 1); // !!!
	public AddressAttachCommand SYNCOUTPUT_ADD = new AddressAttachCommand(
			"SYNCOUTPUT_ADD", 0x06, 1); // @@@

	public AddressAttachCommand TRG_ADD = new AddressAttachCommand("TRG_ADD",
			0x24, 2); //
	public AddressAttachCommand CHL_ON_ADD = new AddressAttachCommand(
			"CHL_ON_ADD", 0xb, 1); // @@@
	public AddressAttachCommand SUF_TRG_ADD = new AddressAttachCommand(
			"SUF_TRG_ADD", 0x56, 4); //
	public AddressAttachCommand PRE_TRG_ADD = new AddressAttachCommand(
			"PRE_TRG_ADD", 0x5a, 2); //
	public AddressAttachCommand DM_ADD = new AddressAttachCommand("DM_ADD",
			0x5C, 2); //

	public AddressAttachCommand PHASE_FINE = new AddressAttachCommand(
			"PHASE_FINE", 0x18, 2); //

	// vds2052专用重采
	public AddressAttachCommand RE_COLLECT = new AddressAttachCommand(
			"RE_COLLECT", 0x03, 1);

}