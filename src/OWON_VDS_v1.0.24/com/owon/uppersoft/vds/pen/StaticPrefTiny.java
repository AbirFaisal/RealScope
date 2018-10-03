package com.owon.uppersoft.vds.pen;

import java.io.InputStream;

import javax.swing.Icon;

import com.owon.uppersoft.vds.core.pref.StaticPref;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.util.Pref;

public class StaticPrefTiny extends StaticPref {
	protected String pentypeName;

	public StaticPrefTiny(InputStream is) {
		super(is);
	}

	@Override
	public void load(Pref p) {
		super.load(p);
		pentypeName = p.getProperty("VDS1021").trim();
	}

	@Override
	public String getOEMLogoPath() {
		/**
		 * 处理OEM:sainsmart
		 * 
		 * logo，VPO1025，www.sainsmart.com，程序图标
		 * 
		 * */
		String mfid = getManufacturerId();
		return "/com/owon/vds/image/" + mfid + "_" + "logo.png";
	}

	public Icon getLogoIcon(StaticPref sp) {
		String logopath = LogoPath;
		if (sp.isOwon()) {
		} else {
			logopath = getOEMLogoPath();
		}

		Icon i = SwingResourceManager.getIcon(StaticPrefTiny.class, logopath);
		return i;
	}
	
	public String getPentypeName() {
		return pentypeName;
	}
}
