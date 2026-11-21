package com.owon.uppersoft.dso.model.trigger.helper;

import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.vds.core.trigger.help.EnumNValue;

/**
 * Delegates method calls to the enum class, and provides itself as the generic variable-parameter class to EnumTypeNValue
 * 
 */
public interface IenumDelegate {

	/**
	 * Use the contents of v to set etv
	 * 
	 * @param v
	 * @param etv
	 */
	void fromInt(int v, EnumNValue etv);

	/**
	 * Return the corresponding EnumLabelProvider (also an enum) instance
	 * 
	 * @param idx
	 * @return
	 */
	EnumLabelProvider value(int idx);

	/**
	 * Take from AbsTrigger the EnumTypeNValue<? extends IenumDelegate> variable that has this delegate class as its generic parameter class
	 * 
	 * @param at
	 * @return
	 */
	EnumNValue getObject(AbsTrigger at);

	/**
	 * Set a new step rate
	 * 
	 * @param idx
	 */
	void nextStep(int idx);

	/**
	 * @return step value
	 */
	int getStepIndex();

	/**
	 * name of the parameter item in the configuration file
	 * 
	 * @return
	 */
	String itemName();

}
