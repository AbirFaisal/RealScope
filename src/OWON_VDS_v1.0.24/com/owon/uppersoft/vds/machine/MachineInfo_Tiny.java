package com.owon.uppersoft.vds.machine;

import static com.owon.uppersoft.vds.util.format.ParseUtil.translateInt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import com.owon.uppersoft.dso.function.FFTView;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.machine.AbstractMachineInfo;
import com.owon.uppersoft.vds.core.sample.SampleRate;
import com.owon.uppersoft.vds.util.ui.ContextFileChooser;

public class MachineInfo_Tiny extends AbstractMachineInfo {

	private static final String TINY_SEPERATE_FREQ = "Tiny_SeperateFreq";
	// private static final String TINY_MULTI_FREQ = "Tiny_MultiFreq";

	protected int[] seperateFreq;

	// 改为基于TinyMachine实现倍频数值
	// protected int[] multiFreq;

	public MachineInfo_Tiny(InputStream is) {
		super(is);
	}

	public static final int FFT_2048_UsePart = 8;

	@Override
	protected void otherHandle(String item, BufferedReader br)
			throws IOException {
		if (item.equalsIgnoreCase(TINY_SEPERATE_FREQ)) {
			StringTokenizer st = nextStringTokenizer(br);
			seperateFreq = new int[st.countTokens()];
			int i = 0;
			while (st.hasMoreTokens()) {
				seperateFreq[i] = translateInt(st.nextToken());
				// System.out.println(seperateFreq[i]);
				i++;
			}
		}

		// else if (item.equalsIgnoreCase(TINY_MULTI_FREQ)) {
		// StringTokenizer st = nextStringTokenizer(br);
		// multiFreq = new int[st.countTokens()];
		// int i = 0;
		// while (st.hasMoreTokens()) {
		// multiFreq[i] = translateInt(st.nextToken());
		// // System.out.println(multiFreq[i]);
		// i++;
		// }
		// }
	}

	@Override
	public int fftAvailablePoints(int fftTimebaseIndex) {
		int fftTimebase = fftTimebaseIndex;
		if (fftTimebase >= FFT_2048_UsePart) {
			return FFTView.fftlen;
		} else {
			// 0 1 2 3 4 5 6 7
			// 8, 16, 32, 64, 128, 256, 512, 1024
			return 8 * (int) Math.pow(2, fftTimebase);
		}
	}

	@Override
	protected void computeChannelMaxSampleRate() {
		channelMaxSampleRate = new SampleRate[sampling_configuration_count];
		for (int k = 0; k < sampling_configuration_count; k++) {
			channelMaxSampleRate[k] = getADCSampleRate();
		}
	}

	public int getChannelSampleType(int channleCount) {
		return 0;
	}

	public int getDMIndexFromLength(int length) {
		int idx = super.getDMIndexFromLength(length);
		if (idx < 0 && length == 5100)
			return 0;
		return idx;
	}

	public int getSeperateFreq(int tb) {
		if (tb < 0 || tb >= seperateFreq.length)
			return -1;
		return seperateFreq[tb];
	}

	public int getFFTSeperateFreq(int tb) {
		return getSeperateFreq(tb);
	}

	// public int getMultiFreq(int tb) {
	// if (multiFreq == null)
	// return -1;
	// if (tb < 0 || tb >= multiFreq.length)
	// return -1;
	// return multiFreq[tb];
	// }

	protected void computeSeperateFreq() {
		int x = SampleRates.length, y = SampleRates[0].length;
		System.err.println("x, y: " + x + ", " + y);
		int[][] seperateFreq = new int[x][y];

		for (int i = 0; i < x; i++) {
			SampleRate[] srs = SampleRates[i];
			int[] sfs = seperateFreq[i];
			for (int j = 0; j < y; j++) {
				SampleRate max = channelMaxSampleRate[i];
				sfs[j] = max.getBDValue_Hz().divide(srs[j].getBDValue_Hz())
						.intValue();
				System.err.print(sfs[j] + ", ");
			}
		}
		System.err.println();

		// fftSeperateFreq = new int[y];
		// int fftup = FFT_2048_UsePart;
		// int spl = SeparatePattern.length;
		// for (int j = 0; j < y; j++) {
		// if (j <= fftup) {
		// fftSeperateFreq[j] = 1;
		// } else if (j > fftup) {
		// /** 用算法实现2,4,8的迭代*10 */
		// int k = j - fftup - 1;
		// fftSeperateFreq[j] = SeparatePattern[k % spl]
		// * ((int) Math.pow(10, k / spl));
		// }
		// System.err.println(fftSeperateFreq[j]);
		// }
	}

	public final static int[] SeparatePattern = { 2, 4, 8 };

	public final int getPos0HalfRange(int currentVolt) {
		return 10 * GDefine.PIXELS_PER_DIV;
	}

	public static void main(String[] args) {
		MachineInfo_Tiny mi = new MachineInfo_Tiny(
				MachineInfo_Tiny.class
						.getResourceAsStream("/com/owon/uppersoft/dso/model/machine/params/"
								+ "VDS2052ONE" + ".txt"));
		// "VDS2062ONE", "VDS3102ONE", "VDS3104ONE", "VDS1022ONE"

		mi.output_SampleRates();
		// if (true)
		// return;
		ContextFileChooser fc = new ContextFileChooser();
		File f = fc.save();
		if (f != null) {
			// mi.output_FullScreenNumbers(f);
			mi.output_CnDRates(f);
		}
	}

}