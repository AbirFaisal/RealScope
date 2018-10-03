package com.owon.uppersoft.vds.ui.prompt;

import java.awt.Window;

/**
 * Gradient top display hints
 * 
 */
public class FadeIOShell {

	public FadeIOShell() {
	}

	/**
	 * Gradient prompt
	 * 
	 * @param m
	 */
	public void prompt(String m, Window owner) {
		prompt(m, owner, 2000);
	}

	/**
	 * Gradient prompt
	 * 
	 * @param m
	 */
	public void prompt(String m, Window owner, int time) {
		NoticeDialog dlg = new NoticeDialog(owner);
		dlg.setMessage(m);
		dlg.keepShow(time);
	}

	public static void main(String[] args) {
		FadeIOShell pv = new FadeIOShell();
		pv.prompt("Hello World! Hello World! Hello World! Hello World!", null);

		// pv.settle("Hello World!");
		// pv.shutdown();
	}

}