package com.owon.uppersoft.vds.socket.handler;

import static com.owon.uppersoft.vds.socket.ScpiPool.ErrCh;
import static com.owon.uppersoft.vds.socket.ScpiPool.Failed;
import static com.owon.uppersoft.vds.socket.command.CmdFactory.ACQuire;
import static com.owon.uppersoft.vds.socket.command.CmdFactory.CHANnel;
import static com.owon.uppersoft.vds.socket.command.CmdFactory.COMMon;
import static com.owon.uppersoft.vds.socket.command.CmdFactory.DISPlay;
import static com.owon.uppersoft.vds.socket.command.CmdFactory.FFT;
import static com.owon.uppersoft.vds.socket.command.CmdFactory.LAN;
import static com.owon.uppersoft.vds.socket.command.CmdFactory.MEASure;
import static com.owon.uppersoft.vds.socket.command.CmdFactory.TIMebase;
import static com.owon.uppersoft.vds.socket.command.CmdFactory.TRIGger;

import java.util.LinkedList;
import java.util.ListIterator;

import com.owon.uppersoft.dso.model.trigger.TrgTypeDefine;
import com.owon.uppersoft.dso.model.trigger.TriggerDefine;
import com.owon.uppersoft.vds.socket.ScpiPool;
import com.owon.uppersoft.vds.socket.command.CmdFactory;
import com.owon.uppersoft.vds.socket.command.CommandKey;
import com.owon.uppersoft.vds.socket.provider.MainProvider;

public class HandlerFactory {
	private MainProvider provider;
	private CmdFactory cmdfac;
	private ListIterator<StringHandler> it;
	private LinkedList<StringHandler> handlers = new LinkedList();
	private final String Dot = ".";

	/** 查询handler的写法要改进参考《Java开发专家》的枚举类Gender类HashMap写法 */
	public HandlerFactory(CmdFactory fac, MainProvider tfc) {
		this.cmdfac = fac;
		this.provider = tfc;
		initHandlerList();
	}

	private void interator() {
		it = handlers.listIterator();
	}

	private void initHandlerList() {
		interator();
		addCommonHandlers();
		addChannelHandlers();
		addMeasureHandlers();
		addACQuireHandlers();
		addTIMebaseHandlers();
		addTRIGgerHandlers();
		addFFTHandlers();
		addLanHandlers();
		addDISPlayHandlers();
	}

	public Object handle(CommandKey cmd) {
		interator();
		String s = cmd.nodeName();
		StringHandler shd;
		while (it.hasNext()) {
			shd = it.next();
			if ((shd.name()).equalsIgnoreCase(s)) {
				return shd.handle(cmd);
			}
		}
		return ScpiPool.ErrHDL;
	}

	private void addCommonHandlers() {
		it.add(IDN);
		it.add(RST);
		it.add(AUTOset);
		it.add(RunStop);
		it.add(MainStatus);
	}

	private void addDISPlayHandlers() {
		it.add($DISPlay);
		it.add(DISPlay_CURSor);
		it.add(DISPlay_DRAW);
		it.add(DISPlay_PERSistence);
	}

	private void addChannelHandlers() {
		it.add($CHANnel);
		it.add(CHANnel_DISPlay);
		it.add(CHANnel_COUPling);
		it.add(CHANnel_PROBe);
		it.add(CHANnel_SCALe);
		it.add(CHANnel_OFFSet);
		it.add(CHANnel_HARDfreq);
		it.add(CHANnel_INVerse);
	}

	private void addMeasureHandlers() {
		it.add($MEASure);
		int len = CmdFactory.MeasValLen;
		for (int i = 0; i < len; i++) {
			final int idx = i;
			StringHandler sh = new StringHandler() {// "3." + i
				public Object handle(CommandKey cmd) {
					Object result = cmd.parentHandle();
					if (result instanceof String)
						return result;
					int chidx = (Integer) result;
					String str = provider.getMeas().getMeasureValue(chidx, idx,
							cmdfac.MEASuretr);
					// System.err.println("MEASure.chidx:" + chidx + ",str" +
					// str);
					return str;
				}

				public String name() {
					return MEASure + Dot + cmdfac.MEASuretr[idx];
				}
			};
			it.add(sh);
		}
		it.add(MEASure_SOURce);
		it.add(MEASure_ADD);
		it.add(MEASure_DELete);
	}

