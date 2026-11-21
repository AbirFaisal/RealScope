package com.owon.uppersoft.vds.util;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

import com.owon.uppersoft.vds.core.measure.MeasureElem;
import com.owon.uppersoft.vds.data.Point;
import com.owon.uppersoft.vds.data.RGB;

/**
 * PropertiesUtil, conversion between values in Properties and primitive types
 * 
 */
public class Pref extends Properties {

	public static final String EmptyString = "";
	public static final String SemicolonString = ";";
	public static final String TRUE_STRING = "1";
	public static final String FALSE_STRING = "0";
	public static final String[] EmptyStringArray = new String[] { EmptyString };
	public static final int[] EmptyIntArray = {};
	public static final byte[] EmptybyteArray = {};

	public static final String Language = "Language";
	public static final String Country = "Country";
	public static final String Variant = "Variant";

	/**
	 * Load a boolean: non-1 is false, 1 is true
	 * 
	 * @param p
	 * @param key
	 * @return
	 */
	public boolean loadBoolean(String key) {
		String s = getProperty(key, EmptyString);
		return s.equalsIgnoreCase(TRUE_STRING) ? true : false;
	}

	/**
	 * Persist a boolean: true is set to 1, false is set to 0
	 * 
	 * @param p
	 * @param key
	 * @param b
	 */
	public void persistBoolean(String key, boolean b) {
		String s = b ? TRUE_STRING : FALSE_STRING;
		setProperty(key, s);
	}

	/**
	 * Load a Locale
	 * 
	 * @param p
	 * @return
	 */
	public Locale loadLocale() {
		Locale locale = Locale.ENGLISH;
		String lan = getProperty(Language, EmptyString);
		if (!lan.equals(EmptyString)) {
			String cou = getProperty(Country, EmptyString);
			String var = getProperty(Variant, EmptyString);
			locale = new Locale(lan, cou, var);
		}
		return locale;
	}

	/**
	 * Persist a Locale
	 * 
	 * @param p
	 * @param locale
	 */
	public void persistLocale(Locale locale) {
		setProperty(Language, locale.getLanguage());
		setProperty(Country, locale.getCountry());
		setProperty(Variant, locale.getVariant());
	}

	/**
	 * Load the file history list
	 * 
	 * @param p
	 * @param key
	 * @param limit
	 * @return
	 */
	public List<File> loadFileHistory(String key, int limit) {
		List<File> list = new ArrayList<File>(limit);
		String fh = getProperty(key, EmptyString);
		StringTokenizer st = new StringTokenizer(fh, SemicolonString);
		int c = 0;
		while (st.hasMoreTokens()) {
			if (c == limit)
				break;
			File file = new File(st.nextToken());
			if (file.exists()) {
				list.add(file);
				c++;
			}
		}
		return list;
	}

	/**
	 * Persist the file history list
	 * 
	 * @param p
	 * @param key
	 * @param list
	 */
	public void persistFileHistory(String key, List<File> list) {
		StringBuilder sb = new StringBuilder();
		for (File file : list) {
			sb.append(file.getPath());
			sb.append(SemicolonString);
		}
		setProperty(key, sb.toString());
	}

	/**
	 * Load a list of integer values
	 * 
	 * @param p
	 * @param key
	 * @param delim
	 * @return
	 */
	public LinkedList<Integer> loadIntegerList(String key, String delim) {
		LinkedList<Integer> list = new LinkedList<Integer>();
		String fh = getProperty(key, EmptyString);
		StringTokenizer st = new StringTokenizer(fh, delim);
		while (st.hasMoreTokens()) {
			list.add(Integer.parseInt(st.nextToken()));
		}
		return list;
	}

	/**
	 * Persist a list of integer values
	 * 
	 * @param p
	 * @param key
	 * @param list
	 * @param delim
	 */
	public void persistIntegerList(String key, List<Integer> list, String delim) {
		StringBuilder sb = new StringBuilder();
		for (Integer s : list) {
			sb.append(s);
			sb.append(delim);
		}
		setProperty(key, sb.toString());
	}

	/**
	 * Load a list of strings
	 * 
	 * @param p
	 * @param key
	 * @param delim
	 * @return
	 */
	public List<String> loadStringList(String key, String delim) {
		List<String> list = new LinkedList<String>();
		String fh = getProperty(key, EmptyString);
		StringTokenizer st = new StringTokenizer(fh, delim);
		while (st.hasMoreTokens()) {
			list.add(st.nextToken().trim());
		}
		return list;
	}

	/**
	 * Load a list of strings
	 * 
	 * @param p
	 * @param key
	 * @param delim
	 * @return
	 */
	public List<String> loadStringList(String key, String delim, int maxline) {
		List<String> list = new LinkedList<String>();
		String fh = getProperty(key, EmptyString);
		StringTokenizer st = new StringTokenizer(fh, delim);
		int i = 0;
		while (st.hasMoreTokens() && i < maxline) {
			list.add(st.nextToken().trim());
			i++;
		}
		return list;
	}

