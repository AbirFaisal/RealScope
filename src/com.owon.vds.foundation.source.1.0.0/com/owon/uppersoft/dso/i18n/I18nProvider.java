package com.owon.uppersoft.dso.i18n;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JComponent;

import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.aspect.base.ResourceBundleProvider;
import com.owon.uppersoft.vds.util.StringPool;

public class I18nProvider {
	public static final String MsgLib_Bundle = "com.owon.uppersoft.dso.i18n.MsgLib";
	private static ResourceBundle bdl;
	static {
		updateLocale();
	}

	private static final ResourceBundle updateLocale() {
		return bdl = ResourceBundle.getBundle(MsgLib_Bundle);
	}

	public static final ResourceBundle getLocaleBundle(Locale loc) {
		return ResourceBundle.getBundle(MsgLib_Bundle, loc);
	}

	/** export */
	public static final ResourceBundle updateLocale(Locale loc) {
		Locale.setDefault(loc);
		JComponent.setDefaultLocale(Locale.getDefault());
		return updateLocale();
	}

	public static final Locale locale() {
		return Locale.getDefault();
	}

	public static final ResourceBundle bundle() {
		return bdl;
	}

	public static void LocalizeSelf(Localizable l) {
		l.localize(bdl);
	}

	public static final ResourceBundleProvider getResourceBundleProvider() {
		return new ResourceBundleProvider() {
			@Override
			public ResourceBundle bundle() {
				return I18nProvider.bundle();
			}
		};
	}

	public static void main(String[] args) {
		// System.out.println(toUnicode("华文细黑", false));
	}

	/*
	 * Converts unicodes to encoded &#92;uxxxx and escapes special characters
	 * with a preceding slash
	 */
	static String toUnicode(String theString, boolean escapeSpace) {
		int len = theString.length();
		int bufLen = len * 2;
		if (bufLen < 0) {
			bufLen = Integer.MAX_VALUE;
		}
		StringBuffer outBuffer = new StringBuffer(bufLen);

		for (int x = 0; x < len; x++) {
			char aChar = theString.charAt(x);
			// Handle common case first, selecting largest block that
			// avoids the specials below
			if ((aChar > 61) && (aChar < 127)) {
				if (aChar == '\\') {
					outBuffer.append('\\');
					outBuffer.append('\\');
					continue;
				}
				outBuffer.append(aChar);
				continue;
			}
			switch (aChar) {
			case ' ':
				if (x == 0 || escapeSpace)
					outBuffer.append('\\');
				outBuffer.append(' ');
				break;
			case '\t':
				outBuffer.append('\\');
				outBuffer.append('t');
				break;
			case '\n':
				outBuffer.append('\\');
				outBuffer.append('n');
				break;
			case '\r':
				outBuffer.append('\\');
				outBuffer.append('r');
				break;
			case '\f':
				outBuffer.append('\\');
				outBuffer.append('f');
				break;
			case '=': // Fall through
			case ':': // Fall through
			case '#': // Fall through
			case '!':
				outBuffer.append('\\');
				outBuffer.append(aChar);
				break;
			default:
				if ((aChar < 0x0020) || (aChar > 0x007e)) {
					outBuffer.append('\\');
					outBuffer.append('u');
					outBuffer.append(toHex((aChar >> 12) & 0xF));
					outBuffer.append(toHex((aChar >> 8) & 0xF));
					outBuffer.append(toHex((aChar >> 4) & 0xF));
					outBuffer.append(toHex(aChar & 0xF));
				} else {
					outBuffer.append(aChar);
				}
			}
		}

		return outBuffer.toString();
	}

	private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static char toHex(int nibble) {
		return hexDigit[(nibble & 0xF)];
	}

	static void transform() {
		Locale locale = Locale.CHINA;
		List<String> list = new ArrayList<String>();
		list.add(MsgLib_Bundle);
		list.add("com.owon.uppersoft.common.i18n.LAMsgLib");
		list.add("com.owon.uppersoft.common.i18n.OSCMsgLib");
		for (String bundle : list) {
			String path = bundle.substring(bundle.lastIndexOf(".") + 1) + "_"
					+ locale + ".txt";
			ascii2nativeByResourceBundle(path, bundle, locale,
					StringPool.UTF8EncodingString);// ////////////////////////"UTF-8";
		}
	}

	static void ascii2nativeByResourceBundle(String name, String bundle,
			Locale locale, String charset) {
		ascii2nativeByResourceBundle(new File(name), bundle, locale, charset);
	}

	static void ascii2nativeByResourceBundle(File f, String bundle,
			Locale locale, String charset) {
		ResourceBundle rb = ResourceBundle.getBundle(bundle, locale);

		Enumeration<String> enties = rb.getKeys();
		try {
			OutputStreamWriter osr = null;
			try {
				osr = new OutputStreamWriter(new FileOutputStream(f), charset);
				String key, value;
				while (enties.hasMoreElements()) {
					key = enties.nextElement();
					value = rb.getString(key);
					osr.append(key + " = " + value + StringPool.LINE_SEPARATOR);// ///////////;System.getProperty("line.separator")
				}
			} finally {
				if (osr != null)
					osr.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	static String ascii2native_DIY(String str) {
		String hex = "0123456789ABCDEF";
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '\\' && i + 1 <= str.length() && str.charAt(i + 1) == '\\') {
				buf.append("\\\\");
				i += 1;
			} else if (c == '\\' && i + 6 <= str.length()
					&& str.charAt(i + 1) == 'u') {
				String sub = str.substring(i + 2, i + 6).toUpperCase();
				int i0 = hex.indexOf(sub.charAt(0));
				int i1 = hex.indexOf(sub.charAt(1));
				int i2 = hex.indexOf(sub.charAt(2));
				int i3 = hex.indexOf(sub.charAt(3));

				if (i0 < 0 || i1 < 0 || i2 < 0 || i3 < 0) {
					buf.append("\\u");
					i += 1;
				} else {
					byte[] data = new byte[2];
					data[0] = i2b(i1 + i0 * 16);
					data[1] = i2b(i3 + i2 * 16);
					try {
						buf.append(new String(data, "UTF-16BE").toString());
					} catch (Exception ex) {
						buf.append("\\u" + sub);
					}
					i += 5;
				}
			} else {
				buf.append(c);
			}
		}

		return buf.toString();
	}

	static final int b2i(byte b) {
		return (int) ((b < 0) ? 256 + b : b);
	}

	static final byte i2b(int i) {
		return (byte) ((i > 127) ? i - 256 : i);
	}
}
