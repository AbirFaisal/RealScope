package com.owon.uppersoft.dso.model.trigger.helper;

import com.owon.uppersoft.dso.model.trigger.holdoff.HoldoffDelegate;

public class ETV_Holdoff extends EnumTypeNValue<HoldoffDelegate> {

	public ETV_Holdoff(int en, int v, HoldoffDelegate t) {
		super(en, v, t);
	}

	public ETV_Holdoff(HoldoffDelegate t) {
		super(t);
	}

	@Override
	public int enumPart() {
		return super.enumPart() + 2;
	}

}