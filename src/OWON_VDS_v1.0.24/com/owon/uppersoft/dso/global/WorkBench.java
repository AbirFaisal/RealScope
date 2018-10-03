package com.owon.uppersoft.dso.global;

import com.owon.uppersoft.dso.view.MainWindow;

/**
 * Workbench, connecting all major functional modules
 * 
 */
public interface WorkBench {

	void join();

	ControlApps getControlApps();

	MainWindow getMainWindow();

	ControlManager getControlManager();

	DataHouse getDataHouse();

	void exit();

}
