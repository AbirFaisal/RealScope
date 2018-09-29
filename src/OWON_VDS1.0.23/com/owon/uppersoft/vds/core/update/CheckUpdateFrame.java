package com.owon.uppersoft.vds.core.update;

import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import com.owon.uppersoft.vds.core.update.download.DownloadFile;
import com.owon.uppersoft.vds.ui.dialog.ProgressMonitorDialog;
import com.owon.uppersoft.vds.util.FileUtil;

public class CheckUpdateFrame implements Runnable {
	public static final String ERROR_INTERNET_CONNECTION = "Error.InternetConnection";
	public static final String INFO_COMPARE_VERSION = "Info.CompareVersion";
	public static final String INFO_DOWN_MANUAL = "Info.DownManual";
	public static final String INFO_NEWEST_VERSION = "Info.NewestVersion";
	public static final String INFO_PROMPT_UPDATE = "Info.PromptUpdate";
	public static final String INFO_DO_CANCEL = "Info.DoCancel";
	public static final String INFO_DETECT_SERVERS = "Info.DetectServers";
	public static final String INFO_PROGRESS_TITLE = "Info.ProgressTitle";
	
	private IUpdatable iu;
	private Runnable r;
	private Thread thd;
	private String terminatMes, cancelMes;
	private ProgressMonitorDialog pbar;

	public CheckUpdateFrame(IUpdatable iu) {
		this.iu = iu;
		pbar = new ProgressMonitorDialog(0, 100);
		localize();
	}

	public void toFront() {
		if (pbar != null)
			pbar.toFront();
		if (uf != null)
			uf.toFront();
	}

	public void run() {
		try {
			pbar.setValue(0);
			onCancel(INFO_DETECT_SERVERS, 5);
			final UpdateDetection ud = new UpdateDetection(iu);
			/* 获取可能的更新 */
			final String url = ud.detectServers();
			if (url == null) {
				/* 在更新机制失效(网络故障或无可用xml配置文件)的情况下提示手动下载新版本程序 */
				onTerminated(ERROR_INTERNET_CONNECTION);// 检查网络连接是否可用
			}

			onCancel(INFO_COMPARE_VERSION, 20);
			/* 通过xml配置文件完成版本检测和更新 */
			boolean flag = ud.isUpdate();
			onCancel("", 40);
			if (!flag) {
				onTerminated(INFO_DOWN_MANUAL);// 当前版本不支持在线更新，请手动
			}

			final List<DownloadFile> urls = ud.getDownloadFileURLs();
			if (urls.size() == 0) {
				onCancel("", 60);
				FileUtil.deleteFile(ud.getLocalUpdateXML());
				onCancel("", 80);
				onTerminated(INFO_NEWEST_VERSION);
			} else {
				/* 需要下载更新 */
				onCancel(INFO_PROMPT_UPDATE, 80);
				// 若要单独测试更新模块，应该去掉该if判断
				boolean isMainExist = iu.getWindow()
						.isDisplayable();
				if (isMainExist) {
					r = new Runnable() {
						public void run() {
							uf = new UpdateFrame(ud, url, urls);
						}
					};
					thd = new Thread(r);
					thd.start();
				}
				return;
			}
		} catch (Exception e) {
			// e.printStackTrace();
		} finally {
			pbar.close();
		}

	}

	protected void onTerminated(final String key) throws Exception {
		r = new Runnable() {
			public void run() {
				terminatMes = iu.bundle().getString(key);
				JOptionPane.showMessageDialog(null, terminatMes);
				iu.notifyDestroy();// UpdateAction的对象设为空
				pbar.close();
			}
		};
		thd = new Thread(r);
		thd.start();
		throw new Exception();// abort
	}

	/**
	 * 任务执行信息更新，并检查是否取消
	 * 
	 * @param pbar
	 * @param key
	 * @param value
	 * @throws Exception
	 *             取消则抛出
	 */
	protected void onCancel(String key, int value) throws Exception {
		pbar.setValue(value);

		if (key != null && key.length() != 0) {
			cancelMes = iu.bundle().getString(key);
			pbar.setNote(cancelMes);
		}
		if (!pbar.isDisplayable()) {
			if (uf == null)
				iu.notifyDestroy();// UpdateAction的对象设为空

			thd.join();
			r = null;
			pbar.close();
			throw new Exception();// abort
		}
		Thread.sleep(1400);
	}

	private UpdateFrame uf;

	public UpdateFrame getUpdateFrame() {
		return uf;
	}

	public CheckUpdateFrame getCheckUpdateFrame() {
		return this;
	}

	public void localize() {
		ResourceBundle rb = iu.bundle();
		pbar.setTitle(rb.getString(INFO_PROGRESS_TITLE));
		pbar.setMessage(rb.getString(INFO_DETECT_SERVERS));
		pbar.setButtonText(rb.getString(INFO_DO_CANCEL));
	}

}