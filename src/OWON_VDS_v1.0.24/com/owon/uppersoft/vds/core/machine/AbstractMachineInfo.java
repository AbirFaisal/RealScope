package com.owon.uppersoft.vds.core.machine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import jxl.Workbook;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.owon.uppersoft.vds.core.sample.SampleRate;
import com.owon.uppersoft.vds.util.PrimaryTypeUtil;

public abstract class AbstractMachineInfo extends MachineInfo {

	protected AbstractMachineInfo(InputStream is) {
		super(is);
	}

	public void output_FullScreenNumbers(File f) {
		try {
			WritableWorkbook wwb = Workbook.createWorkbook(f);// Create a Sheet using the createSheet method of the WritableWorkbook object, path name, etc.

			WritableFont wfc = new WritableFont(WritableFont.ARIAL, 10,
					WritableFont.BOLD, true, UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.RED);
			WritableCellFormat wcf_red = new WritableCellFormat(wfc);

			WritableFont wfc2 = new WritableFont(WritableFont.ARIAL, 10,
					WritableFont.BOLD, true, UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.BLUE);
			WritableCellFormat wcf_blue = new WritableCellFormat(wfc2);

			WritableSheet ws = null;
			Label label;
			Number labelN;

			ws = wwb.createSheet("Sheet", 0);// create a sheet
			int tblen = TIMEBASE.length;
			int cfgCount = sampling_configuration_count;
			int deep = DEEP.length;
			for (int m = 0; m < tblen; m++) {
				label = new Label(0, m, TIMEBASE[m]);
				ws.addCell(label);

				for (int k = 0; k < cfgCount; k++) {
					for (int n = 0; n < deep; n++) {
						int idx = k * deep + n;
						int v = FullScreenData[idx][m];

						SampleRate sr = SampleRates[idx][0];
						SampleRate sr2 = SampleRates[idx][m];
						label = new Label(idx + 1, m, v
								+ sr2.getSampleRateTxt());
						// labelN = new Number(n + 1, m, DEEPValue[(n % 5)] /
						// v);

						label.setCellFormat(sr.equals(sr2) ? wcf_red : wcf_blue);

						ws.addCell(label);
					}

				}
			}

			for (int k = 0; k < cfgCount; k++) {
				for (int n = 0; n < deep; n++) {
					int idx = k * deep + n;
					ws.setColumnView(idx + 1, 15);
				}

			}

			wwb.write();
			wwb.close();
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method collects detailed statistics for the loaded deep memory data across various channel merge and memory depth cases (using different sheets) and timebase scales, showing the full-screen point counts, which appear as red diagonal lines
	 * .
	 * 
	 * Red is also used to mark the case where the full-screen count reaches the memory depth, which is the boundary case in compression where only one compression pass is needed
	 * 
	 * At this line's position, going up the timebase is spread out to zoom in, going down the timebase is stacked to zoom out.
	 * 
	 * The default screen pixel count is 1k, so the value 1k (meaning a 1k full-screen count, i.e. 1:1 display) is the dividing line; above it is the stretch case, below it is the compression case
	 * 
	 * The stretched part shows the stretch ratio; the compressed part shows the compression ratio
	 * 
	 * Blue marks cases where the stretch or compression ratio is not an integer, including cases where the stretch ratio is too large to be represented by int
	 * 
	 * In the stretch case the stretch ratio may exceed 1k, meaning there may be no point on the screen; in the compression case it may compress to less than one point; both are marked in brown
	 * 
	 * @param f
	 */
	public void output_CnDRates(File f) {
		String[] ChannelsUnit = { "CH1", "CH2", "CH4" };
		int cfgCount = sampling_configuration_count;
		try {
			WritableWorkbook wwb = Workbook.createWorkbook(f);// Create a Sheet using the createSheet method of the WritableWorkbook object, path name, etc.

			WritableSheet ws = null;
			Label label;
			Number labelN;

			WritableFont wfc = new WritableFont(WritableFont.ARIAL, 10,
					WritableFont.BOLD, true, UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.RED);
			WritableCellFormat wcf_red = new WritableCellFormat(wfc);

			WritableFont wfc2 = new WritableFont(WritableFont.ARIAL, 10,
					WritableFont.BOLD, true, UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.BLUE);
			WritableCellFormat wcf_blue = new WritableCellFormat(wfc2);

			WritableFont wfc3 = new WritableFont(WritableFont.ARIAL, 10,
					WritableFont.BOLD, true, UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.BROWN);
			WritableCellFormat wcf_brown = new WritableCellFormat(wfc3);

			WritableFont wfc4 = new WritableFont(WritableFont.ARIAL, 10,
					WritableFont.BOLD, true, UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.GOLD);
			WritableCellFormat wcf_gold = new WritableCellFormat(wfc4);

			int tblen = TIMEBASE.length;
			int lastTb = tblen - 1;
			int deep = DEEP.length;

			BigDecimal bd1k = BigDecimal.valueOf(1000);
			for (int a = 0; a < cfgCount; a++) {
				for (int b = 0; b < deep; b++) {
					/** Each channel merge & memory depth case creates a separate sheet */
					int n = a * deep + b;
					ws = wwb.createSheet(ChannelsUnit[a] + "_" + DEEP[b], n);
					int dm = DEEPValue[b];

					for (int m = 0; m < tblen; m++) {// row
						/** 0 column filled with timebase */
						label = new Label(0, m, TIMEBASE[m]);
						ws.addCell(label);

						/** Fill in the base value for the computation; %% is the diagonal position; when stopped, the waveform is loaded with this timebase */
						int fs = FullScreenData[n][m];

						labelN = new Number(m + 1, m, fs);
						labelN.setCellFormat(wcf_red);
						ws.addCell(labelN);

						/** timebase corresponding to the base value */
						BigDecimal bd = bdTIMEBASE[m];
						BigDecimal bdv = BigDecimal.valueOf(fs);

						if (m != lastTb) {
							int i = m;
							/** timebase increment computation */
							while (i < lastTb) {
								i++;

								/** compute the full-screen count under the corresponding timebase */
								BigDecimal tmp = bd.divide(bdTIMEBASE[i]);
								BigDecimal v = bdv.divide(tmp);
								double dbv = v.doubleValue();

								if (dbv == dm) {
									labelN = new Number(m + 1, i, dbv);
									labelN.setCellFormat(wcf_red);
									/**
									 * Mark the possible memory depth position; %% beyond the memory depth the compression workload reaches its maximum, the compression ratio increases,
									 * the result length becomes smaller
									 */
								} else if (dbv > dm) {
									/** full-screen count exceeds the memory depth: zoom-out case */
									/** %%compute the compression ratio of the full memory depth at the current pixel width */
									dbv = v.divide(bd1k).doubleValue();

									labelN = new Number(m + 1, i, dbv);
								} else if (dbv < 1000) {
									/** %%compute the stretch ratio */
									dbv = bd1k.divide(v).doubleValue();
									if (dbv > 1000)
										dbv = 0;

									labelN = new Number(m + 1, i, dbv);
								} else {
									/** %%compute the possible compression ratio */
									if (dbv > 1000)
										dbv = v.divide(bd1k).doubleValue();

									labelN = new Number(m + 1, i, dbv);
								}
								if (dbv >= 1) {
									/** handle values that are too small separately */
									if (!PrimaryTypeUtil.canHoldAsInt(dbv)) // compression ratio is not an integer
										labelN.setCellFormat(wcf_blue);
									else if (dbv == 1) // scale at load time
										labelN.setCellFormat(wcf_red);
									else if (dbv > dm) // compression ratio exceeds the memory depth
										labelN.setCellFormat(wcf_brown);
									ws.addCell(labelN);
								}
							}
						}

						if (m != 0) {
							int i = m;
							/** timebase decrement computation */
							while (i > 0) {
								i--;

								/** compute the full-screen count under the corresponding timebase */
								BigDecimal tmp = bd.divide(bdTIMEBASE[i]);
								BigDecimal v = bdv.divide(tmp);
								double dbv = v.doubleValue();

								/** %%compute the possible compression ratio */
								if (dbv > 1000)
									dbv = v.divide(bd1k).doubleValue();
								else if (dbv < 1000) {
									/** %%compute the stretch ratio */
									dbv = bd1k.divide(v).doubleValue();
									// if (dbv > 1000)
									// dbv = 0;
								}

								if (dbv >= 1) {
									/** handle values that are too small separately */
									labelN = new Number(m + 1, i, dbv);
									if (!PrimaryTypeUtil.canHoldAsInt(dbv))// stretch ratio is not an integer
										labelN.setCellFormat(wcf_blue);
									else if (dbv == 1)// scale at load time
										labelN.setCellFormat(wcf_red);
									else if (dbv > 1000)// compression ratio exceeds 1k; there may be no actual points on the screen
										labelN.setCellFormat(wcf_brown);
									ws.addCell(labelN);
								}
							}
						}
					}
				}
			}
			wwb.write();
			wwb.close();
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void output_IntVoltage() {
		for (int[] iv : intVOLTAGE) {
			for (int i : iv) {
				outprint(i + " ");
			}
			outprintln("");
		}
	}

	public void output_SampleRates() {
		int itv = sampling_configuration_count;
		int dm = DEEP.length;
		int tb = TIMEBASE.length;

		for (int k = 0; k < tb; k++) {
			outprint(k + "\t" + TIMEBASE[k] + "\t");
			for (int i = 0; i < itv; i++) {
				for (int j = 0; j < dm; j++) {
					SampleRate sr = SampleRates[i * dm + j][k];
					outprint(sr + ",\t");
				}
			}
			outprintln("");
		}
	}
}
