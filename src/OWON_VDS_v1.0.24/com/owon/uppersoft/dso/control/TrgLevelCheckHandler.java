package com.owon.uppersoft.dso.control;

import com.owon.uppersoft.dso.model.trigger.TrgCheckType;
import com.owon.uppersoft.dso.wf.WaveForm;

/**
 * Do the check and then handle, but the handle method is called inside the check by default
 * 
 * @author Matt
 * 
 */
public interface TrgLevelCheckHandler {

	/**
	 * Set the waveform that the check corresponds to
	 * 
	 * @param wf
	 */
	void setWaveForm(WaveForm wf);

	/**
	 * Check the given value and handle once; if trueType is NotOver, skip the check and treat it directly as NotOver
	 * 
	 * @param targetv
	 * @param trueType
	 * @param falseType
	 * @return
	 */
	boolean checkAroundTrgAndHandle(int targetv, TrgCheckType trueType, TrgCheckType falseType);

	/**
	 * Check the given value and handle only when true; if trueType is NotOver, directly judge it as false
	 * 
	 * @param targetv
	 * @return
	 */
	boolean checkAroundTrgAndHandleOnTrue(int targetv, TrgCheckType trueType);

}