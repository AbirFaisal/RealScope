package com.owon.uppersoft.vds.core.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import sun.nio.ch.DirectBuffer;

public class CByteArrayInputStream {
	private ByteBuffer buf = null;
	private FileChannel fc = null;
	private File file = null;
	private boolean toDel = false;

	public CByteArrayInputStream(File file) {
		this(file, false);
	}

	private CByteArrayInputStream(File file, boolean toDel) {
		this.file = file;
		this.toDel = toDel;

		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "r");// rw
			fc = raf.getChannel();
			buf = fc.map(MapMode.READ_ONLY, 0, file.length());// READ_ONLY
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// TODO CByteArrayInputStream的构造函数内设置bigendian因为测试作出了修改，测试结束后须改回
		// buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.order(ByteOrder.BIG_ENDIAN);
	}

	public ByteBuffer buf() {
		return buf;
	}

	public CByteArrayInputStream(ByteBuffer buf) {
		this.buf = buf;
		toDel = false;
		this.buf.order(ByteOrder.BIG_ENDIAN);
	}

	public CByteArrayInputStream(byte[] array, int beg, int len) {
		buf = ByteBuffer.wrap(array, beg, len);
		toDel = false;
		buf.order(ByteOrder.BIG_ENDIAN);
	}

	protected void setToDel(boolean toDel) {
		this.toDel = toDel;
	}

	protected boolean isToDel() {
		return toDel;
	}

	public InputStream getInputStream() {
		if (buf.hasArray())
			return new ByteArrayInputStream(buf.array(), buf.position(), buf
					.limit());

		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void dispose() {
		if (fc == null)
			return;

		try {
			fc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (buf instanceof DirectBuffer) {
			DirectBuffer db = (DirectBuffer) buf;
			//db.cleaner().clean();

			System.err.println("RELEASE");
		}

		if (toDel) {
			file.delete();
		}
	}

	public final void get(ByteBuffer buffer) {
		byte[] arr = buffer.array();
		get(arr, buffer.position(), buffer.remaining());
	}

	/**
	 * TODO buf.get(bs, beg, len);
	 * 
	 * 由于buf.get(bs, beg, len)在底层实现反复调用了获取单个元素的方法，效率不高
	 * 
	 * 优化可考虑使用更直接的io数组操作来实现，可能无法使用内存映射，性能决定于底层机制
	 * 
	 */
	public final void get(byte[] bs, int beg, int len) {
		int bslen = bs.length;

		if (bslen < beg + len) {
			System.err.println("CByteArrayInputStream : bslen < len");
			return;
		}
		if (buf.hasArray()) {
			byte[] bufarr = buf.array();
			int bufpos = buf.position();
			System.arraycopy(bufarr, bufpos, bs, beg, len);
			return;
		}

		buf.get(bs, beg, len);
	}

	public final void getIntArray(int[] bs, int beg, int len) {
		int blen = beg + len;
		for (int j = beg; j < blen; j++) {
			bs[j] = buf.getInt();
		}
	}

	public final void getByteAsIntArray(int[] bs, int beg, int len) {
		int blen = beg + len;
		for (int j = beg; j < blen; j++) {
			bs[j] = buf.get();
		}
	}

	public final void gap_get(int[] bs, int beg, int len, int gap) {
		// 由于buf.get(bs, beg, len)在底层实现反复调用了获取单个元素的方法，效率不高
		for (int i = 0, j = beg; i < len; i++, j += gap) {
			bs[j] = buf.get();
		}
	}

	public final int byteAt(int i) {
		return buf.get(i);
	}

	/*
	 * 读取数组，指针步进，用LittleEdian方式取出4字节的下一个整数
	 */
	public final int nextInt() {
		return buf.getInt();
	}

	public final float nextFloat() {
		return buf.getFloat();
	}

	public final double nextDouble() {
		return buf.getDouble();
	}

	public final int pointer() {
		return buf.position();
	}

	public final void reset(int offset) {
		buf.position(offset);
	}

	public final void skip(int offset) {
		buf.position(buf.position() + offset);
	}

	/**
	 * @return 当前指针的下一位起可以提供的字节数
	 */
	public final int available() {
		return buf.remaining();
	}

	public final byte nextByte() {
		return buf.get();
	}

	public final boolean nextBoolean() {
		return buf.get() != 0;
	}
}