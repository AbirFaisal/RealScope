package com.owon.uppersoft.dso.model.trigger.common;

import com.owon.uppersoft.dso.model.trigger.AbsTrigger;

public interface TriggerProvider {
	/**
	 * Save the trigger mode setting
	 * 
	 * @return that trigger mode
	 */
	AbsTrigger submitTrigger();

	AbsTrigger getTrigger();

	/**
	 * Call this method to pass on the modification and send the command; if the trigger class settings perform operations on the program, corresponding changes are also needed where this method is called
	 */
	void bodySend();

	void submitHoldOff();
}
