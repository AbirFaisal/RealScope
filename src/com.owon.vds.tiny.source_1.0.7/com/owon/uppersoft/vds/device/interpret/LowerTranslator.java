package com.owon.uppersoft.vds.device.interpret;

public interface LowerTranslator {
	
	int translate2PosValue(int chl, int pos, int vb);

	int translate2VBValue(int chl, int vb);

	/**
	 * @param level
	 * @param rnf
	 *            Raise=0 Fall=1
	 * @return
	 */
	int getLevelArg(int level, int rnf);

}