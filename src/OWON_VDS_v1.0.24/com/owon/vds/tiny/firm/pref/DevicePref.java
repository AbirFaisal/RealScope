package com.owon.vds.tiny.firm.pref;

import java.beans.PropertyChangeListener;
import java.nio.ByteBuffer;

public interface DevicePref {
	void wrRegister(ByteBuffer bb);

	void rdRegister(ByteBuffer bb);

	void writeArgsAtPartitionBuffer(ByteBuffer bb);

	void readArgsFromPartitionBuffer(ByteBuffer bb);

	void setPropertyChangeListener(PropertyChangeListener pcl);

	public static final String ARGS_UPDATE = "ArgsUpdate";

}