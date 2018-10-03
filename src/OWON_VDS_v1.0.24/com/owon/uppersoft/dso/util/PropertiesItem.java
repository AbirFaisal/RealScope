package com.owon.uppersoft.dso.util;

import java.util.Arrays;
import java.util.List;

/**
 * PropertiesItem，软件信息等相关参数
 * 
 */
public interface PropertiesItem {

	/** 配置信息的键 */
	public static final String LineVisible = "LineVisible";
	public static final String DottedGraticulevisible = "DottedGraticulevisible";

	public static final String Channels = "Channels";
	public static final String ChannelColors = "ChannelColors";

	public static final String FileHistory = "FileHistory";

	public static final String AutoSavePath = "AutoSavePath";

	public static final String SavePath = "SavePath";

	public static final String ShowSplashscreen = "ShowSplashscreen";

	public static final String LoopDelay = "LoopDelay";

	public static final String CloseOnDone = "CloseOnDone";
	public static final String ShowMachineType = "ShowMachineType";
	public static final String AutoPlayerHist = "AutoPlayerHist";

	public static final String NotifyUSBchange = "NotifyUSBchange";

	public static final String ShellSize = "ShellSize";
	public static final String MainSash = "MainSash";
	public static final String UpSash = "UpSash";
	public static final String DownSash = "DownSash";

	public static final String BackgroundRGB = "BackgroundRGB";
	public static final String GridRGB = "GridRGB";
	public static final String ImageScaleRGB = "ImageScaleRGB";

	public static final String[] StickStringArray = { "-" };
	public static final String StickString = "-";
	public static final List<String> StickStringList = Arrays.asList("-");

	public static final String T_SweepOnce2Auto = "T_SweepOnce2Auto";
	public static final String T_2SweepOnce = "T_2SweepOnce";

	public static final String ActionOff = "ActionOff";

	public static final String RecordFrameIndex = "RecordFrameIndex";

	public static final String KEEPGET = "Keepget";

	public static final String SliderViewValueChanged = "SliderViewValueChanged";

	public static final String SELECT_W_F = "SelectWF";

	// public static final String HANDLE_VIDEO_NOUSE = "HANDLE_VIDEO_NOUSE";

	public static final String UPDATE_CHLVOLT = "UPDATE_CHLVOLT";
	public static final String UPDATE_UPP_LOW = "UpdateUppLow";
	public static final String UPDATE_VOLTSENSE = "UpdateVoltsense";
	public static final String CHARTSCREEN_UPDATESHOW = "CHARTSCREEN_UPDATESHOW";

	public static final String PROBECHANGE = "PROBECHANGE";
	public static final String CHANNEL_OPTION = "ChannelOption";
	public static final String CHANNEL_OPPOSITE = "ChannelOpposite";
	public static final String COUPLING_OPTION = "CouplingOption";
	public static final String NEXT_STATUS = "NextStatus";
	public static final String UPDATE_VIDEOMOD = "updateVideoMod";
	public static final String DELTAVALUE = "deltavalue";
	public static final String RESET0 = "reset0";
	public static final String READ_HEADER = "ReadHeader";
	public static final String PLAY_PROGRESS = "PlayProgress";
	public static final String STATUS = "Status";
	public static final String DATA_LEN = "DataLen";
	public static final String CHL_NUM = "ChlNum";
	public static final String PROGRESS = "Progress";
	public static final String NEXT_SINGLE_CHANNEL = "NextSingleChannel";
	public static final String JCOMPONENT_DISPOSE = "JComponentDispose";

	public static final String CHL1ST_DONE = "Chl1stDone";
	public static final String CHL_DONE = "ChlDone";
	public static final String TRANS_DONE = "TransDone";
	public static final String TRANS_FAIL = "TransFail";
	public static final String CHLNEXT_DONE = "ChlnextDone";
	public static final String INITLINK = "InitLink";
	public static final String MachineInformation = "Info";
	public static final String INTERNAL_VBIDX = "Internal.vbidx";
	public static final String ZOOMSELECTED = "zoomSelected";

