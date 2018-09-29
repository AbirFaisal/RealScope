package com.owon.uppersoft.vds.socket.command;

import com.owon.uppersoft.vds.socket.handler.HandlerFactory;

public class CommandKey {

	private CommandKey parentKey;
	private CommandKey[] childrenKeys;
	private HandlerFactory fac;
	private String pre, precmd, subcmd;
	private String shortPre;
	private boolean onShortDetected = false;

	/** return the pre of precmd */
	public String getFitPre() {
		if (onShortDetected)
			return shortPre;
		return pre;
	}

	public String getPre() {
		return pre;
	}

	public String getShortPre() {
		return shortPre;
	}

	private String setShortPre(String pre) {
		shortPre = pre.toUpperCase();
		if (pre.length() > 4) {
			shortPre = pre.substring(0, 4);
			char last = shortPre.charAt(shortPre.length() - 1);
			if (last == 'A' || last == 'E' || last == 'I' || last == 'O'
					|| last == 'U')
				shortPre = pre.substring(0, 3);
		}
		return shortPre;
	}

	/** return the precmd of inputcmd */
	public String getPrecmd() {
		return precmd;
	}

	public void setPrecmd(String precmd) {
		this.precmd = precmd.toUpperCase();
	}

	public CommandKey(String p, String[] branchs, HandlerFactory hf) {
		this.pre = p.toUpperCase();
		this.fac = hf;
		setShortPre(pre);
		if (branchs != null) {
			childrenKeys = createBranchs(branchs);
		}
	}

	public CommandKey hasKey(String line, CommandKey parentCK) {
		println("line: " + line, false);
		parentKey = parentCK;
		if (line.startsWith(CmdFactory.COMMon)) {
			setPrecmd(CmdFactory.COMMon);
			subcmd = ":" + line.substring(1);
		} else if (line.startsWith(":")) {
			String[] splits = line.split(":",3);//分割3-1次，分成3组
			setPrecmd(splits[1]);
			subcmd = line.substring(precmd.length() + 1);
		} else
			return null;
		println("hasKey? Pre:" + getFitPre() + ",precmd:" + precmd + ",subcmd:"
				+ subcmd, false);
		if (compaireCMDHead()) {
			if (childrenKeys == null || subcmd.length() == 0) {
				return this;
			}

			for (int i = 0; i < childrenKeys.length; i++) {
				CommandKey ck = childrenKeys[i].hasKey(subcmd, this);
				if (ck != null) {
					println(ck.pre, false);
					return ck;
				}
			}
		}
		return null;
	}

	public Object handle() {
		println("handle?pre:" + pre + ",precmd: " + precmd, false);
		return fac.handle(this);
	}

	private boolean compaireCMDHead() {
		boolean p1 = precmd.startsWith(pre);
		boolean p2 = precmd.startsWith(shortPre);
		println("samePre:" + p1 + "," + pre + ",sameShort:" + p2 + "," + shortPre,
				false);
		onShortDetected = !p1 && p2;
		return p1 || p2;
	}

	private CommandKey[] createBranchs(String[] branchs) {
		CommandKey[] cks = new CommandKey[branchs.length];
		for (int i = 0, l = branchs.length; i < l; i++) {
			cks[i] = new CommandKey(branchs[i], null, fac);
		}
		return cks;
	}

	public void setBranchs(CommandKey[] branchs) {
		childrenKeys = branchs;
	}

	public Object parentHandle() {
		if (parentKey != null)
			return parentKey.handle();
		return null;
	}

	public String nodeName() {
		if (parentKey != null) {
			return parentKey.nodeName() + "." + pre;
		}
		return pre;
	}
	
	private void println(String s, boolean isErr) {
		// if (isErr)
		// System.err.println(s);
		// else
		// System.out.println(s);
	}
}
