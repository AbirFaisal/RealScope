package com.owon.uppersoft.vds.core.update;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.core.update.download.DownloadFile;
import com.owon.uppersoft.vds.core.update.download.UpdateTask;

/**
 * UpdateFrame，下载更新界面
 */
public class UpdateFrame extends AbstractTableModel implements Localizable {
	public static final String INFO_TITLE = "Info.Title";
	public static final String INFO_DOWNLOADING = "Info.Downloading";
	public static final String INFO_DO_UPDATE = "Info.DoUpdate";
	public static final String INFO_DO_CANCEL = "Info.DoCancel";
	public static final String ERROR_INTERNET_CONNECTION = "Error.InternetConnection";
	public static final String INFO_RESTART = "Info.Restart";
	public static final String INFO_DOWNLOAD_PROGRESS = "Info.DownloadProgress";
	public static final String INFO_DOWNLOAD_FILE = "Info.DownloadFile";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel txtUrl;
	private Thread taskThd;
	private UpdateTask task;
	private List<DownloadFile> files;
	private UpdateDetection ud;
	private JButton cancelButton, updateButton;
	private JTable table;
	private JScrollPane tvscrollpane;
	protected JFrame updateframe;
	private IUpdatable iu;

	public UpdateFrame(UpdateDetection ud, String url, List<DownloadFile> files) {
		this.ud = ud;
		this.iu = ud.getUpdatable();
		this.files = files;
		createContents();
		localize(iu.bundle());
		task = new UpdateTask(this, url, files);
		taskThd = new Thread(task);
	}

	private void createContents() {
		updateframe = new JFrame();
		// 若要单独测试更新功能，应该去掉该监听。
		iu.getWindow().addWindowListener(
				new WindowAdapter() {
					public void windowClosed(WindowEvent e) {
						System.exit(0);
					}
				});
		updateframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		updateframe.setLocationRelativeTo(null);
		updateframe.setPreferredSize(new Dimension(430, 250));

		Container cp = updateframe.getContentPane();
		final BorderLayout borderLayout = new BorderLayout();
		cp.setLayout(borderLayout);

		txtUrl = new JLabel();
		txtUrl.setPreferredSize(new Dimension(430, 30));

		table = new JTable(this);
		table.setShowHorizontalLines(false);
		table.setShowVerticalLines(false);
		table.setRowHeight(25);
		table.getTableHeader().setReorderingAllowed(false);
		DefaultTableCellRenderer hr = (DefaultTableCellRenderer) table
				.getTableHeader().getDefaultRenderer();
		hr.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);

		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(350);
		columnModel.getColumn(1).setPreferredWidth(100);

