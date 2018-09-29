package com.owon.uppersoft.vds.util.ui;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

public class ContextFileChooser {
	private File dir;
	private JFileChooser jfc;

	public ContextFileChooser() {
	}

	private File show(boolean open) {
		if (dir == null) {
			dir = new File("");
		}
		// 设置打开文件名及所在文件夹
		try {
			jfc = new JFileChooser(dir.getCanonicalFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// jfc.setSelectedFile(file);

		int returnVal = open ? jfc.showOpenDialog(null) : jfc
				.showSaveDialog(null);

		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		// 当前目录只在执行情况下保存
		dir = jfc.getCurrentDirectory();

		File f = jfc.getSelectedFile();
		return f;
	}

	public File save() {
		return show(false);
	}

	public File open() {
		return show(true);
	}
}