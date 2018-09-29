package com.owon.uppersoft.dso.view.pane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;

import com.owon.uppersoft.dso.function.FFTCursorControl;
import com.owon.uppersoft.dso.function.MarkCursorControl;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.dso.view.pane.dock.widget.GroupPane;
import com.owon.uppersoft.dso.view.pane.dock.widget.ItemPane;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;
import com.owon.uppersoft.vds.ui.widget.custom.CCheckBox;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.custom.CLabel;

/**
 * 
 * 光标 类型 电压，时间，关闭 信源 ch1,ch2,ch3,ch4(如果有fft，则转为fft信源)
 * 
 * 
 * @author Matt
 * 
 */
public class MarkPane extends FunctionPanel {
	private CComboBox marktype, chlccb;
	private boolean listening = false;

	private CCheckBox tbchb, vbchb, frechb, voltchb;
	private CLabel remindtxt;
	private ItemPane remindip;
	GroupPane fftgp;

	public MarkPane(final ControlManager cm) {
		super(cm);
		ncgp();
		nrip();

		nlbl("M.Mark.Channel");
		nrip();
		tbchb = ncb("M.Mark.Timebase");
		nrip();
		vbchb = ncb("M.Mark.Voltbase");

		int channelNumber = cm.getSupportChannelsNumber();

		if (channelNumber > 1)
			chlccb = nccb(cm.getCoreControl().getWaveFormInfos());
		else
			chlccb = null;

		remindip = nrip();
		remindtxt = nlblt("");// 当某通道电压测量目标关闭,该文本更新用以提醒

		fftgp = ncgp();
		fftgp.setVisible(false);
		nrip();
		nlblt("FFT:");
		nrip();
		frechb = ncb("M.Mark.FFTFreq");
		nrip();
		voltchb = ncb("M.Mark.FFTVamp");

		localizeSelf();
		final MarkCursorControl mcctr = cm.mcctr;
		final FFTCursorControl fftctr = cm.fftctr;

		tbchb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!listening)
					return;
				mcctr.setOnTimebaseM(tbchb.isSelected());
				repaintChartScreen();
			}
		});
		vbchb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!listening)
					return;
				mcctr.setOnVoltbaseM(vbchb.isSelected());
				repaintChartScreen();
			}

		});

		if (chlccb != null) {
			chlccb.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (!listening)
						return;
					if (e.getStateChange() != ItemEvent.SELECTED)
						return;
					if (chlccb == null)
						return;
					int idx = chlccb.getSelectedIndex();
					mcctr.chNum = idx;
					WaveFormManager wfm = Platform.getDataHouse()
							.getWaveFormManager();
					wfm.setSelectedWaveForm(idx);
					ChannelInfo ci = wfm.getWaveForm(idx).wfi.ci;
					mcctr.computeYValues(ci.getPos0(), ci.getVoltValue());

					updateRemindtxt(idx);
					Platform.getMainWindow().getToolPane().getInfoPane()
							.updateSelected(idx);

					repaintChartScreen();
				}
			});
		}

		frechb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!listening)
					return;
				// setOnFFT(mcctr);

				fftctr.setOnFrebaseMark(frechb.isSelected());
				repaintChartScreen();
			}
		});

		voltchb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!listening)
					return;
				// setOnFFT(mcctr);

				fftctr.setOnVoltbaseMark(voltchb.isSelected());
				repaintChartScreen();
			}
		});

		loadMarkCursorSelected(mcctr, fftctr);
		listening = true;
	}

	private void repaintChartScreen() {
		Platform.getMainWindow().getChartScreen().re_paint();
	}

	private void updateRemindtxt(int idx) {
		WaveFormInfo wfi = cm.getCoreControl().getWaveFormInfo(idx);
		boolean isoff = !(wfi.ci.isOn());
		remindip.setVisible(isoff);
		if (isoff) {
			remindtxt.setText(wfi.toString() + ' '
					+ I18nProvider.bundle().getString("M.Channel.OffRemind"));
		} else {
			remindtxt.setText("");
		}
	}

	private void loadMarkCursorSelected(MarkCursorControl mcctr,
			FFTCursorControl fftctr) {
		listening = false;

		tbchb.setSelected(mcctr.getOnTimebaseM());
		vbchb.setSelected(mcctr.getOnVoltbaseM());
		frechb.setSelected(fftctr.getOnFrebaseMark());
		voltchb.setSelected(fftctr.getOnVoltbaseMark());

		int chlidx = mcctr.chNum;
		if (chlccb != null)
			chlccb.setSelectedIndex(chlidx);
		updateRemindtxt(chlidx);

		listening = true;
	}

	protected void loadCurrentChannel(int chlidx) {
		listening = false;
		if (chlccb != null)
			chlccb.setSelectedIndex(chlidx);
		updateRemindtxt(chlidx);
		cm.mcctr.chNum = chlidx;
		listening = true;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();
		if (name.equals(PropertiesItem.SELECT_W_F)) {
			int idx = (Integer) evt.getNewValue();
			loadCurrentChannel(idx);
		} else if (name.equals(PropertiesItem.CHANNEL_OPTION)) {
			int idx = 0;
			if (chlccb != null)
				idx = chlccb.getSelectedIndex();
			updateRemindtxt(idx);
		} else if (name.equals(PropertiesItem.UPDATE_CURSOR)) {
			listening = false;
			MarkCursorControl mc = cm.mcctr;
			tbchb.setSelected(mc.getOnTimebaseM());
			vbchb.setSelected(mc.getOnVoltbaseM());
			listening = true;
		}
	}

}
