package com.owon.uppersoft.dso.function;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.vds.util.StringPool;

public class DocsManager {

	private ControlManager cm;

	public DocsManager(ControlManager cm) {
		this.cm = cm;
	}

	public final void helpAction() {
		String path = getHelpPath(Locale.getDefault());
		File doc = new File(path);
		if (!doc.exists()) {
			path = getHelpPath(Locale.ENGLISH);
			doc = new File(path);
		}
		String chmfile = "hh  " + doc.getAbsolutePath();// "cmd /c start "
		try {
			Runtime.getRuntime().exec(chmfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// docs/VDS_S4/en/help/VDS_S4_Help_en.chm
	private String getHelpPath(Locale loc) {
		String docs = getExternalDoc();
		String locale = loc.toString();
		String folder = "/help/" + getMachineSeriesName() + "_Help_";
		String suffix = StringPool.DotString
				+ StringPool.CHMFileFormatExtension;

		return docs + locale + folder + locale + suffix;
	}

	public String getExternalDoc() {
		String doc = StringPool.DOCFileFormatExtension + "/"
				+ getMachineSeriesName() + "/";
		return doc;
	}

	private String getMachineSeriesName() {
		String name = cm.getMachine().name();
		if (name.equals("VDS1021"))
			name = "RDS_C1";
		else
			name = cm.getMachine().series();
		return name;
	}

}
