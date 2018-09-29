package com.owon.uppersoft.dso.global;

import javax.swing.JOptionPane;

import com.owon.uppersoft.dso.view.MainWindow;

/**
 * 运行平台，上层架构尽量抽象而无须修改替换
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
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}

	public static final void relaunch() {
		launch(fff);
	}

	/**
	 * @return 主框架
	 */
	public static final MainWindow getMainWindow() {
		if (wb == null)
			return null;
		return wb.getMainWindow();
	}

	/**
	 * @return 核心控制
	 */
	public static final ControlApps getControlApps() {
		if (wb == null)
			return null;
		return wb.getControlApps();
	}

	/**
	 * @return 数据模型
	 */
	public static final DataHouse getDataHouse() {
		if (wb == null)
			return null;
		return wb.getDataHouse();
	}

	/**
	 * @return 控制管理器
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
