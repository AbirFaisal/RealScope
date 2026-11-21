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
		// changed from global to per-object
		// divided by model
		// changed from enum to class
	}

	public AddressAttachCommand TRG_D_ADD = new AddressAttachCommand(
			"TRG_D_ADD", 0x01, 1);

	public AddressAttachCommand datafinished_ADD = new AddressAttachCommand(
			"datafinished_ADD", 0x7a, 1);

	public AddressAttachCommand VIDEOTRGD_ADD = new AddressAttachCommand(
			"VIDEOTRGD_ADD", 0x2, 1); // address for determining video trigger

	/** Frequency reference address: ch1, ch2 */
	public AddressAttachCommand channel_freqref[] = {
			new AddressAttachCommand("ADD_CH1_FreqRef", 0x4a, 1),
			new AddressAttachCommand("ADD_CH2_FreqRef", 0x4b, 1) };

	/** Channel setting parameter address: ch1, ch2 */
	public AddressAttachCommand channel_set[] = {
			new AddressAttachCommand("channel_ch1_ADD", 0x111, 1),
			new AddressAttachCommand("channel_ch2_ADD", 0x110, 1) };
	/** Gain parameter address: ch1, ch2 */
	public AddressAttachCommand volt_gain[] = {
			new AddressAttachCommand("volt_gain_ch1_ADD", 0x116, 2),
			new AddressAttachCommand("volt_gain_ch2_ADD", 0x114, 2) };
	/** Zero offset parameter address: ch1, ch2 */
	public AddressAttachCommand zero_offset[] = {
			new AddressAttachCommand("zero_off_ch1_ADD", 0x10a, 2),
			new AddressAttachCommand("zero_off_ch2_ADD", 0x108, 2) };
	/** Edge trigger level parameter address: ch1, ch2 */
	public AddressAttachCommand edge_level[] = {
			new AddressAttachCommand("edge_level_ch1_ADD", 0x2e, 2),
			new AddressAttachCommand("edge_level_ch2_ADD", 0x30, 2), //
			new AddressAttachCommand("edge_level_ext_ADD", 0x10c, 1) };
	/** Pulse width trigger level parameter address: ch1, ch2 */
	public AddressAttachCommand pulse_level[] = {
			new AddressAttachCommand("pulse_level_ch1_ADD", 0x2e, 2),
			new AddressAttachCommand("pulse_level_ch2_ADD", 0x30, 2) };
	/** Slope trigger threshold parameter address: ch1, ch2 */
	public AddressAttachCommand slope_thredshold[] = {
			new AddressAttachCommand("slope_thred_ch1_ADD", 0x10, 2),
			new AddressAttachCommand("slope_thred_ch2_ADD", 0x12, 2) };

	/** Parameter address when the pulse width/slope trigger time condition is =: ch1(h/l), ch2(h/l) */
	public AddressAttachCommand trg_condtion_equal_hl[][] = {
			{
					new AddressAttachCommand("trg_cdt_equal_h_ch1_ADD", 0x32, 2),
					new AddressAttachCommand("trg_cdt_equal_l_ch1_ADD", 0x36, 2) },
			{
					new AddressAttachCommand("trg_cdt_equal_h_ch2_ADD", 0x3a, 2),
					new AddressAttachCommand("trg_cdt_equal_l_ch2_ADD", 0x3e, 2) } };

	/** Parameter address when the pulse width/slope trigger time condition is >, <: ch1/ch2 */
	public AddressAttachCommand trg_condtion_gl[] = {
			new AddressAttachCommand("trg_cdt_gl_ch1_ADD", 0x42, 2),
			new AddressAttachCommand("trg_cdt_gl_ch2_ADD", 0x46, 2) };

	/** Trigger holdoff parameter address: ch1(h/l), ch2(h/l) */
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
			"GETDATA2_ADD", 0x2000, 2); // read address

	public AddressAttachCommand FORCETRG_ADD = new AddressAttachCommand(
			"FORCETRG_ADD", 0x0c, 1); // !!! only the low 2 bits are used; otherwise the cpu sends values during slow sweep

	public AddressAttachCommand VIDEOLINE_ADD = new AddressAttachCommand(
			"VIDEOLINE_ADD", 0x32, 2); //

	public AddressAttachCommand CHECK_STOP_ADD = new AddressAttachCommand(
			"CHECK_STOP_ADD", 0xB1, 1); // @@@
	public AddressAttachCommand RUNSTOP_ADD = new AddressAttachCommand(
			"RUNSTOP_ADD", 0x61, 1); // @@@

	public AddressAttachCommand TIMEBASE_ADD = new AddressAttachCommand(
			"TIMEBASE_ADD", 0x52, 4); // divider coefficient
	public AddressAttachCommand MULTIFREQ_ADD = new AddressAttachCommand(
			"MULTIFREQ_ADD", 0x50, 1); // multiplier coefficient

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

	// vds2052-specific resampling
	public AddressAttachCommand RE_COLLECT = new AddressAttachCommand(
			"RE_COLLECT", 0x03, 1);

}