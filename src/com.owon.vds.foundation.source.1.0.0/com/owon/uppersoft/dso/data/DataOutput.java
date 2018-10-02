package com.owon.uppersoft.dso.data;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.view.ChartScreen;
import com.owon.uppersoft.dso.wf.ON_WF_Iterator;
import com.owon.uppersoft.vds.core.aspect.help.IExportableWF;
import com.owon.uppersoft.vds.ui.dialog.ProgressMonitorDialog;
import com.owon.uppersoft.vds.util.FileUtil;
import com.owon.uppersoft.vds.util.MyFileFilter;
import com.owon.uppersoft.vds.util.StringPool;
import com.owon.uppersoft.vds.util.format.SFormatter;
import com.owon.uppersoft.vds.util.format.SimpleStringFormatter;

public class DataOutput {
	//
	// int pos = 19;// Zone length position 15 plus int length 4
	public static final int everyPageCount = 50000, sheetLimitperBook = 1;
	private final String freqlbl = "AutoMeasure.FREQuency";
	public String unitTxt;

	public DataOutput() {
	}

	public final void exportWaveAsPicture(Component parent, ChartScreen cs) {
		ControlManager cm = Platform.getDataHouse().controlManager;
		String saveimgPath = cm.saveimgPath;

		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File(saveimgPath));
		jfc.addChoosableFileFilter(MyFileFilter.BMPFilter);
		jfc.addChoosableFileFilter(MyFileFilter.GIFFilter);
		jfc.addChoosableFileFilter(MyFileFilter.PNGFilter);

		int reval = jfc.showSaveDialog(parent);
		if (reval == JFileChooser.APPROVE_OPTION) {
			BufferedImage bi = cs.getChartScreenBufferedImage();

			MyFileFilter selFilter = (MyFileFilter) jfc.getFileFilter();
			String format = selFilter.getEnds();

			File f = FileUtil.checkFileSuffix(jfc, format);
			try {
				ImageIO.write(bi, format, f);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			cm.saveimgPath = FileUtil.getFileCanonicalPath(jfc
					.getCurrentDirectory());
		}
		// if (pcs != null)
		// pcs.firePropertyChange(ExportWaveControl.AFTER_EXPORT, null, true);
	}

