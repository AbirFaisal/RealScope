package com.owon.uppersoft.vds.core.machine;

import static com.owon.uppersoft.vds.util.format.ParseUtil.getInStringArray;
import static com.owon.uppersoft.vds.util.format.ParseUtil.translateInt;
import static com.owon.uppersoft.vds.util.format.ParseUtil.translate_KM;
import static com.owon.uppersoft.vds.util.format.ParseUtil.translate_KMG;
import static com.owon.uppersoft.vds.util.format.ParseUtil.translate_numS;
import static com.owon.uppersoft.vds.util.format.ParseUtil.trimQuotes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.StringTokenizer;

import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.sample.SampleRate;
import com.owon.uppersoft.vds.util.PrimaryTypeUtil;
import com.owon.uppersoft.vds.util.StringPool;

/**
 * MachineInfo
 * 
 * Characteristics of sampling rate usage:
 * Starting from the lowest timebase (the max sample rate is the limit), the max sample rate keeps being used (generally unchanged) while the full-screen point count grows with the timebase until the memory depth is reached; this is consistent with how the full-screen count changes when zooming;
 * After that the sample rate is lowered, which also satisfies filling the full screen with the entire memory depth
 * 
 * Full-screen count characteristic: all integers after multiplying by 2.5 (timebase step 2->5),
 * 
 * Notes:
 * 
 * Trigger position characteristic when stopped: consistent with its corresponding sample point, position synchronized
 * 
 * At low timebases there are few points; after stop, interpolation must be considered (sine, linear)
 * 
 * MachineInfo
 * 
 */
public abstract class MachineInfo {

	protected static final String MachineInfo_DeepMemory = "DeepMemory";
	protected static final String MachineInfo_SlowMoveTimebase = "SlowMoveTimebase";
	protected static final String MachineInfo_ProbeRate = "ProbeRate";
	protected static final String MachineInfo_Voltbase = "Voltbase";
	protected static final String MachineInfo_Timebase = "Timebase";

	protected static final String MachineInfo_ChannelConfig = "ChannelConfig";
	protected static final String MachineInfo_Grids = "Grids";
	protected static final String MachineInfo_ADCSampleRate = "ADCSampleRate";
	protected static final String MachineInfo_FFTTimebase = "FFTTimebase";

	private static final String N_A = "N/A";

