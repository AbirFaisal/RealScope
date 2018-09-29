package com.owon.uppersoft.dso.global;

import com.owon.uppersoft.dso.page.DisplayPage;
import com.owon.uppersoft.dso.page.TriggerPage;
import com.owon.uppersoft.dso.page.function.PageManager;
import com.owon.uppersoft.dso.page.function.UtilityPage;
import com.owon.uppersoft.dso.view.pane.DisplayPane;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.function.UtilityPane;
import com.owon.uppersoft.dso.view.trigger.TriggerPane;
import com.owon.uppersoft.vds.machine.PrincipleTiny;
import com.owon.uppersoft.vds.machine.TinyMachine;
import com.owon.vds.calibration.BaselineCalDelegateTiny;

public class PageManagerTiny extends PageManager {
	@Override
	protected TriggerPage createTriggerPage() {
		return new TriggerPage() {

			@Override
			protected TriggerPane createTriggerPane(ContentPane cp) {
				return new TriggerPaneTiny(cp.getControlManager());
			}
		};
	}

	@Override
	protected UtilityPage createUtilityPage() {
		return new UtilityPage() {
			@Override
			protected UtilityPane createUtilityPane(ContentPane cp) {
				return new UtilityPaneWithBall(cp.getControlManager(), cp, this) {
					@Override
					protected void askAutoCal() {
						new BaselineCalDelegateTiny(mw.getWindow(),
								Platform.getControlApps(),
								(TinyMachine) cm.getMachine(),
								(PrincipleTiny) cm.getPrinciple())
								.askAutoCalibration();
					}
				};
			}
		};
	}

	@Override
	protected DisplayPage createDisplayPane() {
		return new DisplayPage() {
			DisplayPaneTiny dpt;

			@Override
			protected DisplayPane createDisplayPane(ContentPane cp) {
				return dpt = new DisplayPaneTiny(cp.getControlManager());
			}

			@Override
			public void beforeLeave() {
				dpt.beforeLeave();
			}
		};
	}
}