package com.owon.uppersoft.dso.page.function;

import com.owon.uppersoft.dso.data.LObject;
import com.owon.uppersoft.dso.page.AbstractContentPage;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.function.UtilityPane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

/**
 * 选项 显示 网格 语言 中文，英文
 * 
 * 
 * 通用 校准 同步输出 触发电平，通过/失败
 * 
 * 打印 帮助 更新 连接
 * 
 * @author Matt
 * 
 */
public class UtilityPage extends AbstractContentPage {

	public static final String Name = "M.Utility.Name";
	public static final LObject[] skintype = { new LObject("M.Skin.Black"),
			new LObject("M.Skin.Blue") };

	@Override
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward) {
		UtilityPane up = createUtilityPane(cp);
		LContainer lc = cp.createContainerWithBackWard(up, this, cp
				.getPageManager().getHomePage());
		return lc;
	}

	protected UtilityPane createUtilityPane(ContentPane cp) {
		return new UtilityPane(cp.getControlManager(), cp, this);
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
