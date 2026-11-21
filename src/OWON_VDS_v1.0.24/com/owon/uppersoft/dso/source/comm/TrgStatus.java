package com.owon.uppersoft.dso.source.comm;

public enum TrgStatus {
	/**
	 * These are the trigger states defined together with the device, at fixed positions; all of them are states where the connection to the device is maintained
	 * 
	 * Click to pop up a menu to disconnect
	 */
	Auto, // Auto trigger mode
	Ready, // Waiting for trigger
	Trg_d, // Already triggered
	Scan, // Slow sweep
	Stop, // Stopped
	Error, // Error

	/** After a mid-way disconnection the settings must be matched again, since the device may have restarted, or the host may have made changes after the interruption that were not applied */
	ReCfg, // Resynchronizing settings with the device for some reason == 6
	AutoSetting, // Auto setting in progress

	/**
	 * The following states are those where the connection to the device is not maintained
	 * 
	 * 
	 */
	Offline, // No connection to the device
	USBFound, // Found an available USB device (scope)
	USBDrvErr, // USBUSB driver installation error
	MachineNotSupport, // Unrecognized scope device

	/**
	 * The following states occur while attempting to connect to the device
	 */
	Linking, // Linking to the device
	Connect, // Connected to the device
	Detect, // Matching the model with the device
	Initialize;// Synchronizing settings with the device

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