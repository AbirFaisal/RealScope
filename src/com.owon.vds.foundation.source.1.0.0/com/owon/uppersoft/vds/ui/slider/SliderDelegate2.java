package com.owon.uppersoft.vds.ui.slider;

/**
 * 在zoom中使用这个
 * 
 */
public interface SliderDelegate2 extends SliderDisposeDelegate {

	// /**
	// * 操作结束
	// */
	// void actionOff();
	//
	// /**
	// * 关闭释放
	// */
	// void onDispose();

	/**
	 * 用于持续性的数值改变"增量"，可以包含默认值(如0)的改变，但是不会触发actionOff()
	 * 
	 * @param v
	 */
	void valueIncret(int delta);

	/**
	 * 默认值(如0)的瞬间设置，会触发actionOff()，只在zoom中使用这个方法
	 */
	void onReset0();

}
