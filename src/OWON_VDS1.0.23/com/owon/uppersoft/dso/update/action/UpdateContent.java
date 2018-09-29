package com.owon.uppersoft.dso.update.action;

import java.awt.Window;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.vds.core.pref.StaticPref;
import com.owon.uppersoft.vds.core.update.IUpdatable;

/**
 * 可用于更新的本地操作
 */
public class UpdateContent implements IUpdatable {

	private StaticPref pre;
	private UpdateAction up;
	private Window wnd;
	private String downTempDir;

	public UpdateContent(UpdateAction ua, ControlManager cm, Window wnd,
			String downTempDir) {
		this.up = ua;
		this.wnd = wnd;
		this.downTempDir = downTempDir;
		pre = cm.getConfig().getStaticPref();
	}

	public void close() {
		wnd.dispose();
	}

	public Window getWindow() {
		return wnd;
	}

	public void startAgain() {
		try {
			Runtime.getRuntime().exec(pre.getLauncher());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getRelativePath() {
		return pre.getUpdateXML();// "OWON_VDS01.xml";
	}

	public List<String> getUpdatableServers() {
		List<String> servers = Arrays.asList(pre.getUpdateServer());
		// new LinkedList<String>();//
		// servers.add("http://127.0.0.1/RCPUpdate/");
		return servers;
	}

	public String getConfigurationDir() {
		return downTempDir;
	}

	public void notifyDestroy() {
		up.release();
	}

	public String getProductVersion() {
		return pre.getVersionId();// "2.0.5.0";//
	}

	@Override
	public ResourceBundle bundle() {
		return I18nProvider.bundle();
	}

}
