package com.owon.uppersoft.vds.core.update;

import java.awt.Window;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;


public class DefaultUpdatable implements IUpdatable {
	
//	public DefaultUpdatable(JFrame checkUpdateFrame){
//		this.checkUpdateFrame=checkUpdateFrame;
//	}
	@Override
	public Window getWindow() {
		return null;
	}
	
	public String getRelativePath() {
		return "OWON_Oscilloscope.xml";
	}

	public List<String> getUpdatableServers() {
		List<String> servers = new LinkedList<String>();
//		servers.add("http://www.owon.com.cn/images/upfile/softdown/RCPUpdate/");
		servers.add("http://127.0.0.1/RCPUpdate/");
		/* http://127.0.0.1/RCPUpdate/ */
		return servers;
	}

	public void close() {
//		CheckUpdateFrame.getCheckUpdateFrame().dispose();
	}

	public void startAgain() {
	}

//	public static void main(String[] args) {
//	}

	public String getConfigurationDir() {
		/* null表示当前目录，空串表示当前根目录 */
		return "configuration/com.owon.uppersoft.dso";
	}

	public void notifyDestroy() {
	}

	public String getProductVersion() {
		return "2.0.5.0";
	}

	@Override
	public ResourceBundle bundle() {
		return null;
	}
}