	public final void exportWaveAsBin(File out, ProgressMonitorDialog pm,
	                                  WaveFormManager wfm, File tempsave) {
		pm.toFront();
		pm.setValue_Note(0);
		File copy = tempsave;

		try {
			FileUtil.copyFile(copy, out);
			pm.addProgressValue_Note(100);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		pm.close();
		// if (pcs != null)
		// pcs.firePropertyChange(ExportWaveControl.AFTER_EXPORT, null, true);
	}

	public final void exportXLS(File out, ProgressMonitorDialog pm,
	                            WaveFormManager wfm) {
		unitTxt = I18nProvider.bundle().getString("Label.ExportUnit");
		// final boolean sequence = dtd.isSequenceSelected();
		int allSheetPage, lastSheetPage, allRowCount = 0;
		int everySheetCount = everyPageCount;

		DataHouse dh = Platform.getDataHouse();
		WFO wfo = new WFO(dh.getDeepMemoryStorage(), dh.controlManager);

		/* Use the maximum of all column counts as the number of columns */
		ON_WF_Iterator owi = wfm.on_wf_Iterator();
		while (owi.hasNext()) {
			IExportableWF wf = wfo.setWF(owi.next());
			allRowCount = Math.max(wf.getDatalen(), allRowCount);
		}
		lastSheetPage = allRowCount % everySheetCount;
		allSheetPage = allRowCount / everySheetCount;
		if (lastSheetPage != 0)
			allSheetPage++;
		pm.toFront();
		pm.setValue_Note(0);
		pm.setMaximum(allSheetPage);

		int lastBookSheets = allSheetPage % sheetLimitperBook;
		int allWorkbooks = allSheetPage / sheetLimitperBook;
		if (lastBookSheets != 0)
			allWorkbooks++;
		try {

			for (int currentBook = 0; currentBook < allWorkbooks; currentBook++) {
				File tmp = new File(out, (currentBook + 1) + ".xls");
				WritableWorkbook wwb = Workbook.createWorkbook(tmp);
				// println("filename:" + tmp.getName());

				WritableSheet ws = null;
				jxl.write.Label label;
				jxl.write.Number labelN;

				int sheets = allSheetPage > sheetLimitperBook ? sheetLimitperBook
						: allSheetPage;

				int offset = currentBook * sheetLimitperBook;

				for (int currentSheetPage = sheets; currentSheetPage > 0; currentSheetPage--) {
					int csp = currentSheetPage;

					ws = wwb.createSheet("Sheet" + csp, 0);
					// System.out.println("sheet:" + csp);
					/* Add unit ID line */
					label = new jxl.write.Label(0, 0, unitTxt);
					ws.addCell(label);

					int length = 0;
					length = everySheetCount;
					if (csp == allSheetPage && lastSheetPage != 0)
						length = lastSheetPage;

					int baseRow = 1;
					int k = 0, alen = 1, j = 0;
					owi = wfm.on_wf_Iterator();
					while (owi.hasNext()) {
						IExportableWF wf = wfo.setWF(owi.next());
						String freq = wf.getFreqLabel();
						if (k == 0) {
							label = new jxl.write.Label(k, j + baseRow,
									I18nProvider.bundle().getString(freqlbl));
							ws.addCell(label);
							k++;
						}
						label = new jxl.write.Label(k, j + baseRow, freq);
						ws.addCell(label);
						k++;
					}

					int dataRow = baseRow + alen;

					/* Add sequence number column */
					// if (sequence) {
					csp += offset;
					int index = ((csp - 1) * everySheetCount) + 1;
					int rowlength = dataRow + length;
					for (int row = dataRow; row < rowlength; row++, index++) {
						labelN = new jxl.write.Number(0, row, index);
						ws.addCell(labelN);
					}
					// }

					/* Add each channel column */
					int channelsOrder = 1;
					owi = wfm.on_wf_Iterator();
					while (owi.hasNext()) {
						IExportableWF wf = wfo.setWF(owi.next());

						if (pm.isCanceled()) {
							wwb.close();
							return;
						}
						/* Whether it exists and is selected */
						if (wf == null) {
							continue;
						}
						String name = wf.getChannelLabel();

						/* Waveform name (column name) */
						label = new jxl.write.Label(channelsOrder, 0, name);
						ws.addCell(label);

						csp = currentSheetPage;
						/* Traverse each line */
						csp += offset;
						int idx = (csp - 1) * everySheetCount;
						int end = dataRow + length;
						for (int row = dataRow; row < end; row++, idx++) {
							/* 对于double，jxl直接转换为int */
							double val = wf.voltAt(idx);
							labelN = new jxl.write.Number(channelsOrder, row,
									val);
							ws.addCell(labelN);
						}
						channelsOrder++;
					}
					pm.addProgressValue_Note(1);
				}
				wwb.write();
				wwb.close();
				System.gc();
			}
			pm.close();
			// if (pcs != null)
			// pcs.firePropertyChange(ExportWaveControl.AFTER_EXPORT,null,true);
		} catch (RowsExceededException ex) {
			// Logger.getLogger(DataTableDialog.class.getName()).log(
			// Level.SEVERE, null, ex);
		} catch (WriteException ex) {
			// Logger.getLogger(DataTableDialog.class.getName()).log(
			// Level.SEVERE, null, ex);
		} catch (IOException ex) {
			// Logger.getLogger(DataTableDialog.class.getName()).log(
			// Level.SEVERE, null, ex);
		}
	}

	public final void exportCSV(File out, ProgressMonitorDialog pm,
	                            WaveFormManager wfm) {
		unitTxt = I18nProvider.bundle().getString("Label.ExportUnit");

		DataHouse dh = Platform.getDataHouse();
		WFO wfo = new WFO(dh.getDeepMemoryStorage(), dh.controlManager);

		/* Use the maximum of all column counts as the number of columns */
		int rowCount = 0;
		ON_WF_Iterator owi = wfm.on_wf_Iterator();
		while (owi.hasNext()) {
			IExportableWF wf = wfo.setWF(owi.next());
			rowCount = Math.max(wf.getDatalen(), rowCount);
		}
		pm.setVisible(true);
		pm.toFront();
		pm.setValue_Note(0);

		final String newline = "\r\n";
		final String comma = ",";

		try {
			FileWriter fw = new FileWriter(out);

			/* Unit identification */
			fw.append(unitTxt + newline);

			// if (sequence)
			fw.append("");

			/* Waveform name (column name) */
			owi = wfm.on_wf_Iterator();
			while (owi.hasNext()) {
				IExportableWF wf = wfo.setWF(owi.next());
				fw.append(comma + wf.getChannelLabel());
			}
			fw.append(newline);

			int k = 0;
			owi = wfm.on_wf_Iterator();
			while (owi.hasNext()) {
				IExportableWF wf = wfo.setWF(owi.next());
				String freq = wf.getFreqLabel();
				if (k == 0) {
					fw.append(I18nProvider.bundle().getString(freqlbl));
					k++;
				}
				fw.append(comma);
				fw.append(freq);
				k++;
			}
			fw.append(newline);

			int util = rowCount / 100;
			// setProgressMaximum(100);
			String ss;
			/* 每一行 */
			for (int i = 0; i < rowCount; i++) {
				if (pm.isCanceled()) {
					fw.close();
					return;
				}
				/* 序号标识 */
				// if (sequence)
				fw.append(String.valueOf(i + 1));

				/* 每一列 */
				owi = wfm.on_wf_Iterator();
				while (owi.hasNext()) {
					IExportableWF wf = wfo.setWF(owi.next());
					if (wf == null || wf.getDatalen() - 1 < i) {
						fw.append(comma);
						continue;
					}
					/* 须确保每个WaveForm的数据都有rowCount个 */
					ss = SFormatter.UIformat("%.2f", (float) wf.voltAt(i));
					fw.append(comma + ss);
				}
				fw.append(newline);

				if (i % util == 0) {
					pm.addProgressValue_Note(1);
				}
			}
			fw.flush();
			fw.close();
			pm.close();
			// if (pcs != null)
			// pcs.firePropertyChange(ExportWaveControl.AFTER_EXPORT,null,true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public final void exportText(File out, ProgressMonitorDialog pm,
	                             WaveFormManager wfm, int dmidx) {
		unitTxt = I18nProvider.bundle().getString("Label.ExportUnit");
		// final boolean sequence = dtd.isSequenceSelected();
		pm.setVisible(true);
		pm.toFront();
		pm.setValue_Note(0);

		DataHouse dh = Platform.getDataHouse();
		WFO wfo = new WFO(dh.getDeepMemoryStorage(), dh.controlManager);

		/* 使用所有列数中的最大值作为列数 */
		int rowCount = 0;
		ON_WF_Iterator owi = wfm.on_wf_Iterator();
		while (owi.hasNext()) {
			IExportableWF wf = wfo.setWF(owi.next());
			rowCount = Math.max(wf.getDatalen(), rowCount);
			println("rowCount:" + rowCount + ",wf.dataLen:" + wf.getDatalen());
		}

		SimpleStringFormatter ssf = new SimpleStringFormatter(dmidx);
		try {
			FileWriter fw = new FileWriter(out);

			String ln = StringPool.LINE_SEPARATOR;// length 2
			/* 单位标识 */
			fw.append(unitTxt + ln);

			// if (sequence)
			fw.append(ssf.emptyblock);// length 12
			/* 波形名(列名) */
			owi = wfm.on_wf_Iterator();
			while (owi.hasNext()) {
				IExportableWF wf = wfo.setWF(owi.next());
				fw.append(ssf.valueToStringOnRight(wf.getChannelLabel()));
			}
			fw.append(ln);

			int k = 0;
			owi = wfm.on_wf_Iterator();
			while (owi.hasNext()) {
				IExportableWF wf = wfo.setWF(owi.next());
				String freq = wf.getFreqLabel();
				if (k == 0) {
					fw.append(ssf.valueToStringOnLeft(I18nProvider.bundle()
							.getString(freqlbl)));
					k++;
				}
				fw.append(ssf.valueToStringOnRight(freq));
				k++;
			}
			fw.append(ln);

			int util = rowCount / 100;
			// setProgressMaximum(100);
			String ss;
			/* 每一行 */
			for (int i = 0; i < rowCount; i++) {
				if (pm.isCanceled()) {
					fw.close();
					return;
				}
				/* 序号标识 */
				// if (sequence)
				fw.append(ssf.valueToStringOnLeft(i + 1));

				/* 每一列 */
				owi = wfm.on_wf_Iterator();
				while (owi.hasNext()) {
					IExportableWF wf = wfo.setWF(owi.next());
					if (wf == null || wf.getDatalen() - 1 < i) {
						println(" wf.dataLen:" + wf.getDatalen() + ",i:" + i);
						fw.append(ssf.emptyblock);
						continue;
					}
					/* 须确保每个WaveForm的数据都有rowCount个 */
					ss = SFormatter.UIformat("%.2f", (float) wf.voltAt(i));
					fw.append(ssf.valueToStringOnRight(ss));
				}
				fw.append(ln);
				if (i % util == 0) {
					pm.addProgressValue_Note(1);
				}
			}
			fw.flush();
			fw.close();
			pm.close();
			// if (pcs != null)
			// pcs.firePropertyChange(ExportWaveControl.AFTER_EXPORT,null,true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void println(String txt) {
		// System.err.println(txt);
	}
}
