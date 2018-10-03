package com.owon.uppersoft.dso.wf.common.dm;

import java.nio.IntBuffer;

import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;

public interface IPluger {

	double getGap();

	/**
	 * 非初始化载入时的插值
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
	 * 初始化载入时的插值
	 * 
	 * @param datalen
	 * @param screendatalen
	 * @param filePointer
	 * @param shouldReverse
	 * @param loadPos0
	 * @param handlebuf
	 * @param cba
	 * @param halfpixs
	 * @return 插值后的满屏点数
	 */
	int processPlugForInit(int datalen, int screendatalen, int filePointer,
			boolean shouldReverse, int loadPos0, IntBuffer handlebuf,
			CByteArrayInputStream cba, int halfpixs);

	boolean canPaintSinePlug(int screendatalen);

	boolean isPlug();

	void setPlug(boolean plug);

	/**
	 * 提取插值信息，返回取出拉触发多拿数据以外，正确的屏幕起始位置
	 * 
	 * @param cdi
	 * @param li
	 * @return 没有多拿数据情况下的起始位置
	 */
	int initPlugInfo(DMDataInfo cdi, MachineType mt);

	int getDrawModeFromLength(int screenADCLen);

}