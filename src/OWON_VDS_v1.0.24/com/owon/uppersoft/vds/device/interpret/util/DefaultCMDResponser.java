package com.owon.uppersoft.vds.device.interpret.util;

import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.device.interpret.CMDResponser;
import com.owon.uppersoft.vds.util.ArrayLogger;
import com.owon.uppersoft.vds.util.format.EndianUtil;

public class DefaultCMDResponser implements CMDResponser {

	@Override
	public void onResponse(byte[] resbuf, int resNum, Logable lg) {
		int res = EndianUtil.nextIntL(resbuf, 1);

		String s = "[Response: ] resNum: " + resNum + ", value: " + res;
		lg.logln(s);
		ArrayLogger.outArray2LogableHex(lg, resbuf, 0, resbuf.length);

		handleResponse(res);
	}

	protected void handleResponse(int res) {
	}
}