	private void addACQuireHandlers() {
		it.add(ACQuire_STATE);
		it.add(ACQuire_TYPE);
		it.add(ACQuire_AVERage);
		it.add(ACQuire_MDEPth);
	}

	private void addTIMebaseHandlers() {
		it.add(TIMebase_SCALe);
		it.add(TIMebase_HOFFset);
	}

	private void addTRIGgerHandlers() {
		it.add(TRIGger_TYPE);
		it.add(TRIGger_MODE);
		it.add(TRIGger_SINGle);
		it.add(TRIGger_ALT);

		it.add(TRIGger_SINGle_EDGe_SOURce);
		// it.add(TRIGger_SINGle_EDGe_COUPling);// Vds无该指令
		it.add(TRIGger_SINGle_EDGe_SLOPe);
		it.add(TRIGger_SINGle_EDGe_LEVel);

		it.add(TRIGger_SINGle_VIDeo_SOURce);
		it.add(TRIGger_SINGle_VIDeo_MODU);
		it.add(TRIGger_SINGle_VIDeo_SYNC);
		it.add(TRIGger_SINGle_VIDeo_LNUM);

		it.add(TRIGger_ALT_EDGe_SOURce);
		// it.add(TRIGger_ALT_EDGe_COUPling);// Vds无该指令
		it.add(TRIGger_ALT_EDGe_SLOPe);
		it.add(TRIGger_ALT_EDGe_LEVel);

		it.add(TRIGger_ALT_VIDeo_SOURce);
		it.add(TRIGger_ALT_VIDeo_MODU);
		it.add(TRIGger_ALT_VIDeo_SYNC);
		it.add(TRIGger_ALT_VIDeo_LNUM);
	}

	private void addFFTHandlers() {
		it.add(FFT_DISPlay);
		it.add(FFT_SOURce);
		it.add(FFT_WINDow);
		it.add(FFT_FORMat);
		it.add(FFT_ZONE);
		it.add(FFT_FREQbase);
	}

	private void addLanHandlers() {
		it.add(LAN_IPADdress);
		it.add(LAN_GATeway);
		it.add(LAN_SMASk);
		it.add(LAN_PORT);
		it.add(LAN_RESTart);

	}

	private Object analyseChIdx(CommandKey cmd) {
		String args = cmd.getPrecmd();
		int nlen = cmd.getFitPre().length();
		args = args.substring(nlen).trim();

		int chidx = -1;
		if (args.length() == 0) {
			chidx = provider.getDefaultSelectWaveform();
		} else {
			try {
				chidx = Integer.parseInt(args) - 1;
			} catch (Exception e) {
				return Failed;
			}
			int chlmax = provider.getMaxChannelIdx();
			if (chidx < 0 || chidx > chlmax)
				return ErrCh;
		}
		return chidx;
	}

	/**
	 * **************** 以下是具体的handler实例化 ******************
	 */

	public StringHandler IDN = new StringHandler() {
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			boolean b = args.equals(name + "?");
			if (!b)
				return ScpiPool.Failed;
			return provider.getIDN();
		}

