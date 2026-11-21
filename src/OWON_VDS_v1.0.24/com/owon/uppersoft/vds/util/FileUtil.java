package com.owon.uppersoft.vds.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import javax.swing.JFileChooser;

/**
 * FileUtil, file utility class
 */
public class FileUtil {

	public static String getExtension(File f) {
		return (f != null) ? getExtension(f.getName()) : "";
	}

	public static String getExtension(String filename) {
		return getExtension(filename, "");
	}

	public static String getExtension(String filename, String defExt) {
		if ((filename != null) && (filename.length() > 0)) {
			int i = filename.lastIndexOf(".");
			if ((i > 0) && (i < (filename.length() - 1))) {
				return filename.substring(i + 1);
			}
			return defExt;
		}
		return defExt;
	}

	public static void checkPath(String path) {
		checkPath(new File(path));
	}

	public static void checkPath(File file) {
		if (file.exists())
			return;

		File parent = file.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
	}

	/** This method must be used after adding a filter,jfc.addChoosableFileFilter(MyFileFilter filter); */
	public static File checkFileSuffix(JFileChooser jfc, String format) {
		File f = jfc.getSelectedFile();

		/** TODO Check whether the filter is usable; if not, return f */
		/** If the file ends with the selected extension, true; use the original name */
		boolean hasSuffix = f.getName().toUpperCase()
				.endsWith("." + format.toUpperCase());
		if (!hasSuffix) {
			// Otherwise, append the selected extension
			f = new File(f.getAbsolutePath() + "." + format);
		}
		return f;
	}

	public static File cutFileSuffix(JFileChooser jfc, String format) {
		File f = jfc.getSelectedFile();

		/** If the file ends with the selected extension, true */
//		boolean hasSuffix = f.getName().toUpperCase()
//				.endsWith("." + format.toUpperCase());
//		if (hasSuffix) {
			// If true, remove the selected extension
			int endIndex=f.getAbsolutePath().toUpperCase().lastIndexOf("."+ format.toUpperCase());
			if(endIndex<0)
				return f;
			
			String ff=f.getAbsolutePath().substring(0, endIndex);
			f = new File(ff);//f.getAbsolutePath()+
//		}
		return f;
	}

	/**
	 * Copy a file or folder
	 * 
	 * @param source
	 * @param dest
	 * @throws IOException
	 */
	public static void copyFile(File source, File dest) throws IOException {
		FileInputStream input = new FileInputStream(source);
		try {
			FileOutputStream output = new FileOutputStream(dest);
			try {
				byte[] buffer = new byte[1 << 13];// 8192
				int n = 0;
				while (-1 != (n = input.read(buffer))) {
					output.write(buffer, 0, n);
				}
			} finally {
				try {
					if (output != null) {
						output.close();
					}
				} catch (IOException ioe) {
				}
			}
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException ioe) {
			}
		}

	}

	public static boolean copyFile2(File source, File dest) {
		try {
			FileInputStream input = new FileInputStream(source);
			FileOutputStream output = new FileOutputStream(dest);
			FileChannel in = input.getChannel();
			FileChannel out = output.getChannel();

			out.transferFrom(in, 0, in.size());
			input.close();
			output.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}
	
	/**
	 * Take the part of file source1 from 0 to s1_End and the part of source2 from s2_skip+1 to the end of the file, and merge the two into one dest file
	 * 
	 * @param source1
	 * @param source2
	 * @param s1_End
	 * @param s2_skip
	 * @param dest
	 * @throws IOException
	 */
	public static void cutFiles2one(File source1, File source2, int s1_End,
			int s2_skip, File dest) throws IOException {
		FileInputStream input1 = new FileInputStream(source1);
		FileInputStream input2 = new FileInputStream(source2);
		try {
			FileOutputStream output = new FileOutputStream(dest);
			try {
				byte[] buffer = new byte[1 << 20];// 8192
				int n = 0;
				// while (-1 != (n = input1.read(buffer,0,len))) {
				input1.read(buffer, 0, s1_End);
				output.write(buffer, 0, s1_End);
				// }

				input2.skip((long) s2_skip);
				while (-1 != (n = input2.read(buffer))) {
					// System.out.println("n:" + n);
					output.write(buffer, 0, n);
				}
			} finally {
				try {
					if (output != null) {
						output.close();
					}
				} catch (IOException ioe) {
				}
			}
		} finally {
			try {
				if (input1 != null)
					input1.close();
				if (input2 != null)
					input2.close();
			} catch (IOException ioe) {
			}
		}

	}

	/**
	 * Delete a file or folder
	 * 
	 * @param path
	 */
	public static void deleteFile(String path) {
		deleteFile(new File(path));
	}

	/**
	 * Delete a file or folder
	 * 
	 * @param file
	 * @return
	 */
	public static void deleteFile(File file) {
		if (!file.exists())
			return;
		if (file.isFile()) {
			file.delete();
			return;
		}

		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				deleteFile(f);
			}
			file.delete();
			return;
		}
	}

	/**
	 * Overwrite a file or folder, forcing deletion
	 * 
	 * @param dest
	 * @return
	 */
	public static void replaceFile(File src, File dest) {
		if (dest.exists()) {
			deleteFile(dest);
		}
		src.renameTo(dest);
	}

	/**
	 * Overwrite a file or folder into the specified directory, forcing deletion
	 * 
	 * @param destP
	 * @param src
	 */
	public static void replaceInFile(File src, File destP) {
		File dest = new File(destP, src.getName());
		replaceFile(src, dest);
	}

	public static void main(String[] args) {
		TimeMeasure tm = new TimeMeasure();
		tm.start();
		deleteFile("F:/Alisoft");
		tm.stop();
		System.out.println(tm.measure());
		// try {
		// cutFiles2one(new File("1.bin"), new File("2.bin"), 193343, 193343,
		// new File("dest3.bin"));
		// System.out.println("dest3 is ok");
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	public static final String getFileCanonicalPath(File f) {
		String p = "";
		try {
			p = f.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
	}

}
