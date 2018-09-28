package com.owon.uppersoft.dso.model.trigger.helper;

import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.vds.core.trigger.help.EnumNValue;

/**
 * 代理对枚举类的方法调用，并将自身作为泛型的可变参数类，提供给EnumTypeNValue
 * 
 */
public interface IenumDelegate {

	/**
	 * 使用v中的内容设置etv
	 * 
	 * @param v
	 * @param etv
	 */
	void fromInt(int v, EnumNValue etv);

	/**
	 * 返回对应的EnumLabelProvider(也是枚举)实例
	 * 
	 * @param idx
	 * @return
	 */
	EnumLabelProvider value(int idx);

	/**
	 * 从AbsTrigger中取出由本代理类作为泛型参数类的EnumTypeNValue<? extends IenumDelegate>变量
	 * 
	 * @param at
	 * @return
	 */
	EnumNValue getObject(AbsTrigger at);

	/**
	 * 设置一种新的步进速率
	 * 
	 * @param idx
	 */
	void nextStep(int idx);

	/**
	 * @return 步进值
	 */
	int getStepIndex();

	/**
	 * 配置文件中的参数项名字
	 * 
	 * @return
	 */
	String itemName();

}
