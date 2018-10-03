package com.owon.uppersoft.vds.socket.handler;

import com.owon.uppersoft.vds.socket.command.CommandKey;


public interface IStringHandler {

	Object handle(CommandKey args);
}
