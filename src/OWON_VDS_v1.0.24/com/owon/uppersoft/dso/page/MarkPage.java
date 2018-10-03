package com.owon.uppersoft.dso.page;

import com.owon.uppersoft.dso.view.pane.MarkPane;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

/**
 * Cursor Type Voltage, time, off Source ch1, ch2, ch3, ch4 (if there is fft, it will be converted to fft source)
 * 
 * @author Matt
 * 
 */
public class MarkPage extends AbstractContentPage {

	public static final String Name = "M.Mark.Name";

	@Override
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward) {
		MarkPane cnp = new MarkPane(cp.getControlManager());
		LContainer jp = cp.createContainerWithBackWard(cnp, this, beforeApply);
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
