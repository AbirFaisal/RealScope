package com.owon.uppersoft.dso.view.trigger;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerSet;
import com.owon.uppersoft.dso.model.trigger.VoltsensableTrigger;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.vds.data.Point;
import com.owon.uppersoft.vds.ui.slider.SliderBarLocation;
import com.owon.uppersoft.vds.ui.slider.SliderDelegate;
import com.owon.uppersoft.vds.ui.slider.SymmetrySliderBar;

public class VoltLevelMouseAdapter extends MouseAdapter implements
		SliderBarLocation {

	private TriggerLoaderPane ep;
	private TriggerControl trgc;
	private int halfRange;

	public VoltLevelMouseAdapter(TriggerLoaderPane ep, TriggerControl trgc,
			int halfRange) {
		this.ep = ep;
		this.trgc = trgc;
		this.halfRange = halfRange;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			TriggerSet ts = trgc.getTriggerSetOrNull(ep.getChannel());
			if (ts == null || !ts.isVoltsenseSupport())
				return;
			WaveFormManager wfm = Platform.getDataHouse().getWaveFormManager();
			wfm.getWaveForm(ep.getChannel()).doubleClickOnLevel();
			// updateTrVtValueLbl(chl);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		AbsTrigger at = ep.getTrigger();
		if (!(at instanceof VoltsensableTrigger))
			return;

		final int x = e.getX();
		final int y = e.getY();
		Point p = getSliderBarLocation(x, y, e.getXOnScreen(),
				e.getYOnScreen(), e.getComponent());
		
		int hr = halfRange;
		final int chl = ep.getChannel();

		WaveFormManager wfm = Platform.getDataHouse().getWaveFormManager();
		ChannelInfo ci = wfm.getWaveForm(chl).wfi.ci;
		int defV = ci.getPos0byRange(hr);

		VoltsensableTrigger vt = (VoltsensableTrigger) at;
		// 附加反相
		int vs = ci.getInverseValue(vt.c_getVoltsense());

		SymmetrySliderBar.createSymmetrySliderFrame(Platform.getMainWindow().getFrame(), p.x, p.y,
				hr, defV, hr - vs, true, Color.LIGHT_GRAY, SliderDelegate.BtnStatusBoth,
				new VoltSliderAdapter(trgc, ci, halfRange, vt), I18nProvider.bundle());
		Platform.getMainWindow().update_ChangeVoltsense(chl);
	}

	@Override
	public Point getSliderBarLocation(int x, int y, int xs, int ys, Component cp) {
		int w = cp.getWidth();

		x = w + xs - x + 5;
		y = ys - y - sliderheight + 20;
		return new Point(x, y);
	}
}