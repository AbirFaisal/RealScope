package com.owon.uppersoft.vds.ui.prompt;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class PopupMenuPane {
	protected JPopupMenu pop;
	private Component invoker;
	private int BtnStatus;

	public static final int PRESSBTN1 = 0, PRESSBTN2 = 1, PRESSBTN3 = 2;

	// JMenuItem[] menu;
	// PropertyChangeListener pcl;
	public PopupMenuPane(final Component parent, Object[] items,
			PropertyChangeListener pcl) {
		this(parent, items, pcl, PRESSBTN3);
	}

	public PopupMenuPane(final Component parent, Object[] items,
			PropertyChangeListener pcl, int BtnSelect) {
		this.invoker = parent;
		this.BtnStatus = BtnSelect;
		pop = new JPopupMenu();

		initMenuItem(items, pcl);
		addListener();
	}

	private void initMenuItem(Object[] items, final PropertyChangeListener pcl) {
		// menu=new JMenuItem[items.length];
		for (int i = 0; i < items.length; i++) {
			final int k = i;
			String label = items[i].toString();
			JMenuItem jmi = new JMenuItem(label);
			// menu[i]=jmi;
			pop.add(jmi);
			jmi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					pcl.propertyChange(new PropertyChangeEvent(
							PopupMenuPane.this, POPUP_MENUITEM_IDX, null, k));
				}
			});
		}
	}

	private void popupShow(MouseEvent e){
		switch (BtnStatus) {
		case PRESSBTN1:
			outprintln("1");
			pop.show(invoker, e.getPoint().x, e.getPoint().y);
			break;
		case PRESSBTN2:
			outprintln("2");
			break;
		case PRESSBTN3:
			outprintln("3");
			if (e.isPopupTrigger()) {
				pop.show(invoker, e.getPoint().x, e.getPoint().y);
			}
			break;
		default:

		}
	}
	private void addListener() {
		invoker.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					pop.show(invoker, e.getPoint().x, e.getPoint().y);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					pop.show(invoker, e.getPoint().x, e.getPoint().y);
				}
			}
		});
	}
	

	public static String[] menuItems = { "RESET 0", "RESET50" };
	public static PropertyChangeListener menupcl = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String n = evt.getPropertyName();
			if (n.equalsIgnoreCase(POPUP_MENUITEM_IDX)) {
				int idx = (Integer) evt.getNewValue();
				switch (idx) {
				case 0:
					outprintln("set0");
					break;
				case 1:
					outprintln("set50");
					break;
				default:
					break;
				}
			}

		}
	};

	public static void main(String[] args) {
		JFrame jf = new JFrame();
		jf.setBounds(500, 500, 250, 250);
		jf.setVisible(true);
		jf.getContentPane().add(
				new JLabel("Click Mouse-Right button,show you Menu!"));
		new PopupMenuPane(jf, menuItems, menupcl);
	}

	public static final String POPUP_MENUITEM_IDX = "popupMenuItemIdx";
	
	protected static void outprintln(String txt) {
		System.out.println(txt);
	}
}
