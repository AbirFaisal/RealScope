package com.owon.uppersoft.vds.core.pref;

import static com.owon.uppersoft.vds.ui.resource.ResourceCenter.ImageDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.swing.Icon;

import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;
import com.owon.uppersoft.vds.util.Pref;
import com.owon.uppersoft.vds.util.StringPool;

/**
 * StaticPref，静态配置信息
 * 
 */
public class StaticPref {

	protected String productId;
	protected String manufacturerId;
	protected String serialId;
	protected String versionId;
	protected String internalVersionId;
	protected String updateServer;
	protected String website;
	protected boolean updatable;

	protected String launcher;
	protected String localeDir;
	protected String exampleDir;
	protected String reinstallUSBDriverCommand;

	protected int fileHisCount;
	protected String[] localeNames;

	public StaticPref(InputStream is) {
		load(is);
	}

	public static void main(String[] args) {
		InputStream is = StaticPref.class
				.getResourceAsStream("/com/owon/uppersoft/dso/pref/default.ini");
		StaticPref ap = new StaticPref(is);
		System.out.println(ap.isNeutral());
		System.out.println(ap.getUpdateXML());
		System.out.println(ap.getVersionText());
	}

	protected void load(String filename) {
		load(new File(filename));
	}

	protected void load(File file) {
		try {
			load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected void load(InputStream is) {
		Pref p = new Pref();
		try {
			p.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		load(p);
	}

	public void load(Pref p) {
		productId = p.getProperty("productId");
		manufacturerId = p.getProperty("manufacturerId");
		versionId = p.getProperty("versionId");
		serialId = p.getProperty("serialId");
		internalVersionId = p.getProperty("internalVersionId");
		updateServer = p.getProperty("updateServer");
		updatable = p.loadBoolean("updatable");

		launcher = p.getProperty("launcher");
		localeDir = p.getProperty("localeDir");
		exampleDir = p.getProperty("exampleDir");
		reinstallUSBDriverCommand = p.getProperty("reinstallUSBDriverCommand");

		fileHisCount = p.loadInt("fileHisCount");
		String lns = p.getProperty("availableLocales", StringPool.EmptyString);
		localeNames = lns.split(StringPool.SemicolonString);
		website = p.getProperty("website").trim();
	}

	public String[] getAvailableLocaleNames() {
		return localeNames;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getManufacturerId() {
		return manufacturerId;
	}

	public String getVersionId() {
		return versionId;
	}

	public String getInternalVersionId() {
		return internalVersionId;
	}

	public String getUpdateServer() {
		return updateServer;
	}

	public boolean getUpdatable() {
		return updatable;
	}

	public String getLauncher() {
		return launcher;
	}

	protected String getExampleDir() {
		return exampleDir;
	}

	protected int getFileHisCount() {
		return fileHisCount;
	}

	protected String getLocaleDir() {
		return localeDir;
	}

	public String getUpdateXML() {
		return manufacturerId + "_" + getSerialId() + ".xml";
	}

	public String getVersionText() {
		return versionId + " (build " + internalVersionId + ")";
	}

	public String getSerialId() {
		return serialId;
	}

	public boolean isNeutral() {
		return manufacturerId.equalsIgnoreCase(Neutral_String);
	}

	public boolean isOwon() {
		return manufacturerId.equalsIgnoreCase(OWON_String);
	}

	public String getReinstallUSBDriverCommand() {
		return reinstallUSBDriverCommand;
	}

	public String getWebsite() {
		if (isNeutral()) {
			if (website.toLowerCase().contains("owon"))
				website = "";
		} else if (Locale.getDefault().toString().equals("ru")) {
			// website = "www.IzmerimVse.com.ua ,  www.VsemPribor.ru";
		}
		return website;
	}

	/** 产品信息 */
	public static final String Neutral_String = "neutral";
	public static final String OWON_String = "OWON";

	public static final String LogoPath = ImageDirectory + "logo.png";

	/**
	 * 在子类中添加规则
	 * 
	 * @return
	 */
	protected String getOEMLogoPath() {
		return "";
	}

	public Icon getLogoIcon(StaticPref sp) {
		String logopath = LogoPath;
		if (sp.isOwon()) {
		} else {
			logopath = getOEMLogoPath();
		}

		Icon i = SwingResourceManager.getIcon(StaticPref.class, logopath);
		return i;
	}
}