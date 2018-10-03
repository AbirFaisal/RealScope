package com.owon.uppersoft.dso.model.trigger.holdoff;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractSpinnerModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFrame;

import com.owon.uppersoft.dso.model.trigger.TriggerDefine;
import com.owon.uppersoft.dso.widget.custom.effect.LabelValueUnitPane;
import com.owon.uppersoft.vds.core.trigger.help.EnumNValue;
import com.owon.uppersoft.vds.core.trigger.help.IStepSpinnerModel;
import com.owon.uppersoft.vds.util.ui.UIUtil;

/**
 * 释抑的SpinnerModel，可分级步进
 * 
 */
public class HoldoffSpinnerModel extends AbstractSpinnerModel implements
		IStepSpinnerModel {
	private EnumNValue etv;

	public HoldoffSpinnerModel(EnumNValue etv) {
		this.etv = etv;
	}

	@Override
	public Object getPreviousValue() {
		return etv.getPrevious();
	}

	@Override
	public Object getNextValue() {
		return etv.getNext();
	}

	@Override
	public Object getValue() {
		return etv;
	}

	@Override
	public void setValue(Object value) {
		if (value instanceof EnumNValue) {
			EnumNValue that = (EnumNValue) value;
			etv = that;// etv.set(that);//
			fireStateChanged();
		}
	}

	@Override
	public Object createDefaultValue() {
		return HoldoffDelegate.createDefaultETV();
	}

	public static void main(String[] args) {
		//UIUtil.modifylaf();
		final JFrame jf = new JFrame();
		jf.setLayout(new FlowLayout());

		final LabelValueUnitPane vup = new LabelValueUnitPane("",
				new HoldoffSpinnerModel(HoldoffDelegate.createDefaultETV()),
				new DefaultComboBoxModel(
						TriggerDefine.CONDITION_STEP_TIME_UNIT));
		jf.add(vup);
		vup.set("Label: ", 10, 0);

		JButton jb = new JButton("change");
		jb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				vup.set("Label          : ", 100000, 2);
				jf.pack();
			}
		});
		jf.add(jb);

		jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		jf.pack();
		jf.setVisible(true);
	}
}