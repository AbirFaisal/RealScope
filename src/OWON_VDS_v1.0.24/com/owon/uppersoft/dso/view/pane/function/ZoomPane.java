package com.owon.uppersoft.dso.view.pane.function;

import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.DefaultComboBoxModel;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.mode.control.TimeControl;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.dso.view.pane.dock.widget.ItemPane;
import com.owon.uppersoft.vds.core.aspect.control.ITimeControl;
import com.owon.uppersoft.vds.core.zoom.AssitControl;
import com.owon.uppersoft.vds.core.zoom.ZBordersSliderView;
import com.owon.uppersoft.vds.ui.slider.SymmetrySliderBar;
import com.owon.uppersoft.vds.ui.widget.custom.CButton;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.custom.CLabel;
import com.owon.uppersoft.vds.ui.widget.help.RadioButtonGroup;

public class ZoomPane extends FunctionPanel {

	private ItemPane cardip;
	private CComboBox astbcbb, zmtbcbb;
	private CLabel slidertxt;

	private boolean listening = false;
	private AssitControl ac;
	private TimeControl tc;
//	private ResourceBundle rb;
	private CardLayout card = new CardLayout();

	/** Under the window settings, the scroll bar listener, the control yellow line moves */
	final public ZBordersSliderView aspcl = new ZBordersSliderView() {
		@Override
		public void adjustAdd(int delta) {
			ac.assistSetMoveb1b2(-delta);
			repaintChartScreen();
		}

		@Override
		public void setDefault() {
			ac.assistMoveb1b2Center();
			repaintChartScreen();
		}

	};

	private void repaintChartScreen() {
		Platform.getMainWindow().getChartScreen().re_paint();
	}

	/** Under the window extension, the scroll bar listener controls the flat touch under the extension */
	final public ZBordersSliderView zoompcl = new ZBordersSliderView() {
		@Override
		public void setDefault() {
			tc.c_setHorizontalTriggerPosition(0);
			ac.zhtp = tc.getHorizontalTriggerPosition();
			updateZoomHriTrgPosLabel();
			cm.pcs.firePropertyChange(ITimeControl.onHTPChanged, null, null);
			cm.mcctr.computeXValues();
		}

		@Override
		public void adjustAdd(int delta) {
			tc.c_addHorizontalTriggerPosition(-delta);
			ac.zhtp = tc.getHorizontalTriggerPosition();
			// ac.zhtp += n;
			updateZoomHriTrgPosLabel();
			cm.pcs.firePropertyChange(ITimeControl.onHTPChanged, null, null);
			cm.mcctr.computeXValues();
		}

	};

	public ZoomPane(final ControlManager cm) {
		super(cm);
		ac = cm.getZoomAssctr();
		tc = cm.getTimeControl();

		ncgp();
		ItemPane rdsip = nrip();
		addRadioButtonGroup(rdsip);

		cardip = nrip();
		cardip.setLayout(card);

		ItemPane asp = createAssistSetPane();
		ItemPane zmp = createZoomPane();
		cardip.add(asp, "AssistSetPane");
		cardip.add(zmp, "ZoomPane");

		updateCardItemPane();
		listening = true;
	}

	/**
	 * ncgp(); nrip(); CButton test = nbtnt("DebugMess"); CButton testb1b2 =
	 * nbtnt("DebugMess"); test.addActionListener(new ActionListener() {
	 * 
	 * @Override public void actionPerformed(ActionEvent e) {
	 *           DBG.dbgln("--------------"); DBG.dbgln("Mtb存:" +
	 *           machine.TIMEBASE[ac.mtbIdx] + " Mtb现拿:" +
	 *           machine.TIMEBASE[tc.getTimebaseIdx()]); DBG.dbgln("Ztb:" +
	 *           machine.TIMEBASE[ac.getZTBidx()]); DBG.dbgln("mhtp存:" + ac.mhtp
	 *           + " htp现拿：" + tc.getHorizontalTriggerPosition());
	 *           DBG.dbgln("--------------"); } });
	 *           testb1b2.addActionListener(new ActionListener() {
	 * @Override public void actionPerformed(ActionEvent e) {
	 *           DBG.dbgln("--------------"); DBG.dbgln("b1:" + ac.b1 + " b2:" +
	 *           ac.b2); DBG.dbgln("deltaB:" + ac.dltb); DBG.dbgln("htp：" +
	 *           tc.getHorizontalTriggerPosition());
	 *           DBG.dbgln("--------------"); } });
	 */

	private void updateCardItemPane() {
		if (ac.isonMain())
			cardip.setVisible(false);
		else {
			String page = ac.isonZoom() ? "ZoomPane" : "AssistSetPane";
			card.show(cardip, page);
			cardip.setVisible(true);
		}
	}

	@Override
	public void localize(ResourceBundle rb) {

	}

	private PropertyChangeListener rdgpcl;
	private RadioButtonGroup rds;

