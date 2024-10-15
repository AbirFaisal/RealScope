package com.owon.vds.tiny.firm.pref.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import com.owon.uppersoft.vds.core.tune.IntVolt;
import com.owon.uppersoft.vds.core.tune.StringRoller;
import com.owon.uppersoft.vds.util.StringPool;
import com.owon.vds.tiny.tune.detail.DefaultCalArgType;

public class TuneTexter {

	private TuneModel tm;
	private String[] chlnames;
	private IntVolt[] volts;

	public TuneTexter(TuneModel tm, String[] chlnames, IntVolt[] volts) {
		this.tm = tm;
		this.chlnames = chlnames;
		this.volts = volts;
	}

	public void output(File f) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(f), StringPool.GBK));

			bw.write("$flash_txt=1;");
			bw.write("\r\n");
			bw.write("\r\n");

			// int i = 0;
			for (DefaultCalArgType cmdt : tm.cmdts) {
				bw.write("//");
				bw.write(cmdt.getType());
				bw.write("\r\n");
				output(cmdt, bw);
				bw.write("\r\n");
				// i++;
			}
			bw.write("\r\n");
			bw.write("\r\n");
			tm.reg.writetxt(bw);
			bw.write("\r\n");
			bw.write("\r\n");
			bw.write("END");

			bw.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void output(DefaultCalArgType cmdt, BufferedWriter bw)
			throws IOException {
		String key = cmdt.prekey();
		int[][] args = cmdt.getArgs();

		int vbc = args[0].length, chlr = args.length;
		// System.out.println("args.length:"+args.length);
		for (int i = 0; i < chlr; i++) {
			for (int j = 0; j < vbc; j++) {
				write2CHInfos(args, bw, key, i, j);

			}
			bw.write("\r\n");
		}
	}

	private void write2CHInfos(int[][] args, BufferedWriter bw, String key,
			int i, int j) throws IOException {
		bw.write(key);

		bw.write(String.valueOf(chlnames[i]));

		bw.write("_");
		bw.write(volts[j].toString());
		bw.write("=");
		bw.write(String.valueOf(args[i][j]));
		bw.write(";");
		bw.write("\r\n");
	}

	public void input(int[][] args, DefaultCalArgType cmdt, String pre, int v)
			throws IOException {
		int j, k;
		j = k = 0;

		StringRoller sr = new StringRoller(pre, cmdt.prekey().length());

		sr.nextWord();
		k = sr.fineWord(chlnames);

		sr.nextWord();
		j = sr.fineWord(volts);

		/** This side actually doesn't need to limit the maximum value */
		if (k < 0)
			return;
		if (j < 0)
			return;
		args[k][j] = v;
	}

	public void save(String path) {
		output(new File(path));
	}

	public void save(File file) {
		output(file);
	}

	public static final String MACHINE_TXT = "machine.txt";
	public static final String FLASHMEMORY_TXT = "flashmemory.txt";
	public static final String FLASH_TXT = "flash_txt";

	public boolean resetup() {
		return resetup(MACHINE_TXT);
	}

	public boolean resetup(String name) {
		File f = new File(FLASH_TXT, name);
		return loadDevicePref(f);
	}

	public boolean loadDevicePref(File f) {
		if (!f.isFile())
			return false;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream(f), StringPool.GBK));

			String oln = br.readLine();
			if (!oln.startsWith("$flash_txt")) {
				br.close();
				return false;
			}

			RL: while (true) {
				oln = br.readLine();
				if (oln == null || oln.equalsIgnoreCase("END"))
					break;
				if (oln.length() == 0) {
					continue;
				}
				if (oln.startsWith("//")) {
					// System.out.println(oln);
					continue;
				}
				// System.out.println(oln);
				int sepidx = oln.indexOf("=");
				int endidx = oln.indexOf(";");

				String pre = oln.substring(0, sepidx).trim();
				String suf = oln.substring(sepidx + 1, endidx).trim();
				if (tm.reg.readtxtline(pre, suf)) {
					continue RL;
				}

				Integer v = Integer.parseInt(suf);

				// AT:
				for (DefaultCalArgType cmdt : tm.cmdts) {
					int[][] args = cmdt.getArgs();
					if (pre.startsWith(cmdt.prekey())) {
						input(args, cmdt, pre, v);
						continue RL;
					}
				}
				// System.err.println(pre + " = " + v);
			}
			br.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
