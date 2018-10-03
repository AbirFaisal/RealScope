package com.owon.uppersoft.dso.view.pane.function;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import com.owon.uppersoft.dso.function.ReferenceWaveControl;
import com.owon.uppersoft.dso.function.ref.IReferenceWaveForm;
import com.owon.uppersoft.dso.function.ref.ReferenceFile;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.CoreControl;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.ref.IRefSource;
import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.dso.view.pane.dock.widget.ItemPane;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.aspect.control.TimeConfProvider;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.core.control.MathControl;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.ui.prompt.FadeIOShell;
import com.owon.uppersoft.vds.ui.widget.custom.CButton;
import com.owon.uppersoft.vds.ui.widget.custom.CCheckBox;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.custom.CLabel;
import com.owon.uppersoft.vds.ui.widget.custom.NLabel;

public class ReferenceWavePane extends FunctionPanel {
	public static final String REFERENCE_DIRNAME = "reference";

	private CComboBox sourceccb, objectsccb, showccb;
	private CButton savebtn, clearAllbtn, confirmbtn;
	private CCheckBox showcb;
	private CLabel usdedlbl;
	private NLabel infolbl;
	private JTextField renamtxf;
	private ItemPane infogp;
	private ReferenceWaveControl rwc;
	private MainWindow mw;

	PlainDocument ipf = new PlainDocument() {
		@Override
		public void insertString(int offs, String str, AttributeSet a)
				throws BadLocationException {
			String pstr = getText(0, getLength());
			String nstr = pstr.substring(0, offs) + str + pstr.substring(offs);
			boolean insert = true;
			final int limit = 14;
			// 如果插入字符串str后，文档超长，则插入失败
			if (nstr.length() > limit) {
				insert = false;
				ResourceBundle rb = I18nProvider.bundle();
				FadeIOShell fs = new FadeIOShell();
				fs.prompt(rb.getString("M.Utility.ReferenceWave.Warn2") + limit
						+ " !", Platform.getMainWindow().getFrame());
			}
			if (insert) {
				super.insertString(offs, str, a);
			}
		}
	};

	private WaveFormManager wfm;

