package com.owon.uppersoft.vds.socket.handler;

import com.owon.uppersoft.vds.socket.command.CommandKey;

public abstract class StringHandler implements IStringHandler {

//	public String ID;

	public StringHandler() {//String idx
//		this.ID = idx;
	}

	public abstract Object handle(CommandKey args);

	public abstract String name();

}
