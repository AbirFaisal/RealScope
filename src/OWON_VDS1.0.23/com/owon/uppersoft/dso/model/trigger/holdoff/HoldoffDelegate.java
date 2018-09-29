package com.owon.uppersoft.dso.model.trigger.holdoff;

import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.dso.model.trigger.helper.ETV_Holdoff;
import com.owon.uppersoft.dso.model.trigger.helper.IenumDelegate;
import com.owon.uppersoft.vds.core.trigger.help.EnumNValue;

public class HoldoffDelegate implements IenumDelegate {
	private static final int DefaultStepIndex = 0;

	public static final ETV_Holdoff createETV() {
		return new ETV_Holdoff(new HoldoffDelegate(0));
	}

	public static final ETV_Holdoff createETV(
			HoldoffStage hs, int hov, int stepidx) {
		return createETV(hs.ordinal(), hov, stepidx);
	}

	public static final ETV_Holdoff createETV(int ord,
			int hov, int stepidx) {
		return new ETV_Holdoff(ord, hov,
				new HoldoffDelegate(stepidx));
	}

	public static final ETV_Holdoff createDefaultETV() {
		return createETV(HoldoffStage.stg_xx_xus, HoldoffStage.stg_xx_xus_min,
				DefaultStepIndex);
	}

	public static final String ItemName = "holdoff";

	private int stepidx = 0;

	public HoldoffDelegate(int stepidx) {
		this.stepidx = stepidx;
	}

	public HoldoffStage value(int idx) {
		return HoldoffStage.VALUES[idx];
	}

	public void fromInt(int v, EnumNValue etv) {
		if (v <= 0)
			v = 1;
		HoldoffStage.fromInt(v, etv);
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
	public void nextStep(int idx) {
		stepidx = idx % HoldoffStage.STEP_LENGTH;
	}

	@Override
	public ETV_Holdoff getObject(AbsTrigger at) {
		return at.etvho;
	}
}
