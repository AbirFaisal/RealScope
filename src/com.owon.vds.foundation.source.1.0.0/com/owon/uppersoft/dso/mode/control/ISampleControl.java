package com.owon.uppersoft.dso.mode.control;

import com.owon.uppersoft.vds.core.data.MinMax;

public interface ISampleControl {

	int getAvgTimes();

	int getModelIdx();

	void c_setAvgTimes(int v);

	void c_setModelIdx(int idx);

	MinMax getAvgTimesRange();
}
