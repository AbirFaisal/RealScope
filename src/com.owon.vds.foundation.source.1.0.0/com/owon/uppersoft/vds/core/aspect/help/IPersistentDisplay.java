package com.owon.uppersoft.vds.core.aspect.help;

public interface IPersistentDisplay {
	boolean isRuntime();

	void syncDealPersistImage(ImageHandler ih);

	void resetPersistBufferImage();

	boolean isExit();

	void re_paint();
}