package com.owon.uppersoft.dso.model.trigger.condition;

import javax.swing.AbstractSpinnerModel;

import com.owon.uppersoft.vds.core.trigger.help.EnumNValue;
import com.owon.uppersoft.vds.core.trigger.help.IStepSpinnerModel;

public class TrgConditionSpinnerModel extends AbstractSpinnerModel implements
		IStepSpinnerModel {
	private EnumNValue etv;

	public TrgConditionSpinnerModel(EnumNValue etv) {
		this.etv = etv;
	}

	@Override
	public Object getPreviousValue() {
		return etv.getPrevious();
	}

	@Override
	public Object getNextValue() {
		return etv.getNext();
	}

	@Override
	public Object getValue() {
		return etv;
	}

	@Override
	public void setValue(Object value) {
		if (value instanceof EnumNValue) {
			EnumNValue that = (EnumNValue) value;
			etv = that;// etv.set(that);//
			fireStateChanged();
		}
	}

	@Override
	public Object createDefaultValue() {
		return TrgConditionDelegate.createDefaultETV();
	}
}