		public String name() {
			return COMMon + Dot + CmdFactory.IDN;
		}
	};
	public StringHandler RST = new StringHandler() {
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			boolean b = args.equals(name);
			if (!b)
				return ScpiPool.Failed;
			return provider.setFactoryDefault();
		}

		public String name() {
			return COMMon + Dot + CmdFactory.RST;
		}
	};
	public StringHandler AUTOset = new StringHandler() {
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			boolean b = args.equals(name);
			if (!b)
				return ScpiPool.Failed;
			return provider.setAUTOset();
		}

		public String name() {
			return COMMon + Dot + CmdFactory.AUToset;
		}
	};

	public StringHandler RunStop = new StringHandler() {
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();

			if (args.startsWith(name + "?")) {
				return provider.getRunStop();
			} else {
				return provider.setRunStop();
			}
		}

		public String name() {
			return COMMon + Dot + CmdFactory.RUNSTOP;
		}
	};
	public StringHandler MainStatus = new StringHandler() {
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();

			if (args.startsWith(name + "?")) {
				return provider.getTitleStatus();
			}
			return ScpiPool.ErrAnalyse;
		}

		public String name() {
			return COMMon + Dot + CmdFactory.MainStatus;
		}
	};

	/** :CHANnel命令子系统 */
	StringHandler $CHANnel = new StringHandler() {// "2"

		public Object handle(CommandKey cmd) {
			return analyseChIdx(cmd);
		}

		public String name() {
			return CHANnel;
		}
	};
	StringHandler CHANnel_DISPlay = new StringHandler() {// "2.0"
		public Object handle(CommandKey cmd) {
			Object result = cmd.parentHandle();
			if (result instanceof String)
				return result;
			int chidx = (Integer) result;

			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				boolean ison = provider.getChl().getIsChannelDisplay(chidx);
				return ison ? "ON" : "OFF";
			} else {
				args = args.substring(name.length()).trim();
				return provider.getChl().setChannelDisplay(chidx, args);
			}
		}

		public String name() {
			return CHANnel + Dot + CmdFactory.DISPlay;
		}
	};
	StringHandler CHANnel_COUPling = new StringHandler() {// "2.1"

		public Object handle(CommandKey cmd) {
			Object result = cmd.parentHandle();
			if (result instanceof String)
				return result;
			int chidx = (Integer) result;

			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getChl().getChannelCoupling(chidx);
			} else {
				args = args.substring(name.length()).trim();
				return provider.getChl().setChannelCoupling(chidx, args);
			}
		}

		public String name() {
			return CHANnel + Dot + CmdFactory.COUPling;
		}
	};
	StringHandler CHANnel_PROBe = new StringHandler() {// "2.2"

		public Object handle(CommandKey cmd) {
			Object result = cmd.parentHandle();
			if (result instanceof String)
				return result;
			int chidx = (Integer) result;

			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getChl().getChannelProbe(chidx);
			} else {
				args = args.substring(name.length()).trim();
				return provider.getChl().setChannelProbe(chidx, args);
			}
		}

		public String name() {
			return CHANnel + Dot + CmdFactory.PROBe;
		}
	};
	StringHandler CHANnel_SCALe = new StringHandler() {// "2.3"

		public Object handle(CommandKey cmd) {
			Object result = cmd.parentHandle();
			if (result instanceof String)
				return result;
			int chidx = (Integer) result;

			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getChl().getChannelSCALe(chidx) + "";
			} else {
				args = args.substring(name.length()).trim();
				return provider.getChl().setChannelSCALe(chidx, args);
			}
		}

		public String name() {
			return CHANnel + Dot + CmdFactory.SCALe;
		}
	};
	StringHandler CHANnel_OFFSet = new StringHandler() {// "2.4"

		public Object handle(CommandKey cmd) {
			Object result = cmd.parentHandle();
			if (result instanceof String)
				return result;
			int chidx = (Integer) result;

			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getChl().getChannelOFFSet(chidx);
			} else {
				args = args.substring(name.length()).trim();
				return provider.getChl().setChannelOFFSet(chidx, args);
			}
		}

		public String name() {
			return CHANnel + Dot + CmdFactory.OFFSet;
		}
	};

	StringHandler CHANnel_HARDfreq = new StringHandler() {// "2.5"
		public Object handle(CommandKey cmd) {
			Object result = cmd.parentHandle();
			if (result instanceof String)
				return result;
			int chidx = (Integer) result;

			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getChl().getHardwareFrequency(chidx);
			}
			return Failed;
		}

		public String name() {
			return CHANnel + Dot + CmdFactory.HARDfreq;
		}
	};
	StringHandler CHANnel_INVerse = new StringHandler() {
		public Object handle(CommandKey cmd) {
			Object result = cmd.parentHandle();
			if (result instanceof String)
				return result;
			int chidx = (Integer) result;

			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getChl().getINVerseState(chidx);
			} else {
				args = args.substring(name.length()).trim();
				return provider.getChl().setINVerseState(chidx, args);
			}
		}

		public String name() {
			return CHANnel + Dot + CmdFactory.INVerse;
		}
	};
	/** :MEASure命令子系统 */
	StringHandler $MEASure = new StringHandler() {// "3"
		public Object handle(CommandKey cmd) {
			return analyseChIdx(cmd);
		}

		public String name() {
			return MEASure;
		}
	};
	StringHandler MEASure_SOURce = new StringHandler() {// "3."+(CmdFactory.MeasValLen)
		public Object handle(CommandKey cmd) {
			// int chidx = getPartentChIdx(cmd);
			// if (chidx < 0 || chidx > provider.getMaxChannelIdx())
			// return ErrCh;
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getMeas().getMeasureSource();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getMeas().setMeasureSource(args);
			}
		}

		public String name() {
			return MEASure + Dot + CmdFactory.SOURce;
		}
	};

	StringHandler MEASure_ADD = new StringHandler() {
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			// if (args.startsWith(name()))
			int nlen = cmd.getFitPre().length();
			args = args.substring(nlen).trim();
			return provider.getMeas().addMeasureElem(args);
		}

		public String name() {
			return MEASure + Dot + CmdFactory.ADD;
		}
	};
	StringHandler MEASure_DELete = new StringHandler() {
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			int nlen = cmd.getFitPre().length();
			args = args.substring(nlen).trim();
			return provider.getMeas().delMeasureElem(args);
		}

		public String name() {
			return MEASure + Dot + CmdFactory.DELete;
		}
	};

	/** :ACQuire命令子系统 */
	StringHandler ACQuire_STATE = new StringHandler() {// "4.0"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getAcq().getACQuireState();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getAcq().setACQuireState(args);
			}
		}

		public String name() {
			return ACQuire + Dot + CmdFactory.STATe;
		}
	};
	StringHandler ACQuire_TYPE = new StringHandler() {// "4.1"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getAcq().getACQuireType();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getAcq().setACQuireType(args);
			}
		}

		public String name() {
			return ACQuire + Dot + CmdFactory.TYPE;
		}
	};
	StringHandler ACQuire_AVERage = new StringHandler() {// "4.2"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getAcq().getACQuireAVERage();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getAcq().setACQuireAVERage(args);
			}
		}

		public String name() {
			return ACQuire + Dot + CmdFactory.AVERage;
		}
	};
	StringHandler ACQuire_MDEPth = new StringHandler() {// "4.3"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getAcq().getACQuireMDEPth();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getAcq().setACQuireMDEPth(args);
			}
		}

		public String name() {
			return ACQuire + Dot + CmdFactory.MDEPth;
		}
	};

	/** :TIMebase命令子系统 */
	StringHandler TIMebase_SCALe = new StringHandler() {// "5.0"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getTb().getTIMebaseSCALe();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getTb().setTIMebaseSCALe(args);
			}
		}

		public String name() {
			return TIMebase + Dot + CmdFactory.SCALe;
		}
	};
	StringHandler TIMebase_HOFFset = new StringHandler() {// "5.1"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getTb().getTIMebaseHOFFset();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getTb().setTIMebaseHOFFset(args);
			}
		}

		public String name() {
			return TIMebase + Dot + CmdFactory.HOFFset;
		}
	};
	/** :TRIGger命令子系统 */
	StringHandler TRIGger_TYPE = new StringHandler() {// "6.0"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getTrg().getTrgType();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getTrg().setTrgType(args);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.TYPE;
		};
	};
	StringHandler TRIGger_MODE = new StringHandler() {// "6.1"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getTrg().getSweepMode();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getTrg().setSweepMode(args);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.MODE;
		};
	};
	StringHandler TRIGger_SINGle = new StringHandler() {// "6.2"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			int trgTyp = TriggerDefine.TrgModeSingleIndex;
			if (args.startsWith(name + "?")) {
				return provider.getTrg().getTrgTypMode(trgTyp);
			} else {
				args = args.substring(name.length()).trim();
				return provider.getTrg().setTrgTypMode(trgTyp, args);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.SINGle;
		};
	};
	StringHandler TRIGger_ALT = new StringHandler() {// "6.3"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			int trgTyp = TriggerDefine.TrgModeAltIndex;
			if (args.startsWith(name + "?")) {
				return provider.getTrg().getTrgTypMode(trgTyp);
			} else {
				args = args.substring(name.length()).trim();
				return provider.getTrg().setTrgTypMode(trgTyp, args);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.ALT;
		};
	};
	// //:TRIGger:SINGle:EDGe
	StringHandler TRIGger_SINGle_EDGe_SOURce = new StringHandler() {// "6.2.0.0"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getTrg().getSingle_Source();
			} else {
				args = args.substring(name.length()).trim();
				int trgTyp = TriggerDefine.TrgModeSingleIndex;
				return provider.getTrg().set_Source(args, trgTyp,
						TrgTypeDefine.Edge);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.SINGle + Dot + CmdFactory.EDGe
					+ Dot + CmdFactory.SOURce;
		};
	};
	StringHandler TRIGger_SINGle_EDGe_COUPling = new StringHandler() {// "6.2.0.1"
		public Object handle(CommandKey args) {
			return "";
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.SINGle + Dot + CmdFactory.EDGe
					+ Dot + CmdFactory.COUPling;
		};
	};
	StringHandler TRIGger_SINGle_EDGe_SLOPe = new StringHandler() {// "6.2.0.2"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			int trgTyp = TriggerDefine.TrgModeSingleIndex;
			if (args.startsWith(name + "?")) {
				return provider.getTrg().get_EdgeSlope(trgTyp);
			} else {
				args = args.substring(name.length()).trim();
				return provider.getTrg().set_EdgeSlope(trgTyp, args);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.SINGle + Dot + CmdFactory.EDGe
					+ Dot + CmdFactory.SLOPe;
		};
	};
	StringHandler TRIGger_SINGle_EDGe_LEVel = new StringHandler() {// "6.2.0.3"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			int trgTyp = TriggerDefine.TrgModeSingleIndex;
			if (args.startsWith(name + "?")) {
				return provider.getTrg().get_EdgeLevel(trgTyp);
			} else {
				args = args.substring(name.length()).trim();
				return provider.getTrg().set_EdgeLevel(trgTyp, args);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.SINGle + Dot + CmdFactory.EDGe
					+ Dot + CmdFactory.LEVel;

		};
	};
	// //:TRIGger:SINGle:VIDeo
	StringHandler TRIGger_SINGle_VIDeo_SOURce = new StringHandler() {// "6.2.1.0"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getTrg().getSingle_Source();
			} else {
				args = args.substring(name.length()).trim();
				int trgTyp = TriggerDefine.TrgModeSingleIndex;
				return provider.getTrg().set_Source(args, trgTyp,
						TrgTypeDefine.Video);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.SINGle + Dot + CmdFactory.VIDeo
					+ Dot + CmdFactory.SOURce;
		};
	};
	StringHandler TRIGger_SINGle_VIDeo_MODU = new StringHandler() {// "6.2.1.1"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			int trgTyp = TriggerDefine.TrgModeSingleIndex;
			if (args.startsWith(name + "?")) {
				return provider.getTrg().get_VideoMod(trgTyp);
			} else {
				args = args.substring(name.length()).trim();
				return provider.getTrg().set_VideoMod(trgTyp, args);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.SINGle + Dot + CmdFactory.VIDeo
					+ Dot + CmdFactory.MODU;
		};
	};
	StringHandler TRIGger_SINGle_VIDeo_SYNC = new StringHandler() {// "6.2.1.2"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			int trgTyp = TriggerDefine.TrgModeSingleIndex;
			if (args.startsWith(name + "?")) {
				return provider.getTrg().get_VideoSync(trgTyp);
			} else {
				args = args.substring(name.length()).trim();
				return provider.getTrg().set_VideoSync(trgTyp, args);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.SINGle + Dot + CmdFactory.VIDeo
					+ Dot + CmdFactory.SYNC;
		};
	};
	StringHandler TRIGger_SINGle_VIDeo_LNUM = new StringHandler() {// "6.2.1.3"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			int trgTyp = TriggerDefine.TrgModeSingleIndex;
			if (args.startsWith(name + "?")) {
				return provider.getTrg().get_VideoLnum(trgTyp);
			} else {
				args = args.substring(name.length()).trim();
				return provider.getTrg().set_VideoLnum(trgTyp, args);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.SINGle + Dot + CmdFactory.VIDeo
					+ Dot + CmdFactory.LNUM;
		};
	};
	// //:TRIGger:ALT:EDGe
	StringHandler TRIGger_ALT_EDGe_SOURce = new StringHandler() {// "6.3.0.0"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getTrg().getAlt_EdgeSource();
			} else {
				args = args.substring(name.length()).trim();
				int trgTyp = TriggerDefine.TrgModeAltIndex;
				return provider.getTrg().set_Source(args, trgTyp,
						TrgTypeDefine.Edge);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.ALT + Dot + CmdFactory.EDGe + Dot
					+ CmdFactory.SOURce;
		};
	};
	StringHandler TRIGger_ALT_EDGe_COUPling = new StringHandler() {// "6.3.0.1"
		public Object handle(CommandKey args) {
			return "";
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.ALT + Dot + CmdFactory.EDGe + Dot
					+ CmdFactory.COUPling;
		};
	};
	StringHandler TRIGger_ALT_EDGe_SLOPe = new StringHandler() {// "6.3.0.2"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			int trgTyp = TriggerDefine.TrgModeAltIndex;
			if (args.startsWith(name + "?")) {
				return provider.getTrg().get_EdgeSlope(trgTyp);
			} else {
				args = args.substring(name.length()).trim();
				return provider.getTrg().set_EdgeSlope(trgTyp, args);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.ALT + Dot + CmdFactory.EDGe + Dot
					+ CmdFactory.SLOPe;
		};
	};
	StringHandler TRIGger_ALT_EDGe_LEVel = new StringHandler() {// "6.3.0.3"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			int trgTyp = TriggerDefine.TrgModeAltIndex;
			if (args.startsWith(name + "?")) {
				return provider.getTrg().get_EdgeLevel(trgTyp);
			} else {
				args = args.substring(name.length()).trim();
				return provider.getTrg().set_EdgeLevel(trgTyp, args);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.ALT + Dot + CmdFactory.EDGe + Dot
					+ CmdFactory.LEVel;
		};
	};
	// //:TRIGger:ALT:VIDeo
	StringHandler TRIGger_ALT_VIDeo_SOURce = new StringHandler() {// "6.3.1.0"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			// int trgTyp = TriggerDefine.TriggerChannelModeAlternateIndex;
			if (args.startsWith(name + "?")) {
				return provider.getTrg().getAlt_VideoSource();
			} else {
				args = args.substring(name.length()).trim();
				int trgTyp = TriggerDefine.TrgModeAltIndex;
				return provider.getTrg().set_Source(args, trgTyp,
						TrgTypeDefine.Video);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.ALT + Dot + CmdFactory.VIDeo
					+ Dot + CmdFactory.SOURce;
		};
	};
	StringHandler TRIGger_ALT_VIDeo_MODU = new StringHandler() {// "6.3.1.1"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			int trgTyp = TriggerDefine.TrgModeAltIndex;
			if (args.startsWith(name + "?")) {
				return provider.getTrg().get_VideoMod(trgTyp);
			} else {
				args = args.substring(name.length()).trim();
				return provider.getTrg().set_VideoMod(trgTyp, args);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.ALT + Dot + CmdFactory.VIDeo
					+ Dot + CmdFactory.MODU;
		};
	};
	StringHandler TRIGger_ALT_VIDeo_SYNC = new StringHandler() {// "6.3.1.2"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			int trgTyp = TriggerDefine.TrgModeAltIndex;
			if (args.startsWith(name + "?")) {
				return provider.getTrg().get_VideoSync(trgTyp);
			} else {
				args = args.substring(name.length()).trim();
				return provider.getTrg().set_VideoSync(trgTyp, args);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.ALT + Dot + CmdFactory.VIDeo
					+ Dot + CmdFactory.SYNC;
		};
	};
	StringHandler TRIGger_ALT_VIDeo_LNUM = new StringHandler() {// "6.3.1.3"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			int trgTyp = TriggerDefine.TrgModeAltIndex;
			if (args.startsWith(name + "?")) {
				return provider.getTrg().get_VideoLnum(trgTyp);
			} else {
				args = args.substring(name.length()).trim();
				return provider.getTrg().set_VideoLnum(trgTyp, args);
			}
		};

		public String name() {
			return TRIGger + Dot + CmdFactory.ALT + Dot + CmdFactory.VIDeo
					+ Dot + CmdFactory.LNUM;
		};
	};

	StringHandler FFT_DISPlay = new StringHandler() {// "7.0"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getFFT().getIsDisplay();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getFFT().setDisplay(args);
			}
		};

		public String name() {
			return FFT + Dot + CmdFactory.DISPlay;
		};
	};

	StringHandler FFT_SOURce = new StringHandler() {// "7.1"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getFFT().getSource();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getFFT().setSource(args);
			}
		};

		public String name() {
			return FFT + Dot + CmdFactory.SOURce;
		};
	};

	StringHandler FFT_WINDow = new StringHandler() {// "7.2"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getFFT().getWINDow();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getFFT().setWINDow(args);
			}
		};

		public String name() {
			return FFT + Dot + CmdFactory.WINDow;
		};
	};

	StringHandler FFT_FORMat = new StringHandler() {// "7.3"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getFFT().getFORMat();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getFFT().setFORMat(args);
			}
		};

		public String name() {
			return FFT + Dot + CmdFactory.FORMat;
		};
	};

	StringHandler FFT_ZONE = new StringHandler() {// "7.4"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getFFT().getZONE();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getFFT().setZONE(args);
			}
		};

		public String name() {
			return FFT + Dot + CmdFactory.ZONE;
		};
	};

	StringHandler FFT_FREQbase = new StringHandler() {// "7.5"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getFFT().getFREQbase();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getFFT().setFREQbase(args);
			}
		};

		public String name() {
			return FFT + Dot + CmdFactory.FREQbase;
		};
	};

	StringHandler LAN_IPADdress = new StringHandler() {// "8.0"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();// .toUpperCase();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getLan().getIP();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getLan().setIP(args);
			}
		};

		public String name() {
			return LAN + Dot + CmdFactory.IPADdress;
		};
	};
	StringHandler LAN_GATeway = new StringHandler() {// "8.1"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();// .toUpperCase();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getLan().getGATeway();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getLan().setGATeway(args);
			}
		};

		public String name() {
			return LAN + Dot + CmdFactory.GATeway;
		};
	};
	StringHandler LAN_SMASk = new StringHandler() {// "8.2"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();// .toUpperCase();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getLan().getSMASk();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getLan().setSMASk(args);
			}
		};

		public String name() {
			return LAN + Dot + CmdFactory.SMASk;
		};
	};
	StringHandler LAN_PORT = new StringHandler() {// "8.3"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();// .toUpperCase();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getLan().getPort();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getLan().setPort(args);
			}
		};

		public String name() {
			return LAN + Dot + CmdFactory.PORT;
		};
	};
	StringHandler LAN_RESTart = new StringHandler() {// "8.4"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();// .toUpperCase();
			int nlen = cmd.getFitPre().length();
			args = args.substring(nlen).trim();
			if (args.equalsIgnoreCase("ON") || args.equalsIgnoreCase("OFF"))
				return provider.getLan().rebootLan();
			return Failed;
		};

		public String name() {
			return LAN + Dot + CmdFactory.RESTart;
		};
	};

	StringHandler $DISPlay = new StringHandler() {// "9"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			int nlen = cmd.getFitPre().length();
			args = args.substring(nlen).trim();
			if (args.equalsIgnoreCase("ON")) {
				return true;
			} else if (args.equalsIgnoreCase("OFF")) {
				return false;
			} else
				return Failed;
		};

		public String name() {
			return DISPlay;
		};
	};

	StringHandler DISPlay_CURSor = new StringHandler() {// "9.0"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getDsp().getCursor();
			} else {
				Object result = cmd.parentHandle();
				if (result instanceof String)
					return result;
				boolean onoff = (Boolean) result;
				args = args.substring(name.length()).trim();
				return provider.getDsp().setCursor(args, onoff);
			}
		};

		public String name() {
			return DISPlay + Dot + CmdFactory.CURSor;
		};
	};
	StringHandler DISPlay_DRAW = new StringHandler() {// "9.1"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getDsp().getDraw();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getDsp().setDraw(args);
			}
		};

		public String name() {
			return DISPlay + Dot + CmdFactory.DRAW;
		};
	};
	StringHandler DISPlay_PERSistence = new StringHandler() {// "9.2"
		public Object handle(CommandKey cmd) {
			String args = cmd.getPrecmd();
			String name = cmd.getFitPre();
			if (args.startsWith(name + "?")) {
				return provider.getDsp().getPersistence();
			} else {
				args = args.substring(name.length()).trim();
				return provider.getDsp().setPersistence(args);
			}
		};

		public String name() {
			return DISPlay + Dot + CmdFactory.PERSistence;
		};
	};

}
