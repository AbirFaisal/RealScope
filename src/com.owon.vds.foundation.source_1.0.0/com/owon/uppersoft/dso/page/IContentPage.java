package com.owon.uppersoft.dso.page;

import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

/**
 * IContentPage，注意，不要在内部持有对应的子容器页面的引用
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
	 * 创建分页的内容
	 * 
	 */
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward);

	/**
	 * 这里不会默认调用对应Pane的beforeLeave()方法，需要子类继承手动添加
	 */
	public void beforeLeave();

	public boolean canLeave();
}
