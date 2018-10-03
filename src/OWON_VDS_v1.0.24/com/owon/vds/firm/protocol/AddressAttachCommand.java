package com.owon.vds.firm.protocol;

import com.owon.uppersoft.vds.device.interpret.util.PackUtil2;

public class AddressAttachCommand {

	public String name;

	public int address;
	public int bytes;

	public AddressAttachCommand(String name, int address, int bytes) {
		this.name = name;
		this.address = address;
		this.bytes = bytes;
	}

	public void setAdd(int add, int bytes) {
		this.address = add;
		this.bytes = bytes;
	}

	@Override
	public String toString() {
		return "[" + name + "]" + " @0x" + Integer.toHexString(address) + ", "
				+ bytes + " bytes";
	}

	public byte[] asBytes(int value) {
		return PackUtil2.packCMD(this, value);
	}
}