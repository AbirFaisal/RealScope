package com.owon.uppersoft.vds.core.update.download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.owon.uppersoft.vds.core.update.IUpdateReference;
import com.owon.uppersoft.vds.core.update.UpdateFrame;
import com.owon.uppersoft.vds.util.FileUtil;

/**
 * UpdateTask，更新任务，作为后台线程运行
 */
public class UpdateTask implements Runnable {

	private byte[] buf = new byte[IUpdateReference.BUFFER_SIZE];
	private boolean isCancel = false;
	private UpdateFrame frame;
	private List<DownloadFile> files;
	private String url;

	public UpdateTask(UpdateFrame frame, String url, List<DownloadFile> files) {
		this.frame = frame;
		this.url = url;
		this.files = files;
	}

	public void run() {
		/*
		 * 更新文件的url与xml文件所在的url相同
		 */
		URL server;
		try {
			server = new URL(url);
			for (DownloadFile file : files) {
				if (isCancel) {
					return;
				}
				if (!getFile(server, file)) {
					frame.downloadFailed();
					return;
				}
			}
			/* 完成下载，仍须判断是否取消 */
			if (isCancel) {
				return;
			}
			/* 仍可能取消，此处暂无好的解决方法 */
			frame.downloadFinished();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取服务器上的文件
	 * 
	 */
	private boolean getFile(URL server, DownloadFile downloadFile) {
		String relativePath = downloadFile.getRelativePath();

		try {
			BufferedInputStream bis = null;
			HttpURLConnection httpUrl = null;
			URL url = new URL(server, downloadFile.getRelativePath());
			httpUrl = (HttpURLConnection) url.openConnection();
			frame.setCurrentDownloadFile(downloadFile, url);
			/* 本地下载的临时文件仍按远程文件的相对路径存放 */
			File localFile = new File(frame.getDownloadTempDir(), relativePath);
			/* 可考虑实现断点续传 */
			httpUrl.setReadTimeout(IUpdateReference.HttpURLConnectionTimeout);
			httpUrl.connect();
			String sHeader = httpUrl.getHeaderField("Content-Length");
			if (sHeader == null) {
				return false;
			}
			long fileLength = Long.parseLong(sHeader);
			if (fileLength <= 0)
				return false;
			downloadFile.setFileLength(fileLength);

			long receive = 0;

			FileUtil.checkPath(localFile);
			RandomAccessFile raf = new RandomAccessFile(localFile, "rw");
			raf.seek(receive);

			int size = 0;
			bis = new BufferedInputStream(httpUrl.getInputStream());
			while ((size = bis.read(buf)) != -1) {
				if (isCancel) {
					raf.close();
					bis.close();
					httpUrl.disconnect();
					onCancel(localFile);
					return false;
				}
				raf.write(buf, 0, size);
				downloadFile.addToRetrived(size);

				frame.updateProgress();
				Thread.sleep(IUpdateReference.TaskDelayTime);
			}
			
			raf.close();
			bis.close();
			httpUrl.disconnect();
			if (downloadFile.getRetrived() < fileLength) {
				return false;
			}

			downloadFile.setLocalTempFile(localFile);
			return true;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * 设置http头以实现断点续传，目前不使用
	 * 
	 * @param httpUrl
	 * @param offset
	 */
	protected void backTransmitFromDisturb(HttpURLConnection httpUrl,
			long offset) {
		// 设置User-Agent
		httpUrl.setRequestProperty("User-Agent", "NetFox");
		// 设置断点续传的开始位置
		httpUrl.setRequestProperty("RANGE", "bytes=" + offset + '-');
	}

	private void onCancel(File file) {
		file.delete();
	}

	/**
	 * 取消更新
	 */
	public void setCancel() {
		isCancel = true;
	}

	public boolean isCancel() {
		return isCancel;
	}
}
