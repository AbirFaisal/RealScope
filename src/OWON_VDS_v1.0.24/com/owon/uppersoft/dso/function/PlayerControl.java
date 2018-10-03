package com.owon.uppersoft.dso.function;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import com.owon.uppersoft.dso.function.record.OfflineChannelsInfo;
import com.owon.uppersoft.dso.function.record.OpenPropertyChangeEvent;
import com.owon.uppersoft.dso.function.record.RecordFileIO;
import com.owon.uppersoft.dso.function.record.Timeline;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.data.Point;
import com.owon.uppersoft.vds.ui.prompt.FadeIOShell;

public class PlayerControl implements Runnable, PropertyChangeListener {
	private final int DotsOneCH = 5000;
	private final int NO_USE = -1, VIDEO_PLAY = 1, VIDEO_PAUSE = 2;
	private int playing = NO_USE;
	private int timegap;
	public int counter;
	private int current;

	public int sta, end;
	public boolean cyc;

	private ControlManager cm;
	private OfflineChannelsInfo ci;

	private File path;
	public final String EmptyPath = "";

	public long filesize;

	public PlayerControl(ControlManager cm, Properties p) {
		this.cm = cm;
		load(p);
		ci = new OfflineChannelsInfo();

		resetCurrent();
	}

	public OfflineChannelsInfo getPlayChannelsInfo() {
		return ci;
	}

	private boolean isCurrentReset() {
		return current == 0;
	}

	private void resetCurrent() {
		current = 0;
	}

	public void setTimegap(int timegap) {
		this.timegap = timegap;
	}

	public void setStartEndFrame(int s, int e) {
		if (s > e)
			current = sta = e - 1;
		else
			current = sta = s;
		if (e > counter)
			end = counter - 1;
		else
			end = e;
	}

	public void persist(Properties p) {
		p.setProperty("PlayPath", getFilePath());
	}

	public void load(Properties p) {
		String playPath = p.getProperty("PlayPath", "wave1.cap");
		setPath(new File(playPath), true);
	}

	// public void resetPlay() {
	// playing = NO_USE;
	// }

	// public boolean isInVideoStaus() {
	// return playing == VIDEO_PLAY || playing == VIDEO_PAUSE;
	// }

	public boolean isPlaying() {
		return playing == VIDEO_PLAY;
	}

	public void setPlay(int status) {
		playing = status;
	}