	private void setup(InputStream is) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					StringPool.UTF8EncodingString));
			String line;

			while ((line = br.readLine()) != null) {
				line = line.trim();
				int len = line.length();
				if (len == 0)
					continue;
				if (line.startsWith("//"))
					continue;

				if (line.charAt(0) == '[') {
					int itemend = line.indexOf(']', 1);
					String item = line.substring(1, itemend);

					handleItem(br, item);

					continue;
				}

			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// for (int i = 0; i < FFTTimeBases.length; i++) {
		// System.err.print("\"" + FFTTimeBases[i] + "\", ");
		// }

		endOfInit();
	}

	private void handleItem(BufferedReader br, String item) throws IOException {
		if (item.equals(MachineInfo_ADCSampleRate)) {
			String cntline = nextTrimLine(br);
			ADC_CLOCK_SampleRate = SampleRate.valueOf(cntline);
		} else if (item.equals(MachineInfo_DeepMemory)) {
			StringTokenizer st = nextStringTokenizer(br);
			int cntsize = st.countTokens();
			DEEP = new String[cntsize];
			DEEPValue = new int[cntsize];
			deepNum = cntsize;
			int i = 0;
			while (st.hasMoreTokens()) {
				String token = trimQuotes(st.nextToken());
				DEEP[i] = token;
				DEEPValue[i] = translate_KM(token);
				i++;
			}
		} else if (item.equals(MachineInfo_SlowMoveTimebase)) {
			String cntline = nextTrimLine(br);
			SlowMoveTimebase = trimQuotes(cntline);
		} else if (item.equals(MachineInfo_ProbeRate)) {
			StringTokenizer st = nextStringTokenizer(br);
			int cntsize = st.countTokens();
			int probeNum = cntsize;
			ProbeTexts = new String[probeNum];
			ProbeMulties = new Integer[probeNum];
			int i = 0;
			while (st.hasMoreTokens()) {
				int stv = translateInt(st.nextToken());
				ProbeTexts[i] = "x" + stv;
				ProbeMulties[i] = stv;
				i++;
			}

		} else if (item.equals(MachineInfo_Voltbase)) {
			StringTokenizer st = nextStringTokenizer(br);
			int cntsize = st.countTokens();
			int probLen = ProbeMulties.length;
			intVOLTAGE = new int[probLen][cntsize];
			int i = 0;
			while (st.hasMoreTokens()) {
				int stv = translateInt(st.nextToken());
				// intVOLTAGE[0][i] = stv;
				// intVOLTAGE[1][i] = stv * 10;
				// intVOLTAGE[2][i] = stv * 100;
				// intVOLTAGE[3][i] = stv * 1000;
				for (int probidx = 0; probidx < probLen; probidx++) {
					intVOLTAGE[probidx][i] = stv
							* ProbeMulties[probidx];
				}
				i++;
			}
		} else if (item.equals(MachineInfo_Timebase)) {
			StringTokenizer st = nextStringTokenizer(br);
			int cntsize = st.countTokens();
			TIMEBASE = new String[cntsize];
			bdTIMEBASE = new BigDecimal[cntsize];
			int tbNum = cntsize;
			int i = 0;
			while (st.hasMoreTokens()) {
				String token = trimQuotes(st.nextToken());
				TIMEBASE[i] = token;
				bdTIMEBASE[i] = translate_numS(token);
				i++;
			}
			SampleRates = new SampleRate[deepNum
					* sampling_configuration_count][tbNum];
			FullScreenData = new int[deepNum
					* sampling_configuration_count][tbNum];
		} else if (item.equals(MachineInfo_FFTTimebase)) {
			StringTokenizer st = nextStringTokenizer(br);
			int cntsize = st.countTokens();
			FFTTimeBases = new String[cntsize];
			BDFFTTimeBases = new BigDecimal[cntsize];
			int i = 0;
			while (st.hasMoreTokens()) {
				String token = trimQuotes(st.nextToken());
				FFTTimeBases[i] = token;
				BDFFTTimeBases[i] = translate_KMG(token);
				// System.out.println(token + "," +
				// BDFFTTimeBases[i]);
				i++;
			}
		} else if (item.startsWith(MachineInfo_ChannelConfig)) {
			StringTokenizer st = nextStringTokenizer(br);
			int cntsize = st.countTokens();
			if (cntsize >= 2) {
				channel_numbers = translateInt(st.nextToken());
				sampling_configuration_count = translateInt(st
						.nextToken());
			}

			if (channel_numbers <= 0
					|| sampling_configuration_count <= 0) {
				channel_numbers = 2;
				sampling_configuration_count = 2;
			}

			computeChannelMaxSampleRate();
			// channelMaxSampleRate = new
			// SampleRate[sampling_configuration_count];
			// for (int k = 0; k < sampling_configuration_count;
			// k++) {
			// channelMaxSampleRate[k] =
			// computeChannelMaxSampleRate(k);
			// // System.err.print(channelMaxSampleRate[k] + ", ");
			// }
			// System.err.println();
		}else{
			otherHandle(item, br);
		}
	}

	protected void otherHandle(String item, BufferedReader br)
			throws IOException {
	}

	protected StringTokenizer nextStringTokenizer(BufferedReader br)
			throws IOException {
		String cntline = nextTrimLine(br);
		StringTokenizer st = new StringTokenizer(cntline, ",");
		return st;
	}

	protected String nextTrimLine(BufferedReader br) throws IOException {
		return br.readLine().trim();
	}

	protected void computeChannelMaxSampleRate() {
		channelMaxSampleRate = new SampleRate[sampling_configuration_count];
		for (int k = 0; k < sampling_configuration_count; k++) {
			channelMaxSampleRate[k] = computeChannelMaxSampleRate_ChannelsMerge(
					ADC_CLOCK_SampleRate, k);
			// System.err.print(channelMaxSampleRate[k] + ", ");
		}
	}

	public static SampleRate computeChannelMaxSampleRate_ChannelsMerge(
			SampleRate adcClockSampleRate, int sampling_configuration) {
		/**
		 * The channel-merge cases are 1, 2, 4, exactly 1<<0, 1<<1, 1<<2,
		 * 
		 * i.e. 1 << sampling_configuration
		 */
		return SampleRate.getSampleRateByDiv1000(adcClockSampleRate
				.getBDValue_Hz().divide(
						BigDecimal.valueOf(1 << sampling_configuration)));
	}

	protected void endOfInit() {
		fillSampleRates();
		// initVOLTAGEMath();
	}

	public String[] FFTTimeBases;
	public BigDecimal[] BDFFTTimeBases;

	/**
	 * No longer reads from text; the sample rate is computed directly, so this method is currently unused
	 * 
	 * @param item
	 * @param tbNum
	 * @param deepNum
	 * @param blockNum
	 * @param br
	 * @throws IOException
	 */
	protected void loadSampleRates(String item, int tbNum, int deepNum,
			int blockNum, BufferedReader br) throws IOException {
		item = item.substring(item.indexOf('_') + 1);
		// String tmp = item;
		int i, len;
		String line;
		int stv = item.charAt(1);

		int itemend = item.indexOf('_');
		item = item.substring(itemend + 1);
		int j = getInStringArray(DEEP, item);
		if (j < 0 || channel_numbers + '0' < stv) {// Invalid memory depth value or channel count, e.g. 10M or 4ch
			// System.err.println(tmp);
			i = 0;
			len = tbNum;
			while (i < len) {
				line = br.readLine();
				i++;
			}
			return;
		}
		switch (stv) {
		case '1':
			stv = SampleType_1CH;
			break;
		case '2':
			stv = SampleType_2CH;
			break;
		case '4':
			stv = SampleType_4CH;
			break;
		}
		len = tbNum;
		SampleRate[] srs = SampleRates[stv * deepNum + j];
		int[] fsd = FullScreenData[stv * deepNum + j];
		i = 0;
		SampleRate sr;

		/** Algorithm for the sample rate at a given timebase: starting from the max sample rate */

		while (i < len) {
			line = nextTrimLine(br);
			line = line.substring(0, line.indexOf(','));
			srs[i] = sr = SampleRate.valueOf(line);
			fsd[i] = sr.compute(bdTIMEBASE[i], blockNum);

			i++;
		}
	}

	protected SampleRate[] channelMaxSampleRate;

	/**
	 * Sample rate algorithm:
	 * 
	 * timebase * 20 divisions * maxSampleRate, then after unifying units to a point count, compare with the corresponding DEEPValue,
	 * 
	 * if <= DEEPValue, maxSampleRate can be used,
	 * 
	 * otherwise, use DEEPValue / (timebase * 20 divisions) to get the corresponding sample rate,
	 * 
	 * This formula is not used from the start because it is limited by the actual maxSampleRate and is not always valid
	 */
	protected void fillSampleRates() {
		int tb = TIMEBASE.length;
		int dmn = DEEPValue.length;
		// System.out.println(ADCSampleRate.getBDValue_Hz());// S/s
		// String err = "value can not be int full screen point number: ";

		for (int j = 0; j < tb; j++) {
			BigDecimal scrTime = bdTIMEBASE[j].multiply(BigDecimal
					.valueOf(GDefine.BlockNum));// s
			// System.out.print(TIMEBASE[j] + "\t");// " + scrTime + "
			for (int m = 0; m < sampling_configuration_count; m++) {
				SampleRate cur_max = channelMaxSampleRate[m];
				BigDecimal current_sample = cur_max.getBDValue_Hz();

				for (int k = 0; k < dmn; k++) {
					int dm = DEEPValue[k];

					// System.err.print((m * dmn + k)+"_"+dm+"\t");
					BigDecimal value = scrTime.multiply(current_sample);
					SampleRate v;
					if (!PrimaryTypeUtil.canHoldAsLong(value)) {
						// System.err.println(err + value);
						v = cur_max;
						SampleRates[m * dmn + k][j] = v;
						// System.err.print(value + "_\t");
					} else {
						// System.err.print(value + "_\t");
						// Check
						if (value.longValue() <= dm) {
							v = cur_max;
							SampleRates[m * dmn + k][j] = v;
							// System.err.print(value + "_\t");
							// System.out.print(dd + "\t");
						} else {
							BigDecimal sr = BigDecimal.valueOf(dm).divide(
									scrTime);
							v = SampleRate.getSampleRateByDiv1000(sr);
							if (v == null) {
								// System.err.print(sr.divide(BigDecimal
								// .valueOf(1000))
								// + "_\t");
							} else {
								// System.out.print(dd + "\t");
							}
							SampleRates[m * dmn + k][j] = v;
						}
					}

					int f = FullScreenData[m * dmn + k][j] = v.compute(bdTIMEBASE[j],
							GDefine.BlockNum);
					// System.out.println("" + f + "("+SampleRates[m * dmn + k][j]+")");
				}
			}
			// System.out.println();
		}

	}

	private int channel_numbers = 2;
	public int sampling_configuration_count = 2;

	public int getChannelNumbers() {
		return channel_numbers;
	}

	public String SlowMoveTimebase;

	// Voltage scale value table, element 0 is for attenuation of 1, unit mV
	public String[] ProbeTexts;
	public Integer[] ProbeMulties;
	public int[][] intVOLTAGE;
	public BigDecimal[] bdTIMEBASE;

	public BigDecimal getVoltagesBDRatioBetween(int vb1, int vb2) {
		int[] vbs = intVOLTAGE[0];
		BigDecimal vbmulti = BigDecimal.valueOf(vbs[vb1]).divide(
				BigDecimal.valueOf(vbs[vb2]));
		return vbmulti;
	}

	/**
	 * Scaling ratio needed from voltage scale a to voltage scale b; a generally uses the absolute voltage scale at the time loading was stopped
	 * 
	 * @param vb1
	 * @param vb2
	 * @return >1 means stretch, <1 means compress
	 */
	public double getVoltagesRatio(int vb1, int vb2) {
		int[] intVOLTAGEs = intVOLTAGE[0];
		return intVOLTAGEs[vb1] / (double) intVOLTAGEs[vb2];
	}

	public String[] DEEP;
	public int[] DEEPValue;
	public String[] TIMEBASE;

	public String getTimebaseLabel(int idx) {
		if (idx < 0 || idx >= TIMEBASE.length)
			return N_A;

		return TIMEBASE[idx];
	}

	public int getTimebaseIndex(String txt) {
		int len = TIMEBASE.length;
		for (int i = 0; i < len; i++) {
			if (TIMEBASE[i].equalsIgnoreCase(txt))
				return i;
		}
		return -1;
	}

	protected SampleRate[][] SampleRates;
	protected int[][] FullScreenData;

	/**
	 * Channel_4 means the max sample rate when all 4 channels are enabled
	 * 
	 * The values of the first three valid types are the array indices, do not modify
	 */
	private static final int SampleType_1CH = 0, SampleType_2CH = 1,
			SampleType_4CH = 2, Channel_NONE = -1;

	public int getChannelSampleType(int channleCount) {
		if (channleCount > 2)
			return SampleType_4CH;
		else if (channleCount == 2)
			return SampleType_2CH;
		else if (channleCount == 1)
			return SampleType_1CH;
		else
			return Channel_NONE;
	}

	private static final int Pos0Range_RelayThredshold_IntVoltage = 100;
	private static final int Pos0Range_RelayThredshold_ExtraDivdior = 50;
	private static final int Pos0Range_RelayThredshold_Dividor = 1000;

	public abstract int fftAvailablePoints(int timebaseIndex);

	public int getPos0HalfRange(int currentVolt) {
		if (channel_numbers > 2)
			return getPos0HalfRange_for4(currentVolt);
		else
			return getPos0HalfRange_for2(currentVolt);
	}

	public int getPos0HalfRange_for4(int currentVolt) {
		/** +-2V (2mV~200mV), +-50V (500mV~5V) */
		final int relayThred = Pos0Range_RelayThredshold_IntVoltage << 1;
		final int relayMultiply = Pos0Range_RelayThredshold_ExtraDivdior;
		/**
		 * vb<=100mV, then 1V/vb, else 50V/vb gives the range (number of divisions for positive and negative each)
		 */
		int d = Pos0Range_RelayThredshold_Dividor;
		if (currentVolt > relayThred) {
			d *= relayMultiply;
		} else
			d *= 2;
		// System.out.println(d + "," + currentVolt);
		return d / currentVolt * GDefine.PIXELS_PER_DIV;
	}

	public int getPos0HalfRange_for2(int currentVolt) {
		/** +-1V (2mV~100mV), +-50V (200mV~5V) */
		final int relayThred = Pos0Range_RelayThredshold_IntVoltage;
		final int relayMultiply = Pos0Range_RelayThredshold_ExtraDivdior;
		/**
		 * vb<=100mV, then 1V/vb, else 50V/vb gives the range (number of divisions for positive and negative each)
		 */
		int d = Pos0Range_RelayThredshold_Dividor;
		if (currentVolt > relayThred) {
			d *= relayMultiply;
		}
		// System.out.println(d + "," + currentVolt);
		return d / currentVolt * GDefine.PIXELS_PER_DIV;
	}

	private SampleRate ADC_CLOCK_SampleRate;

	public SampleRate getADCSampleRate() {
		return ADC_CLOCK_SampleRate;
	}

	/**
	 * Scaling ratio needed from timebase a to timebase b; a generally uses the absolute timebase at the time loading was stopped
	 * 
	 * bdTIMEBASE[a].divide(bdTIMEBASE[b]);
	 * 
	 * @param a
	 * @param b
	 * @return >1 means stretch, <1 means compress
	 */
	public BigDecimal ratio(int a, int b) {
		return bdTIMEBASE[a].divide(bdTIMEBASE[b]);
	}

	public int getDMIndexFromLength(int length) {
		int[] ts = DEEPValue;
		for (int i = 0; i < ts.length; i++) {
			if (ts[i] == length)
				return i;
		}
		outprintln("Mlen: can't be matched" + length);
		return -1;
	}

	protected MachineInfo(InputStream is) {
		init(is);
	}

	private int SlowMoveIdx = -1;
	private int deepNum;

	private void init(InputStream is) {
		// System.err.println(is);
		if (is != null) {
			setup(is);
			SlowMoveIdx = getSlowMoveIdx();
		} else {
			SlowMoveIdx = -1;
		}
	}

	public SampleRate[] getSampleRate(int chl_spl_cfg, int dmidx) {
		// System.err.println(chl_spl_cfg+","+dmidx);
		int dmlen = DEEP.length;
		SampleRate[] sr = SampleRates[chl_spl_cfg * dmlen + dmidx];
		return sr;
	}

	public int[] getFullScreen(int chlnum, int dmidx) {
		int dmlen = DEEP.length;
		int[] sr = FullScreenData[chlnum * dmlen + dmidx];
		return sr;
	}

	protected int getSlowMoveIdx() {
		String[] ts = TIMEBASE;
		for (int i = 0; i < ts.length; i++) {
			if (ts[i].equals(SlowMoveTimebase))
				return i;
		}
		return -1;
	}

	public boolean isSlowMove(int idx) {
		return idx >= SlowMoveIdx;
	}

	protected void outprintln(String string) {
		System.out.println(string);
	}

	protected void outprint(String string) {
		System.out.println(string);
	}

}
