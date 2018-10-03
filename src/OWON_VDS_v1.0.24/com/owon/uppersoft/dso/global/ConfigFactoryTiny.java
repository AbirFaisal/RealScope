package com.owon.uppersoft.dso.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.owon.uppersoft.vds.core.pref.Config;
import com.owon.uppersoft.vds.core.pref.StaticPref;
import com.owon.uppersoft.vds.pen.StaticPrefTiny;
import com.owon.uppersoft.vds.util.Pref;

public class ConfigFactoryTiny {

	public static final Config createConfig(File confFile, InputStream dfconf,
			InputStream staticPrefStream) {
		Pref ssPrpt = null;
		Pref dfPrpt = new Pref();

		try {
			dfPrpt.load(dfconf);

			if (confFile.exists() && confFile.isFile()) {
				ssPrpt = new Pref();
				InputStream is = new FileInputStream(confFile);
				ssPrpt.load(is);
			} else {
				/* 如果配置文件不存在，则使用Configuration中的默认值 */
				ssPrpt = dfPrpt;
			}
			// or p.loadFromXML(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Config(ssPrpt, dfPrpt, staticPrefStream) {
			@Override
			protected StaticPref createStaticPref(InputStream staticPrefStream) {
				return new StaticPrefTiny(staticPrefStream);
			}
		};
	}

}
