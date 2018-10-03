package com.owon.uppersoft.dso.model.trigger;

import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.dso.source.pack.VTPatchable;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;

public class TrgVTPatch implements VTPatchable {
	private TriggerControl tc;

	public TrgVTPatch(TriggerControl tc) {
		this.tc = tc;
	}

	@Override
	public void submitHoldOff(int chl, AbsTrigger at) {
		Submitable sbm = SubmitorFactory.reInit();

		byte alpha = tc.getChannelModeAlpha();
		at.submitHoldOff(alpha, chl, sbm);
		sbm.apply_trg();
	}

	@Override
	public void submiteUpper_Lower(int chl, SlopeTrigger st,
			TrgCheckType trg_slope_type) {
		Submitable sbm = SubmitorFactory.reInit();

		byte alpha = tc.getChannelModeAlpha();
		sbm.recommendOptimize();
		st.submitUpper_Lower(alpha, chl, sbm, trg_slope_type);
		sbm.apply_trg();
	}

	@Override
	public void submiteVoltsense(int chl, VoltsensableTrigger vt) {
		Submitable sbm = SubmitorFactory.reInit();

		byte alpha = tc.getChannelModeAlpha();
		sbm.recommendOptimize();
		vt.submitVoltsense(alpha, chl, sbm);
		sbm.apply_trg();
	}
}