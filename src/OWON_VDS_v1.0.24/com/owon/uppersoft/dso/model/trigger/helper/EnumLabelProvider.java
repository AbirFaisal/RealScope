package com.owon.uppersoft.dso.model.trigger.helper;

import com.owon.uppersoft.vds.core.trigger.help.EnumNValue;

/**
 * Extra methods that an enum class referenced from EnumNValue needs to implement
 * 
 */
public interface EnumLabelProvider {
	/**
	 * Convert text from vpart
	 * 
	 * @param vpart
	 * @return
	 */
	String toString(int vpart);

	/**
	 * Combine with vpart to convert out a value for serialization
	 * 
	 * @param vpart
	 * @return
	 */
	int toInt(int vpart);

	/**
	 * Get the next valid EnumTypeNValue after v; the returned type includes a class inheriting from IenumDelegate as the generic parameter class
	 * 
	 * @param v
	 * @return
	 */
	EnumNValue getNext(int v, int stepidx);

	/**
	 * Get the previous valid EnumTypeNValue before v; the returned type includes a class inheriting from IenumDelegate as the generic parameter class
	 * 
	 * @param v
	 * @return
	 */
	EnumNValue getPrevious(int v, int stepidx);
	
	int getMinStep();

}