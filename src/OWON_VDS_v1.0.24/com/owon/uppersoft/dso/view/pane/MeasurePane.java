package com.owon.uppersoft.dso.view.pane;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.List;

import javax.swing.JPanel;

import com.owon.uppersoft.dso.data.LObject;
import com.owon.uppersoft.dso.function.measure.MeasureManager;
import com.owon.uppersoft.dso.function.measure.MeasureSnapshot;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.view.pane.dock.widget.CLineBorder;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.vds.core.measure.MeasureElem;
import com.owon.uppersoft.vds.core.measure.MeasureModel;
import com.owon.uppersoft.vds.core.measure.MeasureT;
import com.owon.uppersoft.vds.ui.layout.OneColumnLayout;
import com.owon.uppersoft.vds.ui.widget.custom.CButton;
import com.owon.uppersoft.vds.ui.widget.custom.CCheckBox;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;

/**
 * 测量 添加测量 测量类型 20种测量 信源 ch1,ch2,ch3,ch4 快照全部 删除测量 删除全部
 * 
 * @author Matt
 */
public class MeasurePane extends FunctionPanel {
	protected final String MsgKeyPrefix = "AutoMeasure.",
			SnapshotLabStr = "  CH";

	public MeasureModel measMod;
	private CCheckBox[] chcb, mtcb, oth_mtcb;
	private CComboBox Snapshot;
	private MeasureManager mm;

