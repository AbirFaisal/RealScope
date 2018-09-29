package com.owon.uppersoft.dso.control;

import com.owon.uppersoft.dso.model.trigger.TrgCheckType;
import com.owon.uppersoft.dso.wf.WaveForm;

/**
 * 进行判断，然后handle，但是handle的方法默认在判断中调用
 * 
 * @author Matt
 * 
 */
public interface TrgLevelCheckHandler {

	/**
	 * 设置判断对应的波形
	 * 
	 * @param wf
	 */
	void setWaveForm(WaveForm wf);

	/**
	 * 判断给点的值，并进行一次handle，如果trueType为NotOver，则不判断直接处理为NotOver
	 * 
	 * @param targetv
	 * @param trueType
	 * @param falseType
	 * @return
	 */
	boolean checkAroundTrgAndHandle(int targetv, TrgCheckType trueType, TrgCheckType falseType);

	/**
	 * 判断给点的值，并只在为真时handle，如果trueType为NotOver，则直接判断为假
	 * 
	 * @param targetv
	 * @return
	 */
	boolean checkAroundTrgAndHandleOnTrue(int targetv, TrgCheckType trueType);

}