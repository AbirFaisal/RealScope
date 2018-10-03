package com.owon.vds.tiny.ui.tune;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.vds.data.LocaleObject;
import com.owon.uppersoft.vds.ui.layout.OneColumnLayout;
import com.owon.uppersoft.vds.ui.prompt.FadeIOShell;
import com.owon.vds.tiny.firm.pref.model.Register;
import com.owon.vds.tiny.firm.pref.model.TuneTexter;
import com.owon.vds.tiny.tune.TinyTuneDelegate;
import com.owon.vds.tiny.tune.TinyTuneFunction;
import com.owon.vds.tiny.ui.tune.widget.DirScanComboModel;

public class TinyTuneDialog {

	private JDialog frame;

	private TinyTuneFunction tf;
	private Register reg;

	public TinyTuneDialog(TinyTuneFunction tf, Window wnd) {
		this.tf = tf;
		reg = tf.getTuneModel().getRegister();

		dir = TuneTexter.FLASH_TXT;
		if (pref.exists() && pref.isFile()) {
			Properties p = new Properties();
			try {
				p.load(new FileInputStream(pref));

				dir = p.getProperty("dir");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		initialize(wnd);
	}

	public ArgTypePane getCurrentArgTypePane() {
		Component c = jtp.getSelectedComponent();
		if (c == null || !(c instanceof ArgTypePane))
			return null;
		return (ArgTypePane) c;
	}

	public void contentUpdateWithoutSync() {
		Component[] cs = jtp.getComponents();
		for (Component c : cs) {
			if (c == null || !(c instanceof ArgTypePane))
				continue;
			ArgTypePane atp = (ArgTypePane) c;
			atp.contentUpdate();
		}
	}

	private String dir = "";
	private File pref = new File("tune.conf");

	private void initialize(final Window wnd) {
		frame = new JDialog(wnd);

		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		final JPanel base = new JPanel();
		base.setLayout(new OneColumnLayout());
		frame.setContentPane(base);

		final JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(0, 180));
		panel.setLayout(new BorderLayout());
		base.add(panel);

		jtp = tf.createTabs(panel);

		createBottom(base);

		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				tf.release();

				Properties p = new Properties();
				p.setProperty("dir", dir);
				try {
					p.store(new FileOutputStream(pref), null);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	protected void createBottom(Container base) {
		final ResourceBundle rb = I18nProvider.bundle();

		final JPanel bottom = new JPanel();
		bottom.setLayout(new OneColumnLayout());
		// buttonPane.setPreferredSize(new Dimension(0, 80));
		base.add(bottom, BorderLayout.SOUTH);
		bottom.add(new JSeparator());

		JPanel row1 = new JPanel();
		createDevicePart(row1);
		row1.setLayout(new OneColumnLayout());
		// row1.setPreferredSize(new Dimension(0, 100));
		bottom.add(row1);

		bottom.add(new JSeparator());
		JPanel row2 = new JPanel();
		// row.setPreferredSize(new Dimension(650, 40));

		final TinyTuneDelegate ttd = tf.getTinyTuneDelegate();

		File dir = new File(TuneTexter.FLASH_TXT);
		dir.mkdirs();
		// if (dir.list().length <= 0) {
		// File bak = new File(dir, "t.txt");
		// try {
		// bak.createNewFile();
		// } catch (IOException e1) {
		// // e1.printStackTrace();
		// }
		// }

		JPanel prefPane = new JPanel();
		prefPane.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
		prefPane.setPreferredSize(new Dimension(250, 30));

		final DirScanComboModel dcbm = new DirScanComboModel(dir);
		final JComboBox jcb = new JComboBox(dcbm);
		jcb.setSelectedItem(TuneTexter.MACHINE_TXT);
		jcb.setPreferredSize(new Dimension(120, 30));
		prefPane.add(jcb);
		jcb.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				dcbm.refreshDir();
			}

		});

		/** ROW 2 */
		createBtn("<<" + rb.getString("Internal.loadtxt"),
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (!tf.createTuneTexter().resetup(
								jcb.getSelectedItem().toString())) {
							FadeIOShell fs = new FadeIOShell();
							fs.prompt("Cannot load txt", frame);
							return;
						}
						contentUpdateWithoutSync();
						loadLocale();
						ArgTypePane atp = getCurrentArgTypePane();
						if (atp != null)
							atp.sync2Device();
					}
				}, prefPane, 110);

		row2.add(prefPane);

		createBtn(rb.getString("Internal.Calibration"), new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tf.doselfcorrect();
				contentUpdateWithoutSync();
			}
		}, row2, 100);

		if (false)
			createBtn(rb.getString("Internal.WrDevice"), new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ttd.writeDeviceNSync();
				}
			}, row2, 120);

		createBtn(rb.getString("Internal.SaveTxt"), new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveLocaleList();
				fileChooseForSave();
			}
		}, row2, 100);

		createBtn(rb.getString("Internal.WrDeviceSyncFactory"),
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						saveLocaleList();
						ttd.writeFactoryNSync();
					}
				}, row2, 180);

		if (false)
			createBtn(rb.getString("Internal.WrFactory2Device"),
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							ttd.resumeFactoryNSync();
						}
					}, row2, 180);

		bottom.add(row2);
	}

	protected void fileChooseForSave() {
		final JFileChooser jf = new JFileChooser();
		jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
		File cdir = new File(TuneTexter.FLASH_TXT);
		String sn = reg.serialNumber;
		String fn = sn;
		if (sn == null || sn.length() < 3)
			fn = "xxx";
		else
			fn = sn.substring(sn.length() - 3);

		File f = new File(cdir, fn + ".txt");
		jf.setCurrentDirectory(cdir);
		jf.setSelectedFile(f);
		int returnVal = jf.showSaveDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File sf = jf.getSelectedFile();
			dir = sf.getParent();
			tf.createTuneTexter().save(sf);
		}
	}

	protected JButton createBtn(String txt, ActionListener al, Container row,
			int w) {
		final JButton btn = new JButton();
		btn.setPreferredSize(new Dimension(w, 30));
		row.add(btn);
		btn.addActionListener(al);
		btn.setText(txt);
		return btn;
	}

	protected void createDevicePart(Container row) {
		final ResourceBundle bundle = I18nProvider.bundle();

		JPanel mtbar = new JPanel();
		/** ROW 1 */
		JLabel lblver = new JLabel(bundle.getString("About.Version"));
		tfver = new JTextField();
		tfver.setColumns(12);
		tfver.setText(reg.version);
		JLabel lblsn = new JLabel(bundle.getString("Internal.Series"));
		tfsn = new JTextField();
		tfsn.setColumns(12);
		tfsn.setText(reg.serialNumber);
		// JButton apply = new
		// JButton(bundle.getString("Internal.ApplyToMachine"));//
		// "ApplyToMachine"
		// apply.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent arg0) {
		// tf.applyMachineInfo(vstf.getText(), srtf.getText());
		// }}); row.add(apply);
		mtbar.add(lblver);
		mtbar.add(tfver);
		mtbar.add(lblsn);
		mtbar.add(tfsn);

		oemcb = new JCheckBox("OEM");
		oemcb.setSelected(reg.oem);
		mtbar.add(oemcb);

		row.add(mtbar);

		JPanel langbar = new JPanel();
		langbar.setPreferredSize(new Dimension(0, 60));
		langbar.setLayout(new FlowLayout(FlowLayout.LEFT));

		List<Boolean> los = reg.localeSelection;
		List<LocaleObject> langs = Register.localeLists;
		final int len = langs.size();
		langcbs = new JCheckBox[len];
		for (int i = 0; i < len; i++) {
			JCheckBox cb = new JCheckBox(langs.get(i).getDisplayNameForChs());
			cb.setSelected(los.get(i));
			langbar.add(cb);
			langcbs[i] = cb;
		}

		row.add(langbar);

		final JCheckBox all = new JCheckBox("all");
		all.setPreferredSize(new Dimension(0, 20));
		all.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < len; i++) {
					langcbs[i].setSelected(all.isSelected());
				}
			}
		});
		row.add(all);
	}

	private void loadLocale() {
		oemcb.setSelected(reg.oem);
		tfver.setText(reg.version);
		tfsn.setText(reg.serialNumber);

		List<Boolean> los = reg.localeSelection;
		int len = langcbs.length;
		for (int i = 0; i < len; i++) {
			langcbs[i].setSelected(los.get(i));
		}
	}

	private JCheckBox[] langcbs;

	private JTextField tfver;

	private JTextField tfsn;

	private JCheckBox oemcb;

	private JTabbedPane jtp;

	public void saveLocaleList() {
		reg.oem = oemcb.isSelected();
		reg.version = tfver.getText();
		reg.serialNumber = tfsn.getText();

		List<Boolean> los = reg.localeSelection;
		int len = langcbs.length;
		for (int i = 0; i < len; i++) {
			los.set(i, Boolean.valueOf(langcbs[i].isSelected()));
		}
	}

	public void toFront() {
		frame.toFront();
	}

}
