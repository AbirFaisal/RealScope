package com.owon.uppersoft.vds.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.vds.core.aspect.Localizable;

public class LocalizeCenter {

	public static final String LABEL_DIV_UNITS = "Label.DivUnits";
	public static final String LABEL_DIV_UNIT = "Label.DivUnit";

	private List<Localizable> prime_text_list;
	private List<Localizable> loclist;

	private Map<String, String> cache_txt = new HashMap<String, String>();

	public LocalizeCenter() {
		prime_text_list = new LinkedList<Localizable>();
		loclist = new LinkedList<Localizable>();

		cache_txt.put(LABEL_DIV_UNIT, null);
		cache_txt.put(LABEL_DIV_UNITS, null);

		addPrimeTextLocalizable(cacheLocalizer);

		cacheLocalizer.localize(I18nProvider.bundle());
	}

	public String getCacheText(String key) {
		String v = cache_txt.get(key);
		return v;
	}

	private Localizable cacheLocalizer = new Localizable() {

		@Override
		public void localize(ResourceBundle rb) {
			Set<String> set = cache_txt.keySet();
			for (String key : set) {
				cache_txt.put(key, rb.getString(key));
			}
		}
	};

	/**
	 * 用于后续切换语言的内容，一般是基于文本的控件
	 * 
	 * @param l
	 */
	public void addLocalizable(Localizable l) {
		loclist.add(l);
	}

	/**
	 * 用于优先切换语言的内容，一般是文本
	 * 
	 * @param l
	 */
	public void addPrimeTextLocalizable(Localizable l) {
		prime_text_list.add(l);
	}

	public void reLocalize(ResourceBundle rb) {
		for (Localizable loc : prime_text_list) {
			loc.localize(rb);
		}
		for (Localizable loc : loclist) {
			loc.localize(rb);
		}
	}

}