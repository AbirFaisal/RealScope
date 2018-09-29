package com.owon.vds.tiny.tune;

import java.awt.Container;

import javax.swing.JTabbedPane;

public interface ITuneFunction {

	JTabbedPane createTabs(Container panel);

	void release();

//	void contentUpdateWithoutSync();

	void doselfcorrect();

//	void updateVBIndexChange(int chl, int vbidx);

//	int getVBIndexForChannel(int chl);
}