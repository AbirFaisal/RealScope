package com.owon.uppersoft.dso.function;

import java.awt.event.KeyEvent;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.util.PropertiesItem;
import com.owon.uppersoft.vds.core.aspect.IBoard;
//import com.owon.uppersoft.vds.core.update.UpdateDetection;

/**
 * SoftwareControl，Store software configuration information
 * 
 */
public class SoftwareControl implements IBoard {
	public static final int BrightnessRemaining = 40;
	public static final int BrightnessLimit = 255 - BrightnessRemaining;

	/** 此部分为下位机的相关版本信息 */
	private String machine_header = "";// // 机型号SPBVDSXXXX
	public String machine_name = "";

	private String m_series = "";

	/**
	 * 形如V1.2.0，前两个数位用来标记下位机的生产版本，是线性的，根据生产改动的批次进行叠加；
	 * 
	 * 第3位用来标记可能的出厂后升级版本，是并列的，一旦有不同的改动就可以定义为一个数值
	 */
	private String m_version = "";

	private ControlManager cm;

	public SoftwareControl(ControlManager cm) {
		this.cm = cm;
		String machine_header1 = cm.getAllChannelsNumber() > 2 ? "SPBVDSxxx4"
				: "SPBVDSxxx2";
		this.machine_header = machine_header1;
	}

	/** This section is information related to the version of the file read and written by the software. */
//	private int fileVer = VDS_Portable.FILEPROTOCOL_VDS_CURRENT;// 文件格式版本
//	private int recordVer = VDS_Portable.RECORDPROTOCOL_VDS_CURRENT;// 文件格式版本
//	private int refFileVer = VDS_Portable.REFPROTOCOL_VDS_CURRENT;// 文件格式版本
//	public int getFileVersion() {
//		return fileVer;
//	}
//	public int getRecordVer() {
//		return recordVer;
//	}
//	public int getRefFileVer() {
//		return refFileVer;
//	}

	private PWDValidator pwd = new PWDValidator();

	public void validatPWDInput(int cd) {
		boolean b = pwd.input(cd);
		// System.err.println(b);
		if (b)
			cm.pcs.firePropertyChange(
					PropertiesItem.ADMIN_ROOT_PASSWORD_NOTIFY, null,
					Boolean.valueOf(isPWDOpen()));
	}

	public void quickPass() {
		validatPWDInput(KeyEvent.VK_NUMPAD1);
		validatPWDInput(KeyEvent.VK_NUMPAD3);
		validatPWDInput(KeyEvent.VK_NUMPAD5);
		validatPWDInput(KeyEvent.VK_NUMPAD2);
		validatPWDInput(KeyEvent.VK_NUMPAD4);
	}

	public boolean isPWDOpen() {
		return pwd.isOpen();
	}

	public void setBoardVersion(String m_version) {
		this.m_version = m_version;
	}

	public String getBoardVersion() {
		return m_version;
	}

	public boolean isVDS2062Bandlimit() {
		// BoardVersion < 3.0 Turn off Bandlimit
		final String threshold = "3.0";
		try {
			String boardver = m_version.substring(1).trim();// 去除V
			if (compareVersion(boardver, threshold) < 0)
				return false;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isVDS2064Bandlimit() {
		// BoardVersion <= 1.0.3 Turn off Bandlimit
		final String threshold = "1.0.3";
		try {
			String boardver = m_version.substring(1).trim();// 去除V
			if (compareVersion(boardver, threshold) <= 0)
				return false;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// 结果为正前者大，为负前者小，为零相等
	private static int compareVersion(String boardver, String ver) {
		return 1;
	}

	public void setBoardSeries(String m_series) {
		this.m_series = m_series;
	}

	public String getBoardSeries() {
		return m_series;
	}

	public void setMachineHeader(String machine_header) {
		this.machine_header = machine_header;
	}

	public String getMachineHeader() {
		return machine_header;
	}
}
