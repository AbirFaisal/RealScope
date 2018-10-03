package com.owon.uppersoft.dso.page;

import com.owon.uppersoft.dso.view.pane.MathPane;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

/**
 * 波形计算 双波形计算 因数1 ch1,ch2,ch3,ch4 符号 加，减，乘，除 因数2 ch1,ch2,ch3,ch4 FFT 信源
 * ch1,ch2,ch3,ch4 窗口 hamming,rectangle,blackman,hanning 格式 Vrms,dB 缩放
 * X1,X2,X5,X10
 * 
 * @author Matt
 * 
 */
public class MathPage extends AbstractContentPage {

	public static final String Name = "M.Math.Name";
	private MathPane cnp;

	@Override
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward) {
		cnp = new MathPane(cp.getControlManager());
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
		return cnp.canLeave();
	}
}
