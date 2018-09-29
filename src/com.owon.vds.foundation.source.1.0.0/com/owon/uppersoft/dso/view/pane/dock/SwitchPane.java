package com.owon.uppersoft.dso.view.pane.dock;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JPanel;

import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.vds.ui.layout.LayoutManagerAdapter;

/**
 * 通过增加动画帧的间隔时间，减少帧次数，来达到在不同性能设备上的一直表现
 * 
 */
public class SwitchPane extends JPanel {
	private Dimension size = new Dimension();
	private Container curr;
	private Container next;

	/**
	 * Create the panel
	 */
	public SwitchPane() {
		/** 防亮背景色闪屏 */
		// setBackground(Color.black);
		setLayout(new LayoutManagerAdapter() {
			@Override
			public void layoutContainer(Container parent) {
				if (b)
					doResize();
			}
		});
	}

	public void doResize() {
		getSize(size);
		if (curr != null) {
			curr.setBounds(0, 0, size.width, size.height);
		}
	}

	/**
	 * 设置下一页面，适配窗口大小
	 * 
	 * 完成切换动作
	 * 
	 * @param c
	 */

	private void doload() {
		int w = size.width, h = size.height;
		curr.setBounds(-w, 0, w, h);
		next.setBounds(0, 0, w, h);
		removePrevious();
	}

	public void doSwitch(Container c, int toward, boolean isSwitch) {
		if (curr == null) {
			add(c);
			curr = c;
			next = null;
			curr.setBounds(0, 0, size.width, size.height);
		} else {
			add(c);
			next = c;
			if (isSwitch)
				flipContainers(toward);
			else
				doload();
		}
	}

	/**
	 * 移除前者
	 */
	protected void removePrevious() {
		Container tmp = curr;
		if (tmp != null)
			remove(tmp);
		curr = next;
		next = null;
		curr.setBounds(0, 0, size.width, size.height);
	}

	boolean b = true;

	/**
	 * 切换到下一页面
	 * 
	 * TODO 由于快速点击多次快捷按钮会产生多个线程竞争UI资源，这里还可以再优化
	 */
	protected void flipContainers(final int toward) {
		Thread t = new Thread() {
			@Override
			public void run() {
				b = false;
				switch (toward) {
				case IContentPage.Forward:
					flipRight();
					break;
				case IContentPage.Backward:
					flipLeft();
					break;
				case IContentPage.Linkward:
					flipRight();
					break;
				}
				removePrevious();
				b = true;
			}
		};
		t.start();
	}

	public static int MoveTime = 30, FilpTimes = 3, GapTime = MoveTime
			/ FilpTimes;

	private void flipRight() {
		int w = size.width, h = size.height;
		int xc = 0, xn = w;
		try {

			int t = GapTime;
			int MoveDistance = w / FilpTimes;
			int c = FilpTimes;
			while (c > 0) {
				xc -= MoveDistance;
				xn -= MoveDistance;
				curr.setBounds(xc, 0, w, h);
				next.setBounds(xn, 0, w, h);
				revalidate();
				c--;
				Thread.sleep(t);
			}

			curr.setBounds(-w, 0, w, h);
			next.setBounds(0, 0, w, h);
			revalidate();

		} catch (InterruptedException e) {
			e.printStackTrace();
			revalidate();
		}
	}

	private void flipLeft() {
		int w = size.width, h = size.height;
		int xc = 0, xn = -w;
		try {

			int t = GapTime;
			int MoveDistance = w / FilpTimes;
			int c = FilpTimes;

			while (c > 0) {
				xc += MoveDistance;
				xn += MoveDistance;
				curr.setBounds(xc, 0, w, h);
				next.setBounds(xn, 0, w, h);
				revalidate();
				c--;
				Thread.sleep(t);
			}

			curr.setBounds(w, 0, w, h);
			next.setBounds(0, 0, w, h);
			revalidate();

		} catch (InterruptedException e) {
			e.printStackTrace();
			revalidate();
		}
	}
}
