package com.owon.uppersoft.dso.mode.control;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import com.owon.uppersoft.dso.data.LObject;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.vds.core.aspect.base.IOrgan;
import com.owon.uppersoft.vds.core.comm.effect.IPatchable;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.ui.prompt.FadeIOShell;
import com.owon.uppersoft.vds.util.Pref;

/**
 * Machine System Control
 * 
 */
public class SysControl implements IOrgan, IPatchable {
	public static final int SYNOUT_PF = 2;
	public static final int SYNOUT_TrgOut = 1;
	public static final int SYNOUT_TrgIn = 0;

	public static final List<LObject> syncs = new LinkedList<LObject>();
	static {
		syncs.add(new LObject("M.Utility.SYNO.TrgIn"));
		syncs.add(new LObject("M.Utility.SYNO.TrgOut"));
		syncs.add(new LObject("M.Utility.SYNO.PF"));
	}

	public byte[] link_ip_address = { (byte) 192, (byte) 168, (byte) 1,
			(byte) 72 };

	public int link_port = 3000;

	public byte[] ipaddress = { (byte) 192, (byte) 168, (byte) 1, (byte) 72 };

	public byte[] smaddress = { (byte) 255, (byte) 255, (byte) 255, (byte) 0 };

	public byte[] gwaddress = { (byte) 192, (byte) 168, (byte) 1, (byte) 1 };

	private byte[] macaddress = new byte[6];

	public int port = 3000;

	private int sync_output = 0;

	public SysControl() {
	}

	public void load(Pref p, boolean resetNetwork) {
		if (resetNetwork) {
			link_ip_address = p.loadBytes("NetWork.linkIP",
					link_ip_address.length);
			link_port = p.loadInt("NetWork.linkPort");

			ipaddress = p.loadBytes("NetWork.setIP", ipaddress.length);
			smaddress = p.loadBytes("NetWork.setSubnetmask", smaddress.length);
			gwaddress = p.loadBytes("NetWork.setGateway", gwaddress.length);
			port = p.loadInt("NetWork.setPort");
			macaddress = p.loadBytes("NetWork.setMac", macaddress.length);

		}
		setSyncOutput(p.loadInt("NetWork.Sync_out"));

		/** 开机时不要保持TrigIn的状态 */
		if (getSyncOutput() == 0) {
			setSyncOutput(1);
		}
	}

	public void persist(Pref p) {
		p.persistBytes("NetWork.linkIP", link_ip_address);
		p.persistInt("NetWork.linkPort", link_port);

		p.persistBytes("NetWork.setIP", ipaddress);
		p.persistBytes("NetWork.setSubnetmask", smaddress);
		p.persistBytes("NetWork.setGateway", gwaddress);
		p.persistInt("NetWork.setPort", port);
		p.persistBytes("NetWork.setMac", macaddress);
		p.persistInt("NetWork.Sync_out", getSyncOutput());

	}

	public void selfSubmit(Submitable sbm) {
		/** 在网络设置时，会将设置的值保存，所以再批量设置的时候再设置一下也是可以的 */
		sbm.c_sync_output(getSyncOutput());
	}

	public final boolean netOn = true;

	public void c_network() {
		Submitable sbm = SubmitorFactory.reInit();
		if (netOn) {
			sbm.c_network(netOn, ipaddress, port, gwaddress, smaddress,
					macaddress);
		} else {
			sbm.c_network_off();
		}
		sbm.apply();
	}

	public void c_setSyncOut(int syncOut) {
		setSyncOutput(syncOut);

		Submitable sbm = SubmitorFactory.reInit();
		sbm.c_sync_output(syncOut);
		sbm.apply();
	}

	public void setSyncOutput(int syncOut) {
		this.sync_output = syncOut;
	}

	public int getSyncOutput() {
		return sync_output;
	}

	public String getIP() {
		return getIP(ipaddress);
	}

	public boolean setIP(String ip) {
		return c_setIP(ip, ipaddress);
	}

	public String getGATeway() {
		return getIP(gwaddress);
	}

	public boolean setGATeway(String ip) {
		return c_setIP(ip, gwaddress);
	}

	public String getSMASk() {
		return getIP(smaddress);
	}

	public boolean setSMASk(String ip) {
		return c_setIP(ip, smaddress);
	}

	private String getIP(byte[] arr) {
		StringBuilder sb = new StringBuilder("");
		for (int i = 0, len = arr.length; i < len; i++) {
			// System.out.println(ipaddress[i] + "," + (ipaddress[i] & 0xFF));
			String byt = Integer.toString(arr[i] & 0xFF);// Integer.toHexString
			sb.append(byt);
			if (i < len - 1)
				sb.append(".");
		}
		return sb.toString();
	}

	private boolean c_setIP(String ip, byte[] arr) {
		String[] parts = ip.split("\\.");
		if (parts.length != 4)
			return false;
		for (int i = 0; i < 4; i++) {
			int p = 0;
			try {
				p = Integer.parseInt(parts[i]);
			} catch (Exception e) {
				return false;
			}
			arr[i] = (byte) (p & 0xFF);
		}
		// System.out.println("ipaddress:" + getIP());
		// System.out.println("smaddress:" + getSMASk());
		// System.out.println("gwaddress:" + getGATeway());
		c_network();
		return true;
	}

	public String getMac() {
		StringBuilder sb = new StringBuilder("");
		for (int i = 0; i < macaddress.length; i++) {

			String byt = Integer.toHexString(macaddress[i] & 0xFF);
			if (byt.length() < 2) {
				sb.append("0");
			}
			sb.append(byt);
		}
		return sb.toString().toUpperCase();
	}

	public final void saveMac(String mac) {
		if (!isMacvalid(mac))
			return;

		byte[] address = macaddress;
		String byt;
		int v = 0;
		for (int i = 0; i < address.length; i++) {
			int begin = i << 1;
			byt = mac.substring(begin, begin + 2);
			try {
				v = Integer.parseInt(byt, 16);
			} catch (Exception ex) {
			}
			address[i] = (byte) v;
			// System.out.print((address[i] & 0xff) + "-");
		}
		// System.out.println();
	}

	public final boolean isMacvalid(String mac) {
		String msg;
		ResourceBundle rb = I18nProvider.bundle();
		if (mac.length() != 12) {
			msg = rb.getString("M.Utility.MachineSetting.Unfit");
			new FadeIOShell().prompt(msg, Platform.getMainWindow().getFrame());
			return false;
		}

		mac = mac.toUpperCase();
		for (int i = 0; i < mac.length(); i++) {
			int ascii = mac.charAt(i);
			if (!(ascii >= '0' && ascii <= '9')
					&& !(ascii >= 'A' && ascii <= 'F')) {
				msg = rb.getString("M.Utility.MachineSetting.Invalid");
				new FadeIOShell().prompt(msg, Platform.getMainWindow()
						.getFrame());
				return false;
			}
		}
		return true;
	}

}
