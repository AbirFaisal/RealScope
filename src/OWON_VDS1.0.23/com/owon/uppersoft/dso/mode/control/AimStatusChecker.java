package com.owon.uppersoft.dso.mode.control;

public interface AimStatusChecker {
	/**
	 * 验证的方法一般为：
	 * 
	 * 当前的索引值到达最大边界值时，delta为正，返回Max
	 * 
	 * 当前的索引值到达最小边界值时，delta为负，返回Min
	 * 
	 * 如果当前值不是边界值，对其的delta修改后可以自动纠正到合适范围内，仍然可以返回Pass，表示delta可被接受处理
	 * 
	 * @param delta
	 * @return
	 */
	AimStatus validateDeltaIndex(int delta);
}