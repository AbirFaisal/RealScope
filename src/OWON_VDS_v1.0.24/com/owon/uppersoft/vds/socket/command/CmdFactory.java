package com.owon.uppersoft.vds.socket.command;

import java.util.ArrayList;

import com.owon.uppersoft.vds.core.measure.MeasureT;
import com.owon.uppersoft.vds.socket.handler.HandlerFactory;
import com.owon.uppersoft.vds.socket.provider.MainProvider;

public class CmdFactory {
	public static final int MeasValLen = MeasureT.Count + 3;// 3:(Rdelay,Fdelay,RecVamp)
	private String[] COMMontr = { IDN, RST, AUToset, RUNSTOP, MainStatus };
	private String[] CHANneltr = { DISPlay, COUPling, PROBe, SCALe, OFFSet,
			HARDfreq, INVerse };
	public String[] MEASuretr;
	public String[] ACQuiretr = { STATe, TYPE, AVERage, MDEPth };
	public String[] TIMebasetr = { SCALe, HOFFset };
	/** TRIGgertr --- */
	public String[] TRIGgertr = { TYPE, MODE, SINGle, ALT };
	public String[] SINGletr = { EDGe, VIDeo };
	public String[] ALTtr = { EDGe, VIDeo };
	public String[] EDGetr = { SOURce, COUPling, SLOPe, LEVel };
	public String[] VIDeotr = { SOURce, MODU, SYNC, LNUM };
	/** ---- */
	public String[] FFTtr = { DISPlay, SOURce, WINDow, FORMat, ZONE, FREQbase };
	public String[] LANtr = { IPADdress, GATeway, SMASk, PORT, RESTart };
	public String[] DISPlaytr = { CURSor, DRAW, PERSistence };

	private MainProvider provider;
	private HandlerFactory fac;
	private ArrayList<CommandKey> trunk = new ArrayList<CommandKey>();

	public ArrayList<CommandKey> getKeys() {
		return trunk;
	}

	public MainProvider getProvider() {
		return provider;
	}

	public CmdFactory() {
		prepareMeasureTypes();
		provider = new MainProvider();
		fac = new HandlerFactory(this, provider);

		trunk.add(new CommandKey(COMMon, COMMontr, fac));
		trunk.add(new CommandKey(CHANnel, CHANneltr, fac));
		trunk.add(new CommandKey(MEASure, MEASuretr, fac));
		trunk.add(new CommandKey(ACQuire, ACQuiretr, fac));
		trunk.add(new CommandKey(TIMebase, TIMebasetr, fac));
		trunk.add(new_TRIGgerkey());// "6"
		trunk.add(new CommandKey(FFT, FFTtr, fac));
		trunk.add(new CommandKey(LAN, LANtr, fac));
		trunk.add(new CommandKey(DISPlay, DISPlaytr, fac));
	}

	public void prepareMeasureTypes() {
		int i = 0, num = MeasureT.Count;
		MEASuretr = new String[num + 6];
		for (; i < num; i++) {
			MEASuretr[i] = MeasureT.VALUES[i].toString();
		}
		MEASuretr[i++] = RDELay;
		MEASuretr[i++] = FDELay;
		MEASuretr[i++] = RECVamp;

		MEASuretr[i++] = SOURce;
		MEASuretr[i++] = ADD;
		MEASuretr[i] = DELete;

	}

	private CommandKey new_TRIGgerkey() {

		CommandKey edgeck_s = new CommandKey(EDGe, EDGetr, fac);
		CommandKey videock_s = new CommandKey(VIDeo, VIDeotr, fac);
		CommandKey[] SINGlecks = { edgeck_s, videock_s };

		CommandKey edgeck_a = new CommandKey(EDGe, EDGetr, fac);
		CommandKey videock_a = new CommandKey(VIDeo, VIDeotr, fac);
		CommandKey[] ALTcks = { edgeck_a, videock_a };

		CommandKey TYPEck = new CommandKey(TYPE, null, fac);
		CommandKey MODEck = new CommandKey(MODE, null, fac);
		CommandKey SINGleck = new CommandKey(SINGle, null, fac);
		CommandKey ALTck = new CommandKey(ALT, null, fac);
		CommandKey[] trgcks = { TYPEck, MODEck, SINGleck, ALTck };

		CommandKey trgck = new CommandKey(TRIGger, null, fac);
		SINGleck.setBranchs(SINGlecks);
		ALTck.setBranchs(ALTcks);
		trgck.setBranchs(trgcks);
		return trgck;
	}

	/** 一级指令树: */
	public static final String COMMon = "*";
	public static final String CHANnel = "CHANnel";
	public static final String MEASure = "MEASure";
	public static final String ACQuire = "ACQuire";
	public static final String TIMebase = "TIMebase";
	public static final String TRIGger = "TRIGger";
	public static final String FFT = "FFT";
	public static final String LAN = "LAN";
	public static final String DISPlay = "DISPlay";

