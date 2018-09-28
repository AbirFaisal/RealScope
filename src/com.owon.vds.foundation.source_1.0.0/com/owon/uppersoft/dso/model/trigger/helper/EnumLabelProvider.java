package com.owon.uppersoft.dso.model.trigger.helper;

import com.owon.uppersoft.vds.core.trigger.help.EnumNValue;

/**
 * 实现从EnumNValue中引用的枚举类需要具备的额外方法
 * 
 */
public interface EnumLabelProvider {
	/**
	 * 从vpart转换出文本
	 * 
	 * @param vpart
	 * @return
	 */
	String toString(int vpart);

	/**
	 * 结合vpart转换出值用于序列化
	 * 
	 * @param vpart
	 * @return
	 */
	int toInt(int vpart);

	/**
	 * 获取v的下一个有效EnumTypeNValue，返回的类型包含继承自IenumDelegate的类作为泛型参数类
	 * 
	 * @param v
	 * @return
	 */
	EnumNValue getNext(int v, int stepidx);

	/**
	 * 获取v的上一个有效EnumTypeNValue，返回的类型包含继承自IenumDelegate的类作为泛型参数类
	 * 
	 * @param v
	 * @return
	 */
	EnumNValue getPrevious(int v, int stepidx);
	
	int getMinStep();

}