package com.owon.uppersoft.dso.global;

/**
 * OperateBlock加入到dm获取(smart)、自校正、自动设置中
 *
 */
public class OperateBlocker {
	boolean isBlock = false;

	public void block() {
		isBlock = true;
	}

	public void kickThrough() {
		isBlock = false;
	}

	public boolean isBlock() {
		return isBlock;
	}
}