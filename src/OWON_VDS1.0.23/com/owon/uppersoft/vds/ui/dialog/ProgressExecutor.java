package com.owon.uppersoft.vds.ui.dialog;

/**
 * ProgressExecutor，为执行任务的实际体，提供外部方法供控制执行过程
 * 
 */
public interface ProgressExecutor {
	/**
	 * @param po
	 *            进度观察者
	 */
	void execute(ProgressObserver po);

	/**
	 * 在ui线程调用，afterCancel也是执行ui相关内容
	 * 
	 * @param afterCancel
	 */
	void cancel(Runnable afterCancel);
}