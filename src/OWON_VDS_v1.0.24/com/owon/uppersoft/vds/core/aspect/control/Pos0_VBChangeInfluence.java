package com.owon.uppersoft.vds.core.aspect.control;

public interface Pos0_VBChangeInfluence {
	void resetPersistence();

	void notifyChannelUpdate();

	/**
	 * Only changes along with voltage scale changes
	 * 
	 * @param chlidx
	 *            channel index
	 * @param pos0
	 *            zero position, with the vertical center of the screen as zero value
	 * @param vb0
	 *            voltage scale before the change
	 * @param vb1
	 *            voltage scale after the change
	 */
	void thredshold_voltsense_ByVoltBase(int chlidx, int pos0, int vb0,
			int vb1, Runnable r, int destpos0);

	/**
	 * Changes along with zero position changes
	 * 
	 * @param chlidx
	 *            channel index
	 * @param dp
	 *            zero position change difference
	 */
	Runnable thredshold_voltsense_ByPos0(int chlidx, int dp, boolean commit);

}