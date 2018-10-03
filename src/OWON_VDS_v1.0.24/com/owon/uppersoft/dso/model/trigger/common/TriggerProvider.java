package com.owon.uppersoft.dso.model.trigger.common;

import com.owon.uppersoft.dso.model.trigger.AbsTrigger;

public interface TriggerProvider {
	/**
	 * 保存触发模式设置
	 * 
	 * @return 该触发模式
	 */
	AbsTrigger submitTrigger();

	AbsTrigger getTrigger();

	/**
	 * 调用该方法来传递修改，并发出指令，触发类设置如果对程序进行的操作， 此方法调用之处还需注意相应的修改
	 */
	void bodySend();

	void submitHoldOff();
}
