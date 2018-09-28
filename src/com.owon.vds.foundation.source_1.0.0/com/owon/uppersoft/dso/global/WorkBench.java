package com.owon.uppersoft.dso.global;

import com.owon.uppersoft.dso.view.MainWindow;

/**
 * 工作台，连接各个主要功能模块
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
