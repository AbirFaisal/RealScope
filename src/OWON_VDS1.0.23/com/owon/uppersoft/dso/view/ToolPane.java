package com.owon.uppersoft.dso.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.view.sub.ButtonPane;
import com.owon.uppersoft.dso.view.sub.DetailPane;
import com.owon.uppersoft.dso.view.sub.InfoBlock;
import com.owon.uppersoft.dso.view.sub.InfoPane;
import com.owon.uppersoft.dso.view.sub.TriggerInfoPane;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.ui.layout.LayoutManagerAdapter;
import com.owon.uppersoft.vds.ui.layout.OneColumnLayout;
import com.owon.uppersoft.vds.ui.layout.OneRowLayout;

public class ToolPane extends JPanel implements Localizable,
		PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3517025707687469125L;
	private InfoPane infoPane;
	private TriggerInfoPane trginfoPane;
	private ControlManager cm;

	public ToolPane(MainWindow mw, Dimension sz, ControlManager cm) {
		this.cm = cm;
		dh = mw.getDataHouse();
		// setPreferredSize(sz);
		setLayout(new BorderLayout());

		JPanel cen = new JPanel();
		cen.setBackground(Color.BLACK);
		cen.setLayout(new OneColumnLayout());

		valuePane = new ValuePane(dh);
		valuePane.setPreferredSize(new Dimension(0, ValuePane.Height));// 90
		cen.add(valuePane);

		infoPane = new InfoPane(dh);
		infoPane.setPreferredSize(new Dimension(0, InfoBlock.BlockHeight));// 83
		cen.add(infoPane);

		add(cen, BorderLayout.CENTER);

		final int eastWidth = DetailPane.BlockWidth
				+ TriggerInfoPane.BlockWidth;
		JPanel east = new JPanel();
		east.setBackground(Color.WHITE);
		east.setPreferredSize(new Dimension(eastWidth, 0));
		east.setLayout(new OneColumnLayout());

		buttonPane = createButtonPane(mw, cm);
		buttonPane.setPreferredSize(new Dimension(0, ButtonPane.Height));// 69
		east.add(buttonPane);

		JPanel east_bottom = new JPanel();
		east_bottom.setPreferredSize(new Dimension(0, DetailPane.BlockHeigth));// 103
		east_bottom.setLayout(new OneRowLayout());

		detailPane = new DetailPane(dh);
		detailPane.setPreferredSize(new Dimension(DetailPane.BlockWidth, 0));// 126
		east_bottom.add(detailPane);

		trginfoPane = new TriggerInfoPane(dh);
		trginfoPane.setPreferredSize(new Dimension(TriggerInfoPane.BlockWidth,
				0));// 128
		east_bottom.add(trginfoPane);

		east.add(east_bottom);

		add(east, BorderLayout.EAST);

		logP = new JPanel() {
			// @Override
			// protected void paintComponent(Graphics g) {
			// Graphics2D g2d = (Graphics2D) g;
			// dataHouse.getWaveFormManager().paintWFLog(g2d);
			// }
		};
		logP.setBackground(Color.red);
		// add(logP);

		cm.pcs.addPropertyChangeListener(this);
		updateChannels();
		updateTrgVolt();
	}

	protected ButtonPane createButtonPane(MainWindow mw, ControlManager cm) {
		return new ButtonPane(mw, cm, dh);
	}

	public TriggerInfoPane getTrgInfoPane() {
		return trginfoPane;
	}

	private ButtonPane buttonPane;
	private DetailPane detailPane;
	private ValuePane valuePane;
	private DataHouse dh;
	private JPanel logP;

	public void updateAfterData() {
		infoPane.updateFreqs();
		logP.repaint();
	}

	// public void updateValues() {
	// valuePane.repaint();
	// }

	public void updateChannels() {
		infoPane.updateInfos(dh.getWaveFormManager());
	}

	public void updateTrgVolt() {
		trginfoPane.updateInfos(dh.controlManager.getTriggerControl());
	}

	public void updateDetail() {
		detailPane.updateInfo();
		detailPane.repaint();
	}

	public ButtonPane getButtonPane() {
		return buttonPane;
	}

	public InfoPane getInfoPane() {
		return infoPane;
	}

	public DetailPane getDetailPane() {
		return detailPane;
	}

	@Override
	public void localize(ResourceBundle rb) {
		detailPane.localize(rb);
		buttonPane.localize(rb);
		trginfoPane.localize(rb);
		infoPane.localize(rb);
		dh.localize(rb);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		detailPane.propertyChange(evt);
		buttonPane.propertyChange(evt);
		trginfoPane.propertyChange(evt);
		infoPane.propertyChange(evt);
		valuePane.propertyChange(evt);
	}
}