	/**
	 * @return isPlaying，如果为真，则代表继续进行回放，而退出准备进行的其它可能打断回放的操作
	 */
	public boolean confirmGoOnPlaying() {
		boolean keepPlay = isPlaying();
		String msn = I18nProvider.bundle().getString("Status.LeavePlaying");
		if (keepPlay) {
			int re = JOptionPane.showConfirmDialog(Platform.getMainWindow()
					.getFrame(), msn, null, JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (re == JOptionPane.YES_OPTION) {
				pausePlaying();
			}
		}
		return keepPlay;
	}

	public boolean confirmStopRunningforPlaying() {
		boolean canplay = false;
		String msn = I18nProvider.bundle().getString("Status.StopforPlaying");
		int re = JOptionPane.showConfirmDialog(Platform.getMainWindow()
				.getFrame(), msn, null, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (re == JOptionPane.YES_OPTION) {
			Platform.getControlApps().interComm.statusStop(false);
			int counter = 0;
			do {
				try {
					Thread.sleep(50);
					counter++;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (!Platform.getControlApps().getDaemon().isAfterStop()
					&& counter < 60);

			canplay = true;
		} else
			canplay = false;
		return canplay;
	}

	private PropertyChangeListener pcl;

	public void setPropertyChangeListener(PropertyChangeListener pcl) {
		this.pcl = pcl;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		pcl.propertyChange(evt);
	}

	public void setPath(File f, boolean isCanonicalFile) {
		if (isCanonicalFile)
			try {
				path = f.getCanonicalFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		else
			path = f;
	}

	public boolean setPathNCheck(File path, boolean inTread) {
		cm.reloadManager.releaseReloadFlag();
		if (!path.isFile())
			return false;

		setPath(path, true);
		Thread initTimeLine = new Thread() {
			@Override
			public void run() {
				initPlayTimeLine();
			}
		};
		if (inTread)
			initTimeLine.start();
		else
			initTimeLine.run();
		return true;
	}

	public String getFilePath() {
		return path.getPath();
	}

	public Boolean isFilePathEmpty() {
		return path.getPath() == EmptyPath;
	}

	private void initPlayTimeLine() {

		tl = new Timeline();
		if (ba != null)
			ba.dispose();
		ba = new CByteArrayInputStream(path);
		frame0Pos = RecordFileIO.readHeader(ba, this, ci);

		if (frame0Pos <= -1) {
			filesize = -1;
			return;
		}

		if (filesize <= 0)
			return;

		dh = Platform.getDataHouse();

		resetCurrent();

		OpenPropertyChangeEvent pce = new OpenPropertyChangeEvent(this,
				PropertiesItem.READ_HEADER, 0, 0);
		pce.newInt = counter;
		propertyChange(pce);
		RecordFileIO.readTimeline(tl, ba, this);
	}

	public void promptInvalidLoad() {
		ResourceBundle rb = I18nProvider.bundle();
		FadeIOShell fs = new FadeIOShell();
		fs.prompt(rb.getString("M.Record.LoadInvalid"), Platform
				.getMainWindow().getFrame());
	}

	private int frame0Pos;
	private Timeline tl;
	private Thread playtrd;

	public void startPlaying() {
		if (!isPlaying()) {// 从当前帧开始播放文件中的波形
			playing = VIDEO_PLAY;

			if (isCurrentReset())
				ba.reset(frame0Pos);
			threadStart();
		}
	}

	private void threadStart() {
		playtrd = new Thread(this);
		playtrd.start();
	}

	public void pausePlaying() {
		playing = VIDEO_PAUSE;
		// 暂停播放和拖拽的时候一定要join，防止争抢ba.
		// 暂停时线程还没OUT,又new start，有概率会争抢ba;拖拽同理。
		try {
			playtrd.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		playtrd = null;
	}

	public boolean fileInvalid() {
		/** 当前显示的播放地址不存在文件，则返回 */
		return filesize <= 0;
	}

	/**
	 * @return 是否需要继续更新控件文本
	 */
	public boolean switchPlaying() {

		if (!isPlaying()) {// 从当前帧开始播放文件中的波形
			playing = VIDEO_PLAY;

			if (isCurrentReset())
				ba.reset(frame0Pos);
			threadStart();
		} else {// 暂停播放
			pausePlaying();
		}
		return true;
	}

	private CByteArrayInputStream ba;
	private DataHouse dh;

	private int bp, len, num;

	public CByteArrayInputStream getCByteArrayInputStream() {
		return ba;
	}

	public void setCurrentNStreamPointer(int curf) {
		// 设置当前帧
		current = curf;
		// 通过当前帧获取该帧CByteArrayInputStream上的指针
		List<Point> tps = tl.timepoints;
		Iterator<Point> it = tps.iterator();
		Point p;
		// System.out.println("tps.size:" + tps.size());
		if (it.hasNext()) {
			p = it.next();
			len = p.x;// 帧所在buf的位置
			num = p.y;// p.x表示的帧总共个数
		}
		bp = frame0Pos;// 0帧
		for (int f = 0; f < curf; f++) {// curf==pc.getCurrent()
			bp += len;// 加1帧

			num--;
			// 当len改变，得到正确len;
			if (num == 0) {
				if (it.hasNext()) {
					p = it.next();
					len = p.x;
					num = p.y;
				}
			}
		}

		ba.reset(bp);
	}

	public void updateCurrentScreen() {
		RecordFileIO.readFrame(ci, ba);

		ci.loadFinish();
		dh.receiveOfflineVideoData(ci);
	}

	@Override
	public void run() {
		Platform.getDataHouse().getWaveFormManager().resetVbmulti();
		OpenPropertyChangeEvent oevt = new OpenPropertyChangeEvent(this,
				PropertiesItem.PLAY_PROGRESS, null, null);
		IN: while (true) {// current < end // counter
			if (cm.isExit())
				break IN;// playing=false;
			if (!isPlaying())
				break IN;
			/**
			 * 下句pcl会把oevt.newInt发送给界面的timelineslider.setValue(newInt);
			 * updateCurrentScreen()改在setValue(newInt)里调用
			 */
			// updateCurrentScreen();
			current++;

			oevt.newInt = current;
			pcl.propertyChange(oevt);

			try {
				Thread.sleep(timegap);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 如果当前帧播放完毕，清零
			if (current >= end) {// counter
				if (cyc) {
					setCurrentNStreamPointer(sta);
				} else {
					playing = NO_USE;
					oevt.newInt = -1;
					pcl.propertyChange(oevt);

					// resetCurrent();
					current = sta;// 播放完从sta开始不是0
					break IN;
				}
			}
		}

	}

	/** 以下含savedcur的方法，用于离开Play页面current被设0之前，保存current值。 */
	private int savedcur = 0;
	private boolean reset = false;

	public void saveCurrent() {
		savedcur = current;
	}

	public void loadSavedCurrent() {
		if (!reset)
			current = savedcur;
		reset = false;
	}

	public void resetSavedCurrent() {
		savedcur = 0;
		reset = true;
	}

	public int getSavedCurrent() {
		return savedcur;
	}
}
