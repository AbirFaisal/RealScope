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




public class CByteArrayInputStream  {


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

		// TODO CByteArrayInputStream's constructor sets bigendian, which was changed for testing; must be reverted after testing ends
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
		if (buf.isDirect()) {
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
	 * Because buf.get(bs, beg, len) repeatedly calls the single-element getter in the underlying implementation, it is inefficient
	 * 
	 * For optimization, consider using more direct io array operations, which may not be able to use memory mapping; performance depends on the underlying mechanism
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
		// Because buf.get(bs, beg, len) repeatedly calls the single-element getter in the underlying implementation, it is inefficient
		for (int i = 0, j = beg; i < len; i++, j += gap) {
			bs[j] = buf.get();
		}
	}

	public final int byteAt(int i) {
		return buf.get(i);
	}

	/*
	 * Read an array, advance the pointer, fetch the next 4-byte integer in LittleEdian fashion
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
	 * @return number of bytes available starting from the next position after the current pointer
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