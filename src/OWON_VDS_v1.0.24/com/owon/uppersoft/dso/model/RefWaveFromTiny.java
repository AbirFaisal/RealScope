package com.owon.uppersoft.dso.model;

import java.io.File;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.ref.IRefSource;
import com.owon.uppersoft.dso.ref.RefWaveForm;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.machine.TinyMachine;

public class RefWaveFromTiny extends RefWaveForm {

	@Override
	protected void setPK_detect_typeByDrawMode(boolean pk_detect, MachineType mt) {
		if (pk_detect) {
			pk_detect_type = ((TinyMachine) mt).getPKType(drawMode);
		}
	}

	@Override
	protected RefWaveForm loadFromFile(File f, ControlManager cm) {
		return super.loadFromFile(f, cm);
	}

	@Override
	protected RefWaveForm createFromWF(ControlManager cm, IRefSource wf,
			WaveFormManager wfm) {
		return super.createFromWF(cm, wf, wfm);
	}
}