package com.owon.uppersoft.dso.function;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.owon.uppersoft.dso.function.record.OpenPropertyChangeEvent;
import com.owon.uppersoft.dso.function.record.RecordFileIO;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.core.wf.rt.LoadMedia;
import com.owon.uppersoft.vds.util.Pref;

public class RecordControl {
	private boolean recording = false;
	private File path;
	private ControlManager cm;
	private RandomAccessFile raf;

	private int timegap = 10;
	private long sizePos;
	private int counter;// 保存帧数
	private int maxframe;

	public int intervalTime;
	public int endFrame;
	public String recordPath;

	private PropertyChangeListener pcl;

	public boolean isRecording() {
		return recording;
	}

	public void setTimegap(int timegap) {
		this.timegap = timegap;
	}

	public void setMaxframe(int maxframe) {
		this.maxframe = maxframe;
	}

	public int getMaxframe() {
		return maxframe;
	}

	public RecordControl(ControlManager cm, Pref p) {
		this.cm = cm;
		load(p);
		setPath(new File(recordPath));
		keepRecord = false;
	}

	public void setPropertyChangeListener(PropertyChangeListener pcl) {
		this.pcl = pcl;
	}

	public void setPath(File f) {
		try {
			path = f.getCanonicalFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getFilePath() {
		return path.getPath();
	}

	public int getCounter() {
		return counter;
	}

	private boolean filePathValid() {
		boolean pathvalid;// createNewFile
		if (path.exists() && path.canWrite()) {
			pathvalid = true;
		} else {
			try {
				pathvalid = path.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				pathvalid = false;
			}
		}
		return pathvalid;
	}

	/**
	 * @return 是否需要继续更新控件文本
	 */
	public boolean switchRecording(Runnable invalidPathHandler) {
		if (!recording) {// 判断RT，开始录制，写入文件
			if (filePathValid()) {
				recording = true;

				pce = new OpenPropertyChangeEvent(this,
						PropertiesItem.RecordFrameIndex, 0, 0);
				try {
					raf = new RandomAccessFile(path, "rw");
					sizePos = RecordFileIO.writeHeader(raf, cm);
				} catch (FileNotFoundException e) {
					// recording = false;
					// lastRecord = true;
					e.printStackTrace();
				}
				keepRecord = true;
				lastRecord = false;
				counter = 0;

				return true;
			} else {
				invalidPathHandler.run();
				return false;
			}

		} else {// 从录制中停止，结束写入文件
			// 由最后一次recordOnce触发保存
			lastRecord = true;

			return false;
		}
	}

	/**
	 * 录制时停止要数据，因录制在原线程的获取数据之后，而数据不再获取，这里快速模拟一次结束录制关闭文件
	 */
	public void forceStop() {
		if (!recording)
			return;

		lastRecord = true;
		sealTail();
	}

	private boolean keepRecord, lastRecord;
	private OpenPropertyChangeEvent pce;

	/**
	 * 一次写入
	 * 
	 * @param raf
	 * @param cm
	 * @param wfm
	 * @param cti
	 * @return 写入的帧数
	 */
	public int writeOnce(RandomAccessFile raf, ControlManager cm,
			WaveFormManager wfm, LoadMedia cti, RecordControl rc) {
		int ct = rc.getCounter();
		int maxframe = rc.getMaxframe();
		int c = cti.getFrameCount();
		int i = 0;
		while (i < c) {
			RecordFileIO.writeFrame(raf, cm, wfm, cti, i);
			i++;
			if (++ct >= maxframe)
				break;
		}
		return i;
	}

	/**
	 * 此时是在获取数据画图之后，还可以拿到多个帧
	 */
	public void recordOnce(WaveFormManager wfm, LoadMedia lm) {
		// System.err.println("recordOnce: " + keepRecord);
		if (!keepRecord)
			return;
		if (raf == null)
			return;

		if (!checkAppendable()) {
			lastRecord = true;
		}
		if (lastRecord) {// 最后一次
			sealTail();
			return;
		}

		int c = writeOnce(raf, cm, wfm, lm, this);
		counter += c;

		/** 设置最大帧 */
		if (counter >= maxframe && maxframe > 0)
			lastRecord = true;
		pce.newInt = counter;
		pcl.propertyChange(pce);

		/** 在获取数据线程中sleep */
		try {
			Thread.sleep(timegap);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void sealTail() {
		if (!keepRecord)
			return;
		if (raf == null)
			return;
		// System.err.println("lastRecord");
		try {
			int v = (int) raf.getFilePointer();
			raf.seek(sizePos);
			raf.writeInt(v);
			raf.writeInt(timegap);
			raf.writeInt(counter);
			// raf.seek(v);
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		keepRecord = false;
		recording = false;

		pce.newInt = -1;
		pcl.propertyChange(pce);
		// 并fire出结束信息，改变运停按钮文本
	}

	/**
	 * @return 是否到了文件大小限界
	 */
	private boolean checkAppendable() {
		long size = 0;
		try {
			size = raf.getFilePointer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		size = size / 1000000;// M
		if (size >= 4000)
			return false;
		return true;
	}

	public void persist(Pref p) {
		recordPath = getFilePath();
		p.setProperty("RecordPath", recordPath);

		p.persistInt("RecordCounter", counter);
		p.persistInt("RecordIntervalTime", intervalTime);
		p.persistInt("RecordEndFrame", endFrame);
	}

	public void load(Pref p) {
		recordPath = p.getProperty("RecordPath", "wave1.cap");
		counter = p.loadInt("RecordCounter");
		intervalTime = p.loadInt("RecordIntervalTime");
		endFrame = p.loadInt("RecordEndFrame");
		if (endFrame <= 0)
			endFrame = 1;
	}
}
