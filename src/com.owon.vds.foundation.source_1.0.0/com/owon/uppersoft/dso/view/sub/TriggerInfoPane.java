package com.owon.uppersoft.dso.view.sub;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerSet;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.dso.pref.Style;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.ui.layout.OneColumnLayout;
import com.owon.uppersoft.vds.ui.resource.SwingResourceManager;

public class TriggerInfoPane extends JPanel implements Localizable,
		PropertyChangeListener {

	//public static final String trgvoltPath = (Define.def.style.path + Style.Trgvoltname);
	public static final int floor0 = 22;
	// public static final int floor1 = 47;
	// public static final int floor2 = 72;
	// public static final int floor3 = 96;
	
	public static final int BlockHeigth = 103;
	public static final int BlockWidth = 128;

	public static final int forceBtnOrigin = 85, titleWidth = 47;

	//private Image img = SwingResourceManager.getIcon(InfoBlock.class, trgvoltPath).getImage();
	private DataHouse dh;

	public TriggerInfoPane(final DataHouse dh) {
		this.dh = dh;
		setOpaque(false);

		setLayout(new BorderLayout(3, 3));

		trgToolbar = new TrgToolBar(dh);
		add(trgToolbar, BorderLayout.NORTH);

		con = new JPanel();
		add(con, BorderLayout.CENTER);
		con.setOpaque(false);
		con.setLayout(new OneColumnLayout());

		initTitle();
	}

	private List<Label> ibs = new LinkedList<Label>();
	private ListIterator<Label> li;

	protected void init() {
		li = ibs.listIterator();
	}

	protected void end() {
		Label ib;
		while (li.hasNext()) {
			ib = li.next();
			li.remove();
			con.remove(ib);
		}
	}

	protected void updateLabel(int idx) {
		init();
		Label ib;
		while (li.hasNext()) {
			ib = li.next();
			if (ib.getIdx() == idx) {
				ib.repaint();
				break;
			}
		}
	}

	protected Label process(TriggerSet ts, int idx) {
		Label ib;
		if (li.hasNext()) {
			ib = li.next();
			ib.setTriggerSet(ts, idx);
		} else {
			ib = new Label(dh, ts, idx);
			li.add(ib);
			con.add(ib);
		}
		return ib;
	}

	public void updateInfos(TriggerControl tc) {
		init();

		if (!tc.isSingleTrg()) {
			int len = tc.getChannelsNumber();
			for (int i = 0; i < len; i++) {
				TriggerSet ts = tc.getAlternateTriggerSet(i);
				// if (isChannelOn(i))
				process(ts, i);
			}
		} else {
			int idx = tc.getSingleTrgChannel();
			// if (isChannelOn(idx))
			process(tc.singleTriggerSet, idx);
		}
		end();
		con.doLayout();
		repaint();
	}

	public Label get(int chl) {
		init();
		Label ib2;
		while (li.hasNext()) {
			ib2 = li.next();
			if (chl == ib2.getIdx())
				return ib2;
		}
		return null;
	}

	private JPanel con;
	private TrgToolBar trgToolbar;

	private void initTitle() {
		I18nProvider.LocalizeSelf(trgToolbar);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		//g2d.drawImage(img, 0, 0, null);
		g2d.setColor(Color.BLACK);
		g2d.drawRect(0,0,1,100);

		// g2d.setColor(Color.WHITE);
		// g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);
		// g2d.drawString(t, 5, 22);
		// drawStringframe(g2d, onTitle);
		// g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	@Override
	public void localize(ResourceBundle rb) {
		trgToolbar.localize(rb);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String pn = evt.getPropertyName();
		if (pn.equals(PropertiesItem.FFT_ON)) {
			trgToolbar.setTrgInfPaneEnable(false);
		} else if (pn.equals(PropertiesItem.FFT_OFF)) {
			trgToolbar.setTrgInfPaneEnable(true);
		}
	}
}