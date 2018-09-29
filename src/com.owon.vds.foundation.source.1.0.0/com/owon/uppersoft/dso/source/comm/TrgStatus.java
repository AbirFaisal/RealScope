package com.owon.uppersoft.dso.source.comm;

public enum TrgStatus {
	/**
	 * 这几种为同下位机定义的触发状态，位置固定，它们也都是在和下位机保持连接的状态
	 * 
	 * 可点击弹出菜单来断开连接
	 */
	Auto, // 自动触发模式
	Ready, // 等待触发
	Trg_d, // 已经触发
	Scan, // 慢扫描
	Stop, // 停止
	Error, // 出错

	/** 中途断开的情况需要重新匹配，因为下位机可能重启了，或中断后上位机做了一些操作没有设置下去 */
	ReCfg, // 因为某些原因和下位机重新同步设置 == 6
	AutoSetting, // 自动设置进行中

	/**
	 * 以下几种是在未和下位机保持连接的状态
	 * 
	 * 
	 */
	Offline, // 和下位机无连接
	USBFound, // 发现可用的下位机USB设备
	USBDrvErr, // USB驱动安装出错
	MachineNotSupport, // 无法识别的下位机设备

	/**
	 * 以下几种为尝试和下位机进行连接过程中的状态
	 */
	Linking, // 和下位机连接中
	Connect, // 和下位机连接成功
	Detect, // 和下位机匹配机型中
	Initialize;// 和下位机同步设置中

	@Override
	public String toString() {
		if (this == Trg_d) {
			return "Trig'd";
		} else if (this == Detect) {
			return "Match";
		} else if (this == Initialize) {
			return "Syncing";
		} else if (this == ReCfg) {
			return "ReSyncing";
		} else if (this == AutoSetting) {
			return "AutoSet...";
		}
		return super.toString();
	}

	public static final TrgStatus[] VALUES = TrgStatus.values();

	public static boolean isFreqShowableOnTrgStatus(int i) {
		TrgStatus ts = VALUES[i];
		return ts == Trg_d || ts == Stop || ts == Auto || ts == Scan;
	}

	public static void main(String[] args) {
	}
}