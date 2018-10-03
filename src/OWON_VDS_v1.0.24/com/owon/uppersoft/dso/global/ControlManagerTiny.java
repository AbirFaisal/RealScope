package com.owon.uppersoft.dso.global;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import com.owon.uppersoft.dso.control.IDataImporter;
import com.owon.uppersoft.dso.function.RecordControl;
import com.owon.uppersoft.dso.function.ref.IReferenceWaveForm;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.DataImporterTiny;
import com.owon.uppersoft.dso.model.RefWFCreator;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.model.trigger.TrgTypeDefine;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.page.function.PageManager;
import com.owon.uppersoft.dso.ref.IRefSource;
import com.owon.uppersoft.dso.ref.RefWaveForm;
import com.owon.uppersoft.dso.source.comm.detect.PromptPlace;
import com.owon.uppersoft.dso.source.manager.SourceManager;
import com.owon.uppersoft.dso.source.usb.USBPortsFilter;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.core.comm.IRuntime;
import com.owon.uppersoft.vds.core.pref.Config;
import com.owon.uppersoft.vds.source.comm.PortFilterTiny;
import com.owon.uppersoft.vds.source.comm.SourceManagerTiny;
import com.owon.uppersoft.vds.util.Pref;
import com.owon.vds.tiny.firm.FPGADownloader;

public class ControlManagerTiny extends ControlManager {

	public ControlManagerTiny(Config conf, Principle principle, CoreControl cc) {
		super(conf, principle, cc);
	}

	@Override
	public IReferenceWaveForm loadRefWF(File file) {
		return RefWFCreator.loadRefWF(file, this);
	}

	@Override
	protected SourceManager getSourceManager(IRuntime ir) {
		USBPortsFilter pf = createPortFilter();
		SourceManagerTiny smt = new SourceManagerTiny(ir, pf);
		smt.setup();
		return smt;
	}

	@Deprecated
	private USBPortsFilter createPortFilter() {
		return new PortFilterTiny() {
			@Override
			protected String getMachineNameFromCode(int machineCode) {
				switch (machineCode) {
				case 1:
					return "VDS1022";
				case 3:
					return "VDS2052";
				default:
					return null;
				}
			}

		};
	}

	@Override
	public IDataImporter geBinaryFileImporter() {
		return new DataImporterTiny();
	}

	@Override
	public void persist(Pref p) {
		super.persist(p);
		Platform.getControlApps().onPersist(p);
	}

	@Override
	public RefWaveForm createReferenceWaveForm(IRefSource selchl,
			WaveFormManager wfm) {
		return RefWFCreator.createRefWF(this, selchl, wfm);
	}

	@Override
	protected RecordControl createRecordControl(ControlManager cm, Pref p) {
		return new RecordControlTiny(cm, p);
	}

	private DockControl dc;

	@Override
	public DockControl getDockControl() {
		// Use this method to create a subclass object in the parent class constructor
		if (dc == null)
			dc = new DefaultDockControl() {
				@Override
				protected PageManager createPageManager() {
					return new PageManagerTiny();
				}
			};
		return dc;
	}

	@Override
	public boolean singleVideoAlow(TrgTypeDefine etd, int chl,
			TriggerControl trgc) {
		if (chl != 0 && etd == TrgTypeDefine.Video) {
			String message = I18nProvider.bundle().getString(
					"Info.VideoOnlyForCH1"), title = "";
			JFrame jf = Platform.getMainWindow().getFrame();
			JOptionPane.showMessageDialog(jf, message, title,
					JOptionPane.INFORMATION_MESSAGE);
			// cbb.setSelectedItem(TrgTypeDefine.Edge);
			PropertyChangeSupport pcs = trgc.getDelegate()
					.getPropertyChangeSupport();
			pcs.firePropertyChange(PropertiesItem.CHOOSE_TRGMODECB, 0,
					TrgTypeDefine.Edge);
			return false;
		}
		return true;
	}

	@Override
	public PromptPlace createPromptPlace() {
		return new PromptPlace(this) {
			protected JProgressBar jpb;

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				super.propertyChange(evt);
				String pn = evt.getPropertyName();
				if (pn.equals(FPGADownloader.FPD_START_SEND)) {
					jpb = new JProgressBar();
					jpb.setMaximum((Integer) evt.getNewValue());
					jpb.setBounds(0, 125, 350, 22);
					add(jpb);
				} else if (pn.equals(FPGADownloader.FPD_SEND_LENGTH)) {
					if (jpb != null)
						jpb.setValue(jpb.getValue()
								+ (Integer) evt.getNewValue());
				} else if (pn.equals(FPGADownloader.FPD_DONE)) {
					if (jpb != null)
						remove(jpb);
					jpb = null;
				} else if (pn.equals(FPGADownloader.FPD_NOFILE)) {
					infoarea.setText("No FPGA File.");
				} else if (pn.equals(ProgressStart)) {
					jpb = new JProgressBar();
					jpb.setMaximum((Integer) evt.getNewValue());
					jpb.setBounds(0, 125, 350, 22);
					add(jpb);
				} else if (pn.equals(ProgressIncrease)) {
					if (jpb != null)
						jpb.setValue(jpb.getValue()
								+ (Integer) evt.getNewValue());
				} else if (pn.equals(ProgressDone)) {
					if (jpb != null)
						remove(jpb);
					jpb = null;
				}
			}
		};
	}
}