package com.owon.uppersoft.vds.util;

/**
 * StringPool，文本串池，存放通用的文本串
 */
public interface StringPool {
	public static final String DOCFileFormatExtension ="docs";
	public static final String XMLFileFormatExtension = "xml";
	public static final String CHMFileFormatExtension = "chm";
	public static final String LAFileFormatExtension = "bin";
	public static final String BINFileFormatExtension = "bin";
	public static final String BMPFileFormatExtension = "bmp";
	public static final String GIFFileFormatExtension = "gif";
	public static final String PNGFileFormatExtension = "png";
	public static final String XLSFileFormatExtension = "xls";
	public static final String XLSFileFormatExtensionDesc = "Microsoft Office Excel Workbook(*.xls)";
	public static final String TXTFileFormatExtension = "txt";
	public static final String TXTFileFormatExtensionDesc = "Text(*.txt)";
	public static final String CSVFileFormatExtension = "csv";
	public static final String CSVFileFormatExtensionDesc = "Comma Separated Value Text(*.csv)";

	/** useful Strings */
	public static final String EmptyString = "";
	public static final String SemicolonString = ";";
	public static final String LINE_SEPARATOR = "\n";
	public static final String TRUE_STRING = "1";
	public static final String FALSE_STRING = "0";
	public static final String ONE = "1";
	public static final String ZERO = "0";
	public static final String[] EmptyStringArray = new String[] { EmptyString };
	public static final String StarString = "*";
	public static final String DotString = ".";
	public static final String StarDotString = "*.";
	public static final String SpaceString = " ";
	public static final String Underline = "_";
	public static final String Slash = "/";
	public static final String BackSlash = "\\";
	public static final int[] EmptyIntArray = {};
	public static final byte[] EmptybyteArray = {};

	public static final String GBK = "GBK";
	public static final String UTF8EncodingString = "UTF-8";
	public static final String ASCIIString = "ASCII";

}