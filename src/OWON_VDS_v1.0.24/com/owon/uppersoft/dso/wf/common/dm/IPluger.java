package com.owon.uppersoft.dso.wf.common.dm;

import java.nio.IntBuffer;

import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;

public interface IPluger {

	double getGap();

	/**
	 * Interpolation for non-initial loading
	 * 
	 * @param iPos
	 * @param len
	 * @param datalen
	 * @param screendatalen
	 * @param filePointer
	 * @param shouldReverse
	 * @param loadPos0
	 * @param handlebuf
	 * @param cba
	 */
	void processPlug(int iPos, int len, int datalen, int screendatalen,
			int filePointer, boolean shouldReverse, int loadPos0,
			IntBuffer handlebuf, CByteArrayInputStream cba);

	/**
	 * Interpolation for initial loading
	 * 
	 * @param datalen
	 * @param screendatalen
	 * @param filePointer
	 * @param shouldReverse
	 * @param loadPos0
	 * @param handlebuf
	 * @param cba
	 * @param halfpixs
	 * @return the full-screen point count after interpolation
	 */
	int processPlugForInit(int datalen, int screendatalen, int filePointer,
			boolean shouldReverse, int loadPos0, IntBuffer handlebuf,
			CByteArrayInputStream cba, int halfpixs);

	boolean canPaintSinePlug(int screendatalen);

	boolean isPlug();

	void setPlug(boolean plug);

	/**
	 * Extract the interpolation info and return the correct screen start position, excluding the extra data fetched for trigger pull
	 * 
	 * @param cdi
	 * @param li
	 * @return the start position without extra data fetched
	 */
	int initPlugInfo(DMDataInfo cdi, MachineType mt);

	int getDrawModeFromLength(int screenADCLen);

}