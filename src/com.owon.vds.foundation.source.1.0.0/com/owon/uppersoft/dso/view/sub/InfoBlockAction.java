package com.owon.uppersoft.dso.view.sub;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeSupport;

import com.owon.uppersoft.dso.function.MarkCursorControl;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.page.ChannelPage;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.ChartScreen;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.ChannelInfo.Volt;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.data.Point;
import com.owon.uppersoft.vds.ui.prompt.FadeIOShell;
import com.owon.uppersoft.vds.ui.slider.SliderBarLocation;
import com.owon.uppersoft.vds.ui.slider.SliderDelegate;
import com.owon.uppersoft.vds.ui.slider.SymmetrySliderBar;
import com.owon.uppersoft.vds.ui.slider.SymmetrySliderView;
import com.owon.uppersoft.vds.ui.widget.ComboBoxOwner;
import com.owon.uppersoft.vds.ui.widget.OwnedComboBox;

/**
 * InfoBlockAction，Acting different floor operations in InfoBlock, including ComboBox, SlideDelegate, etc.
 */
public class InfoBlockAction implements ComboBoxOwner, SliderDelegate, SliderBarLocation {
	private InfoBlock ib;
	private OwnedComboBox vbcbb = null;
	private MarkCursorControl mcctr;
	private ControlManager cm;
	private PropertyChangeSupport pcs;

	public InfoBlockAction(ControlManager cm, InfoBlock ib) {
		this.ib = ib;
		this.cm = cm;
		pcs = cm.pcs;
		mcctr = cm.mcctr;
	}

	@Override
	public void removeOwnedComboBox() {
		if (vbcbb != null) {
			ib.remove(vbcbb);
			vbcbb = null;
		}
	}

	@Override
	public void afterRemoved() {
		ib.returnVbFloor();
		pcs.firePropertyChange(PropertiesItem.DOCK_REPAINT, null, null);
	}

	public void incretSelect(int del) {
		WaveForm wf = ib.getWaveForm();
		if (!wf.isOn())
			return;
		ChannelInfo ci = wf.wfi.ci;

		selected(ci.getVoltbaseIndex() + del);
	}

	@Override
	public void selected(int idx) {
		// System.err.println(idx);
		WaveForm wf = ib.getWaveForm();
		ChannelInfo ci = wf.wfi.ci;
		WaveFormManager wfm = Platform.getDataHouse().getWaveFormManager();
		mcctr.chNum = ci.getNumber();

		boolean b = wfm.setVoltBaseIndex(wf, idx);// 设置通道电压档位
		if (!b)
			return;
		mcctr.computeYValues(ci.getPos0(), ci.getVoltValue());

		// Platform.getMainWindow().getToolPane().updateTrgVolt();
		/** Math中vrms缩放比率重计算，内部判断只在运行时重算 */
		wfm.getFFTView().updateYScaleRate();
		pcs.firePropertyChange(PropertiesItem.TUNE_VBBCHANGE, null, ci);
	}

	public void onBlockPressed2Select(ChannelInfo ci) {
		/**
		 * The channel change causes the measured value and the trigger
		 * level position to change, and the ChartScreen refresh is changed.
		 *
		 * But when you click to close the channel, setting the current channel
		 * to the cursor channel is not the result of the user's desired operation.
		 */
		mcctr.chNum = ci.getNumber();
		mcctr.computeYValues(ci.getPos0(), ci.getVoltValue());

		/** Click on the channel, ChartScreen, the channel ruler is topped, and the ChartScreen refresh is changed. */
		ChartScreen cs = Platform.getMainWindow().getChartScreen();
		cs.setScreenSelectWFidx(ci.getNumber());
		cs.re_paint();
	}

	public void f2_pos0Pressed(final WaveForm wf, MouseEvent e) {
		ChannelInfo ci = wf.wfi.ci;
		cm.reduceFrame();

		if (vbcbb != null)
			vbcbb.onReleaseFocus();

		Point p = getSliderBarLocation(e.getX(), e.getY(), e.getXOnScreen(),
				e.getYOnScreen(), e.getComponent());
		int x = p.x;
		int y = p.y;
		int hpr = ci.getHalfPosRange();
		int defV = ci.getPos0byRange(hpr);
		if (e.getClickCount() == 2) {
			Dimension sz = new Dimension(SymmetrySliderBar.sliderwidth, SymmetrySliderBar.sliderheight);

			SymmetrySliderView sv = new SymmetrySliderView(sz, (hpr << 1) + 1,
					hpr, defV, true, ci.getColor(), this);
			sv.setDefault();
		} else {
			SymmetrySliderBar.createSymmetrySliderFrame(Platform
					.getMainWindow().getFrame(), x, y, hpr, hpr, defV, true, ci
					.getColor(), BtnStatus0, this, I18nProvider.bundle());
		}
	}

	@Override
	public Point getSliderBarLocation(int x, int y, int xs, int ys, Component cp) {
		int bw = InfoBlock.BlockWidth;
		x = bw + xs - x;
		y = InfoBlock.floor2 + ys - y - sliderheight + 10;
		return new Point(x, y);
	}

