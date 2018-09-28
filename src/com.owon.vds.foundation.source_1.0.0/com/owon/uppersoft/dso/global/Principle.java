package com.owon.uppersoft.dso.global;

import java.awt.Window;
import java.beans.PropertyChangeSupport;
import java.io.File;

import com.owon.uppersoft.dso.function.ref.IReferenceWaveForm;
import com.owon.uppersoft.vds.core.comm.JobQueueDispatcher;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.core.machine.MachineInfo;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.util.Pref;

public interface Principle {

	Submitable createSubmitor(JobQueueDispatcher df, ControlManager cm);

	boolean isSameSeries(int id);

	MachineInfo getMachine();

	MachineType getMachineType();

	void persist(Pref p);

	void openTuneDialog(Window wnd, PropertyChangeSupport pcs);

	/**
	 * 这样可以自定tune初始化的时机
	 * 
	 * @param p
	 */
	void prepareTuneFunction(Pref p);

	String getMachineTypeName(int id);

	int getMachineID(String name);

	String getConfigurationDirectory();

}