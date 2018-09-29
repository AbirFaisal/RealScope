package com.owon.uppersoft.dso.global;

import com.owon.uppersoft.dso.view.MainWindow;

/**
 * Run the platform, the upper architecture is as abstract as possible without modifying and replacing
 */
public class Platform {
	static PrincipleFactory principleFactory;
	private static WorkBench workBench;

	public static final void launch(PrincipleFactory pf) {
		principleFactory = pf;
		workBench = null;
		try {
			workBench = pf.createWorkBench();
			workBench.join();
		} catch (Throwable e) {
			e.printStackTrace();
			// 方便测试中捕获异常，确定处理
			System.out.println(e.getMessage());
		}
	}

	public static final void relaunch() {
		launch(principleFactory);
	}

	/**
	 * @return Main frame
	 */
	public static final MainWindow getMainWindow() {
		if (workBench == null)
			return null;
		return workBench.getMainWindow();
	}

	/**
	 * @return Core control
	 */
	public static final ControlApps getControlApps() {
		if (workBench == null)
			return null;
		return workBench.getControlApps();
	}

	/**
	 * @return Data model
	 */
	public static final DataHouse getDataHouse() {
		if (workBench == null)
			return null;
		return workBench.getDataHouse();
	}

	/**
	 * @return Control manager
	 */
	public static final ControlManager getControlManager() {
		if (workBench == null)
			return null;
		return workBench.getControlManager();
	}

	/**
	 * @return 核心控制器
	 */
	public static final CoreControl getCoreControl() {
		if (workBench == null)
			return null;
		return getControlManager().getCoreControl();
	}

	public interface PrincipleFactory {
		WorkBench createWorkBench();
	}
}
