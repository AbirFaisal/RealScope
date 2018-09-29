package com.owon.uppersoft.vds.ui.slider;

import java.awt.Graphics2D;

/**
 * setValue()和getValue()方法不开放
 * 
 * 50%的对应值目前无法设置到slider内部而是在外部改变而是由SliderDelegate在外部改变
 * 
 */
public interface ISliderView {

	/**
	 * 设置为默认，具体含义由模型自行定义
	 */
	void setDefault();

	/**
	 * 调整情况下的增量设置
	 * 
	 * @param delta
	 */
	void adjustAdd(int delta);

	/**
	 * 绘制内容
	 * 
	 * @param g
	 */
	void paintSelf(Graphics2D g);

	/**
	 * 操作释放，如鼠标释放等，仅代表拖拽结束，不是界面关闭或不再操作
	 * 
	 * 用于通过adjustAdd(delta)产生单任务同步后(鼠标拖拽的情况 & 鼠标点击的情况)的停止
	 */
	void actionOff();

}
