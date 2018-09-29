package com.owon.uppersoft.dso.function;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;

import com.owon.uppersoft.dso.data.DataOutput;
import com.owon.uppersoft.dso.global.ControlApps;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.source.comm.AbsInterCommunicator;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;
import com.owon.uppersoft.vds.ui.dialog.ProgressMonitorDialog;
import com.owon.uppersoft.vds.ui.prompt.FadeIOShell;
import com.owon.uppersoft.vds.util.FileUtil;
import com.owon.uppersoft.vds.util.MyFileFilter;

public class ExportWaveControl implements PropertyChangeListener {

	private static final int TYPE_TXT = 0, TYPE_CSV = 1, TYPE_XLS = 2,
			TYPE_BIN = 3;
	public static MyFileFilter[] exportFilters = { MyFileFilter.TxtFilter,
			MyFileFilter.CsvFilter, MyFileFilter.XlsFilter,
			MyFileFilter.BINFilter };
	private boolean authorizeExport = false;

	public boolean isAuthorizeExport() {
		return authorizeExport;
	}

	public void setAuthorizeExport(boolean authorizeExport) {
		this.authorizeExport = authorizeExport;
	}

	public ProgressMonitorDialog exportPMD;
	public Thread exprotThread;
	private int expTypidx;
	public File expOutFile;

	protected ControlManager cm;
	// protected PropertyChangeSupport pcs;
	protected DataOutput dataOutput;

	// public static final String AFTER_EXPORT = "afterExport";

	public ExportWaveControl(ControlManager cm) {
		this.cm = cm;
		// this.pcs = cm.pcs;
		this.dataOutput = new DataOutput();
		cm.pcs.addPropertyChangeListener(this);
	}

	public DataOutput getDataOutput() {
		return dataOutput;
	}

	public void exportDMfile(Component parent) {
		if (Platform.getDataHouse().isPlayRecord()
				&& getExpTypidx() == TYPE_BIN) {
			FadeIOShell fs = new FadeIOShell();
			fs.prompt(
					I18nProvider.bundle().getString(
							"M.Utility.ExportWave.nonsupport"), Platform
							.getMainWindow().getFrame());
			return;
		}

		setAuthorizeExport(true);

		if (exprotThread != null && exprotThread.isAlive()) {
			if (exportPMD != null && exportPMD.isVisible())
				exportPMD.toFront();
			return;
		}
		if (getExpTypidx() == TYPE_XLS) {
			JFileChooser jfc = new JFileChooser();
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			jfc.setCurrentDirectory(new File(cm.exportPath));
			int rsl = jfc.showSaveDialog(parent);
			if (rsl == JFileChooser.APPROVE_OPTION) {

				File fnoSuffix = jfc.getSelectedFile();
				expOutFile = fnoSuffix;// 要导出,先确定文件
				pauseNIntendtoExport();

			} else if (rsl == JFileChooser.CANCEL_OPTION) {
				// pcs.firePropertyChange(ExportWaveControl.AFTER_EXPORT, null,
				// true);
				setAuthorizeExport(false);
			}
			cm.exportPath = FileUtil.getFileCanonicalPath(jfc
					.getCurrentDirectory());
			return;
		}

		JFileChooser jfc = new JFileChooser();
		MyFileFilter filter = exportFilters[getExpTypidx()];
		jfc.setCurrentDirectory(new File(cm.exportPath));
		jfc.addChoosableFileFilter(filter);
		int rsl = jfc.showSaveDialog(parent);
		if (rsl == JFileChooser.APPROVE_OPTION) {
			final File out = FileUtil.checkFileSuffix(jfc, filter.getEnds());

			expOutFile = out;// 要导出,先确定文件
			pauseNIntendtoExport();

		} else if (rsl == JFileChooser.CANCEL_OPTION) {
			// pcs.firePropertyChange(ExportWaveControl.AFTER_EXPORT, null,
			// true);
			setAuthorizeExport(false);
		}
		cm.exportPath = FileUtil
				.getFileCanonicalPath(jfc.getCurrentDirectory());
	}

	private void pauseNIntendtoExport() {
		DataHouse dh = Platform.getDataHouse();
		WaveFormManager wfm = dh.getWaveFormManager();
		ControlApps ca = dh.getControlApps();
		AbsInterCommunicator interComm = Platform.getControlApps().interComm;
		if (dh.isPlayRecord()) {
			println("isPlayRecord()");

			interComm.prepare2PersistDMData();
			DMInfo ci = interComm.createDatasaver().saveFileM(cm,
					AbsInterCommunicator.dmf, null, null);
			if (ci != null) {
				int tmpStatus = dh.getStatus();
				dh.receiveOfflineDMData(ci);
				dh.setStatus(tmpStatus);
				lastExportWave(expOutFile, wfm);
			}
			return;
		}

		/** 离线载入时也可导出 */
		if (dh.isDMLoad()) {
			println("isDMLoad()");
			lastExportWave(expOutFile, wfm);
			return;
		}

		// 只有深存储获取完成才能exportDMfile();
		if (cm.isRuntime()) {
			/** 停止并深存储,导出会在深存储完fire机制执行 */
			interComm.statusStop(true);
		} else {
			if (!ca.isDMDataGotAlready()) {
				interComm.onExport_get();
			} else {
				if (ca.isDMAvailable()) {
					/** 只导出 */
					lastExportWave(expOutFile, wfm);
				}
			}
		}
	}

	public void lastExportWave(final File out, final WaveFormManager wfm) {
		if (!isAuthorizeExport())
			return;
		final File tempsave = AbsInterCommunicator.dmf;
		final int selIdx = getExpTypidx();
		exportPMD = new ProgressMonitorDialog(wfm.getDataHouse()
				.getMainWindow().getFrame(), I18nProvider.bundle().getString(
				"Action.ExportWave"), "", "", 0, 100);

		if (selIdx == TYPE_XLS) {
			exprotThread = new Thread() {
				@Override
				public void run() {
					out.mkdirs();
					dataOutput.exportXLS(out, exportPMD, wfm);
					setAuthorizeExport(false);
				}
			};
			exprotThread.start();
		} else {
			exprotThread = new Thread() {
				@Override
				public void run() {
					switch (selIdx) {
					case TYPE_BIN:
						dataOutput.exportWaveAsBin(out, exportPMD, wfm,
								tempsave);
						break;
					case TYPE_TXT:
						dataOutput.exportText(out, exportPMD, wfm, cm
								.getDeepMemoryControl().getDeepIdx());
						break;
					case TYPE_CSV:
						dataOutput.exportCSV(out, exportPMD, wfm);
						break;
					default:
						break;
					}
					setAuthorizeExport(false);
				}
			};
			exprotThread.start();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String n = evt.getPropertyName();
		/** 用于在获取深存储的线程完成后，紧接着导出波形到硬盘 */
		if (n.equalsIgnoreCase(PropertiesItem.AFTER_GOT_DM_DATA)) {
			WaveFormManager wfm = (WaveFormManager) evt.getNewValue();
			lastExportWave(cm.ewc.expOutFile, wfm);
			configln("lastExportWave");
		}
	}

	public int getExpTypidx() {
		return expTypidx;
	}

	public void setExpTypidx(int expTypidx) {
		this.expTypidx = expTypidx;
	}

	private void configln(String p) {
	}

	private void println(String p) {
	}
}
