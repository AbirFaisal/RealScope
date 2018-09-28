package com.owon.uppersoft.dso.view.trigger;

import java.beans.PropertyChangeEvent;

import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.dso.model.trigger.common.IValueChange;
import com.owon.uppersoft.dso.model.trigger.common.TriggerProvider;
import com.owon.uppersoft.dso.model.trigger.helper.EnumTypeNValue;
import com.owon.uppersoft.dso.model.trigger.helper.IenumDelegate;
import com.owon.uppersoft.dso.view.pane.dock.widget.GroupPane;
import com.owon.uppersoft.vds.core.aspect.Localizable;

public abstract class TriggerLoaderPane extends GroupPane implements
		Localizable, TriggerProvider {
	protected class DefaultValueChange implements IValueChange {
		@Override
		public void stateChanged(Object o) {
			AbsTrigger at = (AbsTrigger) getTrigger();

			EnumTypeNValue<? extends IenumDelegate> v = (EnumTypeNValue<? extends IenumDelegate>) o;

			if (v.trySet(at)) {
				submitChange();
				// System.out.println("send");// v + "{,}" + ov
			}
		}

		protected void submitChange() {
		}
	}

	protected class HoldoffValueChange extends DefaultValueChange {
		@Override
		protected void submitChange() {
			submitHoldOff();
		}
	}

	private TriggerPane tp;

	public TriggerLoaderPane(TriggerPane tp) {
		this.tp = tp;
	}

	public void localizeSelf() {
		localize(I18nProvider.bundle());
	}

	public abstract void loadTrigger(int chl);

	protected abstract void addListeners();

	public abstract void fireProperty(PropertyChangeEvent evt);

	@Override
	public void submitHoldOff() {
		getTriggerPane().submitHoldOff(getChannel(), this);
	}

	public TriggerPane getTriggerPane() {
		return tp;
	}

	private int chl;

	protected void setChannel(int chl) {
		this.chl = chl;
	}

	protected int getChannel() {
		return chl;
	}

}
