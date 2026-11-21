package com.owon.uppersoft.dso.page;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.mode.control.SampleControl;
import com.owon.uppersoft.dso.view.pane.SamplePane;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

/**
 * Sampling  acquisition mode  sampling, peak detect, average (4, 16, 64, 128 times)
 * 
 * @author Matt
 * 
 */
public class SamplePage extends AbstractContentPage {

	public static final String Name = "M.Sample.Name";

	@Override
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward) {
		ControlManager cm = cp.getControlManager();
		SampleControl sc = cm.getSampleControl();
		SamplePane sp = new SamplePane(cm, sc);
		LContainer jp = cp.createContainerWithBackWard(sp, this, beforeApply);
		return jp;
	}

	@Override
	public String getContentID() {
		return Name;
	}

	@Override
	public void beforeLeave() {
	}

	@Override
	public boolean canLeave() {
		return true;
	}
}