	/**
	 * Persist a list of strings
	 * 
	 * @param p
	 * @param key
	 * @param list
	 * @param delim
	 */
	public void persistStringList(String key, List<String> list, String delim) {
		StringBuilder sb = new StringBuilder();
		for (String s : list) {
			sb.append(s);
			sb.append(delim);
		}
		setProperty(key, sb.toString());
	}

	/**
	 * Load RGB; changes the value of the passed-in rgb
	 * 
	 * @param p
	 * @param key
	 * @param rgb
	 *            default rgb; if null, a default is created
	 * @return
	 */
	public RGB loadRGB(String key, RGB rgb) {
		String name = getProperty(key, EmptyString);
		return new RGB(name);
	}

	/**
	 * Persist RGB
	 * 
	 * @param p
	 * @param key
	 * @param rgb
	 */
	public void persistRGB(String key, RGB rgb) {
		setProperty(key, rgb.toHexString());
	}

	/**
	 * Load int
	 * 
	 * @param p
	 * @param key
	 * @return
	 */
	public int loadInt(String key) {
		String name = getProperty(key);// , EmptyString
		if (name == null) {
			// System.err.println(key);
			return 0;
		}

		int value = 0;
		if (name.length() == 0)
			return value;
		try {
			value = Integer.valueOf(name);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * Persist int
	 * 
	 * @param p
	 * @param key
	 * @param value
	 */
	public void persistInt(String key, int value) {
		setProperty(key, String.valueOf(value));
	}

	/**
	 * @param name
	 * @return Get the name's corresponding Locale
	 */
	public static final Locale forLocale(String name) {
		String[] strs = name.split("_");
		switch (strs.length) {
		case 1:
			return new Locale(strs[0]);
		case 2:
			return new Locale(strs[0], strs[1]);
		case 3:
			return new Locale(strs[0], strs[1], strs[2]);
		case 0:
		default:
			return null;
		}
	}

	/**
	 * @param names
	 * @return Batch get the names' corresponding Locale
	 */
	public static final List<Locale> forNames(String[] names) {
		List<Locale> tls = new LinkedList<Locale>();
		for (String ln : names) {
			Locale l = PropertiesUtil.forLocale(ln);
			if (l != null)
				tls.add(l);
		}
		return tls;
	}

	/**
	 * @param p
	 * @param key
	 * @param pt
	 *            May contain default values; if null, will create default (0, 0)
	 * @return
	 */
	public Point loadPoint(String key, Point pt) {
		if (pt == null)
			pt = new Point(0, 0);
		String v = getProperty(key, EmptyString);
		String[] xy = v.split(",");
		if (xy.length >= 2) {
			try {
				int x = Integer.parseInt(xy[0]);
				int y = Integer.parseInt(xy[1]);
				pt.x = x;
				pt.y = y;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return pt;
	}

	public void persistPoint(String key, Point pt) {
		setProperty(key, pt.x + "," + pt.y);
	}

	/**
	 * @param p
	 * @param key
	 * @param length
	 *            >0: used to determine the required array size
	 * @return
	 */
	public int[] loadInts(String key, int length) {
		String v = getProperty(key, EmptyString);
		String[] ss = v.split(",");
		if (length > 0 && ss.length < length)
			return EmptyIntArray;
		int[] ints = new int[length];
		try {
			for (int i = 0; i < length; i++) {
				ints[i] = Integer.parseInt(ss[i]);
			}
			return ints;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return EmptyIntArray;
		}
	}

	public void persistInts(String key, int[] ints) {
		StringBuilder sb = new StringBuilder();
		if (ints != null && ints.length != 0) {
			for (int i : ints) {
				sb.append(i);
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		setProperty(key, sb.toString());
	}

	public byte[] loadBytes(String key, int length) {
		String v = getProperty(key, EmptyString);
		String[] ss = v.split("\\.");
		if (length > 0 && ss.length < length)
			return EmptybyteArray;
		byte[] bytes = new byte[length];
		try {
			for (int i = 0; i < length; i++) {
				int tmp = Integer.parseInt(ss[i]);
				bytes[i] = (byte) tmp;
			}
			// for(byte b:bytes){
			// System.out.print(b+",");
			// }
			// System.out.println();
			return bytes;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return EmptybyteArray;
		}
	}

	public void persistBytes(String key, byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		if (bytes != null && bytes.length != 0) {
			for (byte i : bytes) {
				sb.append(i & 0xff);//
				sb.append(".");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		setProperty(key, sb.toString());
		// System.out.println(sb);
	}

	public void persistMeasureElem(LinkedList<MeasureElem> measureQueue) {
		LinkedList<Integer> measureDelayCode = new LinkedList<Integer>();
		for (MeasureElem me : measureQueue) {
			if (me.on)
				measureDelayCode.add((Integer) me.idx);
		}
		persistIntegerList("MeasureDelayCode", measureDelayCode, ",");
	}

}