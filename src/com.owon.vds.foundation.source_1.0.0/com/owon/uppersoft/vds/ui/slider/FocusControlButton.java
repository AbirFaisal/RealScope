/**
 * 
 */
package com.owon.uppersoft.vds.ui.slider;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;


public abstract class FocusControlButton extends JButton {
	
	public Font controlfont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
	
	public FocusControlButton(String text, final SharedFocusAdapter fa,
			final JDialog dlg) {
		super(text);
		setFont(controlfont);
		setForeground(Color.WHITE);
		setBackground(Color.DARK_GRAY);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				fa.setOn(false);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				fa.setOn(true);
			}

		});

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				action();
				fa.setOn(true);

				/** 详见 SharedFocusAdapter中的说明 */
				// dlg.repaint();
				// dlg.requestFocus();

				dlg.dispose();
			}
		});
	}

	protected abstract void action();

	/**
	 * 重置和50%是不同的实现入口，分别为sliderview和sliderdelegate
	 * 
	 * @param sv
	 * @param csb
	 * @param dlg
	 * @param BtnStatus
	 * @param pcl
	 * @param vertical
	 * @return
	 */
	public static FocusListener addShareFocusControlButtons(
			final ISliderView sv, SymmetrySliderBar csb, final JDialog dlg,
			boolean vertical, ResourceBundle rb) {
		String reset = rb.getString("Option.reset");

		SharedFocusAdapter fa = new SharedFocusAdapter(new SliderAdapter2(),
				dlg);

		btn0(sv, csb, dlg, vertical, reset, fa);
		return fa;
	}

	/**
	 * 重置和50%是不同的实现入口，分别为sliderview和sliderdelegate
	 * 
	 * @param sv
	 * @param csb
	 * @param dlg
	 * @param BtnStatus
	 * @param pcl
	 * @param vertical
	 * @return
	 */
	public static FocusListener addShareFocusControlButtons(
			final ISliderView sv, SymmetrySliderBar csb, final JDialog dlg,
			int BtnStatus, final SliderDelegate pcl, boolean vertical, ResourceBundle rb) {
		String reset = rb.getString("Option.reset");
		String trg50 = rb.getString("Option.set50percent");
		if (BtnStatus == SliderDelegate.BtnStatusNO) {
			dlg.setLayout(null);
			dlg.setContentPane(csb);
			return null;
		}

		SharedFocusAdapter fa = new SharedFocusAdapter(pcl, dlg);
		if (BtnStatus == SliderDelegate.BtnStatus0) {
			btn0(sv, csb, dlg, vertical, reset, fa);
		} else if (BtnStatus == SliderDelegate.BtnStatus50) {
			btn50(csb, dlg, pcl, trg50, fa);
		} else if (BtnStatus == SliderDelegate.BtnStatusBoth) {
			btnboth(sv, csb, dlg, pcl, reset, trg50, fa);
		}

		return fa;
	}

	private static void btn0(final ISliderView sv, SymmetrySliderBar csb,
			final JDialog dlg, boolean vertical, String reset,
			SharedFocusAdapter fa) {
		dlg.setLayout(new BorderLayout());
		JPanel btnp = new JPanel();
		btnp.setOpaque(false);
		JButton btn0 = new FocusControlButton(reset, fa, dlg) {
			@Override
			protected void action() {
				sv.setDefault();
			}
		};
		btn0.setOpaque(false);
		dlg.add(csb, BorderLayout.CENTER);
		if (vertical) {
			btnp.setLayout(new BoxLayout(btnp, BoxLayout.LINE_AXIS));
			dlg.add(btnp, BorderLayout.EAST);
		} else {
			btnp.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			dlg.add(btnp, BorderLayout.SOUTH);
		}
		btnp.add(btn0);
		dlg.pack();
	}

	private static void btnboth(final ISliderView sv, SymmetrySliderBar csb,
			final JDialog dlg, final SliderDelegate pcl, String reset,
			String trg50, SharedFocusAdapter fa) {
		dlg.setLayout(new BorderLayout());
		JPanel btnp = new JPanel();
		btnp.setOpaque(false);
		btnp.setLayout(new BoxLayout(btnp, BoxLayout.Y_AXIS));
		JButton btn0 = new FocusControlButton(reset, fa, dlg) {
			@Override
			protected void action() {
				sv.setDefault();
			}
		};

		JButton btn50 = new FocusControlButton(trg50, fa, dlg) {
			@Override
			protected void action() {
				pcl.on50percent();
			}
		};

		btnp.add(btn50);
		btnp.add(btn0);
		dlg.add(btnp, BorderLayout.EAST);
		dlg.add(csb, BorderLayout.CENTER);
		dlg.pack();
	}

	private static void btn50(SymmetrySliderBar csb, final JDialog dlg,
			final SliderDelegate pcl, String trg50, SharedFocusAdapter fa) {
		dlg.setLayout(new BorderLayout());
		JPanel btnp = new JPanel();
		btnp.setOpaque(false);
		btnp.setLayout(new BoxLayout(btnp, BoxLayout.LINE_AXIS));
		JButton btn50 = new FocusControlButton(trg50, fa, dlg) {
			@Override
			protected void action() {
				pcl.on50percent();
			}
		};

		btnp.add(btn50);
		dlg.add(btnp, BorderLayout.EAST);
		dlg.add(csb, BorderLayout.CENTER);
		dlg.pack();
	}

}