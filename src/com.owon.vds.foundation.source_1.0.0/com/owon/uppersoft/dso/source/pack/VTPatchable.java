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
	/** 最后一个参数用于指明改变的是哪一个值，只在改变一个值时调用本方法 */
	void submiteUpper_Lower(int chl, SlopeTrigger st, TrgCheckType trg_slope_type);

	void submitHoldOff(int chl, AbsTrigger at);
}