	public void f1_vbPressed(ChannelInfo ci, int f1y) {
		cm.reduceFrame();

		ib.requestFocus();
		if (vbcbb == null) {
			Volt[] vs = ci.getVoltageLabels();
			vbcbb = new OwnedComboBox(vs, ci.getVoltbaseIndex(), this, true);
			vbcbb.setMaximumRowCount(vs.length);
			vbcbb.setSelectedIndex(ci.getVoltbaseIndex());
			vbcbb.setBounds(30, f1y - 19, 90, 22);
			ib.add(vbcbb);
			vbcbb.requestFocus();
			vbcbb.showPopup();
		}
	}

	public void f0_back_uncheck_Pressed(WaveForm wf) {
		ChannelInfo ci = wf.wfi.ci;
		boolean on = ci.isOn();

		/** 如果通道在运行时是关闭着的，当它停止下来也没数据，做成不让打开该通道 */
		WaveFormManager wfm = Platform.getDataHouse().getWaveFormManager();
		if (!on && wfm.isNoWFDataFilled(wf)) {
			FadeIOShell fs = new FadeIOShell();
			fs.prompt(I18nProvider.bundle().getString("M.Channel.NoData"),
					Platform.getMainWindow().getFrame());
			return;
		}

		// if (on) {
		// /** 要关闭 */
		// MathControl mc = cm.mathControl;
		// if (mc.isFFTon() && mc.getFFTchl() == ci.number) {
		// /** 提示一下通道在做fft，将使fft失效，然后不允许关闭 */
		// FadeIOShell pv = new FadeIOShell();
		// pv.prompt(I18nProvider.bundle().getString(
		// "M.Math.FFT.chlturnoffWarn"), Platform.getMainWindow()
		// .getFrame());
		// return;
		// }
		// }

		ci.c_setOn(!on);
		MainWindow mw = Platform.getMainWindow();
		if (on && cm.mathControl.isMathChannelUse(wf.getChannelNumber())) {
			String text = I18nProvider.bundle().getString("M.Math.Name") + ":"
					+ wf.toString() + " "
					+ I18nProvider.bundle().getString("M.Channel.OffRemind");

			FadeIOShell fs = new FadeIOShell();
			fs.prompt(text, mw.getFrame());
		}
		pcs.firePropertyChange(PropertiesItem.CHANNEL_OPTION, -1,
				ci.getNumber());
		pcs.firePropertyChange(PropertiesItem.TUNE_CHLNUMSCHANGE, null, null);
		mw.channelOnOffRepaint(wf);
		// pcs.firePropertyChange(PropertiesItem.UPDATE_SWITCH, null,
		// null);

	}

	public void f1_couplingPressed(ChannelInfo ci) {
		ci.c_setNextCoupling();
		pcs.firePropertyChange(PropertiesItem.COUPLING_OPTION, null, null);
		ib.repaint();
	}

	protected void clickInverse(ChannelInfo ci) {
		/** 加入反相点击切换 */
		ci.c_setInverse(!ci.isInverse());
		/** 请求更新触发电平等值 */
		pcs.firePropertyChange(PropertiesItem.UPDATE_VOLTSENSE, -1,
				ci.getNumber());
		Platform.getMainWindow().updateShow();
		ib.repaint();
	}

	public void f1_notPressed() {
		if (vbcbb != null)
			vbcbb.onReleaseFocus();
	}

	public void onClicked(int clickCount) {
		if (clickCount == 1) {
			pcs.firePropertyChange(PropertiesItem.CHOOSE_CHANNELS, null, null);
		} else if (clickCount == 2) {
			dockDialogQuickAct();
		}
	}

	/**
	 * @RF 改用fire
	 */
	public void dockDialogQuickAct() {
		cm.getDockControl().dockDialogQuickOpenHide(ChannelPage.Name);
	}

	@Override
	public void valueChanged(int oldV, int newV) {
		WaveForm wf = ib.getWaveForm();
		ChannelInfo ci = wf.wfi.ci;
		/** newV值以屏幕左上角为零点坐标,转化为屏幕中间坐标传入使用 */
		newV = ci.getHalfPosRange() - newV;//
		valueChanged_Directly(newV);
	}

	public int valueChanged_Directly(int newV) {
		WaveForm wf = ib.getWaveForm();
		ChannelInfo ci = wf.wfi.ci;
		// v=mcctr.limitPos0Edge(v);//限制零点位置上下边界
		MainWindow mw = Platform.getMainWindow();
		DataHouse dh = Platform.getDataHouse();
		dh.getWaveFormManager().setZeroYLoc(wf, newV, true);
		ib.updatePos0();
		ib.repaint();

		mcctr.computeYValues(ci.getPos0(), ci.getVoltValue());
		mw.update_Pos0();
		return ci.getPos0();
	}

	public void actionOff() {
	}

	@Override
	public void on50percent() {
	}

	@Override
	public void onDispose() {
	}

}
