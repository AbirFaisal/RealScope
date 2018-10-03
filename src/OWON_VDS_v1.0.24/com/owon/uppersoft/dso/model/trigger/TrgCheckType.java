package com.owon.uppersoft.dso.model.trigger;

import com.owon.uppersoft.dso.source.comm.CProtocol;
import com.owon.uppersoft.dso.util.PropertiesItem;

public enum TrgCheckType {
	NotOver(-1, ""), //
	VoltsenseOver(CProtocol.trg_voltsense, PropertiesItem.UPDATE_VOLTSENSE), //
	UppOver(CProtocol.trg_slope_uppest, PropertiesItem.UPDATE_UPP_LOW), //
	LowOver(CProtocol.trg_slope_lowest, PropertiesItem.UPDATE_UPP_LOW)//
	;

	public final int code;
	public final String fireItem;

	private TrgCheckType(int v, String fi) {
		code = v;
		fireItem = fi;
	}

}
