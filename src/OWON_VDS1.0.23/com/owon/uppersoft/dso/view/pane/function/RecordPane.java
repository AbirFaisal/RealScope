package com.owon.uppersoft.dso.view.pane.function;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.owon.uppersoft.dso.function.PlayerControl;
import com.owon.uppersoft.dso.function.RecordControl;
import com.owon.uppersoft.dso.function.record.OpenPropertyChangeEvent;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.dso.view.pane.dock.widget.ItemPane;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.ui.resource.FontCenter;
import com.owon.uppersoft.vds.ui.widget.custom.CButton;
import com.owon.uppersoft.vds.ui.widget.custom.CCheckBox;
import com.owon.uppersoft.vds.ui.widget.custom.CLabel;
import com.owon.uppersoft.vds.ui.widget.custom.ExcludeButtons;
import com.owon.uppersoft.vds.ui.widget.custom.HCLabel;
import com.owon.uppersoft.vds.util.FileUtil;
import com.owon.uppersoft.vds.util.MyFileFilter;

/**
 * <code>波形录制	模式	录制，回放，存储，关闭
 操作	开始，停止
 录制	
 帧设置	终止帧，时间间隔（1ms~????s）
 波形刷新	开启，关闭
 回放	
 帧设置	起始帧，终止帧，当前帧，时间间隔
 回放模式	循环，单次
 存储	
 帧设置	起始帧，终止帧
 保存	
 调出	

 文件操作	打开	波形文件
 另存为	
 <code>

 可能引起状态改变的情况：初试载入；主界面RT与否切换；(在可操作的前提下)点击切换录制状态；(在可操作的前提下)点击回放录制状态
 * 
 * @author Matt
 * 
 */
public class RecordPane extends FunctionPanel {

	private PlayerControl pc;
	private RecordControl rc;
	private CLabel cnt, cnt2;
	private CButton rbtn;
	private CButton rbrw;
	private JSpinner rjs, mjs;
	private CButton pbrw;
	private CButton pbtn;
	private JSpinner pjs;
	private JSlider timelineslider;
	private CCheckBox cycle;
	private JSpinner sta, end;

	private HCLabel rhcl, phcl;
	private ResourceBundle rb;
	private final static int PLAYPAGE = 1, RECORDPAGE = 0;

	private ExcludeButtons ebs;
	private int selPage;
	private ControlManager cm;
	private FunctionPanel rp;

