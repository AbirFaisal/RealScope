package com.owon.uppersoft.vds.core.aspect.help;

import com.owon.uppersoft.vds.util.Pref;

public interface ILoadPersist {
	void load(Pref p);

	void persist(Pref p);
}
