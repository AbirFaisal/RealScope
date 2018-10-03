package com.owon.vds.firm.protocol;

import com.owon.uppersoft.vds.device.interpret.util.PackUtil2;

public class AddValueAttachCommand extends AddressAttachCommand {
	public AddValueAttachCommand(String name, int address, int bytes, int value) {
		super(name, address, bytes);
		this.value = value;
	}

	public int value;

	@Override
	public String toString() {
		return super.toString() + ", " + value;
	}

	public byte[] asBytes() {
		return PackUtil2.packCMD(this);
	}
}