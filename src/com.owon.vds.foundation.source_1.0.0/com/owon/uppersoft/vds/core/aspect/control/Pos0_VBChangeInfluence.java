package com.owon.uppersoft.vds.core.aspect.control;

public interface Pos0_VBChangeInfluence {
	void resetPersistence();

	void notifyChannelUpdate();

	/**
	 * 限随电压档位改变而改变
	 * 
	 * @param chlidx
	 *            通道序号
	 * @param pos0
	 *            零点位置，以屏幕竖直中心为零值
	 * @param vb0
	 *            改变前的电压档位
	 * @param vb1
	 *            改变后的电压档位
	 */
	void thredshold_voltsense_ByVoltBase(int chlidx, int pos0, int vb0,
			int vb1, Runnable r, int destpos0);

	/**
	 * 随零点位置改变而改变
	 * 
	 * @param chlidx
	 *            通道序号
	 * @param dp
	 *            零点变化差
	 */
	Runnable thredshold_voltsense_ByPos0(int chlidx, int dp, boolean commit);

}