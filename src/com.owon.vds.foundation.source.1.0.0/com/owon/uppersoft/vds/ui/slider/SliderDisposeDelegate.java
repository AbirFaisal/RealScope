package com.owon.uppersoft.vds.ui.slider;

public interface SliderDisposeDelegate {

	/**
	 * 操作结束
	 */
	//void actionOff();

	/**
	 * 关闭释放
	 */
	void onDispose();

	/**
	 * "50%"的瞬间设置，不会触发actionOff()，但处理的时候含有了endSync的部分
	 */
	//void on50percent();

}
