package com.owon.uppersoft.dso.wf.common.dm;

import java.math.BigDecimal;

import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.wf.dm.DMDataInfo;

public interface IDiluteInfoUnit extends InfoUnit {

//	void setCenterPoint(int i, int j);
//
//	void retrieveBuf(int pos, int i, int len, IntBuffer handlebuf,
//			int filePointer, CByteArrayInputStream cba);

	void tbTranslate2C(BigDecimal bdtb, BigDecimal nexbdtb,
			CompressInfoUnit ciu, int idx);

	void fakeIn(int scpi, int tbidx);

	void setPlug(boolean b);

	void reinit();

	void initLoad(DMDataInfo cdi, int tbidx, MachineType mt);

}