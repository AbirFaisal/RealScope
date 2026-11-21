package com.owon.uppersoft.vds.ui.dialog;

/**
 * ProgressExecutor, the actual body that executes the task, providing external methods to control the execution process
 * 
 */
public interface ProgressExecutor {
	/**
	 * @param po
	 *            progress observer
	 */
	void execute(ProgressObserver po);

	/**
	 * Called on the ui thread; afterCancel also executes ui-related content
	 * 
	 * @param afterCancel
	 */
	void cancel(Runnable afterCancel);
}