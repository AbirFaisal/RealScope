package com.owon.uppersoft.dso.view.pane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

import com.owon.uppersoft.dso.function.FFTView;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.mode.control.FFTControl;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.view.MainWindow;
import com.owon.uppersoft.dso.view.pane.dock.widget.FunctionPanel;
import com.owon.uppersoft.dso.view.pane.dock.widget.GroupPane;
import com.owon.uppersoft.dso.view.pane.dock.widget.ItemPane;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.control.MathControl;
import com.owon.uppersoft.vds.core.fft.WndType;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.core.wf.WaveFormInfo;
import com.owon.uppersoft.vds.ui.prompt.FadeIOShell;
import com.owon.uppersoft.vds.ui.widget.custom.CCheckBox;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.custom.CLabel;
import com.owon.uppersoft.vds.util.format.SFormatter;

/**
 * <code>
 Waveform math	Dual-waveform math	
 Operand 1	ch1,ch2,ch3,ch4
 Operator	add, subtract, multiply, divide
 Operand 2	ch1,ch2,ch3,ch4
 FFT	
 Source	ch1,ch2,ch3,ch4
 Window	hamming,rectangle,blackman,hanning
 Format	Vrms,dB
 Zoom	X1,X2,X5,X10
 <code>
 * 
 */
public class MathPane extends FunctionPanel {
	public static final boolean SupportOffChannel = false;
	public final boolean SupportMarkCursorInFFT = true;

	private ItemPane mathp1, mathp2, mathp3, fcp, fwp, ffp, fsp, ftbp;
	private CCheckBox mathcb, fftcb;
	private CComboBox mtx1, mtx2, mtop, mvb, ftch, ftwt, ftfm, ftv, ftsc, fttb;
	private CLabel remindLbl;
	boolean listening = false;

	private CComboBox attcbb;
	private CCheckBox frechb, voltchb;
	private GroupPane fftCursorgp;

	private FFTView fftv;
	private MathControl mc;
	private FFTControl fftc;

	private WaveFormManager wfm;
	private DataHouse dh;

	private void createMathPart(ControlManager cm) {
		if (!wfm.isMathSupport())
			return;
		ncgp();
		nrip();
		mathcb = ncb("M.Math.Name");

		WaveFormInfo[] wfis = cm.getCoreControl().getWaveFormInfos();

		mathp1 = nrip();
		mtx1 = nccb(wfis);
		mtop = nccb(MathControl.operations);
		mtx2 = nccb(wfis);

		mathp2 = nrip();
		nlbl("M.Math.Volt");
		mvb = nccb(mc.getMathVBs());
		mvb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!listening)
					return;
				if (ItemEvent.SELECTED != e.getStateChange())
					return;

