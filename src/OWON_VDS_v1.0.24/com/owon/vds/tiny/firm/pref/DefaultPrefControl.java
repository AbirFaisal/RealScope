package com.owon.vds.tiny.firm.pref;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.util.BufferHandleUtil;
import com.owon.vds.tiny.firm.FlashMirror;

/**
 * Translator_Tiny, used to convert instruction parameters into values defined by the protocol.
 * 
 * Internally, it uses TuneFunction for parameter translation.
 * 
 *
 *
 * FactoryPartition_LE
 * 
 * 200以外
 *
 * Register
 * 
 * DeviceArgPartition_LE
 */

public class DefaultPrefControl implements PrefControl, Logable {

	private DevicePref tf;

	private FlashMirror flashMirror;

	public DefaultPrefControl(DevicePref tf) {
		this.tf = tf;
		flashMirror = new FlashMirror();
	}

	@Override
	public byte[] outputAsSyncImage() {
		return flashMirror.array();
	}

	@Override
	public void save2DevicePart() {
		logln("wrDevicePref");
		ByteBuffer bb = flashMirror.getDeviceArgPartition_LE();
		tf.writeArgsAtPartitionBuffer(bb);
		// FlashMirror.memset(bb, 0);
	}

	@Override
	public void rollFactory2Device() {
		ByteBuffer bb = flashMirror.getFactoryPartition_LE();
		tf.readArgsFromPartitionBuffer(bb);

		save2DevicePart();
	}

	@Override
	public void saveRegistry() {
		wrRegister();
	}

	@Override
	public void save2FactoryPart() {
		logln("wrDevicePref2FactorySetPref");

		/** Write to current settings partition */
		save2DevicePart();

		ByteBuffer bb = flashMirror.getFactoryPartition_LE();
		tf.writeArgsAtPartitionBuffer(bb);

		wrRegister();
		// FlashMirror.memset(bb, 0);
	}

	private static final int FACTORYSET_LEN = 200;

	private void wrRegister() {
		ByteBuffer bb = flashMirror.getFactoryPartition_LE();
		BufferHandleUtil.skipByteBuffer(bb, FACTORYSET_LEN);
		tf.wrRegister(bb);
	}

	private void rdRegister() {
		ByteBuffer bb = flashMirror.getFactoryPartition_LE();
		BufferHandleUtil.skipByteBuffer(bb, FACTORYSET_LEN);
		tf.rdRegister(bb);
	}

	@Override
	public boolean loadSyncImageFromDevice(byte[] devPref) {
		boolean support = flashMirror.loadDevPref(devPref);

		if (!support) {
			/**
			 * Overwrite unsupported flashMirror to prevent various reading errors.
			 * 
			 * Detect and read invalid overwrite from flash information on both sides to prevent errors.
			 */
			save2FactoryPart();
			return false;
		}

		ByteBuffer bb = flashMirror.getDeviceArgPartition_LE();
		tf.readArgsFromPartitionBuffer(bb);

		rdRegister();

		return true;
	}

	@Override
	public void log(Object txt) {
		System.out.print(txt);
	}

	@Override
	public void logln(Object txt) {
		System.out.println(txt);
	}

}
