package com.owon.uppersoft.dso.global;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeSupport;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.view.pane.DisplayPane;
import com.owon.uppersoft.dso.wf.ON_WF_Iterator;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.device.interpret.Tune2;
import com.owon.uppersoft.vds.machine.PrincipleTiny;
import com.owon.uppersoft.vds.machine.TinyMachine;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.vds.calibration.BaselineCalDelegateTiny;
import com.owon.vds.tiny.circle.AGPControl;
import com.owon.vds.tiny.circle.DFAGP;
import com.owon.vds.tiny.circle.GainCalDelegate;
import com.owon.vds.tiny.circle.agp.SerialPortUtil;

public class DisplayPaneTiny extends DisplayPane {

	private static String[] VmpList = { "30mVpp", "60mVpp", "120mVpp",
			"300mVpp", "600mVpp", "1.2Vpp", "3Vpp", "6Vpp", "12Vpp", "30Vpp" };

	private boolean btBool;
	private DFAGP dfagp;

	public DisplayPaneTiny(ControlManager cm) {
		super(cm);
	}

	@Override
	public void beforeLeave() {
		// System.out.println(dfagp);
		if (dfagp != null)
			dfagp.finish();
		super.beforeLeave();
	}

	protected void circleCal(final Window wnd, final PrincipleTiny pt,
			final AGPControl agp) {
		final ControlApps ca = Platform.getControlApps();
		final TinyMachine tm = pt.getMachineType();

		final BaselineCalDelegateTiny bcd1 = new BaselineCalDelegateTiny(wnd,
				ca, tm, pt);
		// 第1步自校正
		bcd1.prompte = false;
		bcd1.nextJob = new Runnable() {
			@Override
			public void run() {
				final GainCalDelegate gcd = new GainCalDelegate(wnd, ca, tm, pt);
				// 第2步闭环
				gcd.prompte = false;
				gcd.nextJob = new Runnable() {

					@Override
					public void run() {
						final BaselineCalDelegateTiny bcd2 = new BaselineCalDelegateTiny(
								wnd, ca, tm, pt);
						// 第3步自校正
						bcd2.prompte = false;
						bcd2.nextJob = new Runnable() {
							@Override
							public void run() {
								agp.turnChannels(true);
							}
						};
						agp.turnChannels(false);
						/**
						 * 这里以线程的方式执行下一步，否则会出现莫名死锁锁住Flow的mission，问题尚未查明；
						 * 另AutomaticWorkManager的onFinished任务中出现了在指令队列中添加任务的语句
						 * ，可能影响执行顺序
						 */
						new Thread() {
							@Override
							public void run() {
								bcd2.askAutoCalibration();
							}
						}.start();
					}
				};
				agp.turnChannels(true);
				/**
				 * 这里以线程的方式执行下一步，否则会出现莫名死锁锁住Flow的mission，问题尚未查明；
				 * 另AutomaticWorkManager的onFinished任务中出现了在指令队列中添加任务的语句 ，可能影响执行顺序
				 */
				new Thread() {
					@Override
					public void run() {
						gcd.askCircleCalibration(agp);
					}
				}.start();
			}
		};
		agp.turnChannels(false);
		bcd1.askAutoCalibration();
	}

	@Override
	protected void createHideContent(final Window wnd, final JPanel jp,
			final ControlManager cm) {
		final PrincipleTiny pt = (PrincipleTiny) cm.getPrinciple();
		final JButton smallBtn = new JButton("25M");
		smallBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PropertyChangeSupport pcs = cm.pcs;
				new Tune2(wnd, pcs, pt.getTuneFunction());
			}
		});

		jp.add(smallBtn);

		/** The following are the controls corresponding to the closed loop correction. */

		final JButton cicleBtn = new JButton(I18nProvider.bundle().getString("M.Display.circle"));
		cicleBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				circleCal(wnd, pt, dfagp);
			}

		});

		jp.add(cicleBtn);

		final CComboBox vmpComboBox = new CComboBox(VmpList);

		jp.add(vmpComboBox);

		List<String> list = SerialPortUtil.loadAvailablePort();

		final CComboBox comComboBox = new CComboBox(list.toArray());
		jp.add(comComboBox);

		vmpComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int vbidx = vmpComboBox.getSelectedIndex();
				dfagp.genWFwithVB(vbidx);

				ON_WF_Iterator owi = Platform.getDataHouse()
						.getWaveFormManager().on_wf_Iterator();
				while (owi.hasNext()) {
					WaveForm wf = owi.next();
					wf.setVoltBaseIndex(vbidx, false);
				}
			}
		});

		final JButton comJButton = new JButton(I18nProvider.bundle().getString("M.Display.select"));
		comJButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				/** 由于闭环的子界面仅在绑定串口后才可工作，所以agp可在绑定时初始化 */
				if (dfagp == null) {
					dfagp = new DFAGP(cm.getCoreControl().getVoltageProvider());
					btBool = false;
				}

				btBool = !btBool;
				if (btBool) {
					String sPort = comComboBox.getSelectedItem().toString();
					dfagp.init(sPort);
					comJButton.setText(I18nProvider.bundle().getString("M.Display.release"));
				} else {
					dfagp.finish();
					comJButton.setText(I18nProvider.bundle().getString("M.Display.select"));
				}
				vmpComboBox.setVisible(btBool);
				cicleBtn.setVisible(btBool);
			}

		});
		jp.add(comJButton);

		{
			boolean b = false;
			vmpComboBox.setVisible(b);
			cicleBtn.setVisible(b);
		}

		wnd.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				beforeLeave();
			}
		});
		// int circleSerialPort = 0;
		// if (list.size() <= 0 || list.size() < circleSerialPort + 1) {
		// DBG.config("no this comm:" + circleSerialPort);
		// return;
		// }

	}
}