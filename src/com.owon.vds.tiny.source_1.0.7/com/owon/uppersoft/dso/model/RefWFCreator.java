package com.owon.uppersoft.dso.model;

import java.io.File;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.ref.IRefSource;
import com.owon.uppersoft.dso.ref.RefWaveForm;

public class RefWFCreator {

	public static final RefWaveForm loadRefWF(File f, ControlManager cm) {
		if (!f.exists())
			return null;
		RefWaveFromTiny rwf = new RefWaveFromTiny();
		return rwf.loadFromFile(f, cm);
	}

	public static final RefWaveForm createRefWF(ControlManager cm,
			IRefSource wf, WaveFormManager wfm) {
		RefWaveFromTiny rwf = new RefWaveFromTiny();
		return rwf.createFromWF(cm, wf, wfm);
	}
}
