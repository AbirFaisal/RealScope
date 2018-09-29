package com.owon.uppersoft.vds.device.interpret.util;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.device.interpret.TinyCommunicationProtocol;
import com.owon.uppersoft.vds.util.format.EndianUtil;
import com.owon.vds.firm.protocol.AddValueAttachCommand;
import com.owon.vds.firm.protocol.AddressAttachCommand;

public class PackUtil2 {

//	public static final ByteBuffer getCMDBuffer(TinyCommAddress tca) {
//		return getCMDBuffer(tca.getAdd(), tca.getBytes());
//	}

	public static final ByteBuffer getCMDBuffer(final int add, final int bytes) {
		ByteBuffer bb = ByteBuffer.allocate(4 + 1 + bytes);
		byte[] buf = bb.array();

		int p = 0;
		EndianUtil.writeIntL(buf, p, add);
		p += 4;
		buf[p++] = (byte) bytes;

		bb.position(p);

		return bb;
	}

//	public static final byte[] packCMD(TinyCommAddress tca, int value) {
//		return packCMD(tca.getAdd(), tca.getBytes(), value);
//	}

	public static final byte[] packCMD(AddressAttachCommand tca, int value) {
		return packCMD(tca.address, tca.bytes, value);
	}

	public static final byte[] packCMD(AddValueAttachCommand tca) {
		return packCMD(tca, tca.value);
	}

	public static final byte[] packCMD(final int add, final int bytes,
			final int value) {
		ByteBuffer bb = getCMDBuffer(add, bytes);

		byte[] buf = bb.array();
		int p = bb.position();

		int n = bytes;
		int v = value;
		while (n > 0) {
			buf[p++] = (byte) v;
			v = v >> 8;
			n--;
		}

		return buf;
	}

	public static final byte[] packCMD2(int bytes, int value) {
		return packCMD_5(bytes, value);
	}

	public static final byte[] packCMD_5(int prebyte, int value) {
		byte[] buf = new byte[TinyCommunicationProtocol.RESPONSE_LEN];
		int i = 0;
		buf[i++] = (byte) prebyte;

		EndianUtil.writeIntL(buf, i, value);
		/** value 按小尾序放入buf数组后4位 */
		// buf[i++] = (byte) value;
		// buf[i++] = (byte) (value >> 8);
		// buf[i++] = (byte) (value >> 16);
		// buf[i] = (byte) (value >> 24);
		return buf;
	}

}