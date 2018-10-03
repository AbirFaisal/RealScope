package com.owon.uppersoft.dso.page;

public abstract class AbstractContentPage implements IContentPage {

	@Override
	public void beforeLeave() {
	}

	@Override
	public boolean canLeave() {
		return true;
	}
}
