package com.owon.uppersoft.dso.i18n;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.owon.uppersoft.vds.util.StringPool;

public class MultiPropertiesTransverter {

	public MultiPropertiesTransverter() {
		final String inPath = "1.txt";// /com/owon/uppersoft/dso/
		final String outPath = "2.txt";

		String line;
		try {
			FileInputStream fis = new FileInputStream(new File(inPath));
			BufferedReader br = new BufferedReader(new InputStreamReader(fis,
					StringPool.UTF8EncodingString));
			BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));

			while ((line = br.readLine()) != null) {
				line = line.trim();
				int len = line.length();
				if (len == 0)
					continue;

				if (line.indexOf('=') < 0)
					continue;
				else if (line.indexOf('(') >= 0 && line.indexOf(')') >= 0) {
					line = trimBrackets(line);

					bw.write(line, 0, line.length());
				} else {
					bw.write(line);
				}
				bw.newLine();
			}
			br.close();
			bw.close();
			System.err.println("simplify is done");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String trimBrackets(String line) {
		int leftidx = line.indexOf('(');
		int rightidx = line.indexOf('=', leftidx);
		// 在“=”右边的“()”不裁剪
		if (leftidx > rightidx)
			return line;

		String left = line.substring(0, leftidx);
		String right = line.substring(rightidx, line.length());
		line = left + "  " + right.trim();// .trim()
		return line;
	}

	public static final String ISO_8859_1 = "ISO-8859-1";

	/** 将字符编码转换成ISO-8859-1 */
	public String toISO_8859_1(String str) throws UnsupportedEncodingException {
		return changeCharset(str, ISO_8859_1);
	}

	/**
	 * 字符串编码转换的实现方法
	 * 
	 * @param str
	 *            待转换的字符串
	 * @param newCharset
	 *            目标编码
	 */
	public String changeCharset(String str, String newCharset)
			throws UnsupportedEncodingException {
		if (str != null) {
			// 用默认字符编码解码字符串。与系统相关，中文windows默认为GB2312
			byte[] bs = str.getBytes();
			return new String(bs, newCharset); // 用新的字符编码生成字符串
		}
		return null;
	}

	/**
	 * 字符串编码转换的实现方法
	 * 
	 * @param str
	 *            待转换的字符串
	 * @param oldCharset
	 *            源字符集
	 * @param newCharset
	 *            目标字符集
	 */
	public String changeCharset(String str, String oldCharset, String newCharset)
			throws UnsupportedEncodingException {
		if (str != null) {
			// 用源字符编码解码字符串
			byte[] bs = str.getBytes(oldCharset);
			return new String(bs, newCharset);
		}
		return null;
	}

	public static void main(String[] args) {
		// String source = "M\u00E1quina";// Máquina
		// byte[] bs = source.getBytes();
		// try {
		// source = new String(bs, "ISO-8859-1");
		// System.out.println(new String(bs, "ISO-8859-1"));
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// }
		new MultiPropertiesTransverter();
	}
}
