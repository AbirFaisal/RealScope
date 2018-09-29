package com.owon.uppersoft.dso.page;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.view.pane.ChannelPane;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.wf.IChannel;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;
import com.owon.uppersoft.vds.ui.widget.custom.LContainer;

/**
 * 通道设置 通道选择 ch1,ch2,ch3,ch4 开关 通道耦合 交流直流 反相 开启，关闭 探头 X1,X10,X100,X1000
 * 
 * @author Matt
 * 
 */
public class ChannelPage extends AbstractContentPage {

	public static final String Name = "M.Channel.Name";

	@Override
	public LContainer createPage(IContentPage beforeApply, ContentPane cp,
			int toward) {
		cm = cp.getControlManager();
		ChannelPane chp = new ChannelPane(cm, this);
		LContainer jp = cp.createContainerWithBackWard(chp, this, beforeApply);
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
		return true;
	}

	private ControlManager cm;
	private int current_channel_page_idx;

	public void setCurrentChannelPageIndex(int idx) {
		this.current_channel_page_idx = idx;
	}

	public IChannel getCurrentChannel() {
		return cm.getWaveFormInfoControl().getWaveFormChannelInfo(
				current_channel_page_idx);
	}

	public WaveFormInfo[] getChannelNames() {
		return cm.getCoreControl().getWaveFormInfos();
	}

	public int getSelectedChannelIndex() {
		return cm.getWaveFormInfoControl().getSelectedwfIdx();
	}

	public boolean isBandLimit() {
		return false;
	}
}
