package com.owon.uppersoft.vds.source.comm;

import com.owon.uppersoft.dso.source.manager.SourceManager;
import com.owon.uppersoft.dso.source.net.NetSourceManager;
import com.owon.uppersoft.dso.source.usb.USBPortsFilter;
import com.owon.uppersoft.dso.source.usb.USBSourceManager;
import com.owon.uppersoft.vds.core.comm.IRuntime;

public class SourceManagerTiny extends SourceManager {
	private USBPortsFilter pf;

	public SourceManagerTiny(IRuntime ir, USBPortsFilter pf) {
		super(ir);
		this.pf = pf;
	}

	@Override
	public USBSourceManager createUSBSourceManager() {
		return new USBSourceManagerTiny(pf);
	}

	@Override
	protected NetSourceManager createNetSourceManager() {
		return new NetSourceManager();
	}
}