				resetPersistence();// Clear persistence
				mc.setMathvbidx(mvb.getSelectedIndex());
				updateMathCompute();
			}
		});

		if (false) {
			nrip();
			nlbl("M.Channel.ProbeRate");
			attcbb = nccb(cm.getMachineInfo().ProbeMulties);
			attcbb.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					resetPersistence();// Clear persistence
					// mc.probeIndex = attcbb.getSelectedIndex();
					updateMathCompute();
				}
			});
		}

		mathp3 = nrip();
		remindLbl = nlblt("");

		boolean mathon = mc.mathon;
		mathcb.setSelected(mathon);
		mathshow(mathon);
		if (mathon) {
			updateMathVbIdx();
			mvb.setSelectedIndex(mc.getMathvbidx());
		}

		mathcb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!listening)
					return;

				boolean b = mc.mathon = mathcb.isSelected();
				// KNOW The switch toggling always goes through here and is toggled repeatedly; when going from invalid to valid, compute the effective value first, then update the scale
				if (!b) {
					mc.setMathvbidx(-1);
					getMainWindow().updateShow();
				} else {
					updateMathVbIdx();

					listening = false;
					refreshMathVoltBase();
					listening = true;
					updateMathCompute();
				}
				mathshow(b);
				updateMathRemindLbl(wfm, true);
			}
		});
		mtx1.setSelectedIndex(mc.m1);
		mtx2.setSelectedIndex(mc.m2);
		mtop.setSelectedIndex(mc.operation);
		updateMathRemindLbl(wfm, true);

		mtx1.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!listening)
					return;
				if (ItemEvent.SELECTED != e.getStateChange())
					return;

				resetPersistence();// Clear persistence
				mc.m1 = mtx1.getSelectedIndex();
				updateMathCompute();
				refreshMathVoltBase();
				updateMathRemindLbl(wfm, true);
			}
		});
		mtop.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!listening)
					return;
				if (ItemEvent.SELECTED != e.getStateChange())
					return;
				wfm.setMathOperation(mtop.getSelectedIndex());
				updateMathCompute();

				mvb.setModel(new DefaultComboBoxModel(mc.getMathVBs()));
				refreshMathVoltBase();
			}
		});
		mtx2.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!listening)
					return;
				if (ItemEvent.SELECTED != e.getStateChange())
					return;

				resetPersistence();// Clear persistence
				mc.m2 = mtx2.getSelectedIndex();
				updateMathCompute();
				refreshMathVoltBase();
				updateMathRemindLbl(wfm, true);
			}
		});
	}

	public MathPane(final ControlManager cm) {
		super(cm);
		dh = Platform.getDataHouse();
		mc = cm.mathControl;
		fftc = cm.getFFTControl();
		wfm = dh.getWaveFormManager();

		fftv = wfm.getFFTView();
		fftv.vrmsbaseidx = wfm.getWaveForm(fftc.getFFTchl()).getVoltbaseIndex();

		createMathPart(cm);

		WaveFormInfo[] wfis = cm.getCoreControl().getWaveFormInfos();

		ncgp();
		nrip();
		fftcb = ncbt("FFT");

		fcp = nrip();
		nlbl("M.Math.Channel");

		if (SupportOffChannel)
			ftch = nccb(getComposite_suffix(wfis, "---"));
		else {
			ftch = nccb(wfis);
			if (cm.getAllChannelsNumber() == 1)
				ftch.setEnabled(false);
		}

		fwp = nrip();
		nlbl("M.Math.FFT.Window");
		ftwt = nccb(WndType.values());

		ffp = nrip();
		nlbl("M.Math.FFT.Format");
		ftfm = nccb(FFTControl.format);

		reBuildValuePerDivComboBox(mc);

		fsp = nrip();
		nlbl("M.Math.FFT.Scale");
		ftsc = nccb(FFTControl.scale);

		ftbp = nrip();
		nlbl("M.Math.FFT.Freq");
		fttb = nccb(cm.getMachineInfo().FFTTimeBases);
		fttb.setSelectedIndex(fftc.getFFTTimebaseIndex());
		nlblt("/Div");
		fttb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!listening)
					return;
				if (ItemEvent.SELECTED != e.getStateChange())
					return;

				fftc.setFFTTimebaseIndex(fttb.getSelectedIndex());
				updateFFT();
			}
		});

		if (SupportMarkCursorInFFT) {
			fftCursorgp = ncgp();
			nrip();
			nlbl("M.Mark.Name");
			nrip();
			frechb = ncb("M.Mark.FFTFreq");
			voltchb = ncb("M.Mark.FFTVamp");
		}
		fftcb.setSelected(fftc.isFFTon());
		fftshow(fftc.isFFTon());
		localizeSelf();

		fftcb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!listening)
					return;

				fftcb.setEnabled(false);
				changeFFT(fftcb.isSelected());
				/** fftAdd a delay at the fft switching point to slow the sending of commands */
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				fftcb.setEnabled(true);
			}

		});

		int idx = fftc.getFFTchl();
		ftch.setSelectedIndex(idx);

		final int lastchlidx = ftch.getItemCount() - 1;
		if (SupportOffChannel) {
			if (!wfm.getWaveForm(idx).isOn()) {
				int invalidchlidx = lastchlidx;
				ftch.setSelectedIndex(invalidchlidx);
			}
		}
		ftwt.setSelectedIndex(fftc.getFFTwnd());
		ftfm.setSelectedIndex(fftc.fftvaluetype);
		ftsc.setSelectedIndex(fftc.fftscale);
		ftch.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!listening)
					return;
				if (ItemEvent.SELECTED != e.getStateChange())
					return;
				int idx = ftch.getSelectedIndex();

				/**
				 * TODO The only problem here is that the selected item in mc.fftchl is inconsistent with the front end; when ftch is "---",
				 * in the back end an existing channel is actually selected, and that channel can then not be turned off
				 */
				if (SupportOffChannel) {
					int invalidchlidx = lastchlidx;
					if (idx == invalidchlidx) {
						return;
					}
				}

				if (fftc.getFFTchl() == idx)
					return;

				WaveForm wf = wfm.getWaveForm(idx);
				if (wf == null)
					return;

				if (SupportOffChannel) {
					if (!wf.isOn()) {
						int invalidchlidx = lastchlidx;
						ftch.setSelectedIndex(invalidchlidx);
						FadeIOShell pv = new FadeIOShell();
						String n = SFormatter.UIformat(
								wfm.getFFTView().fftchlLack,
								wf.wfi.ci.getName());
						pv.prompt(n, getMainWindow().getFrame());
						return;
					}
				}

				fftc.c_setFFTchl(idx);
				updateFFT();
				dh.getMainWindow().getToolPane().getInfoPane().repaint();
			}
		});
		ftwt.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!listening)
					return;
				if (ItemEvent.SELECTED != e.getStateChange())
					return;
				fftc.setFFTwnd(ftwt.getSelectedIndex());
				updateFFT();
			}
		});
		ftfm.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!listening)
					return;
				if (ItemEvent.SELECTED != e.getStateChange())
					return;
				fftc.fftvaluetype = ftfm.getSelectedIndex();
				updateFFT();

				reBuildValuePerDivComboBox(mc);
			}
		});

		ftsc.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!listening)
					return;
				if (ItemEvent.SELECTED != e.getStateChange())
					return;
				int idx = ftsc.getSelectedIndex();
				fftc.fftscale = idx;
				fftv.scale();
				updateFFT();
			}
		});
		if (SupportMarkCursorInFFT) {
			frechb.setSelected(cm.fftctr.getOnFrebaseMark());
			voltchb.setSelected(cm.fftctr.getOnVoltbaseMark());

			frechb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!listening)
						return;
					cm.fftctr.setOnFrebaseMark(frechb.isSelected());
					Platform.getMainWindow().getChartScreen().re_paint();
				}
			});

			voltchb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!listening)
						return;
					cm.fftctr.setOnVoltbaseMark(voltchb.isSelected());
					Platform.getMainWindow().getChartScreen().re_paint();
				}
			});
		}
		listening = true;
	}

	private MainWindow getMainWindow() {
		return dh.getMainWindow();
	}

	protected void updateFFT() {
		/**
		 * Takes effect directly on the next frame while running; needs a refresh when stopped
		 */
		if (cm.isKeepGet())
			return;
		fftv.receiveNewData();
		getMainWindow().updateShow();
	}

	protected void resetPersistence() {
		cm.resetPersistence();
	}

	public void changeFFT(boolean b) {
		fftc.c_setFFTon(b);
		fftshow(b);
		if (b) {
			listening = false;
			fttb.setSelectedIndex(fftc.getFFTTimebaseIndex());
			cm.pcs.firePropertyChange(PropertiesItem.TURN_ON_MARKBULLETIN,
					null, false);//Switching to fft will turn off cursor measurement, as it duplicates fft measurement
			listening = true;
		}
		getMainWindow().getToolPane().getButtonPane().switch_3in1_fft(b);
		// Takes effect directly on the next frame while running; needs a refresh when stopped
		if (!cm.sourceManager.isConnected() && b) {
			updateFFT();
		}
	}

	private void reBuildValuePerDivComboBox(final MathControl mc) {
		if (ftv != null) {
			ffp.remove(ftv);
			ftv = null;
			ffp.repaint();
		}
		switch (fftc.fftvaluetype) {
		case 0:
			ftv = new CComboBox(fftc.VOLTAGEs);
			ffp.add(ftv);
			ftv.setSelectedIndex(fftv.vrmsbaseidx);
			fftv.setVrmsIdx(ftv.getSelectedIndex());
			break;
		case 1:
			ftv = new CComboBox(fftc.dBPerDiv);
			ffp.add(ftv);
			ftv.setSelectedIndex(fftv.dBbaseidx);
			fftv.setVrmsIdx(ftv.getSelectedIndex());
			break;
		}
		ftv.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!listening)
					return;
				if (ItemEvent.SELECTED != e.getStateChange())
					return;
				int idx = 0;
				switch (fftc.fftvaluetype) {
				case 0:
					idx = ftv.getSelectedIndex();
					fftv.vrmsbaseidx = idx;
					break;
				case 1:
					idx = ftv.getSelectedIndex();
					fftv.dBbaseidx = idx;
					break;
				}
				fftv.setVrmsIdx(idx);
				updateFFT();
			}
		});
	}

	protected void fftshow(boolean b) {
		fcp.setVisible(b);
		fwp.setVisible(b);
		ffp.setVisible(b);
		fsp.setVisible(b);
		ftbp.setVisible(b);
		if (SupportMarkCursorInFFT) {
			fftCursorgp.setVisible(b);
		}
	}

	protected void updateMathVbIdx() {
		WaveForm ch1 = wfm.getM1(), ch2 = wfm.getM2();
		mc.updateMathVbIdx(ch1.getVoltbaseIndex(), ch2.getVoltbaseIndex());
	}

	/**
	 * Takes effect directly on the next frame while running; needs a refresh when stopped
	 */
	protected void updateMathCompute() {
		if (cm.isKeepGet())
			return;
		MainWindow mw = getMainWindow();
		ScreenContext pc = mw.getChartScreen().getPaintContext();

		updateMathVbIdx();

		wfm.getCompositeWaveForm().receiveNewData(pc);
		mw.updateShow();
	}

	protected void updateMathRemindLbl(WaveFormManager wfm, boolean b) {
		boolean mathon = mathcb.isSelected();
		if (mathon) {
			String chName = wfm.getClosedChannelName(mc.m1, mc.m2);
			if (chName.length() == 0) {
				mathp3.setVisible(false);
			} else {
				String text = chName
						+ " "
						+ I18nProvider.bundle()
								.getString("M.Channel.OffRemind");
				remindLbl.setText(text);
				mathp3.setVisible(true);
				if (b) {
					FadeIOShell fs = new FadeIOShell();
					String s = I18nProvider.bundle().getString("M.Math.Name")
							+ ":" + text;
					fs.prompt(s, getMainWindow().getFrame());
				}
			}
		}

	}

	public void refreshMathVoltBase() {
		WaveForm ch1 = wfm.getM1(), ch2 = wfm.getM2();
		mc.refreshMathVoltBase(ch1.getVoltbaseIndex(), ch2.getVoltbaseIndex());
		mvb.setSelectedIndex(mc.getMathvbidx());
	}

	protected void mathshow(boolean mathon) {
		mathp1.setVisible(mathon);
		mathp2.setVisible(mathon);
		mathp3.setVisible(mathon);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String n = evt.getPropertyName();
		if (n.equals(PropertiesItem.CHANNEL_OPTION)) {
			updateMathRemindLbl(wfm, false);
		} else if (n.equals(PropertiesItem.START_AUTOSET)) {
			/** fftfft is closed and not reopened, since auto setting does not enter fftfft */
			changeFFT(false);
			fftcb.setSelected(false);
		} else if (n.equals(PropertiesItem.MACHINETYPE_CHANGE)) {
			listening = false;
			fttb.setModel(new DefaultComboBoxModel(
					cm.getMachineInfo().FFTTimeBases));
			listening = true;
		} else if (n.equals(PropertiesItem.UPDATE_CURSOR)) {
			listening = false;
			if (SupportMarkCursorInFFT) {
				frechb.setSelected(cm.fftctr.getOnFrebaseMark());
				voltchb.setSelected(cm.fftctr.getOnVoltbaseMark());
			}
			listening = true;
		} else if (n.equals(PropertiesItem.UPDATE_FFT)) {
			listening = false;
			boolean format = (Boolean) evt.getNewValue();
			if (format) {
				ftfm.setSelectedIndex(fftc.fftvaluetype);
				reBuildValuePerDivComboBox(mc);
			} else {
				ftch.setSelectedIndex(fftc.getFFTchl());
				ftwt.setSelectedIndex(fftc.getFFTwnd());
				ftsc.setSelectedIndex(fftc.fftscale);
				fttb.setSelectedIndex(fftc.getFFTTimebaseIndex());
			}
			listening = true;
		} else if (n.equals(PropertiesItem.CHANGE_FFT)) {
			listening = false;
			fftcb.setSelected(fftc.isFFTon());
			fftshow(fftc.isFFTon());
			listening = true;//
		}
	}

	public boolean canLeave() {
		if (fftc.isFFTon()) {
			int res = JOptionPane.showConfirmDialog(Platform.getMainWindow()
					.getFrame(),
					I18nProvider.bundle().getString("Label.FFTLock"), null,
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (res == JOptionPane.YES_OPTION) {
				changeFFT(false);
				return true;
			} else
				return false;
		}
		return true;
	}

}
