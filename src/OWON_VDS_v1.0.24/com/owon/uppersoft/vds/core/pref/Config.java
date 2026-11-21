package com.owon.uppersoft.vds.core.pref;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.core.aspect.help.ILoadPersist;
import com.owon.uppersoft.vds.data.LocaleObject;
import com.owon.uppersoft.vds.util.Pref;

/**
 * Config, configuration center; the configuration center no longer stores the main information itself but relays information internally to the internal logic
 */
public class Config implements PropertiesItem {
	private Pref defalutProperties, sessionProperties;
	private final StaticPref staticPref;

	private List<LocaleObject> locales, locales_support_native;
	private int localeIndex = 0;
	private Pref save_properties;

	protected int getDefaultLocaleIndex(Locale l) {
		int len = locales.size();
		for (int i = 0; i < len; i++) {
			if (locales.get(i).getLocale().equals(l))
				return i;
		}
		return -1;
	}

	public List<LocaleObject> getSupport_native_Locales() {
		return locales_support_native;
	}

	public Config(Pref ssPrpt, Pref dfPrpt, InputStream staticPrefStream) {
		this.sessionProperties = ssPrpt;
		this.defalutProperties = dfPrpt;

		DBG.prepareLogType(ssPrpt.loadInt(PropertiesItem.LOGTYPE), new File(
				"log"));

		staticPref = createStaticPref(staticPrefStream);

		initLocales();
		loadConfigurationFromProperties(ssPrpt);
		I18nProvider.updateLocale(locales.get(localeIndex).getLocale());
	}

	protected StaticPref createStaticPref(InputStream staticPrefStream) {
		return new StaticPref(staticPrefStream);
	}

	private void initLocales() {
		String[] lns = staticPref.getAvailableLocaleNames();
		int len = lns.length;
		locales = new LinkedList<LocaleObject>();
		locales_support_native = new LinkedList<LocaleObject>();
		for (int i = 0; i < len; i++) {
			LocaleObject lo = new LocaleObject(Pref.forLocale(lns[i]));
			locales.add(lo);
			locales_support_native.add(lo);
		}
	}

	/**
	 * Load information from Properties, and also correct invalid information here
	 * 
	 * @param p
	 */
	private void loadConfigurationFromProperties(Pref p) {
		this.localeIndex = getDefaultLocaleIndex(p.loadLocale());
		if (localeIndex < 0) {
			this.localeIndex = 0;
		}
	}

	/**
	 * Persist information to Properties
	 * 
	 * @param p
	 */
	protected void persistConfigurationToProperties(Pref p) {
		p.persistLocale(locales.get(localeIndex).getLocale());
	}

	public void persist(File file, ILoadPersist ilp) {
		save_properties = defalutProperties;// new Properties();//
		ilp.persist(save_properties);
		persistConfigurationToProperties(save_properties);

		try {
			save_properties.store(new FileOutputStream(file), null);
			// or p.storeToXML(new FileOutputStream(file), null,
			// StringPool.UTF8EncodingString);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void releaseSession() {
		sessionProperties = null;
	}

	public void setLocaleIndex(int localeIndex) {
		this.localeIndex = localeIndex;
	}

	public int getLocaleIndex() {
		return localeIndex;
	}

	public StaticPref getStaticPref() {
		return staticPref;
	}

	public List<LocaleObject> getLocales() {
		return locales;
	}

	public void updateLocales(List<LocaleObject> txtlocales) {
		// International version
		if (1 == 1)
			return;
		LocaleObject lo = locales.get(localeIndex);
		Locale def = lo.getLocale();

		Iterator<LocaleObject> it = txtlocales.iterator();
		while (it.hasNext()) {
			// Only use locales_support_native (the list of fully supported models) here, rather than locales, the language support list saved last time (which may come from other loaded model settings)
			if (!locales_support_native.contains(it.next()))
				it.remove();
		}
		if (txtlocales.size() <= 0)
			txtlocales.add(lo);
		locales = txtlocales;

		this.localeIndex = getDefaultLocaleIndex(def);
		if (localeIndex < 0)
			Platform.getDataHouse().adjustLocale_LocalizeWindow(this, 0);
	}

	public Pref getSaveProperties() {
		return save_properties;
	}

	public Pref getFactoryProperties() {
		return defalutProperties;
	}

	public Pref getSessionProperties() {
		return sessionProperties;
	}

}
