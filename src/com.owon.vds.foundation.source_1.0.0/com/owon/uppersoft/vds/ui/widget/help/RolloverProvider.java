package com.owon.uppersoft.vds.ui.widget.help;

import com.owon.uppersoft.vds.core.aspect.base.IBoolean;

public class RolloverProvider implements IBoolean {
	private boolean b = false;

	@Override
	public boolean isOrNot() {
		return b;
	}

	@Override
	public void set(boolean yes) {
		b = yes;
	}
}