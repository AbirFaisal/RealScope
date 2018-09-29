package com.owon.uppersoft.dso.function;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.vds.core.aspect.Paintable;
import com.owon.uppersoft.vds.core.aspect.help.AreaImageHelper;
import com.owon.uppersoft.vds.core.aspect.help.IPersistentDisplay;
import com.owon.uppersoft.vds.core.aspect.help.ImageHandler;
import com.owon.uppersoft.vds.core.comm.IRuntime;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.data.Point;

/**
 * PersistentDisplay，余辉画图显示器
 * 
 */
public class PersistentDisplay implements IPersistentDisplay {
	public static final String PERSISTENCE_RESET = "PERSISTENCE_RESET";

	private IRuntime rt;
	private AreaImageHelper gd;
	private Paintable pt;

	private boolean useCanvasBuffer = false;
	private BufferedImage persistbi, tempbi;
	private Thread pThread;
	private PersistenceFader pf;

	public PersistentDisplay(IRuntime rt, AreaImageHelper gd,
			int channelsNumber, Paintable pt) {
		this.rt = rt;
		this.gd = gd;
		this.pt = pt;
		pf = new PersistenceFader(this);// , channelsNumber
	}

	public void adjustView(ScreenContext pc, Rectangle bound) {
		if (useCanvasBuffer) {
			/**
			 * KNOW 出于对原有波形画图代码的保留的考虑，图像缓存的区域是包含四边空白的，到了画图像时只画其中对应到网格内的一部分
			 */
			tryCreateFadeBufferedImage();
			// pc.currentLocInfo = bound;
			if (pf.isOnInfinite())
				resetPersistBufferImage();
		}
	}

	public void paintView(Graphics2D g2d, Point drawsz) {
		if (rt.isAllChannelShutDown())
			return;
		// synchronized (persistbi)
		{
			g2d.drawImage(persistbi, 0, 0, drawsz.x, drawsz.y, 0, 0, drawsz.x,
					drawsz.y, null);
		}
	}

	/**
	 * KNOW 这里是在通信线程中获取数据，和刷屏线程竞争bi，必须竞争，然后才进行下一次获取，否则新旧获取的数据可能互相污染
	 * 
	 * @param pc
	 */
	public void bufferWaveForms(ScreenContext pc, WaveFormManager wfm,
			MainWindow mw) {
		if (pf.isPhosphor())
			bufferPhosphorWaveForms(pc, wfm, mw);
		else
			bufferPersistWaveforms(pc, wfm, mw);
	}

	public void bufferPersistWaveforms(ScreenContext pc, WaveFormManager wfm,
			MainWindow mw) {
		if (persistbi == null)
			return;

		synchronized (persistbi) {
			Graphics2D g2d = (Graphics2D) persistbi.getGraphics();
			/**
			 * 这里原本没有重设pc的currentLocInfo为ChartView的Rectangle，
			 * 
			 * 会导致可能为两个子窗口的Rectangle，而导致画余辉波形左边界不对
			 * 
			 * 为什么这里重设了仍然无法避免画图的时候被改动：因为没有synchronized(pc.currentLocInfo)
			 * 
			 * 不再重设了，余辉画图时使用固定的Rectangle会更容易解决，但是继承结构暂不方便打破，
			 * 所以改为直接在WaveForm内部使用固定的Rectangle，这样改动最小
			 * 
			 * 
			 */
			wfm.paintView(g2d, pc, mw.getChartScreen().getChart().getLocInfo());
			g2d.dispose();
		}
	}

	public void bufferPhosphorWaveForms(ScreenContext pc, WaveFormManager wfm,
			MainWindow mw) {
		if (tempbi == null)
			return;

		synchronized (tempbi) {
			Graphics2D g2d = (Graphics2D) tempbi.getGraphics();
			gd.resetARGBBufferImage(tempbi);
			wfm.paintView(g2d, pc, mw.getChartScreen().getChart().getLocInfo());
			g2d.dispose();
		}
	}

	@Override
	public void syncDealPersistImage(ImageHandler ih) {
		synchronized (persistbi) {
			ih.handle(persistbi, tempbi);
		}
	}

	/**
	 * KNOW 原样，只是图形不是从0,0开始画；原样，方便图形的归图形，方便原来的用原来
	 * 
	 */
	@Override
	public void resetPersistBufferImage() {
		if (useCanvasBuffer)
			synchronized (persistbi) {
				gd.resetARGBBufferImage(persistbi);
				// wfm.paintView(g2d, pc);
				// paintLabels(pc);
			}
	}

	@Override
	public void re_paint() {
		pt.re_paint();
	}

	@Override
	public boolean isRuntime() {
		return rt.isRuntime();
	}

	@Override
	public boolean isExit() {
		return rt.isExit();
	}

	public boolean isUseCanvasBuffer() {
		return useCanvasBuffer;
	}

	public void resetPersistence() {
		if (pThread != null && pThread.isAlive())
			pf.setCover();
	}

	/**
	 * 设置画布是否开启
	 */
	public void setUseCanvasBuffer(boolean use) {
		useCanvasBuffer = use;
		if (use) {
			tryCreateFadeBufferedImage();
		} else {
			persistbi = null;
			tempbi = null;
		}
	}

	private void tryCreateFadeBufferedImage() {
		Point drawsz = gd.getDrawSize();
		if (persistbi == null || persistbi.getHeight() < drawsz.y
				|| persistbi.getWidth() < drawsz.x) {
			persistbi = gd.createARGBScreenBufferedImage();// TYPE_USHORT_565_RGB
			// Graphics2D g2d = pesistbi.createGraphics();
			// AlphaComposite composite = AlphaComposite.getInstance(
			// AlphaComposite.CLEAR, 0.0f);
			// g2d.setComposite(composite);
		}
		tryCreateTempBufImgForPhosphor();
	}

	private void tryCreateTempBufImgForPhosphor() {
		Point drawsz = gd.getDrawSize();
		if (tempbi == null || tempbi.getHeight() < drawsz.y
				|| tempbi.getWidth() < drawsz.x) {
			tempbi = gd.createARGBScreenBufferedImage();
		}
	}

	public void fadeThdOn_Off_UI(int arIdx) {
		if (arIdx == 0) {
			if (persistbi != null)
				destroyFadeThread();
			setUseCanvasBuffer(false);// usbCanvasBuffer=false;bi=null;
		} else {
			setUseCanvasBuffer(true);
			createFadeThread(arIdx);
		}
	}

	private void createFadeThread(int arIdx) {
		if (pThread == null) {
			pf.setFadeTime(arIdx);
			pThread = new Thread(pf);
			pThread.start();
		} else {
			pf.setFadeTime(arIdx);
		}
	}

	/** 线程在处理persistbi画布，摧毁线程前仍需要画布存在 */
	public void destroyFadeThread() {
		pf.setOff();
		if (pThread != null && pThread.isAlive())
			try {
				pThread.join();
				pThread = null;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
	}

}