package com.owon.uppersoft.dso.model.trigger.helper;

import com.owon.uppersoft.dso.model.trigger.condition.TrgConditionDelegate;

public class ETV_TrgConditon extends EnumTypeNValue<TrgConditionDelegate> {

	public ETV_TrgConditon(int en, int v, TrgConditionDelegate t) {
		super(en, v, t);
	}

	public ETV_TrgConditon(TrgConditionDelegate t) {
		super(t);
	}

}