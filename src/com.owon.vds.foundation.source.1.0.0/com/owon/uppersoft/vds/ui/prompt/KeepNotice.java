package com.owon.uppersoft.vds.ui.prompt;

import com.owon.uppersoft.dso.global.Platform;

public class KeepNotice {

	// private static LinkedList<String> msgs = new LinkedList<String>();
	private static KeepNoticeDialog nd;

	public static void notice(String msg) {
		if (nd == null) {
			nd = new KeepNoticeDialog(Platform.getMainWindow().getWindow());
		}
		nd.setMessage(msg);
		nd.keepShow();
	}

}
