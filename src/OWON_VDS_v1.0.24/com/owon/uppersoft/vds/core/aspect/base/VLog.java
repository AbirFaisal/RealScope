package com.owon.uppersoft.vds.core.aspect.base;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class VLog implements Logable {

	public boolean on = true;

	@Override
	public void log(Object o) {
		if (on)
			System.out.print(o);
	}

	@Override
	public void logln(Object o) {
		if (on)
			System.out.println(o);
	}

	public void loglnf(String format, Object... args) {
		if (on)
			System.out.println(String.format(format, args));
	}

	public void logByteBuffer(ByteBuffer bb) {
		if (!on)
			return;
		loglnf("pos: %d", bb.position());
		loglnf("limit: %d", bb.limit());
	}
	
	public void logByteBuffer(IntBuffer bb) {
		if (!on)
			return;
		loglnf("pos: %d", bb.position());
		loglnf("limit: %d", bb.limit());
	}
}
