package com.owon.uppersoft.vds.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * ZipUtil, encoding issues are not yet resolved (English is used throughout); consider Apache Ant's org.apache.tools.zip.*,
 * 
 */
public class ZipUtil {
	public static final int BufferSize = 2 << 10;

	/**
	 * Extract the files in the zipFile archive, forcibly overwriting into the dest folder
	 * 
	 * @param zipFile
	 * @param destName
	 */
	public static void unzip(File zipFile, String destName) {
		File f = new File(destName);
		unzip(zipFile, f);
	}

	/**
	 * Extract the files in the zipFile archive, forcibly overwriting into the dest folder
	 * 
	 * @param zipFile
	 * @param dest
	 */
	public static void unzip(File zipFile, File dest) {
		dest.mkdirs();

		try {
			ZipFile zf = new ZipFile(zipFile);
			Enumeration<? extends ZipEntry> zfe = zf.entries();

			byte[] buffer = new byte[BufferSize];

			while (zfe.hasMoreElements()) {
				ZipEntry ze = zfe.nextElement();
				/* File name encoding not yet handled */
				String name = ze.getName();
				File file = new File(dest, name);
				/* Delete a file that may already exist */
				FileUtil.deleteFile(file);
				if (ze.isDirectory()) {
					file.mkdirs();
				} else {
					file.createNewFile();

					InputStream is = zf.getInputStream(ze);
					FileOutputStream fos = new FileOutputStream(file);
					int l;
					while ((l = is.read(buffer)) != -1) {
						fos.write(buffer, 0, l);
					}
					fos.close();
					is.close();
				}
			}
			/* Close ZipFile */
			zf.close();
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		ZipUtil.unzip(new File("c:/a.zip"), new File("c:/b"));
	}
}