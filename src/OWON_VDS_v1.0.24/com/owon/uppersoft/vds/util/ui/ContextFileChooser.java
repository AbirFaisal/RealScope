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
		// Set the file name to open and its folder
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

		// The current directory is saved only while executing
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