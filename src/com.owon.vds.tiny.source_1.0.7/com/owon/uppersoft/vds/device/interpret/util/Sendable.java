package com.owon.uppersoft.vds.device.interpret.util;

import static com.owon.uppersoft.vds.device.interpret.util.PackUtil2.packCMD;
import static com.owon.uppersoft.vds.device.interpret.util.PackUtil2.packCMD2;

import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.core.comm.ICommunicateManager;
import com.owon.uppersoft.vds.core.comm.effect.JobUnitDealer;
import com.owon.uppersoft.vds.core.comm.job.JobUnit_I;
import com.owon.uppersoft.vds.device.interpret.CMDResponser;
import com.owon.uppersoft.vds.device.interpret.TinyCommunicationProtocol;
import com.owon.uppersoft.vds.util.ArrayLogger;
import com.owon.vds.firm.protocol.AddValueAttachCommand;
import com.owon.vds.firm.protocol.AddressAttachCommand;

/**
 * Sendable，提供标准的发送规范，说明传输协议可用的发送格式
 * 
 */
public class Sendable {
	public static final String SEND_CMD2_RESPONSE = "sendCMD2_response";
	public static final String SEND_CMD_RESPONSE = "sendCMD_response";

	private JobUnitDealer st;
	private Logable vl;

	public Sendable(JobUnitDealer st, Logable vl) {
		this.st = st;
		this.vl = vl;
	}

	public Sendable(JobUnitDealer st) {
		this(st, new VLog());
	}

	public void sendCMD(final AddressAttachCommand aac, final int value,
			final CMDResponser pc) {
		final int add = aac.address;
		final int bytes = aac.bytes;
		sendCMD(add, bytes, value, aac.name, pc);
	}

	public void sendCMDbyBytes(final AddressAttachCommand tca, final int value,
			final CMDResponser pc) {
		int add = tca.address;
		final int bytes = tca.bytes;
		// System.out.println(getHex(add) + " : " + getHex(value));

		int v;
		for (int p = 0; p < bytes; p++, add++) {
			v = (0xff) & (value >>> (p << 3));
			// System.out.println(getHex(v));
			sendCMD(add, 1, v, tca.toString() + '[' + p + ']', pc);
		}
	}

	public String getHex(int b) {
		return Integer.toHexString(b);
	}

	public void sendCMD(final int add, final int bytes, final int value,
			final CMDResponser pc) {
		sendCMD(add, bytes, value, "", pc);
	}

	public void sendCMD(AddValueAttachCommand aac, final CMDResponser pc) {
		sendCMD(aac.address, aac.bytes, aac.value, "", pc);
	}

	public JobUnit_I prepareCMD(final AddressAttachCommand tca,
			final int value, final String info, final CMDResponser pc) {
		final int add = tca.address;
		final int bytes = tca.bytes;
		return prepareCMD(add, bytes, value, info, pc);
	}

	public JobUnit_I prepareCMD(final int add, final int bytes,
			final int value, final String info, final CMDResponser pc) {
		final byte[] sd = packCMD(add, bytes, value);
		int len = TinyCommunicationProtocol.RESPONSE_LEN;
		final byte[] buf2 = new byte[len];
		JobUnit_I jui = new JobUnit_I(sd, buf2) {
			protected void init() {
				StringBuilder s = new StringBuilder();
				s.append("[Send: ] 0x");
				s.append(Integer.toHexString(add));
				s.append(", ");
				s.append(bytes);
				s.append(", 0x");
				String t = Integer.toHexString(value);
				s.append(t);

				s.append("  @");
				s.append(info);
				t = s.toString();

				setName(t);
			}

			@Override
			protected void prepareJob() {
				vl.logln(getName());
				ArrayLogger.outArray2LogableHex(vl, sd, 0, sd.length);
			}

			@Override
			protected void afterJob(ICommunicateManager sm) {
				if (pc != null)
					pc.onResponse(buf2, resNum, vl);
			}
		};
		return jui;
	}

	public void sendCMD(final int add, final int bytes, final int value,
			final String info, final CMDResponser pc) {
		JobUnit_I jui = prepareCMD(add, bytes, value, info, pc);
		st.addJobUnit(jui);
	}

	public void sendCMD2(final int bytes, final int value, final CMDResponser pc) {
		byte[] buf = packCMD2(bytes, value);
		int len = TinyCommunicationProtocol.RESPONSE_LEN;
		final byte[] buf2 = new byte[len];
		JobUnit_I jui = new JobUnit_I(buf, buf2) {
			@Override
			protected void init() {
				StringBuilder s = new StringBuilder();
				s.append("[Send: ] ");
				s.append(bytes);
				s.append(", 0x");
				String t = Integer.toHexString(value);
				s.append(t);
				t = s.toString();
				setName(t);
			}

			@Override
			protected void afterJob(ICommunicateManager sm) {
				if (pc != null)
					pc.onResponse(buf2, resNum, vl);
			}
		};
		st.addJobUnit(jui);
	}

	public static String getCommmandLog(AddressAttachCommand tca, int value) {
		int add = tca.address;
		int bytes = tca.bytes;

		StringBuilder s = new StringBuilder();
		s.append("[Send: ] 0x");
		s.append(Integer.toHexString(add));
		s.append(", ");
		s.append(bytes);
		s.append(", 0x");
		String t = Integer.toHexString(value);
		s.append(t);
		s.append(" @" + tca.name);
		t = s.toString();
		return t;
	}

	public static int writeCommmand(ICommunicateManager ism,
			AddressAttachCommand tca, int value) {
		byte[] buf = PackUtil2.packCMD(tca, value);
		return ism.write(buf, buf.length);
	}

	public static int writeCommmand(ICommunicateManager ism,
			AddValueAttachCommand tca) {
		byte[] buf = PackUtil2.packCMD(tca);
		return ism.write(buf, buf.length);
	}

	public static int interCommmands(ICommunicateManager ism,
			AddValueAttachCommand tca, byte[] buf2, Logable lg) {
		int add = tca.address;
		int bytes = tca.bytes;
		int value = tca.value;
		byte[] buf = PackUtil2.packCMD(add, bytes, value);

		lg.logln(tca);

		int len = buf.length;
		int wn = ism.write(buf, len);
		if (wn < len)
			return -1;

		int rn = ism.acceptResponse(buf2, buf2.length);
		return rn;
	}

}