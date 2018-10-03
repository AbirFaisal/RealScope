package com.owon.vds.tiny.firm;

import static com.owon.vds.tiny.firm.DeviceFlashCommunicator.DEVPREF_CRCSKIPBYTES;
import static com.owon.vds.tiny.firm.DeviceFlashCommunicator.DEVPREF_RBYTE0;
import static com.owon.vds.tiny.firm.DeviceFlashCommunicator.DEVPREF_RBYTE1;
import static com.owon.vds.tiny.firm.DeviceFlashCommunicator.FLASH_SIZE;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.util.BufferHandleUtil;
import com.owon.uppersoft.vds.util.format.EndianUtil;

/**
 * FlashMirror，Flash image ，Device partition and factory partition are available
 * 
 */
public class FlashMirror implements Logable {
	private byte[] flashArray;

	public FlashMirror() {
		flashArray = new byte[FLASH_SIZE];
	}

	public byte[] array() {
		EndianUtil.writeIntL(flashArray, DEVPREF_CRCSKIPBYTES, DEVPREF_PTC_VER);
		return flashArray;
	}

	/**
	 * @return Provide raw partition，Starting from the available starting position
	 */
	private ByteBuffer prepareRawPartition_LE() {
		ByteBuffer b2 = ByteBuffer.wrap(flashArray, 0, flashArray.length);
		b2.order(ByteOrder.LITTLE_ENDIAN);

		/** ByteBuffer wraps the array corresponding to flash，The starting position of the mark read-only memory area */
		BufferHandleUtil.skipByteBuffer(b2, DEVPREF_CRCSKIPBYTES
				+ DEVPREF_PTC_VER_LEN);
		return b2;
	}

	/**
	 * @return Provide factory partition and set position and limit
	 */
	public ByteBuffer getFactoryPartition_LE() {
		logln("getFactoryPartition_LE");
		ByteBuffer b2 = prepareRawPartition_LE();
		b2.limit(b2.position() + DEVPREF_READ_ONLY_LEN);

		return b2;
	}

	/**
	 * @return Provide device partitioning and set position and limit
	 */
	public ByteBuffer getDeviceArgPartition_LE() {
		logln("getDeviceArgPartition_LE");
		ByteBuffer b2 = prepareRawPartition_LE();
		BufferHandleUtil.skipByteBuffer(b2, DEVPREF_READ_ONLY_LEN);

		// position + limit
		return b2;
	}

	public boolean loadDevPref(byte[] devPref) {
		byte[] arr = flashArray;
		if (devPref == null || devPref.length != arr.length)
			return false;

		System.arraycopy(devPref, 0, arr, 0, arr.length);
		byte b0 = arr[0];
		byte b1 = arr[1];
		/** The first two bytes are special codes */
		if (b0 != DEVPREF_RBYTE0 || b1 != DEVPREF_RBYTE1)
			return false;
		int version = EndianUtil.nextIntL(arr, DEVPREF_CRCSKIPBYTES);
		/** If the prefix int is not satisfied, it is considered invalid. */
		if (version != DEVPREF_PTC_VER)
			return false;

		return true;
	}

	public static void memset(ByteBuffer bb, int v) {
		byte[] arr = bb.array();
		int p = bb.position();
		int l = bb.limit();
		Arrays.fill(arr, p, l, (byte) v);
	}

	private static int DEVPREF_PTC_VER = 0x0002;
	private static int DEVPREF_PTC_VER_LEN = 4;

	private static int DEVPREF_READ_ONLY_LEN = 1000;
	// private static int DEVPREF_READ_WRITE_LEN = 1000;

	@Override
	public void log(Object o) {
	}

	@Override
	public void logln(Object o) {
	}

}