	public ReferenceWavePane(final ControlManager cm) {
		super(cm);
		this.mw = Platform.getMainWindow();
		this.rwc = cm.rwc;
		wfm = mw.getDataHouse().getWaveFormManager();

		ncgp();
		nrip();
		savebtn = nbtn("Action.Save");

		nrip();
		nlbl("M.Trg.Source");

		int chlnum = cm.getAllChannelsNumber();
		if (chlnum > 1) {
			sourceccb = nccb(getComposite_suffix(cm.getCoreControl()
					.getWaveFormInfos(), "Math"));
		} else {
			sourceccb = nccb(cm.getCoreControl().getWaveFormInfos());
		}

		nrip();
		nnlbl("Action.To");
		nlbl("M.Utility.ReferenceWave.Object");
		objectsccb = nccb(rwc.getReferfileROM());
		nrip();
		nlbl("M.Utility.ReferenceWave.Rename");
		renamtxf = ntf("");
		renamtxf.setPreferredSize(new Dimension(80, 30));
		renamtxf.setDocument(ipf);
		confirmbtn = nbtn("Action.OK");

		ncgp();
		nrip();

		clearAllbtn = nbtn("Action.ClearAllShow");

		nrip();
		showcb = ncb("Action.Show");
		// nlbl("M.Utility.ReferenceWave.Object");
		showccb = nccb(rwc.getReferfileROM());
		// nrip();
		usdedlbl = nlblt("");

		infogp = nrip();
		infolbl = nnnlbl("");// nlbldt("");
		infogp.setVisible(false);

		sourceccb.setSelectedIndex(rwc.getSourceIdx());
		sourceccb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rwc.setSourceIdx(sourceccb.getSelectedIndex());
			}
		});

		savebtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean isRuntime = cm.isRuntime();
				DBG.outprintln("isRuntime? :" + isRuntime);
				int chl = sourceccb.getSelectedIndex();
				int saveId = objectsccb.getSelectedIndex();
				if (!checkSourceOnOff(chl))
					return;
				boolean isMath = (chl + 1) > cm.getSupportChannelsNumber();
				IRefSource wf = null;
				if (!isMath) {
					wf = wfm.getWaveForm(chl);
				} else {
					wf = wfm.getCompositeWaveForm();
				}
				IReferenceWaveForm rwf = cm.createReferenceWaveForm(wf, wfm);
				if (rwf == null)
					return;

				ScreenContext pc = cm.paintContext;
				rwf.resetRTIntBuf(pc.getHcenter(), pc.isScreenMode_3());
				/** 波形将显示在界面上 */
				rwc.addtoRAMList(rwf, saveId);
				/** 波形将以内部文件形式保存下来 */
				refwavfile = createRefFile(rwc.getReferfileROM()[saveId]
						.getPath());
				rwf.persistRefFile(cm, refwavfile, Platform.getControlApps()
						.getDaemon().getChannelsTransportInfo().screendatalen);

				updateShowccb();
				updateUselbl();
				updateInfogp();
				showccb.setSelectedIndex(objectsccb.getSelectedIndex());
				mw.re_paint();
			}
		});
		showcb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean sel = showcb.isSelected();
				if (sel) {
					showObject();
				} else {
					closeObjectShow();
				}
				updateInfogp();
				mw.re_paint();
			}
		});

		clearAllbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rwc.clearAllshow();
				mw.getChartScreen().re_paint();
				updateShowccb();
			}
		});

		showccb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateShowccb();
				updateUselbl();
				updateInfogp();
				mw.re_paint();
			}
		});
		renamtxf.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					renameConfirm();
			}
		});
		confirmbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				renameConfirm();

			}
		});

		updateUselbl();
		updateShowccb();
		localizeSelf();
	}

	public File refwavfile;

	private boolean checkSourceOnOff(int sel) {
		MathControl mc = cm.mathControl;
		boolean on;
		if ((sel + 1) > cm.getSupportChannelsNumber()) {
			boolean isMathvalid = (wfm.getWaveForm(mc.m1).isOn() && wfm
					.getWaveForm(mc.m2).isOn());
			on = mc.mathon && isMathvalid;
			if (wfm.getCompositeWaveForm().getADC_Buffer() == null)
				on = false;
		} else {
			WaveForm wf = wfm.getWaveForm(sel);
			on = wf.wfi.ci.isOn();
			if (wf.getADC_Buffer() == null)
				on = false;
		}

		if (!on) {
			FadeIOShell fs = new FadeIOShell();
			fs.prompt(
					I18nProvider.bundle().getString(
							"M.Utility.ReferenceWave.Warn"), mw.getFrame());
		}
		return on;
	}

	private void updateUselbl() {
		/** 判断显示序号对应的硬盘存储位置是否被占用，并提醒 */
		String s = I18nProvider.bundle().getString(
				"M.Utility.ReferenceWave.NoObject");
		int idx = showccb.getSelectedIndex();
		boolean use = rwc.getReferfileROM()[idx].use;
		showcb.setEnabled(use);
		if (use) {
			usdedlbl.setText("");
		} else
			usdedlbl.setText(s);
	}

	private void updateShowccb() {
		/** 判断显示序号对应对象是否加入内存的显示队列，是则勾选界面显示下拉框 */
		int selIdx = showccb.getSelectedIndex();
		boolean sel = rwc.isObjShowing(selIdx);
		showcb.setSelected(sel);
	}

	private void updateInfogp() {
		CoreControl cc = cm.getCoreControl();
		MathControl mc = cm.mathControl;
		TimeConfProvider tcp = cc.getTimeConfProvider();
		VoltageProvider vp = cc.getVoltageProvider();

		/** 更新screen的多条参考信息 */
		rwc.updateAllInfo(tcp, vp, mc);
		/** 更新DockDialog的单条参考信息 */
		boolean show = showcb.isSelected();
		infogp.setVisible(show);
		if (show) {
			int selIdx = showccb.getSelectedIndex();
			String singleChInfo = rwc.updateSingleInfo(selIdx, tcp, vp, mc);//
			infolbl.setText(singleChInfo);
		}
	}

	private void updateWidgetUI() {
		objectsccb.updateUI();
		showccb.updateUI();
	}

	private void renameConfirm() {
		String name = renamtxf.getText();
		int objsel = objectsccb.getSelectedIndex();
		rwc.getReferfileROM()[objsel].setName(name);
		updateInfogp();

		updateWidgetUI();
		mw.getChartScreen().re_paint();
	}

	private void showObject() {
		int showIdx = showccb.getSelectedIndex();
		boolean show = rwc.isObjShowing(showIdx);
		if (show)
			return;

		ReferenceFile rf = rwc.getReferfileROM()[showIdx];
		if (rf.use) {
			refwavfile = createRefFile(rf.getPath());
			IReferenceWaveForm rwf = cm.loadRefWF(refwavfile);

			if (rwf == null)
				return;
			ScreenContext pc = cm.paintContext;
			rwf.resetRTIntBuf(pc.getHcenter(), pc.isScreenMode_3());
			rwc.addtoRAMList(rwf, showIdx);
		} else {
			// TODO 目标没有文件
		}
	}

	private void closeObjectShow() {
		int dest = showccb.getSelectedIndex();
		rwc.removefromRAMList(dest);
	}

	private File createRefFile(String filePath) {
		File dir = new File("ref", REFERENCE_DIRNAME);
		dir.mkdirs();
		return new File(dir, filePath);

	}
}