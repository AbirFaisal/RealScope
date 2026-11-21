package com.owon.uppersoft.dso.view.pane.function;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.page.IContentPage;
import com.owon.uppersoft.dso.page.function.PageManager;
import com.owon.uppersoft.dso.view.pane.dock.BottomBar;
import com.owon.uppersoft.dso.view.pane.dock.ContentPane;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.dso.view.pane.dock.widget.ItemPane;
import com.owon.uppersoft.vds.ui.widget.custom.CCheckBox;

/**
 * 
 * <code>
 * Pass/fail	on, off	
 Operation	start, stop
 Output	pass, fail, beep
 Stop on output (on, off)
 Info display (on, off)

 Rule	Source (ch1,ch2,ch3,ch4)
 Horizontal setting (x divisions)
 Vertical setting (x divisions)
 Create rule
 <code>
 * 
 * @author Matt
 * 
 */
public class CustomPane extends FunctionPanel {
	public static final int jbnum = 5; // checkBox limited to 5 checked
	private boolean isSelected = false;
	private CCheckBox[] cb;
	private String icn;
	private int cbindex = 0;
	private ContentPane cp;
	private BottomBar bb;
	private PageManager pm;

	public CustomPane(final ContentPane cp, final ControlManager cm,
			final BottomBar bb) {
		super(cm);
		this.cp = cp;
		this.bb = bb;
		cbindex = 0;
		pm = cp.getPageManager();
		cb = new CCheckBox[pm.getPageNum()];

		ncgp();
		// Create a button to uncheck all in the first-row ItemPane
		ItemPane ip = nrip();
		ip.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton cancel = new JButton();
		cancel.setOpaque(false);
		cancel.setPreferredSize(new Dimension(90, 31));
		cancel.setText(I18nProvider.bundle().getString("M.Custom.clear"));
		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				for (Integer i : pm.linked) {
					cb[i].setSelected(false);
				}
				pm.linked.clear();
				bb.updateButtonItems();
			}

		});
		ip.add(cancel);

		// Create 12 checkBoxes, each put into an ItemPane, and listen; checking adds the item to the shortcut bar and draws it, unchecking removes the item from the shortcut bar and erases it.
		Iterator<IContentPage> it = pm.pageIterator();
		while (it.hasNext()) {
			IContentPage icp = it.next();
			nrip();
			final int index = cbindex;
			icn = icp.getContentID();
			cb[cbindex] = ncb(icn);
			cb[cbindex].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					isSelected = ((CCheckBox) e.getSource()).isSelected();

					if (isSelected) {
						addelem(index);
						bb.updateButtonItems();
					} else {
						delelem(index);
						bb.updateButtonItems();
					}
				}
			});
			cbindex++;

		}

		for (Integer i : pm.linked) {
			cb[i].setSelected(true);
		}

		localizeSelf();
	}

	protected void addelem(int p) {
		if (pm.linked.size() == jbnum) {
			int i = (Integer) (pm.linked.removeFirst());
			cb[i].setSelected(false);
		}
		pm.linked.add(p);
	}

	protected void delelem(Integer p) {
		pm.linked.remove(p);

	}

	public void beforeLeave() {
		bb.setCustomBtnIconOriginal();
	}

}
