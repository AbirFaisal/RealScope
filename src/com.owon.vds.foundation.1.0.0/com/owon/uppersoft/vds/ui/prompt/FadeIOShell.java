package com.owon.uppersoft.vds.ui.prompt;

import java.awt.Window;

/**
 * 可实现渐变式的置顶显示提示
 * 
 */
public class FadeIOShell {

	public FadeIOShell() {
	}

	/**
	 * 渐变式提示
	 * 
	 * @param m
	 */
	public void prompt(String m, Window owner) {
		prompt(m, owner, 2000);
	}

	/**
	 * 渐变式提示
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