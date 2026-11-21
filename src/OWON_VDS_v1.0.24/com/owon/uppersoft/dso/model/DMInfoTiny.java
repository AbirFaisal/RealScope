package com.owon.uppersoft.dso.model;

import com.owon.uppersoft.vds.core.aspect.control.DeepProvider;
import com.owon.uppersoft.vds.core.wf.dm.DMInfo;

public class DMInfoTiny extends DMInfo {
	public static final int FILEPROTOCOL_VDS_INVERSE = 1;// starts from 1, 0 denotes the prototype version
	public static final int FILEPROTOCOL_VDS_EXTEND = 2;// starts from 2, adds the extension flag
	public static final int FILEPROTOCOL_VDS_PK_PULLTRG = 3;// starts from 3, adds peak detect and trigger pull information
	public static final int FILEPROTOCOL_VDS_PLUG10 = 4;
	public static final int FILEPROTOCOL_VDS_CURRENT = FILEPROTOCOL_VDS_PLUG10;// always use the software's current version

	public DMInfoTiny(DeepProvider dp) {
		super(dp);
	}
}