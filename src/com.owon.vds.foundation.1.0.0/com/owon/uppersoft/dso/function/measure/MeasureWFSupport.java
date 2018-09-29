package com.owon.uppersoft.dso.function.measure;

import com.owon.uppersoft.dso.wf.ON_WF_Iterator;
import com.owon.uppersoft.vds.core.aspect.help.IWF;

public interface MeasureWFSupport {
	ON_WF_Iterator on_wf_Iterator();

	IWF getWaveForm(int idx);
}