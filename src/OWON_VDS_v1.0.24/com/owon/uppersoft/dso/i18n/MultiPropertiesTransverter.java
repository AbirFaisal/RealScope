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
		// Do not trim the "()" to the right of the "="
		if (leftidx > rightidx)
			return line;

		String left = line.substring(0, leftidx);
		String right = line.substring(rightidx, line.length());
		line = left + "  " + right.trim();// .trim()
		return line;
	}

	public static final String ISO_8859_1 = "ISO-8859-1";

	/** Convert the character encoding to ISO-8859-1 */
	public String toISO_8859_1(String str) throws UnsupportedEncodingException {
		return changeCharset(str, ISO_8859_1);
	}

	/**
	 * Implementation of string encoding conversion
	 * 
	 * @param str
	 *            string to be converted
	 * @param newCharset
	 *            target encoding
	 */
	public String changeCharset(String str, String newCharset)
			throws UnsupportedEncodingException {
		if (str != null) {
			// decode the string using the default character encoding. System dependent; Chinese Windows defaults to GB2312
			byte[] bs = str.getBytes();
			return new String(bs, newCharset); // create the string using the new character encoding
		}
		return null;
	}

	/**
	 * Implementation of string encoding conversion
	 * 
	 * @param str
	 *            string to be converted
	 * @param oldCharset
	 *            source charset
	 * @param newCharset
	 *            target charset
	 */
	public String changeCharset(String str, String oldCharset, String newCharset)
			throws UnsupportedEncodingException {
		if (str != null) {
			// decode the string using the source character encoding
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
