package com.owon.uppersoft.vds.ui.prompt;

import javax.swing.JComponent;

public interface IPopDown {

	void promptUp();

	/**
	 * 渐进式提示
	 * 
	 * @param jp
	 * @param xloc
	 *            如为Integer.MIN_VALUE，则为居中
	 */
	void prompt(JComponent jp, final Runnable r);


	void promptClose();

}