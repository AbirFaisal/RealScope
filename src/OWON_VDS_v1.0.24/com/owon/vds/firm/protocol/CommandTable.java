package com.owon.vds.firm.protocol;

/**
 * @deprecated
 *
 */
public class CommandTable {
	public static final int RESPONSE_LEN = 5;

	public static final CommandTable instance = new CommandTable();
	// v=0
	public AddValueAttachCommand query_fpga = new AddValueAttachCommand(
			"FPGA_DOWNLOAD_QUERY_ADD", 0x223, 1, 0);

	public AddValueAttachCommand download_fpga = new AddValueAttachCommand(
			"FPGA_DOWNLOAD_ADD", 0x4000, 4, 1);

	public AddValueAttachCommand write_flash = new AddValueAttachCommand(
			"write_flash", 0x1a0, 1, 1);// 写flash
	public AddValueAttachCommand read_flash = new AddValueAttachCommand(
			"read_flash", 0x1b0, 1, 1);// 读flash

	public AddressAttachCommand write_trigger = new AddressAttachCommand(
			"TRG_ADD", 0x24, 2);

	public AddressAttachCommand edge_level_ch1_ADD = new AddressAttachCommand(
			"edge_level_ch1_ADD", 0x2E, 2);

	public AddressAttachCommand trg_holdoff_arg_ch1_ADD = new AddressAttachCommand(
			"trg_holdoff_arg_ch1_ADD", 0x26, 1);
	public AddressAttachCommand trg_holdoff_index_ch1_ADD = new AddressAttachCommand(
			"trg_holdoff_index_ch1_ADD", 0x27, 1);

	public AddressAttachCommand CHL_ON_ADD = new AddressAttachCommand(
			"CHL_ON_ADD", 0xb, 1);

	public AddressAttachCommand volt_gain_ch1_ADD = new AddressAttachCommand(
			"volt_gain_ch1_ADD", 0x116, 2);
	public AddressAttachCommand zero_off_ch1_ADD = new AddressAttachCommand(
			"zero_off_ch1_ADD", 0x10A, 2);
	public AddressAttachCommand channel_ch1_ADD = new AddressAttachCommand(
			"channel_ch1_ADD", 0x111, 1);

	public AddressAttachCommand volt_gain_ch2_ADD = new AddressAttachCommand(
			"volt_gain_ch2_ADD", 0x114, 2);
	public AddressAttachCommand zero_off_ch2_ADD = new AddressAttachCommand(
			"zero_off_ch2_ADD", 0x108, 2);
	public AddressAttachCommand channel_ch2_ADD = new AddressAttachCommand(
			"channel_ch2_ADD", 0x110, 1);

	public AddressAttachCommand SAMPLE_ADD = new AddressAttachCommand(
			"SAMPLE_ADD", 0x09, 1);

	public AddressAttachCommand SUF_TRG_ADD = new AddressAttachCommand(
			"SUF_TRG_ADD", 0x56, 4);
	public AddressAttachCommand PRE_TRG_ADD = new AddressAttachCommand(
			"PRE_TRG_ADD", 0x5a, 2);
	public AddressAttachCommand DM_ADD = new AddressAttachCommand("DM_ADD",
			0x5C, 2);
	public AddressAttachCommand PREGET_SLOW_ADD = new AddressAttachCommand(
			"PREGET_SLOW_ADD", 0xc, 1);

	public AddressAttachCommand TIMEBASE_ADD = new AddressAttachCommand(
			"TIMEBASE_ADD", 0x52, 4);
	public AddressAttachCommand SLOWMOVE_ADD = new AddressAttachCommand(
			"SLOWMOVE_ADD", 0xa, 1);
	public AddressAttachCommand SYNCOUTPUT_ADD = new AddressAttachCommand(
			"SYNCOUTPUT_ADD", 0x6, 1);

	public AddValueAttachCommand trg_d_ADD = new AddValueAttachCommand(
			"trg_d_ADD", 0x1, 1, 0);

}