	private PropertyChangeListener rec_playBtnPcl = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String n = evt.getPropertyName();
			if (n.equalsIgnoreCase(ExcludeButtons.EXCLUDE)) {
				boolean leave = canLeave();
				// 后续考虑移入ExcludeButtons控件类中实现(状态可以选择性切换)。
				if (!leave) {
					ebs.setSelected(selPage);
					return;
				}

				if (selPage == PLAYPAGE)
					beforePlayLeave();

				selPage = (Integer) evt.getNewValue();
				switch2Pane();
				// System.out.println("pc.cur"+pc.current);
			}
		}
	};

	private void beforePlayLeave() {
		boolean pr = pc.isPlaying();
		if (pr) {
			// 释放当前载入的播放文件
			CByteArrayInputStream ba = pc.getCByteArrayInputStream();
			if (ba != null) {
				// if (pc.playing) {
				pc.pausePlaying();
				updatePlayStatus(pr);
				// }
				ba.dispose();
			}
		}
		// 离开play前保存currnt,在进入时载入。
		pc.saveCurrent();
	}

	public RecordPane(ControlManager cm) {
		super(cm);
		this.cm = cm;
		cm.pcs.addPropertyChangeListener(this);
		pc = cm.playCtrl;
		rc = cm.rc;

		rb = I18nProvider.bundle();
		String[] text = { rb.getString("Action.Record"),
				rb.getString("Action.Play") };
		ebs = new ExcludeButtons(text, rec_playBtnPcl, 0, 130, 30,
				FontCenter.getLabelFont());

		ncgp();
		nrip().add(ebs);

		rp = createRecordPane(cm);
		selPage = RECORDPAGE;
		addLocalizable(rp);

		add(rp);

		localizeSelf();
		updateRuntimeStatus(cm.isRuntime());
	}

	private void switch2Pane() {
		remove(rp);
		if (selPage == PLAYPAGE) {
			rp = createPlayPane(cm);// 先构建后Check，文件预读信息才能设置到界面上
			pc.setPathNCheck(new File(pc.getFilePath()), true);
		} else {
			rp = createRecordPane(cm);
		}
		addLocalizable(rp);
		add(rp);
		localizeSelf();
		updateRuntimeStatus(cm.isRuntime());
		repaint();
	}

	private FunctionPanel createRecordPane(final ControlManager cm) {
		FunctionPanel fp = new FunctionPanel(cm) {
		};

		fp.ncgp();
		fp.nrip();
		rbrw = fp.nbtn("Action.SaveAs");
		rbrw.setPreferredSize(new Dimension(160, 30));

		fp.nrip();
		rhcl = fp.nlbldt(rc.getFilePath());
		rhcl.setEditable(false);
		rhcl.setPreferredSize(new Dimension(250, 55));

		rbrw.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				browseforSaveas();
				cnt.setText("0");
			}
		});

		fp.ncgp();
		fp.nrip();
		rbtn = fp.nbtnt("");
		rbtn.setPreferredSize(new Dimension(135, 30));

		rc.setPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String n = evt.getPropertyName();
				if (n.equalsIgnoreCase(PropertiesItem.RecordFrameIndex)) {
					OpenPropertyChangeEvent oevt = (OpenPropertyChangeEvent) evt;
					int i = oevt.newInt;
					if (i >= 0) {
						cnt.setText(String.valueOf(i));
					} else {
						updateRecordStatus(rc.isRecording());
					}
				}
			}
		});

		fp.nlbl("Action.FrameCount");
		cnt = fp.nlblt(String.valueOf(rc.getCounter()));

		ItemPane ip = fp.nrip();
		fp.nlbl("Action.TimeGap");
		final int gdp = rc.intervalTime;
		rjs = new JSpinner(new SpinnerNumberModel(gdp, 0, 100000, 10));// cm.getDataPeroid
		rjs.setPreferredSize(new Dimension(80, 30));
		ip.add(rjs);
		rjs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				rc.intervalTime = (Integer) rjs.getValue();
			}
		});
		CLabel ucl = new CLabel();
		ucl.setText("ms");
		ip.add(ucl);

		ItemPane ip2 = fp.nrip();
		fp.nlbl("Action.RecordEndFrame");
		final int gef = rc.endFrame;
		mjs = new JSpinner(new SpinnerNumberModel(gef, 1, 100000, 1));
		mjs.setPreferredSize(new Dimension(80, 30));
		ip2.add(mjs);
		mjs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				rc.endFrame = (Integer) mjs.getValue();
			}
		});
		ip2.add(nnlbl("M.Record.Frame"));

		rbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rc.setTimegap((Integer) rjs.getValue());// - cm.getDataPeroid;
				int max = (Integer) mjs.getValue();
				rc.setMaxframe(max);

				boolean r = rc.switchRecording(new Runnable() {

					@Override
					public void run() {
						String mes = I18nProvider.bundle().getString(
								"M.Record.PathInvalid");
						String title = I18nProvider.bundle().getString(
								"M.Record.Name");
						int rsp = JOptionPane.showConfirmDialog(Platform
								.getMainWindow().getFrame(), mes, title,
								JOptionPane.YES_NO_OPTION,
								JOptionPane.ERROR_MESSAGE);
						if (rsp == JOptionPane.YES_OPTION) {
							browseforSaveas();
						}
					}
				});
				if (r)
					updateRecordStatus(rc.isRecording());
			}
		});

		fp.nrip();
		final HCLabel hcl2 = fp.nlbld("Label.RecordTip");
		hcl2.setEditable(false);
		hcl2.setPreferredSize(new Dimension(250, 140));

		return fp;
	}

	public void browseforSaveas() {
		JFileChooser jfc = new JFileChooser();
		jfc.addChoosableFileFilter(MyFileFilter.CAPFilter);
		jfc.setCurrentDirectory(new File(rc.getFilePath()).getParentFile());

		JFrame mf = Platform.getMainWindow().getFrame();
		int rsl = jfc.showSaveDialog(mf);
		if (rsl == JFileChooser.APPROVE_OPTION) {
			File f = FileUtil.checkFileSuffix(jfc,
					MyFileFilter.CAPFilter.getEnds());
			rc.setPath(f);
			rhcl.setText(rc.getFilePath());
		}
	}

	private FunctionPanel createPlayPane(final ControlManager cm) {
		FunctionPanel fp = new FunctionPanel(cm) {
		};

		fp.ncgp();
		fp.nrip();
		pbrw = fp.nbtn("Action.Use");
		pbrw.setPreferredSize(new Dimension(120, 30));

		fp.nrip();

		// hcl = fp.nlbldt("");
		phcl = fp.nlbldt(pc.getFilePath());
		phcl.setEditable(false);
		phcl.setPreferredSize(new Dimension(250, 75));

		pbrw.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.addChoosableFileFilter(MyFileFilter.CAPFilter);
				jfc.setCurrentDirectory(new File(pc.getFilePath())
						.getParentFile());
				int rsl = jfc.showOpenDialog(RecordPane.this);
				if (rsl == JFileChooser.APPROVE_OPTION) {
					pc.resetSavedCurrent();
					cnt2.setText("0");

					File f = jfc.getSelectedFile();
					pc.setPathNCheck(f, false);
					phcl.setText(pc.getFilePath());
				}
			}
		});

		pc.setPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String n = evt.getPropertyName();
				if (n.equalsIgnoreCase(PropertiesItem.PLAY_PROGRESS)) {// TimeLineCheck
					OpenPropertyChangeEvent oevt = (OpenPropertyChangeEvent) evt;
					int i = oevt.newInt;
					if (i >= 0) {
						// 播放时更新Text帧数,在timelineslider.setValue(i);会更新
						// cnt2.setText(String.valueOf(i));
						timelineslider.setValue(i);
					} else {
						// 当自动播放结束用于切换状态
						updateRecordStatus(pc.isPlaying());
						updatePlayStatus(pc.isPlaying());
					}
				}
				if (n.equalsIgnoreCase(PropertiesItem.READ_HEADER)) {
					OpenPropertyChangeEvent oevt = (OpenPropertyChangeEvent) evt;
					int i = oevt.newInt;
					sta.setValue(0);
					end.setValue(i);
					listening = false;
					timelineslider.setMaximum(i);
					timelineslider.setValue(0);
					listening = true;

					pc.setTimegap((Integer) pjs.getValue());
					pc.setStartEndFrame((Integer) sta.getValue(),
							(Integer) end.getValue());
					loadPauseStatus();
				}
			}
		});

		fp.ncgp();
		fp.nrip();
		pbtn = fp.nbtnt("");

		pbtn.setPreferredSize(new Dimension(120, 30));

		fp.nlbl("Action.FrameNumber");
		cnt2 = fp.nlblt("0");

		ItemPane ip = fp.nrip();
		fp.nlbl("Action.TimeGap");
		pjs = new JSpinner(new SpinnerNumberModel(200, 0, 100000, 10));
		pjs.setPreferredSize(new Dimension(80, 30));
		ip.add(pjs);
		pjs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				pc.setTimegap((Integer) pjs.getValue());
			}
		});
		CLabel ucl = new CLabel();
		ucl.setText("ms");
		ip.add(ucl);

		pbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO 暂停时，因为设置起始播放帧，播放起点将从控件上的起始帧开始播(默认为0)。

				boolean isRuntime = cm.isRuntime();
				if (isRuntime) {
					boolean canplay = pc.confirmStopRunningforPlaying();
					if (!canplay)
						return;
				}
				if (pc.fileInvalid()) {
					phcl.setText(rb.getString("M.Record.LoadInvalid") + '\n'
							+ pc.getFilePath());
					pc.promptInvalidLoad();
					return;
				}
				pc.switchPlaying();
				// 当播放或暂停时用于切换状态
				updateRecordStatus(pc.isPlaying());
				updatePlayStatus(pc.isPlaying());
			}
		});

		fp.nrip();
		cycle = fp.ncb("M.Record.Cycle");
		cycle.setSelected(pc.cyc);
		cycle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pc.cyc = cycle.isSelected();
			}
		});
		ip = fp.nrip();
		fp.nlbl("M.Record.Start");
		sta = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
		sta.setPreferredSize(new Dimension(60, 30));
		ip.add(sta);
		sta.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				pc.setStartEndFrame((Integer) sta.getValue(),
						(Integer) end.getValue());
			}
		});
		fp.nlbl("M.Record.End");
		end = new JSpinner(new SpinnerNumberModel(30, 1, 10000, 1));
		end.setPreferredSize(new Dimension(60, 30));
		ip.add(end);
		end.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				pc.setStartEndFrame((Integer) sta.getValue(),
						(Integer) end.getValue());
			}
		});

		ip = fp.nrip();
		timelineslider = new JSlider();
		// 拖拽可重定位当前帧
		timelineslider.setPreferredSize(new Dimension(250, 50));
		// timelineslider.setValue(0);//构造时setpathNcheck()会设零
		ip.add(timelineslider);
		timelineslider.addMouseListener(new MouseAdapter() {
			boolean foreplaying;

			@Override
			public void mousePressed(MouseEvent e) {
				foreplaying = pc.isPlaying();
				if (pc.isPlaying())
					pc.pausePlaying();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				int curf = timelineslider.getValue();

				if (!foreplaying)
					return;
				pc.startPlaying();
			}
		});

		timelineslider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!listening)
					return;
				if (pc.fileInvalid())
					return;
				int curf = timelineslider.getValue();
				cnt2.setText("" + curf);
				/**
				 * 当timelineslider拖拽到最后一帧时该帧不设置且不播放
				 */
				if (curf < 0 || curf >= pc.counter)
					return;
				pc.setCurrentNStreamPointer(curf);
				if (!cm.isRuntime())
					pc.updateCurrentScreen();
			}

		});
		return fp;
	}

	boolean listening = true;

	/**
	 * 更新录制按钮文本
	 * 
	 * @param rs
	 * @param recording
	 */
	private void updateRS(CButton rs, boolean recording) {
		ResourceBundle rb = I18nProvider.bundle();
		rs.setText(recording ? rb.getString("Action.EndRecord") : rb
				.getString("Action.BeginRecord"));
	}

	/**
	 * 更新回放按钮文本
	 * 
	 * @param rs
	 * @param playing
	 */
	private void updatePP(CButton rs, boolean playing) {
		ResourceBundle rb = I18nProvider.bundle();
		rs.setText(playing ? rb.getString("Action.Pause") : rb
				.getString("Action.Play"));
	}

	/**
	 * 在可录制前提下，更新录制状态
	 * 
	 * @param p
	 */
	private void updateRecordStatus(boolean r) {
		boolean op = !r;
		rbrw.setEnabled(op);
		rjs.setEnabled(op);
		mjs.setEnabled(op);
		updateRS(rbtn, r);
	}

	/**
	 * 在可回放前提下，更新回放状态
	 * 
	 * @param p
	 */
	private void updatePlayStatus(boolean p) {
		boolean op = !p;
		pbrw.setEnabled(op);
		pjs.setEnabled(op);
		sta.setEnabled(op);
		end.setEnabled(op);
		updatePP(pbtn, p);
	}

	private void loadPauseStatus() {
		// 恢复暂停时状态
		pc.loadSavedCurrent();
		cnt2.setText("" + pc.getSavedCurrent());
		listening = false;
		timelineslider.setValue(pc.getSavedCurrent());
		listening = true;
	}

	/**
	 * 更新运行停止状态，以此判断并更新所有的录制回放的状态
	 * 
	 * @param rt
	 */
	private void updateRuntimeStatus(boolean rt) {
		boolean stop = !rt;
		boolean rr, pr;
		rr = rc.isRecording();
		pr = pc.isPlaying();
		if (selPage == PLAYPAGE) {
			updatePlayStatus(stop && pr);
			// pbtn.setEnabled(stop);
		} else {
			updateRecordStatus(rt && rr);
			rbtn.setEnabled(rt);
		}

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String n = evt.getPropertyName();
		if (n.equalsIgnoreCase(PropertiesItem.KEEPGET)) {
			boolean rt = (Boolean) evt.getNewValue();
			// int sel = jtp.getSelectedIndex();
			if (this != null)
				updateRuntimeStatus(rt);
			if (!rt && selPage == PLAYPAGE && !cm.reloadManager.isReload()) {
				// (如果多&&sel==1的判断，若在sel==0软件就停，
				// 判断进不来，回放的文件便不是刚录制的文件。)
				boolean p = pc.setPathNCheck(new File(pc.getFilePath()), false);
				if (!p)
					phcl.setText(rb.getString("M.Record.LoadInvalid") + '\n'
							+ pc.getFilePath());
			}
		} else if (n.equalsIgnoreCase(PropertiesItem.SWITCH_PLAYPANE)) {
			ebs.setSelected(PLAYPAGE);
			selPage = PLAYPAGE;
			switch2Pane();
		}
	}

	public void beforeLeave() {
		if (selPage == PLAYPAGE)
			beforePlayLeave();
	}

	public boolean canLeave() {
		switch (selPage) {
		case RECORDPAGE:
			boolean rr = rc.isRecording();
			// if (rr) {
			// FadeIOShell pv = new FadeIOShell();
			// pv.prompt(rb.getString("M.Record.LeaveRecWarn"), Platform
			// .getMainWindow().getFrame());
			// return false;
			// }
			break;
		case PLAYPAGE:
			// FadeIOShell pv = new FadeIOShell();
			// pv.prompt(rb.getString("M.Record.LeavePlayWarn"), Platform
			// .getMainWindow().getFrame());
			// return false;
			break;
		}
		return true;
	}
}
