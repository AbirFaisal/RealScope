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
			WritableWorkbook wwb = Workbook.createWorkbook(f);// 用WritableWorkbook对象的createSheet方法创建Sheet,路径名等

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

			ws = wwb.createSheet("Sheet", 0);// 创建一个sheet
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
	 * 这个方法将详细统计，在多种通道合并及存储深度情况下(使用不同sheet页)、时基档位情况下，载入深存储数据，展示的满屏数情况，以红色斜线的方式出现
	 * 。
	 * 
	 * 同时红色还用来表示满屏数达到存储深度的情况，这是压缩情况下只用压缩一次的边界情况
	 * 
	 * 这条线的位置，往上时基拉开进行放大，往下时基叠起进行缩小。
	 * 
	 * 默认的屏幕像素点是1k，所以以1k数值(代表1k满屏数，那么就是1:1显示)为分界，往上为拉伸的情况，往下为压缩的情况
	 * 
	 * 拉伸的部分显示的数值是拉伸率，压缩的部分显示的数值是压缩率
	 * 
	 * 蓝色标明的是拉伸率或压缩率不为整数的情况，也有拉伸率过大而无法用int表示的情况
	 * 
	 * 拉伸的情况会出现拉伸率大于1k，即屏幕上不一定有点；压缩的情况会出现压缩在不足一点的情况，都采用棕色标明
	 * 
	 * @param f
	 */
	public void output_CnDRates(File f) {
		String[] ChannelsUnit = { "CH1", "CH2", "CH4" };
		int cfgCount = sampling_configuration_count;
		try {
			WritableWorkbook wwb = Workbook.createWorkbook(f);// 用WritableWorkbook对象的createSheet方法创建Sheet,路径名等

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
					/** 每种通道合并&存储深度情况单独创建一个sheet */
					int n = a * deep + b;
					ws = wwb.createSheet(ChannelsUnit[a] + '_' + DEEP[b], n);
					int dm = DEEPValue[b];

					for (int m = 0; m < tblen; m++) {// 行
						/** 0列填充时基 */
						label = new Label(0, m, TIMEBASE[m]);
						ws.addCell(label);

						/** 填充计算的基础值，%%即对角线位置，停止的情况下，以此时基载入波形 */
						int fs = FullScreenData[n][m];

						labelN = new Number(m + 1, m, fs);
						labelN.setCellFormat(wcf_red);
						ws.addCell(labelN);

						/** 基础值对应的时基 */
						BigDecimal bd = bdTIMEBASE[m];
						BigDecimal bdv = BigDecimal.valueOf(fs);

						if (m != lastTb) {
							int i = m;
							/** 时基递增计算 */
							while (i < lastTb) {
								i++;

								/** 计算对应时基下的满屏数 */
								BigDecimal tmp = bd.divide(bdTIMEBASE[i]);
								BigDecimal v = bdv.divide(tmp);
								double dbv = v.doubleValue();

								if (dbv == dm) {
									labelN = new Number(m + 1, i, dbv);
									labelN.setCellFormat(wcf_red);
									/**
									 * 标记可能的存储深度位置，%%超过存储深度压缩的处理量达到最大，压缩率增大，
									 * 结果长度变小
									 */
								} else if (dbv > dm) {
									/** 满屏数超过存储深度为缩小的情况 */
									/** %%计算满存储深度在当前像素宽度上的压缩率 */
									dbv = v.divide(bd1k).doubleValue();

									labelN = new Number(m + 1, i, dbv);
								} else if (dbv < 1000) {
									/** %%计算拉伸率 */
									dbv = bd1k.divide(v).doubleValue();
									if (dbv > 1000)
										dbv = 0;

									labelN = new Number(m + 1, i, dbv);
								} else {
									/** %%计算可能的压缩率 */
									if (dbv > 1000)
										dbv = v.divide(bd1k).doubleValue();

									labelN = new Number(m + 1, i, dbv);
								}
								if (dbv >= 1) {
									/** 过小数值另外考虑 */
									if (!PrimaryTypeUtil.canHoldAsInt(dbv)) // 压缩率非整
										labelN.setCellFormat(wcf_blue);
									else if (dbv == 1) // 载入时的档位
										labelN.setCellFormat(wcf_red);
									else if (dbv > dm) // 压缩率超过存储深度
										labelN.setCellFormat(wcf_brown);
									ws.addCell(labelN);
								}
							}
						}

						if (m != 0) {
							int i = m;
							/** 时基递减计算 */
							while (i > 0) {
								i--;

								/** 计算对应时基下的满屏数 */
								BigDecimal tmp = bd.divide(bdTIMEBASE[i]);
								BigDecimal v = bdv.divide(tmp);
								double dbv = v.doubleValue();

								/** %%计算可能的压缩率 */
								if (dbv > 1000)
									dbv = v.divide(bd1k).doubleValue();
								else if (dbv < 1000) {
									/** %%计算拉伸率 */
									dbv = bd1k.divide(v).doubleValue();
									// if (dbv > 1000)
									// dbv = 0;
								}

								if (dbv >= 1) {
									/** 过小数值另外考虑 */
									labelN = new Number(m + 1, i, dbv);
									if (!PrimaryTypeUtil.canHoldAsInt(dbv))// 拉伸率非整
										labelN.setCellFormat(wcf_blue);
									else if (dbv == 1)// 载入时的档位
										labelN.setCellFormat(wcf_red);
									else if (dbv > 1000)// 压缩率超过1k，屏幕上可能无实际点
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
			outprint(k + "\t" + TIMEBASE[k] + '\t');
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
