package com.owon.uppersoft.vds.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import com.owon.uppersoft.dso.i18n.I18nProvider;

public class MyFileFilter extends FileFilter {

	public static final MyFileFilter BMPFilter;
	public static final MyFileFilter GIFFilter;
	public static final MyFileFilter PNGFilter;
	public static final MyFileFilter BINFilter;
	public static final MyFileFilter CAPFilter;

	public static final MyFileFilter XlsFilter;
	public static final MyFileFilter CsvFilter;
	public static final MyFileFilter TxtFilter;

	static {
		BMPFilter = new MyFileFilter("Filter.BMP", "bmp");
		GIFFilter = new MyFileFilter("Filter.GIF", "gif");
		PNGFilter = new MyFileFilter("Filter.PNG", "png");
		BINFilter = new MyFileFilter("Filter.Data", "bin");
		CAPFilter = new MyFileFilter("Filter.Record", "cap");

		XlsFilter = new MyFileFilter("Filter.Excel", "xls");
		CsvFilter = new MyFileFilter("Filter.CSV", "csv");
		TxtFilter = new MyFileFilter("Filter.Text", "txt");

	}

	public String ends; // 文件后缀
	public String description;

	@Override
	public String toString() {
		return getDescription();
	}

	public MyFileFilter(String description, String ends) {
		this.ends = ends;
		this.description = description;
	}

	@Override
	// 只显示符合扩展名的文件，目录全部显示
	public boolean accept(File file) {
		if (file.isDirectory())
			return true;
		String fileName = file.getName();
		if (fileName.toUpperCase().endsWith(this.ends.toUpperCase()))
			return true;
		return false;
	}

	@Override
	// 返回这个扩展名过滤器的描述
	public String getDescription() {
		String tail = "(*." + ends + ")";
		return I18nProvider.bundle().getString(description) + tail;
	}

	// 返回这个扩展名过滤器的扩展名
	public String getEnds() {
		return this.ends;
	}
}
