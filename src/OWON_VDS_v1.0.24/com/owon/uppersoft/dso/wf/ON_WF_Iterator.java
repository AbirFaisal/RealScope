package com.owon.uppersoft.dso.wf;

import java.util.Iterator;
import java.util.List;

public class ON_WF_Iterator implements Iterator<WaveForm> {
	private WaveForm wf;
	private Iterator<WaveForm> wi;
	private List<WaveForm> wfs;

	public ON_WF_Iterator(List<WaveForm> wfs) {
		this.wfs = wfs;
		reset();
	}

	public void reset() {
		wi = wfs.iterator();
	}

	public WaveForm next() {
		return wf;
	}

	@Override
	public boolean hasNext() {
		while (wi.hasNext()) {
			wf = wi.next();
			if (wf.isOn())
				return true;
		}
		wf = null;
		return false;
	}

	@Override
	public void remove() {
	}

}