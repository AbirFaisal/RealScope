package com.owon.uppersoft.vds.core.update.download;

import java.io.File;

import com.owon.uppersoft.vds.util.format.SFormatter;

public class DownloadFile {

	private String relativePath;
	private long retrived;
	private long fileLength;
	private File localTempFile;
	private boolean isZip;
	private String destFile;

	/**
	 * @param destFile
	 *            更新的最终目标文件名，若是文件夹压缩包则为要解压到的文件夹名
	 * @param relativePath
	 *            远程url相对路径，也被作为本地下载后的临时文件名
	 */
	public DownloadFile(String destFile, String relativePath) {
		this.relativePath = relativePath;
		this.destFile = destFile;
		retrived = 0;
	}

	/**
	 * @return 下载最终对应的目标文件
	 */
	public String getDestFile() {
		return destFile;
	}

	public boolean isZip() {
		return isZip;
	}

	public void setZip(boolean isZip) {
		this.isZip = isZip;
	}

	/**
	 * @return 远程和本地同时使用的单个文件名，对于文件夹则是zip的文件名
	 */
	public String getRelativePath() {
		return relativePath;
	}

	public long getRetrived() {
		return retrived;
	}

	public void addToRetrived(long incr) {
		retrived += incr;
	}

	public long getFileLength() {
		return fileLength;
	}

	public void setFileLength(long length) {
		this.fileLength = length;
	}

	public void setLocalTempFile(File localTempFile) {
		this.localTempFile = localTempFile;
	}

	/**
	 * @return 下载在本地temp中的临时文件
	 */
	public File getLocalTempFile() {
		return localTempFile;
	}

	public String getPercent() {
		if (fileLength <= 0)
			return "0%";
		double percent = (double) retrived / fileLength * 100;
		return SFormatter.UIformat("%d %%", (int) percent);
	}
}