	public static final String TIME_LINE_CHECK = "TimeLineCheck";

	// Apply Item
	public static final String APPLY_TIMEBASE = "APPLY_TIMEBASE";
	public static final String APPLY_CHANNELS = "APPLY_CHANNELS";
	public static final String APPLY_SAMPLING = "APPLY_SAMPLING";
	public static final String APPLY_TRIGGER = "APPLY_TRIGGER";
	public static final String APPLY_DEEPMEMORY = "APPLY_DEEPMEMORY";

	public static final String CHOOSE_TRGMODECB = "CHOOSE_TRGMODECB";
	public static final String CHOOSE_CHANNELS = "CHOOSE_CHANNELS";

	public static final String PFStopOnOutput = "PFStopOnOutput";

	public static final String SampleRateChange = "SampleRateChange";

	public static final String AFTER_GOT_DM_DATA = "afterGotDMData";
	public static final String SLIDER_50percent = "SLIDER_50percent";

	public static final String POPUP_MENUITEM_IDX = "popupMenuItemIdx";

	public static final String RSSTATUS_CHANGE = "RSStatusChange";
	public static final String PAU_EXP_BTN_UPDATE = "Pau_expBtnUpdate";
	public static final String SWITCH_SLOWMOVE = "SwitchSlowMove";
	public static final String SWITCH_NormalMOVE = "SwitchNormalMove";

	public static final String FFT_ON = "FFT_ON";
	public static final String FFT_OFF = "FFT_OFF";

	public static final String TimebaseChange = "TimebaseChange";

	public static final String TUNE_VBBCHANGE = "tune_vbbChange";
	public static final String TUNE_CHLNUMSCHANGE = "tune_chlnumsChange";
	public static final String LOGTYPE = "log_type";

	public static final String ADMIN_ROOT_PASSWORD_NOTIFY = "ADMIN_ROOT_PASSWORD_NOTIFY";

	public static final String START_AUTOSET = "START_AUTOSET";
	public static final String STOP_AUTOSET = "STOP_AUTOSET";

	public static final String UPDATE_MAC_DISPLAY = "updateMacDisplay";

	public static final String DURINGDMFETCH = "DURINGDMFETCH";

	public static final String REPAINT_CHARTSCREEN = "REPAINT_CHARTSCREEN";

	public static final String DOCK_REPAINT = "dock_repaint";
	public static final String UPDATE_TXT_LOCALES = "updateTxtLocales";
	public static final String SWITCH_PLAYPANE = "switchPlayPane";

	public static final String MACHINETYPE_CHANGE = "MACHINETYPE_CHANGE";
	public static final String SYNCOUTPUTCHANGE = "SYNCOUTPUTCHANGE";

	public static final String UPDATE_PK = "UPDATE_PK";
	public static final String updateFrameCounter = "updateFrameCounter";

	public static final String APPEND_TXT = "APPEND_TXT";
	public static final String APPEND_TXTLINE = "APPEND_TXTLINE";
	public static final String UPDATE_MARKBULLETIN = "update_markbulletin";
	public static final String UPDATE_MARKBULLETIN_BOUND = "update_markbulletin_bound";
	public static final String UPDATE_CURSOR = "UPDATE_CURSOR";
	public static final String TURN_ON_MARKBULLETIN = "TURN_ON_MARKBULLETIN";

	public static final String OPERATE_RUN = "OPERATE_RUN";
	public static final String OPERATE_STOP = "OPERATE_STOP";
	public static final String CHANGE_FFT = "CHANGE_FFT";

	public static final String UPDATE_PERSISTENCE_INDEX = "UPDATE_PERSISTENCE_INDEX";
	public static final String UPDATE_LINELINK = "UPDATE_LINELINK";
	public static final String UPDATE_FFT = "UPDATE_FFT";
	public static final String UPDATE_FACTORY_VIEW = "UPDATE_FACTORY_VIEW";

}
