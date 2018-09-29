package com.owon.uppersoft.dso.global;

import javax.swing.JOptionPane;

import com.owon.uppersoft.dso.view.MainWindow;

/**
 * Run the platform, the upper architecture is as abstract as possible without modifying and replacing
 * 
 */
public class Platform {
	public interface PrincipleFactory {
		WorkBench createWorkBench();
	}

	static PrincipleFactory fff;

	public static final void launch(PrincipleFactory ff) {
		fff = ff;
		wb = null;
		try {
			wb = ff.createWorkBench();
			wb.join();
		} catch (Throwable e) {
			e.printStackTrace();
			// 方便测试中捕获异常，确定处理
			System.out.println(e.getMessage());
			//JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}

	public static final void relaunch() {
		launch(fff);
	}

	/**
	 * @return Main frame
	 */
	public static final MainWindow getMainWindow() {
		if (wb == null)
			return null;
		return wb.getMainWindow();
	}

	/**
	 * @return Core control
	 */
	public static final ControlApps getControlApps() {
		if (wb == null)
			return null;
		return wb.getControlApps();
	}

	/**
	 * @return Data model
	 */
	public static final DataHouse getDataHouse() {
		if (wb == null)
			return null;
		return wb.getDataHouse();
	}

	/**
	 * @return Control manager
	 */
	public static final ControlManager getControlManager() {
		if (wb == null)
			return null;
		return wb.getControlManager();
	}

	/**
	 * @return 核心控制器
	 */
	public static final CoreControl getCoreControl() {
		if (wb == null)
			return null;
		return getControlManager().getCoreControl();
	}

	private static WorkBench wb;
}
