package com.owon.uppersoft.dso.view.sub;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.ui.layout.OneRowLayout;

public class InfoPane extends JPanel implements Localizable,
		PropertyChangeListener {
	private List<InfoBlock> ibs = new LinkedList<InfoBlock>();
	private ListIterator<InfoBlock> li;

	public InfoPane(DataHouse dh) {
		setOpaque(false);
		setLayout(new OneRowLayout(1));

		initInfoBlocks(dh);
	}

	private void initInfoBlocks(DataHouse dh) {
		ibs.clear();
		removeAll();
		WaveFormManager wfm = dh.getWaveFormManager();
		init();
		int i = 1;

		Iterator<WaveForm> iwf = wfm.getWaveFormInfoControl()
				.getLowMachineWFIterator();
		ControlManager cm = dh.controlManager;
		int chls = cm.getSupportChannelsNumber();
		while (iwf.hasNext()) {
			WaveForm wf = iwf.next();
			if (i > chls)
				break;
			InfoBlock ib = new InfoBlock(this, wf, cm);
			li.add(ib);
			add(ib);
			i++;
		}
	}

	protected void init() {
		li = ibs.listIterator();
	}

	protected InfoBlock processSingle(WaveForm wf, boolean sel) {
		InfoBlock ib = null;
		boolean has = li.hasNext();
		// System.out.println("processSingle,hasnext?" + has + ",size:"
		// + ibs.size() + "," + li.nextIndex());
		if (has) {
			ib = li.next();
			ib.setWaveForm(wf);
			ib.setSelected(sel);
		}
		return ib;
	}

	protected void end() {
		InfoBlock ib;
		while (li.hasNext()) {
			ib = li.next();
			li.remove();
			remove(ib);
		}
	}

	public void notify_a(InfoBlock ib) {
		init();
		InfoBlock ib2;
		while (li.hasNext()) {
			ib2 = li.next();
			if (ib != ib2)
				ib2.setSelected(false);
		}
		ib.setSelected(true);
		int idx = ib.getChannelNumber();
		wfm.setSelectedWaveForm(idx);
		repaint();
	}

	private WaveFormManager wfm;

	public void updatePos0(int number) {
		init();
		InfoBlock ib;
		while (li.hasNext()) {
			ib = li.next();
			if (ib.getChannelNumber() == number) {
				ib.updatePos0();
				ib.repaint();
				break;
			}
		}
	}

	public void updateFreqs() {
		init();
		InfoBlock ib;
		while (li.hasNext()) {
			ib = li.next();
			ib.updateFreq();
		}
		repaint();
	}

	public void updateSelected(int idx) {
		init();
		InfoBlock ib;
		while (li.hasNext()) {
			ib = li.next();
			boolean sel = (ib.getChannelNumber() == idx);
			ib.setSelected(sel);
		}
		repaint();
	}

	public void updateInfos(WaveFormManager wfm) {
		this.wfm = wfm;
		Iterator<WaveForm> iwf = wfm.getWaveFormInfoControl()
				.getLowMachineWFIterator();
		init();
		WaveForm swf = wfm.getSelectedWaveForm();
		while (iwf.hasNext()) {
			WaveForm wf = iwf.next();
			// if (!wf.isOn())
			// System.out.println("ch:" + wf.getChannelNumber() + ",vb:"
			// + wf.getVoltbaseIndex());
			processSingle(wf, swf.equals(wf));
		}
		// end();
		// doLayout();
		repaint();
	}

	public void updateInfos2(WaveFormManager wfm) {
		this.wfm = wfm;
		Iterator<WaveForm> iwf = wfm.getWaveFormInfoControl()
				.getLowMachineWFIterator();
		init();
		// System.out.println("InfoblocksNum:" + ibs.size());
		WaveForm swf = wfm.getSelectedWaveForm();
		while (iwf.hasNext()) {
			WaveForm wf = iwf.next();
			// System.out.println("ch:" + wf.getChannelNumber() + ",vb:"
			// + wf.getVoltbaseIndex());
			// System.out.println(",size:" + ibs.size() + "," + li.nextIndex());

			InfoBlock ib = ibs.get(wf.getChannelNumber());
			ib.setWaveForm(wf);
			ib.setSelected(swf.equals(wf));
		}
		// end();
		// doLayout();
		repaint();
	}

	public void updateInfo(WaveForm wf) {
		InfoBlock ib = get(wf.getChannelNumber());
		if (ib != null) {
			ib.setWaveForm(wf);
		}
		ib.repaint();
	}

	public void localize(ResourceBundle rb) {
		init();
		InfoBlock ib;
		while (li.hasNext()) {
			ib = li.next();
			ib.localize(rb);
		}
		repaint();
	}

	public void vbChangeForChannel(int chl, int vbidx) {
		WaveForm wf = wfm.getWaveForm(chl);
		// System.out.println(chl + ", " + vbidx);
		if (wf.isOn()) {
			InfoBlock ib = get(chl);
			if (ib != null) {
				ib.getComboBoxOwner().selected(vbidx);
				Platform.getMainWindow().getToolPane().updateChannels();
			} else {
				DBG.errprintln("InfoPane cannot find InfoBlock!");
			}
		} else {
			wfm.setVoltBaseIndex(wf, vbidx);
		}
	}

	public InfoBlock get(int chl) {
		init();
		InfoBlock ib2;
		while (li.hasNext()) {
			ib2 = li.next();
			if (chl == ib2.getChannelNumber())
				return ib2;
		}
		return null;
	}

	public void setInfoPaneEnable(boolean b) {
		// 每个InfoBlock设Enable(b),由它内部各个地方的isFFTon控制,设完repaint(),后面要改掉。
		repaint();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String pn = evt.getPropertyName();
		Object o = evt.getNewValue();
		if (pn.equals(PropertiesItem.FFT_ON)) {
			setInfoPaneEnable(false);
		} else if (pn.equals(PropertiesItem.FFT_OFF)) {
			setInfoPaneEnable(true);
		} else if (pn.equals(PropertiesItem.CHANNEL_OPTION)) {
			int chl = (Integer) o;
			get(chl).propertyChange(evt);
		} else if (pn.equals(PropertiesItem.TUNE_VBBCHANGE)) {
			ChannelInfo ci = (ChannelInfo) o;
			get(ci.getNumber()).propertyChange(evt);
		} else if (pn.equals(PropertiesItem.UPDATE_CHLVOLT)) {
			int chl = (Integer) o;
			get(chl).propertyChange(evt);
		}
	}
}