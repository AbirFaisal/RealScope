package com.owon.vds.tiny.tune;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.vds.core.tune.IntVolt;
import com.owon.uppersoft.vds.util.Pref;
import com.owon.vds.tiny.firm.pref.DefaultPrefControl;
import com.owon.vds.tiny.firm.pref.DevicePref;
import com.owon.vds.tiny.firm.pref.model.TuneModel;
import com.owon.vds.tiny.firm.pref.model.TuneTexter;
import com.owon.vds.tiny.tune.detail.AbsCalArgType;
import com.owon.vds.tiny.tune.detail.DefaultCalArgType;
import com.owon.vds.tiny.ui.tune.ArgTypePane;
import com.owon.vds.tiny.ui.tune.TinyTuneDialog;
import com.owon.vds.tiny.ui.tune.control.DefaultTinyTuneDelegate;

/**
 * Selecting different tabs or dropdowns loads saved values, but only sends and saves them when the values have been successfully modified.
 * 
 * Currently not in use, as a text-based update is provided temporarily until the production debugging interface layout is finalized.
 */

public class TinyTuneFunction implements ITuneFunction {

	private int tid = 0;

	public IntVolt[] getIntVolts() {
		return ios;
	}

	private String[] channels;

	public String[] getChannelNames() {
		return channels;
	}

	public IntVolt[] ios;
	public int vlen;

	private TinyTuneDelegate ttd;

	public TinyTuneDelegate getTinyTuneDelegate() {
		return ttd;
	}

	private TuneModel tm;

	public TuneModel getTuneModel() {
		return tm;
	}

	public DefaultPrefControl getDefaultPrefControl() {
		return dpc;
	}

	private DefaultPrefControl dpc;
	public AbsCalArgType cmdt;

	public TinyTuneFunction(int channelsNumber, int[] vbs) {

		/** Internal initialization of some parameters */

		channels = new String[channelsNumber];
		for (int i = 0; i < channelsNumber; i++) {
			channels[i] = "CH" + (i + 1);
		}

		vlen = vbs.length;
		ios = new IntVolt[vlen];
		int i = 0;
		while (i < vlen) {
			ios[i] = new IntVolt(vbs[i]);
			i++;
		}
		tm = createTuneModel(channelsNumber, vlen);
		dpc = new DefaultPrefControl(tm);
		ttd = new DefaultTinyTuneDelegate(dpc);

		tid = 0;
		if (tid >= tm.cmdts.size())
			tid = 0;
		cmdt = tm.cmdts.get(tid);

		tm.setPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String pn = evt.getPropertyName();
				// if (pn.equals(PropertiesItem.SampleRateChange)) {
				// SampleRate sr = (SampleRate) evt.getNewValue();
				//
				// for (AbstractAdjustCMDType cmdt : cmdts)
				// cmdt.onSampleRateChange(sr);
				// } else
				if (pn.equals(PropertiesItem.TUNE_VBBCHANGE)) {
					if (tunewnd == null)
						return;
					ChannelInfo ci = (ChannelInfo) evt.getNewValue();
					int channel = ci.getNumber();
					int idx = ci.getVoltbaseIndex();
					ArgTypePane atp = tunewnd.getCurrentArgTypePane();
					if (atp != null)
						atp.onVoltbaseChange(channel, idx);
				} else if (pn.equals(DevicePref.ARGS_UPDATE)) {
					if (tunewnd != null)
						tunewnd.contentUpdateWithoutSync();
					backupFlashMemory();
				}
			}
		});

		createTuneTexter().resetup();
	}

	protected TuneModel createTuneModel(int channelsNumber, int vbNum) {
		return new TuneModel(channelsNumber, vbNum);
	}

	public TuneTexter createTuneTexter() {
		return new TuneTexter(tm, channels, ios);
	}

	public void persist(Pref p) {
		p.persistInt("Tune.tid", tid);
	}

	public static String[] load(ResourceBundle b) {
		String[] CMDType = { b.getString("Internal.CMDType.CoarseGain"),
				b.getString("Internal.CMDType.ZeroAmplitude"),
				b.getString("Internal.CMDType.ZeroCompensation"),
		// b.getString("Internal.CMDType.FullScale"),
		// b.getString("Internal.CMDType.ADCPhase")
		};
		return CMDType;
	}

	public JTabbedPane createTabs(Container panel) {

		ResourceBundle bundle = I18nProvider
				.getLocaleBundle(Locale.SIMPLIFIED_CHINESE);

		String[] types = load(bundle);

		final JTabbedPane jtp = new JTabbedPane();
		int pi = 0;
		for (String n : types) {
			DefaultCalArgType cmdt = tm.cmdts.get(pi);
			ArgTypePane ctn = new ArgTypePane(cmdt, ttd);
			// j.setLayout(new OneRowLayout());
			ctn.setPreferredSize(new Dimension(400, 300));
			ctn.createContent(ios);
			ctn.contentUpdate();

			jtp.addTab(n, ctn);
			pi++;
		}
		jtp.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int i = jtp.getSelectedIndex();
				cmdt = tm.cmdts.get(i);
				tid = i;
				// controlManager.pcs.firePropertyChange(PropertiesItem.SampleRateChange,
				// null, controlManager.getCurrentSampleRate());
			}
		});
		panel.add(jtp, BorderLayout.CENTER);
		jtp.setSelectedIndex(tid);
		return jtp;
	}

	public void doselfcorrect() {
		ttd.selfCalibration();
	}

	private TinyTuneDialog tunewnd;

	public void open(Window wnd) {
		if (tunewnd == null) {
			tunewnd = new TinyTuneDialog(this, wnd);

		} else
			tunewnd.toFront();
	}

	public void release() {
		/** The "machine.txt" file should not be overwritten by the current settings */
		// output(new File(txtrpath));
		tunewnd = null;
	}

	public void backupFlashMemory() {
		createTuneTexter().output(
				new File(TuneTexter.FLASH_TXT, TuneTexter.FLASHMEMORY_TXT));
	}

	// @Override
	// public int getVBIndexForChannel(int chl) {
	// return
	// Platform.getDataHouse().getWaveFormManager().getWaveForm(chl).wfi.ci
	// .getVoltbaseIndex();
	// }

	private void logln(String string) {
		System.out.println(string);
	}

}