package com.owon.uppersoft.dso.global;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.owon.uppersoft.dso.page.function.UtilityPage;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.function.UtilityPane;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.machine.TinyMachine;
import com.owon.uppersoft.vds.ui.widget.custom.CButton;

public class UtilityPaneWithBall extends UtilityPane {
	public UtilityPaneWithBall(ControlManager cm, ContentPane cp, UtilityPage up) {
		super(cm, cp, up);
	}

	@Override
	protected void createFunctionGroup(MachineType mt, Dimension s,
			ContentPane cp, UtilityPage up) {
		super.createFunctionGroup(mt, s, cp, up);
		final TinyMachine tm = (TinyMachine) mt;
		if (tm.isFetchBallEvent()) {
			nrip();
			CButton ballcb = nbtn("M.Utility.Ball");
			ballcb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					tm.showBallControl(Platform.getControlApps().getDaemon(),
							mw.getFrame());
				}
			});
		}
	}
}