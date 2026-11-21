package com.owon.uppersoft.dso.source.pack;

import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.dso.model.trigger.SlopeTrigger;
import com.owon.uppersoft.dso.model.trigger.TrgCheckType;
import com.owon.uppersoft.dso.model.trigger.VoltsensableTrigger;

/**
 * For Voltsense Or Thredshold
 * 
 */
public interface VTPatchable {
//	boolean isTrgEnable();
//
//	void doSubmit();

	void submiteVoltsense(int chl, VoltsensableTrigger vt);
	/** The last parameter indicates which value changed; this method is only called when a single value changes */
	void submiteUpper_Lower(int chl, SlopeTrigger st, TrgCheckType trg_slope_type);

	void submitHoldOff(int chl, AbsTrigger at);
}
