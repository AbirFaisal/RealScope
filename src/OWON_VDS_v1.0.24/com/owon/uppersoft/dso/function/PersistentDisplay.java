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
 * PersistentDisplay, persistence drawing display
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
			 * KNOW To preserve the original waveform drawing code, the image cache area includes blank margins on all four sides, and only the part corresponding to the grid is drawn when drawing the image
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
	 * KNOW Here data is acquired in the communication thread and competes with the refresh thread for bi; it must compete, and only then proceed to the next acquisition, otherwise old and new acquired data may pollute each other
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
			 * Originally, pc's currentLocInfo was not reset to the ChartView's Rectangle here,
			 * 
			 * which could end up being the Rectangle of one of two sub-windows, making the left boundary of the persistence waveform wrong
			 * 
			 * Why resetting here still cannot prevent changes during drawing: because there is no synchronized(pc.currentLocInfo)
			 * 
			 * No longer reset here; using a fixed Rectangle when drawing persistence would be easier to solve, but the inheritance structure is not convenient to break for now,
			 * so a fixed Rectangle is used directly inside WaveForm instead, which is the smallest change
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
	 * KNOW as is, except the graphics are not drawn from 0,0; as is, so that graphics stay with graphics and the original code keeps using the original
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
	 * Set whether the canvas is enabled
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

	/** The thread is processing the persistbi canvas; the canvas must still exist before the thread is destroyed */
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