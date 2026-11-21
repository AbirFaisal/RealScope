package com.owon.uppersoft.dso.global;

/**
 * OperateBlock is added to dm acquisition (smart), self-calibration, and auto setup
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