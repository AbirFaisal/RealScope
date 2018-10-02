package com.owon.uppersoft.dso.page;

import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

/**
 * IContentPage, note that don't hold a reference to the corresponding subcontainer page internally
 * 
 * @author Matt
 * 
 */
public interface IContentPage {
	public static final int Forward = 1;
	public static final int Backward = -1;
	public static final int Linkward = 0;

	public String getContentID();

	/**
	 * Create paginated content
	 */
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward);

	/**
	 * The beforeLeave() method of the corresponding Pane is not called by
	 * default, and the subclass inheritance needs to be manually added.
	 */
	public void beforeLeave();

	public boolean canLeave();
}
