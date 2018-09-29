package com.owon.uppersoft.vds.ui.widget.custom;

import java.awt.FlowLayout;
import java.util.ResourceBundle;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.ui.resource.FontCenter;
import com.owon.uppersoft.vds.ui.widget.help.CListCellRenderer;
import com.owon.uppersoft.vds.util.ui.ComboboxMouseWheelSupport;
import com.owon.uppersoft.vds.util.ui.UIUtil;

public class CComboBox extends JComboBox implements Localizable {

	public CComboBox() {
		super();
		customize(false);
	}

	public CComboBox(ComboBoxModel model) {
		super(model);
		customize(false);
	}

	public CComboBox(Object[] items) {
		this(items, false);
	}

	public CComboBox(Object[] items, boolean r) {
		super(items);
		customize(r);
	}

	public CComboBox(ComboBoxModel model, boolean r) {
		super(model);
		customize(r);
	}

	public CListCellRenderer getCListCellRenderer() {
		return CListCellRenderer.clc;
	}

	private void customize(boolean r) {
		setFont(FontCenter.getComboFont());
		setRenderer(r ? CListCellRenderer.rclc : CListCellRenderer.clc);
		new ComboboxMouseWheelSupport(this);
	}

	@Override
	public void localize(ResourceBundle rb) {
		/** Modifying the language in a separate interface will not affect this. */
		// setSelectedItem(getSelectedItem());
		setFont(FontCenter.getComboFont());
	}

	public static void main_(String args[]) {
		try {
			//UIUtil.modifyui();
			JFrame frame = new JFrame();
			frame.setLayout(new FlowLayout());
			frame.setBounds(100, 100, 500, 375);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			CComboBox comboBox = new CComboBox();
			comboBox.setModel(new DefaultComboBoxModel(new String[] { "10 mV",
					"20 mV", "30 mV", "yes" }));
			System.out.println(comboBox.getUI());
			frame.add(comboBox);

			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
