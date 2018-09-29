package com.owon.uppersoft.dso.model.trigger;

import com.owon.uppersoft.dso.i18n.I18nProvider;

public class TrgTypeText {
	private TrgTypeDefine ttd;

	public TrgTypeText(TrgTypeDefine ttd) {
		this.ttd = ttd;
	}

	public TrgTypeDefine getTrgTypeDefine() {
		return ttd;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof TrgTypeText))
			return false;

		TrgTypeText ttt = (TrgTypeText) obj;
		return (ttt.ttd == ttd);// 任一为空也不等
	}

	@Override
	public int hashCode() {
		return ttd.hashCode();
	}

	@Override
	public String toString() {
		return I18nProvider.getResourceBundleProvider().bundle()
				.getString(ttd.key);
	}
}