	/** (*COMMon) */
	public static final String ADC = "*ADC?";
	public static final String LDM_ADC = "*LDM?";
	public static final String RDM_ADC = "*RDM?";
	public static final String IDN = "IDN";
	public static final String AUToset = "AUToset";
	public static final String RST = "RST";
	public static final String RUNSTOP = "RUNStop";
	public static final String MainStatus = "MSTatus";

	/** :CHANnel */
	// public static final String DISPlay = "DISPlay";
	public static final String COUPling = "COUPling";
	public static final String PROBe = "PROBe";
	public static final String SCALe = "SCALe";
	public static final String OFFSet = "OFFSet";
	public static final String HARDfreq = "HARDfreq";
	public static final String INVerse = "INVerse";

	/** :MEASure */
	public static final String RECVamp = "RECVamp";
	public static final String SOURce = "SOURce";
	public static final String ADD = "ADD";
	public static final String DELete = "DELete";
	public static final String RDELay = "RDELay";
	public static final String FDELay = "FDELay";

	/** :ACQuire */
	public static final String STATe = "STATe";
	// public static final String TYPE = "TYPE";
	public static final String AVERage = "AVERage";
	public static final String MDEPth = "MDEPth";

	/** :TIMebase */
	// public static final String SCALe = "SCALe";
	public static final String HOFFset = "HOFFset";

	/** :TRIGger */
	public static final String TYPE = "TYPE";
	public static final String MODE = "MODE";
	public static final String SINGle = "SINGle";
	public static final String ALT = "ALT";
	/** :TRIGger:SINGle:EDGe */
	public static final String EDGe = "EDGE";
	// public static final String SOURce = "SOURce";
	// public static final String COUPling = "COUPling";
	public static final String SLOPe = "SLOPe";
	public static final String LEVel = "LEVel";
	/** :TRIGger:SINGle:VIDeo */
	public static final String VIDeo = "VIDeo";
	// public static final String SOURce = "SOURce";
	public static final String MODU = "MODU";
	public static final String SYNC = "SYNC";
	public static final String LNUM = "LNUM";

	/** :TRIGger:ALT:EDGe */
	// public static final String EDGe = "EDGe";
	// public static final String SOURce = "SOURce";
	// public static final String COUPling = "COUPling";
	// public static final String SLOPe = "SLOPe";
	// public static final String LEVel = "LEVel";
	/** :TRIGger:ALT:VIDeo */
	// public static final String VIDeo = "VIDeo";
	// public static final String SOURce = "SOURce";
	// public static final String MODU = "MODU";
	// public static final String SYNC = "SYNC";
	// public static final String LNUM = "LNUM";

	/** FFT */
	// public static final String DISPlay = "DISPlay";
	// public static final String SOURce = "SOURce";
	public static final String WINDow = "WINDow";
	public static final String FORMat = "FORMat";
	public static final String ZONE = "ZONE";
	public static final String FREQbase = "FREQbase";

	/** LAN */
	public static final String IPADdress = "IPADdress";
	public static final String GATeway = "GATeway";
	public static final String SMASk = "SMASk";
	public static final String PORT = "PORT";
	public static final String RESTart = "RESTart";
	/** DISPlay */
	public static final String CURSor = "CURSor";
	public static final String DRAW = "DRAW";
	public static final String PERSistence = "PERSistence";

	// protected String[] creatTree() {
	// DefaultMutableTreeNode root = new DefaultMutableTreeNode();
	//
	// DefaultMutableTreeNode COMMonNode = new DefaultMutableTreeNode(
	// "COMMonNode");
	// DefaultMutableTreeNode CHANnelNode = new DefaultMutableTreeNode(
	// "CHANnelNode");
	// DefaultMutableTreeNode MEASureNode = new DefaultMutableTreeNode(
	// "MEASureNode");
	// DefaultMutableTreeNode TIMebaseNode = new DefaultMutableTreeNode(
	// "TIMebaseNode");
	// DefaultMutableTreeNode TRIGgerNode = new DefaultMutableTreeNode(
	// "TRIGgerNode");
	//
	// root.add(COMMonNode);
	// root.add(CHANnelNode);
	// root.add(MEASureNode);
	// for (String s : COMMontr)
	// COMMonNode.add(new DefaultMutableTreeNode(s));
	// for (String s : CHANneltr)
	// CHANnelNode.add(new DefaultMutableTreeNode(s));
	// for (String s : MEASuretr)
	// MEASureNode.add(new DefaultMutableTreeNode(s));
	// for (String s : TIMebasetr)
	// TIMebaseNode.add(new DefaultMutableTreeNode(s));
	// for (String s : TRIGgertr)
	// TRIGgerNode.add(new DefaultMutableTreeNode(s));
	//
	// Enumeration enums = root.breadthFirstEnumeration();
	// StringBuilder sb = new StringBuilder();
	// while (enums.hasMoreElements()) {
	// Object o = enums.nextElement();
	// sb.append(o);
	// sb.append(",");
	// }
	// String[] tree = new String(sb).split(",");
	// return tree;
	// }

}
