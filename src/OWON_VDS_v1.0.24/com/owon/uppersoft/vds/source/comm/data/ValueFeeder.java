package com.owon.uppersoft.vds.source.comm.data;

import com.owon.uppersoft.dso.wf.ON_WF_Iterator;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.comm.ICommunicateManager;
import com.owon.uppersoft.vds.device.interpret.TinyCommunicationProtocol;
import com.owon.uppersoft.vds.device.interpret.util.Sendable;
import com.owon.uppersoft.vds.util.TimeMeasure;
import com.owon.vds.firm.protocol.AddressAttachCommand;

public class ValueFeeder {

	private boolean forceFeedMiddle;
	private AddressAttachCommand[] channel_freqref;

	public ValueFeeder(AddressAttachCommand[] channel_freqref) {
		this.channel_freqref = channel_freqref;
		forceFeedMiddle = false;
	}

	public void forceFeedMiddle() {
		forceFeedMiddle = true;
	}

	public void resumeFeedMiddle() {
		forceFeedMiddle = false;
	}

	private TimeMeasure tm = new TimeMeasure();
	/** 单例数组优化频繁使用 */
	private byte[] RESPONSE_BUF = new byte[TinyCommunicationProtocol.RESPONSE_LEN];

	protected void syncChannelsMiddle(ICommunicateManager ism,
			ON_WF_Iterator owfi) {
		if (!forceFeedMiddle) {
			tm.stop();
			// System.out.println("check syncChannelsMiddle");
			boolean freshFreq = (tm.measure() > 2000);
			if (!freshFreq)
				return;

			// System.out.println("syncChannelsMiddle");

			tm.start();
		}

		while (owfi.hasNext()) {
			WaveForm wf = owfi.next();
			int chl = wf.getChannelNumber();
			int mid = wf.computeMiddle();
			if (mid == Integer.MAX_VALUE)
				continue;

			int wn = Sendable.writeCommmand(ism, channel_freqref[chl], mid);

			String msg = Sendable.getCommmandLog(channel_freqref[chl], mid);
			// vl.logln(msg);

			if (wn <= 0) {
				continue;
			}
			byte[] rep = RESPONSE_BUF;
			int rn = ism.acceptResponse(rep, rep.length);
			if (rn <= 0) {
				continue;
			}
			// int v = EndianUtil.nextIntL(rep, 1);
			// logln("v: " + v);

		}
	}
}