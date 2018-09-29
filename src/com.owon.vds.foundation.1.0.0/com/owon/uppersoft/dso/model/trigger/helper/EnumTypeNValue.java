package com.owon.uppersoft.dso.model.trigger.helper;

import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.vds.core.trigger.help.EnumNValue;

public class EnumTypeNValue<T extends IenumDelegate> extends EnumNValue {
	private T t;

	public EnumTypeNValue(T t) {
		super();
		this.t = t;
	}

	public EnumTypeNValue(int en, int v, T t) {
		super(en, v);
		this.t = t;
	}

	@Override
	public String toString() {
		return getEnumLabelProvider().toString(v);
	}

	@Override
	public int toInt() {
		// System.err.println("toInt: "+v);
		return getEnumLabelProvider().toInt(v);
	}

	@Override
	public EnumNValue getNext() {
		return getEnumLabelProvider().getNext(v, t.getStepIndex());
	}

	@Override
	public EnumNValue getPrevious() {
		return getEnumLabelProvider().getPrevious(v, t.getStepIndex());
	}

	/** 以下为枚举参数类代理的方法，实际为static的获取继承自EnumLabelProvider的枚举类的方法 */
	public final EnumLabelProvider getEnumLabelProvider() {
		return t.value(en);
	}

	@Override
	public void fromInt(int v) {
		t.fromInt(v, this);
	}

	@Override
	public String itemName() {
		return t.itemName();
	}

	public boolean trySet(AbsTrigger at) {
		return t.getObject(at).set(this);
	}

	public void nextStep(int idx) {
		t.nextStep(idx);
	}

	public int getStepIndex() {
		return t.getStepIndex();
	}

	public int getValueDivTimeOnStage() {
		EnumLabelProvider ep = getEnumLabelProvider();
		int minstep = ep.getMinStep();
		// DBG.configln("divTimeOnStage: " + v + " / " + minstep);
		return v / minstep;
	}

}