		tvscrollpane = new JScrollPane(table);
		updateButton = new JButton();
		updateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateButton.setEnabled(false);
				taskThd.start();
			}
		});
		cancelButton = new JButton();
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					task.setCancel();
					taskThd.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				iu.notifyDestroy();
				updateframe.dispose();
			}
		});

		JPanel buttonPane = new JPanel();
		// buttonPane.setLayout(new FlowLayout());
		buttonPane.add(updateButton);
		buttonPane.add(cancelButton);

		cp.add(txtUrl, BorderLayout.NORTH);
		cp.add(tvscrollpane, BorderLayout.CENTER);
		cp.add(buttonPane, BorderLayout.SOUTH);
		updateframe.setVisible(true);
		updateframe.pack();

		updateframe.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				task.setCancel();
				if (taskThd.isAlive()) {// || taskThd != null) {
					try {
						taskThd.join();// * 等待task线程结束
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				iu.notifyDestroy();
			}

		});
	}

	public void localize(ResourceBundle bundle) {
		updateframe.setTitle(bundle.getString(INFO_TITLE));
		txtUrl.setText(bundle.getString(INFO_DOWNLOADING));

		table.getColumnModel().getColumn(0).setHeaderValue(
				bundle.getString(INFO_DOWNLOAD_FILE));
		table.getColumnModel().getColumn(1).setHeaderValue(
				bundle.getString(INFO_DOWNLOAD_PROGRESS));
		updateframe.repaint();

		updateButton.setText(bundle.getString(INFO_DO_UPDATE));
		cancelButton.setText(bundle.getString(INFO_DO_CANCEL));
	}

	public void toFront() {
		updateframe.toFront();
	}

	public File getDownloadTempDir() {
		return ud.getDownloadTempDir();
	}

	/**
	 * 下载失败后
	 */
	public void downloadFailed() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (!task.isCancel()) {
					ResourceBundle bundle = iu.bundle();
					String failedMesg = bundle
							.getString(ERROR_INTERNET_CONNECTION);
					JOptionPane.showMessageDialog(updateframe, failedMesg);
				}
				iu.notifyDestroy();
				updateframe.dispose();
			}
		});
	}

	/**
	 * 下载更新完毕后开始进行更新
	 */
	public void downloadFinished() {
		/* 应该添加用户界面的提示信息 */
		/* 此处必须是异步调用，因为UI线程等待当前线程结束，而当前线程的UI调用为同步时也要等待UI线程空闲即导致死锁 */
		ResourceBundle bundle = iu.bundle();
		String restartMesg = bundle.getString(INFO_RESTART);

		int re = JOptionPane.showConfirmDialog(iu
				.getWindow(), restartMesg, null, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (re == JOptionPane.YES_OPTION) {
			SwingUtilities.invokeLater(updateNRestart);
		} else if (re == JOptionPane.NO_OPTION) {
			task.setCancel();
			iu.notifyDestroy();
			updateframe.dispose();
		} else {
			SwingUtilities.invokeLater(updateNext);
		}
	}

	private UpdateNRestart updateNRestart = new UpdateNRestart();

	class UpdateNRestart implements Runnable {
		public void run() {
			/* 判断是否关闭 */
			if (updateframe.isActive()) {
				cancelButton.setEnabled(false);
				updateframe.dispose();
			}
			/* iu = ud.getUpdatable();放在UI线程中作文件拷贝，因为拷贝完毕才能重启 */
			iu.notifyDestroy();
			iu.close();
			/* 在主程序关闭后再作解压覆盖的操作，这样可以避免影响到其它资源 */
			ud.filesUpdate(files);
			String eN = ud.getExecName();
			if (eN.length() == 0) {
				iu.startAgain();
			} else {
				try {
					Process pro = Runtime.getRuntime().exec(eN);// Program.launch(eN);
				} catch (IOException e) {
					e.printStackTrace();
				}
				;
			}
		}
	}

	private UpdateNext updateNext = new UpdateNext();

	class UpdateNext implements Runnable {
		public void run() {
			/* 判断是否关闭 */
			if (updateframe.isActive()) {

				cancelButton.setEnabled(false);
				updateframe.dispose();
			}
			/* iu = ud.getUpdatable();放在UI线程中作文件拷贝，因为拷贝完毕才能重启 */
			iu.notifyDestroy();
			/* 在主程序关闭后再作解压覆盖的操作，这样可以避免影响到其它资源 */
			ud.filesUpdate(files);
		}
	}

	// TODO
	public void setCurrentDownloadFile(final DownloadFile downloadFile,
			final URL url) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// table.transferFocus();
				// table.setLocation(null);
				// tableViewer.setSelection(new
				// StructuredSelection(downloadFile),
				// true);
			}
		});
	}

	public void updateProgress() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				table.repaint();
			}
		});
	}

	public String getColumnName(int column) {
		ResourceBundle bundle = iu.bundle();
		switch (column) {
		case 0:
			return bundle.getString(INFO_DOWNLOAD_FILE);
		case 1:
			return bundle.getString(INFO_DOWNLOAD_PROGRESS);
		default:
			return "";
		}
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return files.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		DownloadFile df = files.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return df.getRelativePath().replaceAll("owon.", "");
		case 1:
			return df.getPercent();
		default:
			break;
		}
		return "?";
	}

}