	private void addRadioButtonGroup(ItemPane rdsip) {
		ResourceBundle rb = I18nProvider.bundle();
		String[] rdgtxts = { rb.getString("M.Zoom.Main"),
				rb.getString("M.Zoom.Assist"), rb.getString("M.Zoom.Zoom") };
		// +" (M)"+" (A)"+" (Z)"
		rdgpcl = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (!listening)
					return;
				int i = (Integer) evt.getNewValue();
				ac.setSelected(i);
				/** 如果重复点击同一按钮，可能污染已保存的值 */
				if (ac.isPressedtheSame(i))
					return;
				switch (i) {
				case 0:
					ac.switch2Main();
					break;
				case 1:
					ac.switch2AssistSet();
					astbcbb.setSelectedIndex(ac.getZTBidx());
					break;
				case 2:
					ac.switch2Zoom(cm);
					zmtbcbb.setSelectedIndex(ac.getZTBidx());
					updateZoomHriTrgPosLabel();
					break;
				}
				updateCardItemPane();
				repaintChartScreen();
				if (AssitControl.FastMWstwich) {
					cm.pcs.firePropertyChange(ITimeControl.onTimebaseUpdated,
							null, null);
				}
			}
		};

		rds = new RadioButtonGroup(rdgtxts, rdgpcl, ac.getSelected(), 85, 30);

		rdsip.add(rds);
	}

	private ItemPane createAssistSetPane() {
		ItemPane asp = nrip();
		nlblt(" W:");
		astbcbb = nccb(cm.getMachineInfo().TIMEBASE);
		final CButton sliderbt = nbtnt("<-  ->");

		astbcbb.setSelectedIndex(ac.getZTBidx());
		astbcbb.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!listening)
					return;
				if (e.getStateChange() != ItemEvent.SELECTED)
					return;
				int idx = astbcbb.getSelectedIndex();
				int midx = tc.getTimebaseIdx();
				/** ASet状态下调时基,扩展时基大于主时基，则DetailPane主时基跟着变大更新 */
				if (idx > midx) {
					/** 扩展时基变大导致主时基跟着变大 */
					tc.c_setTimebaseIdx(idx, false);
				}

				ac.assistSetcomputDeltabb1b2(idx, tc.getTimebaseIdx());// 原方法里有Tm>=b2的限制，才放在tc设时基后。
				repaintChartScreen();
			};
		});

		sliderbt.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int x = e.getXOnScreen() - 108;
				int y = e.getYOnScreen() + 25;
				SymmetrySliderBar.createAssistZoomViewSliderFrame(Platform
						.getMainWindow().getFrame(), x, y, false, aspcl,
						I18nProvider.bundle());
			}
		});
		return asp;
	}

	private ItemPane createZoomPane() {
		ItemPane zmp = nrip();
		nlblt(" W:");
		zmtbcbb = nccb(cm.getMachineInfo().TIMEBASE);
		nlblt("   T:");
		slidertxt = nlblt("");
		updateZoomHriTrgPosLabel();
		zmtbcbb.setSelectedIndex(ac.getZTBidx());
		zmtbcbb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!listening)
					return;
				if (e.getStateChange() != ItemEvent.SELECTED)
					return;
				int ztbIdx = zmtbcbb.getSelectedIndex();
				tc.c_setTimebaseIdx(ztbIdx, false);
			}
		});
		slidertxt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		slidertxt.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int x = e.getXOnScreen() - 108;
				int y = e.getYOnScreen() + 25;
				boolean operatable = Platform.getControlApps().interComm
						.isTimeOperatableNTryGetDM();
				if (!operatable)
					return;
				SymmetrySliderBar.createAssistZoomViewSliderFrame(Platform
						.getMainWindow().getFrame(), x, y, false, zoompcl,
						I18nProvider.bundle());
			}
		});

		return zmp;
	}

	public void updateZoomHriTrgPosLabel() {
		String label = tc.getHorizontalTriggerLabel();
		String txt = "<html><font><u>" + label + "<u></font></html>";
		slidertxt.setText(txt);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String pn = evt.getPropertyName();
		if (pn.equals(AssitControl.UPDATE_ZOOM_HRI_TRG)) {
			// if(ac.isZoom)
			updateZoomHriTrgPosLabel();
		} else if (pn.equals(AssitControl.UPDATE_ZOOM_TIMEBASE)) {
			listening = false;
			zmtbcbb.setSelectedIndex(ac.getZTBidx());
			listening = true;
		} else if (pn.equals(PropertiesItem.ZOOMSELECTED)) {
			if (!AssitControl.FastMWstwich)
				return;
			int k = (Integer) evt.getNewValue();
			rds.setSelected(k);
			rdgpcl.propertyChange(new PropertyChangeEvent(this,
					RadioButtonGroup.RADIO, null, k));
			updateCardItemPane();
		} else if (pn.equals(PropertiesItem.MACHINETYPE_CHANGE)) {
			listening = false;
			zmtbcbb.setModel(new DefaultComboBoxModel(
					cm.getMachineInfo().TIMEBASE));
			astbcbb.setModel(new DefaultComboBoxModel(
					cm.getMachineInfo().TIMEBASE));
			listening = true;
		}
	}
}
