package com.owon.uppersoft.dso.control;

import java.io.RandomAccessFile;

import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;

public interface SaverHandler {

	void prehandle(DMDataInfo cd);

	void handle(DMDataInfo cd, WaveForm wf, RandomAccessFile raf);
}