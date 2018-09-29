package com.owon.vds.tiny.firm.pref.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.owon.uppersoft.vds.data.LocaleObject;
import com.owon.uppersoft.vds.util.Pref;

public class Register {
	/** 确保一个字节存储一个语言开关，总共用掉的字节数不能多于REG_LOCALE_LEN */
	public static final int REG_LOCALE_LEN = 100;
	private static final String[] localeTypes = { "zh_CN", "zh_TW", "en", "fr",
			"es", "ru", "de", "pl", "pt_BR", "it", "ja", "ko_KR" };

	public static final List<LocaleObject> localeLists = new ArrayList<LocaleObject>(
			localeTypes.length);
	static {
		int len = localeTypes.length;
		for (int i = 0; i < len; i++) {
			localeLists.add(getLocaleObject(localeTypes[i]));
		}
	}

	public static final LocaleObject getLocaleObject(String lang) {
		return new LocaleObject(Pref.forLocale(lang));
	}

	public static final String getString(ByteBuffer bb) {
		int p = bb.position();

		int v = 0, i = 0;
		StringBuffer sb = new StringBuffer();
		while (bb.hasRemaining() && (v = bb.get()) != '\0') {
			sb.append((char) v);
			i++;

			if (i > 100) {
				// bb.position(p);
				bb.position(bb.limit());
				return "...";
			}
		}

		return sb.toString();
	}

	public boolean oem;
	public String version = "";
	public String serialNumber = "";
	public List<Boolean> localeSelection = new ArrayList<Boolean>(
			localeTypes.length);

	public Register() {
		int i = 0;
		int size = localeTypes.length;
		while (i < size) {
			localeSelection.add(Boolean.FALSE);
			i++;
		}
	}

	public List<LocaleObject> getSelectedLocaleObject() {
		List<LocaleObject> lists = new LinkedList<LocaleObject>();
		int i = 0;
		int size = localeSelection.size();
		while (i < size) {
			if (localeSelection.get(i)) {
				lists.add(localeLists.get(i));
			}
			i++;
		}
		// LocaleObject[] los = new LocaleObject[lists.size()];
		return lists;
	}

	public void read(ByteBuffer bb) {
		oem = bb.get() != 0;
		version = getString(bb);
		serialNumber = getString(bb);

		/** 记录语言开关起始位置 */
		int ptr = bb.position();
		int i = 0;
		int size = localeSelection.size();
		while (i < size && bb.hasRemaining()) {
			localeSelection.set(i, Boolean.valueOf(bb.get() != 0));
			i++;
		}

		/** 强制到第REG_LOCALE_LEN位读取相位细调参数 */
		bb.position(ptr + REG_LOCALE_LEN);
		pf.setRdValue(bb.getShort());
	}

	public void write(ByteBuffer bb) {
		bb.put((byte) (oem ? 1 : 0));
		byte[] arr;
		try {
			arr = version.getBytes("ASCII");
			bb.put(arr, 0, arr.length);
			bb.put((byte) '\0');

			arr = serialNumber.getBytes("ASCII");
			bb.put(arr, 0, arr.length);
			bb.put((byte) '\0');
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}

		/** 记录语言开关起始位置 */
		int ptr = bb.position();
		int i = 0;
		int size = localeSelection.size();
		while (i < size) {
			bb.put((byte) (localeSelection.get(i) ? 1 : 0));
			i++;
		}

		/** 强制到第REG_LOCALE_LEN位写入相位细调参数 */
		bb.position(ptr + REG_LOCALE_LEN);
		bb.putShort(pf.getWrValue());
	}

	private PhaseFine pf = new PhaseFine();

	public PhaseFine getPhaseFine() {
		return pf;
	}

	private static final String versionkey = "version", serieskey = "series",
			phase_fine_key = "phase_fine_value", oemkey = "oemlogo",
			newline = "\r\n";

	/**
	 * @param lan
	 *            用pt_BR在定制机的patch时
	 */
	public void enableLanguage(String lan) {
		int len = localeLists.size();
		for (int i = 0; i < len; i++) {
			LocaleObject lo = localeLists.get(i);
			String name = lo.getLocale().toString();
			if (name.equals(lan)) {
				localeSelection.set(i, Boolean.TRUE);
				return;
			}
		}
	}

	boolean isTrue(String txt) {
		return !txt.equalsIgnoreCase("0");
	}

	public boolean readtxtline(String pre, String suf) {
		if (!pre.startsWith("@")) {
			return false;
		}

		pre = pre.substring(1);
		if (pre.equals(versionkey)) {
			version = suf;
		} else if (pre.equals(serieskey)) {
			serialNumber = suf;
		} else if (pre.equals(oemkey)) {
			oem = isTrue(suf) ? true : false;
		} else if (pre.equals(phase_fine_key)) {
			pf.setRdValue(Short.valueOf(suf));
		} else {
			LocaleObject lo = getLocaleObject(pre);
			int idx = localeLists.indexOf(lo);
			if (idx >= 0) {
				localeSelection.set(idx, Boolean.valueOf(isTrue(suf)));
			}
		}
		return true;
	}

	public void writetxt(BufferedWriter bw) throws IOException {
		bw.write("@" + versionkey + "=" + version + ";");
		bw.write("\t//\u7248\u672C\u4FE1\u606F\uFF08\u4E2D\u6587\uFF09");
		bw.write(newline);
		bw.write("@" + serieskey + "=" + serialNumber + ";");
		bw.write("\t//\u5E8F\u5217\u53F7\uFF08\u4E2D\u6587\uFF09");
		bw.write(newline);
		bw.write("@" + oemkey + "=" + (oem ? 1 : 0) + ";");
		bw.write("\t//OEM \u7684Logo\u5F00\u5173");
		bw.write(newline);
		bw.write("@" + phase_fine_key + "=" + pf.getWrValue() + ";");
		bw.write(newline);
		bw.write(newline);
		bw.write("//");
		bw.write(newline);

		int len = localeLists.size();
		for (int i = 0; i < len; i++) {
			LocaleObject lo = localeLists.get(i);
			String name = lo.getLocale().toString();
			bw.write("@" + name + "=" + (localeSelection.get(i) ? 1 : 0) + ";");

			bw.write("\t");
			if (name.length() <= 2)
				bw.write("\t");

			bw.write("//" + lo.getDisplayNameForChs()
					+ "\u8BED\u8A00\u5F00\u5173");
			bw.write(newline);
		}
	}

	public int setPhaseFineValue(boolean b, short value) {
		pf.setPhaseFineValue(b, value);
		return pf.getWrValue();
	}
}