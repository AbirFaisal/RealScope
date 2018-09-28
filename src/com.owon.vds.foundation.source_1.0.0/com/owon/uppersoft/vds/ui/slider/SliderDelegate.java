package com.owon.uppersoft.vds.ui.slider;

/**
 * SliderDelegate
 * 
 */
public interface SliderDelegate extends SliderDisposeDelegate{
	public static final int BtnStatusNO = 0, BtnStatus0 = 1, BtnStatus50 = 2,
			BtnStatusBoth = 3;

//	/**
//	 * 操作结束
//	 */
	void actionOff();
//
//	/**
//	 * 关闭释放
//	 */
//	void onDispose();

	/**
	 * 用于持续性的数值改变，可以包含默认值(如0)的改变，但是不会触发actionOff()
	 * 
	 * @param v
	 */
	void valueChanged(int oldV, int newV);

//	/**
//	 * "50%"的瞬间设置，不会触发actionOff()，但处理的时候含有了endSync的部分
//	 */
	void on50percent();

}
