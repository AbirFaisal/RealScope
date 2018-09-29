package com.owon.uppersoft.dso.control;

import java.awt.Window;
import java.io.File;

import com.owon.uppersoft.dso.global.DataHouse;

public interface IDataImporter {

	boolean openfile(DataHouse dh, File f);

	boolean openfile(DataHouse dh, File f, Window wnd);

}