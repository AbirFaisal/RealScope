package com.owon.uppersoft.dso.function;

import java.awt.Color;

import com.owon.uppersoft.dso.data.LObject;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.vds.core.paint.ColorProvider;
import com.owon.uppersoft.vds.data.RGB;
import com.owon.uppersoft.vds.ui.prompt.FadeIOShell;
import com.owon.uppersoft.vds.util.Pref;

public class DisplayControl implements ColorProvider {
	public static final Object[] Persistence = {
			new LObject("M.Display.Persistence.Off"), "0.5s", "1s", "2s", "5s",
			new LObject("M.Display.Persistence.Infinite") };

	public static final Object[] Persistence_phosphor = {
			new LObject("M.Display.Persistence.Off"), "0.5s", "1s", "2s", "5s",
			new LObject("M.Display.Persistence.Infinite"),
			new LObject("M.Display.Phosphor") };

	private boolean phosphorSwitch;
	private int persistenceIndex;
	public int marktypeIndex = 0;
	private RGB gridBrightness = new RGB();
	public boolean linelink;
	private boolean xyon;

	public int wfx = 0, wfy = 1;

	public int CompositDrawTimes = 3;
	public static final int MaxCompositDrawTimes = 10;
	private ControlManager cm;

	public DisplayControl(Pref p, ControlManager cm) {
		this.cm = cm;
		load(p);
		phosphorSwitch = cm.getMachine().isPhosphorOn();
	}

	public Object[] getPersistenceItems() {
		if (phosphorSwitch)
			return Persistence_phosphor;
		else
			return Persistence;
	}

	public boolean isPhosphorOn() {
		return phosphorSwitch;
	}

	public boolean isXYModeOn() {
		return xyon;
	}

	public void setXYMode(boolean on) {
		xyon = on;

		if (!on)
			return;

		if (cm.getDeepMemoryControl().restrictDeepMemory()) {
			Platform.getMainWindow().getToolPane().getDetailPane()
					.updateDM_Sample();

			FadeIOShell pv = new FadeIOShell();
			pv.prompt(
					I18nProvider.bundle().getString(
							"Info.XYModeForceDeepMemory"), Platform
							.getMainWindow().getFrame());
		}
	}

	public int getGridBrightness() {
		return gridBrightness.blue;
	}

	public Color getGridColor() {
		return gridBrightness.getColor();
	}

	private void initGridBrightness(int v) {
		v = Math.min(v, 255);
		gridBrightness.blue = v;
		gridBrightness.green = v;
		gridBrightness.red = v;
	}

	public void changeGridBrightness(int v, MainWindow mw) {
		initGridBrightness(v);
//		if (cm.paintContext != null)
//			cm.paintContext.setGridColor(getGridColor());
		mw.updateGridColor();
	}

	public void loadFactorySet(Pref p) {
		load(p);
	}

	public void load(Pref p) {
		initGridBrightness(p.loadInt("GridBrightness"));
		linelink = p.loadBoolean("LineLink");
		wfx = p.loadInt("WaveFormX");
		wfy = p.loadInt("WaveFormY");
		if (wfx >= cm.getSupportChannelsNumber())
			wfx = cm.getSupportChannelsNumber() - 1;
		if (wfy >= cm.getSupportChannelsNumber())
			wfy = cm.getSupportChannelsNumber() - 1;

		xyon = p.loadBoolean("XYModeOn");

		CompositDrawTimes = p.loadInt("CompositDrawTimes");
		if (CompositDrawTimes > MaxCompositDrawTimes) {
			CompositDrawTimes = MaxCompositDrawTimes;
		} else if (CompositDrawTimes < 1) {
			CompositDrawTimes = 1;
		}
		setPersistenceIndex(p.loadInt("PersistenceIndex"));
		// phosphorSwitch = p.loadBoolean( "phosphorSwitch");
	}

	public void persist(Pref p) {
		p.persistBoolean("XYModeOn", xyon);
		p.persistInt("WaveFormX", wfx);
		p.persistInt("WaveFormY", wfy);
		p.persistInt("GridBrightness", getGridBrightness());
		p.persistBoolean("LineLink", linelink);

		p.persistInt("CompositDrawTimes", CompositDrawTimes);
		p.persistInt("PersistenceIndex", getPersistenceIndex());
		// p.persistBoolean( "phosphorSwitch", phosphorSwitch);
	}

	public int getPersistenceIndex() {
		int len = getPersistenceItems().length;
		if (persistenceIndex >= len)
			return len - 1;
		return persistenceIndex;
	}

	public int setPersistenceIndex(int persistenceIndex) {
		this.persistenceIndex = persistenceIndex;
		return persistenceIndex;
	}

}