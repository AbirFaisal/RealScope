package com.owon.uppersoft.vds.ui.prompt;

import javax.swing.JComponent;

public interface IPopDown {

	void promptUp();

	/**
	 * Progressive prompt
	 * 
	 * @param jp
	 * @param xloc
	 *            if Integer.MIN_VALUE, centered
	 */
	void prompt(JComponent jp, final Runnable r);


	void promptClose();

}