	public MeasurePane(final ControlManager cm) {
		super(cm);
		measMod = cm.measMod;
		mm = cm.getMeasureManager();

		ncgp();
		nrip();
		nlbl("M.Measure.showAll");

		int channelNumber = cm.getSupportChannelsNumber();
		Object[] SnapshotList = new Object[channelNumber + 1];
		SnapshotList[0] = new LObject("M.Measure.Off");
		for (int i = 1; i <= channelNumber; i++) {
			SnapshotList[i] = "CH" + i;
		}

		Snapshot = nccb(SnapshotList);
		Snapshot.setSelectedIndex(cm.measureSnapshotIdx);
		Snapshot.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED)
					return;
				int idx = Snapshot.getSelectedIndex();
				cm.measureSnapshotIdx = idx;
				MeasureSnapshot.handleSnapshot(cm.getLocalizeCenter(), idx,
						Snapshot, cm);
			}
		});

		ncgp();
		nrip();
		CButton removeall = nbtn("M.Measure.removeAll");
		removeall.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				measMod.removeAllMeasures();
				// mm.setMeasureOn(false);
				mm.setMeasureOn_enforce();
				// removeAllSelected();
				measMod.updateMeasureSelected(chcb, mtcb, oth_mtcb);
			}
		});

		JPanel chgp = new JPanel();
		chgp.setOpaque(false);
		chgp.setPreferredSize(new Dimension(300, 30));
		chgp.setBorder(new CLineBorder());
		add(chgp);

		int chnum = cm.getSupportChannelsNumber();
		chcb = new CCheckBox[chnum];
		for (int i = 0; i < chcb.length; i++) {
			chcb[i] = new CCheckBox();
			chcb[i].setText("CH" + (i + 1));

			if (chnum == 1)
				chcb[i].setPreferredSize(new Dimension(60, 20));
			else if (chnum <= 2)
				chcb[i].setPreferredSize(new Dimension(125, 20));

			chgp.add(chcb[i]);

			final int idx = i;
			chcb[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean isSelected = ((CCheckBox) e.getSource())
							.isSelected();
					if (isSelected) {
						measMod.addChElem(idx);
					} else {
						measMod.delChElem(idx);
					}
					cm.pcs.firePropertyChange(
							MeasureManager.RefreshMeasureResult, null, null);
				}
			});
		}

		JPanel typgp = new JPanel();
		typgp.setOpaque(false);
		typgp.setPreferredSize(new Dimension(300, 190));
		typgp.setBorder(new CLineBorder());
		add(typgp);

		JPanel vtypp = new JPanel();
		vtypp.setOpaque(false);
		vtypp.setLayout(new OneColumnLayout());
		JPanel htypp = new JPanel();
		htypp.setOpaque(false);
		htypp.setLayout(new OneColumnLayout());
		typgp.add(vtypp);
		typgp.add(htypp);

		JPanel jp = vtypp;
		int k = MeasureT.MAX.idx;
		mtcb = new CCheckBox[MeasureT.VALUES.length];
		for (int i = 0; i < MeasureT.VALUES.length; i++) {
			if (i >= k)
				jp = htypp;

			String ccbName = MsgKeyPrefix + MeasureT.VALUES[i];
			CCheckBox lbl = new CCheckBox();
			lbl.setName(ccbName);
			jp.add(lbl);
			addLocalizable(lbl);

			mtcb[i] = lbl;
			mtcb[i].setPreferredSize(new Dimension(125, 20));
			final int idx = i;
			mtcb[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean isSelected = ((CCheckBox) e.getSource())
							.isSelected();
					if (isSelected) {
						measMod.addMtElem(idx);
					} else {
						measMod.delMtElem(idx);
					}
					mm.setMeasureOn_enforce();
				}
			});
		}

		if (cm.getSupportChannelsNumber() >= 2) {
			JPanel othtypgp = new JPanel();
			othtypgp.setOpaque(false);
			othtypgp.setPreferredSize(new Dimension(300, 90));
			othtypgp.setBorder(new CLineBorder());
			add(othtypgp);

			List<MeasureElem> lists = measMod.othMTlinked;

			int len = lists.size();
			oth_mtcb = new CCheckBox[len];
			for (int i = 0; i < len; i++) {
				CCheckBox lbl = new CCheckBox();
				lbl.setName(lists.get(i).label);
				othtypgp.add(lbl);
				addLocalizable(lbl);
				oth_mtcb[i] = lbl;
				oth_mtcb[i].setPreferredSize(new Dimension(250, 15));
				final int idx = i;
				oth_mtcb[i].addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						boolean isSelected = ((CCheckBox) e.getSource())
								.isSelected();
						measMod.selectDelay(isSelected, idx);
						mm.setMeasureOn_enforce();
					}
				});
			}
		}

		/* 更新按钮 */
		// ncgp();
		// nrip();
		// nbtnt("update").addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// mm.measure();
		// Platform.getMainWindow().getToolPane().updateValues();
		// }
		// });
		/* 注释组 */
		// ncgp();
		// nrip();
		/**
		 * TODO Function New Idea,后面考虑是否添加 功能：找回 Remove all删掉&默认 的勾选项
		 * 做法：只作用于画图(控制其是否valuePane显示)，或者作用于操作设置(根本上控制)
		 */
		/**
		 * 上述添加是基于Remove all按钮已经存在基础上，增新效果 变通是：Remove all按钮替换成一个开关，控制其显示与否
		 */
		// loadpref(chcb, mtcb, oth_mtcb);
		measMod.updateMeasureSelected(chcb, mtcb, oth_mtcb);
		localizeSelf();
		cm.pcs.addPropertyChangeListener(this);
	}

	@Deprecated
	private void removeAllSelected() {
		for (int i = 0; i < chcb.length; i++) {
			chcb[i].setSelected(false);
		}
		for (int i = 0; i < mtcb.length; i++) {
			mtcb[i].setSelected(false);
		}

		if (oth_mtcb != null)
			for (int i = 0; i < oth_mtcb.length; i++) {
				oth_mtcb[i].setSelected(false);
			}
	}

	@Deprecated
	private void loadpref(CCheckBox[] chcb, CCheckBox[] mtcb,
			CCheckBox[] othmtcb) {
		// int channelNumber = controlManager.getSupportChannelsNumber();
		// for (Integer i : measMod.CHlinked) {
		// if (i < channelNumber)
		// chcb[(int) i].setSelected(true);
		// }
		// for (Integer i : measMod.MTlinked) {
		// mtcb[(int) i].setSelected(true);
		// }
		// for (MeasureElem me : measMod.othMTlinked) {
		// if (me.on)
		// othmtcb[me.idx].setSelected(true);
		// }
	};

	public void localizeSelf() {
		I18nProvider.LocalizeSelf(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String en = evt.getPropertyName();
		if (en.equals(MeasureModel.Refresh_MeasurePane_Selected)) {
			measMod.updateMeasureSelected(chcb, mtcb, oth_mtcb);
		}
	}

}
