package com.owon.uppersoft.dso.model.trigger.condition;

import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.dso.model.trigger.PulseTrigger;
import com.owon.uppersoft.dso.model.trigger.SlopeTrigger;
import com.owon.uppersoft.dso.model.trigger.helper.ETV_TrgConditon;
import com.owon.uppersoft.dso.model.trigger.helper.IenumDelegate;
import com.owon.uppersoft.vds.core.trigger.help.EnumNValue;

public class TrgConditionDelegate implements IenumDelegate {

	 static int stg_xx0ns_min = 3;
	 static int stg_xx0ns_max = 99, stg_x_xxus_max = 998;
	 static int stg_xx0ns_ministep = 1, stg_x_xxus_ministep = 2;

	public static final int common_min = 100, common_max = 999,
			common_ministep = 1;

	private static final int DefaultStepIndex = 0;

	public static final ETV_TrgConditon createETV() {
		return new ETV_TrgConditon(new TrgConditionDelegate(0));
	}

	public static final ETV_TrgConditon createETV(TrgCondition hs, int hov,
			int stepidx) {
		return createETV(hs.ordinal(), hov, stepidx);
	}

	private static final ETV_TrgConditon createETV(int ord, int hov, int stepidx) {
		return new ETV_TrgConditon(ord, hov, new TrgConditionDelegate(stepidx));
	}

	public static final ETV_TrgConditon createDefaultETV() {
		TrgCondition tc_enum = TrgCondition.stg_xx0ns;
		int min = tc_enum.getMin();
		// System.err.println("min: " + min);
		return createETV(tc_enum, min, DefaultStepIndex);
	}

	public static final String ItemName = "argument";

	private int stepidx = DefaultStepIndex;

	public TrgConditionDelegate(int stepidx) {
		this.stepidx = stepidx;
	}

	public TrgCondition value(int idx) {
		return TrgCondition.VALUES[idx];
	}

	public void fromInt(int v, EnumNValue etv) {
		TrgCondition.fromInt(v, etv);
	}

	@Override
	public void nextStep(int idx) {
		stepidx = idx % TrgCondition.STEP_LENGTH;
	}

	@Override
	public int getStepIndex() {
		return stepidx;
	}

	@Override
	public String itemName() {
		return ItemName;
	}

	@Override
	public ETV_TrgConditon getObject(AbsTrigger at) {
		if (at instanceof PulseTrigger) {
			PulseTrigger pt = (PulseTrigger) at;
			return pt.trgcondition;
		} else if (at instanceof SlopeTrigger) {
			SlopeTrigger st = (SlopeTrigger) at;
			return st.trgcondition;
		}
		return null;
	}

}
