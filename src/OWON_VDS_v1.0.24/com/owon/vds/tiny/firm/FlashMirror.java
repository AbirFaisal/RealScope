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
 * FlashMirror，Flash镜像，可提供设备分区和厂家分区
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
	 * @return 提供原始分区，从可用的起始位置开始
	 */
	private ByteBuffer prepareRawPartition_LE() {
		ByteBuffer b2 = ByteBuffer.wrap(flashArray, 0, flashArray.length);
		b2.order(ByteOrder.LITTLE_ENDIAN);

		/** ByteBuffer包装flash对应的数组，mark只读存储区的起始位置 */
		BufferHandleUtil.skipByteBuffer(b2, DEVPREF_CRCSKIPBYTES
				+ DEVPREF_PTC_VER_LEN);
		return b2;
	}

	/**
	 * @return 提供厂家分区，并已设置的position和limit
	 */
	public ByteBuffer getFactoryPartition_LE() {
		logln("getFactoryPartition_LE");
		ByteBuffer b2 = prepareRawPartition_LE();
		b2.limit(b2.position() + DEVPREF_READ_ONLY_LEN);

		return b2;
	}

	/**
	 * @return 提供设备分区，并已设置的position和limit
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
		/** 前两个字节是特殊码 */
		if (b0 != DEVPREF_RBYTE0 || b1 != DEVPREF_RBYTE1)
			return false;
		int version = EndianUtil.nextIntL(arr, DEVPREF_CRCSKIPBYTES);
		/** 不满足前缀int则视为无效 */
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