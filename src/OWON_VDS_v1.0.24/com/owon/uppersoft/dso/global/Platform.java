package com.owon.uppersoft.dso.global;

import com.owon.uppersoft.dso.view.MainWindow;

/**
 * Run the platform, the upper architecture is as abstract as possible without modifying and replacing
 * 
 */
public class Platform {

	static PrincipleFactory principleFactory;


	public interface PrincipleFactory {
		WorkBench createWorkBench();
	}


	public static final void launch(PrincipleFactory ff) {
		principleFactory = ff;
		wb = null;
		try {
			wb = ff.createWorkBench();
			wb.join();
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println(e);
		}
	}

	public static final void relaunch() {
		launch(principleFactory);
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
	 * @return Core controller
	 */
	public static final CoreControl getCoreControl() {
		if (wb == null)
			return null;
		return getControlManager().getCoreControl();
	}

	private static WorkBench wb;
}
