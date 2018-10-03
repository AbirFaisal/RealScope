package com.owon.uppersoft.vds.device.interpret;

import com.owon.uppersoft.vds.core.aspect.base.Logable;

public interface CMDResponser {

	void onResponse(byte[] buf2, int resNum, Logable vl);
}
