package com.owon.vds.tiny.firm.pref;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.util.BufferHandleUtil;
import com.owon.vds.tiny.firm.FlashMirror;

/**
 * Translator_Tiny，用于将指令的参数部分转化为协议规定的数值
 * 
 * 内部通过TuneFunction实现参数转译
 * 
 * 
 * 
 * FactoryPartition_LE
 * 
 * 200以外
 * 
 * Register
 * 
 * 1k以外
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

		/** 写入了当前设置区 */
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
			 * 对不支持的flashMirror进行覆写，防止各种读入时读取出错误的值
			 * 
			 * 从flash的信息读取检测和读取无效覆写两方面防止出错
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
