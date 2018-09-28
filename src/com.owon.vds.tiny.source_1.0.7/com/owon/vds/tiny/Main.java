package com.owon.vds.tiny;

import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.global.WorkBench;
import com.owon.uppersoft.dso.global.WorkBenchTiny;

public class Main {
	public static void main(String[] args) {
		// UsbCommunicator.launch();
		Platform.launch(new Platform.PrincipleFactory() {

			@Override
			public WorkBench createWorkBench() {
				return new WorkBenchTiny();
			}

		});
		// com.owon.vds.transfer.Main.main(null);
	}
}
