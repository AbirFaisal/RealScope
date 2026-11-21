package com.owon.uppersoft.dso.source.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.owon.uppersoft.vds.core.comm.ICommunicateManager;
import com.owon.uppersoft.vds.core.comm.ISource;
import com.owon.uppersoft.vds.util.PrimaryTypeUtil;
import com.owon.uppersoft.vds.util.SystemPropertiesUtil;

/**
 * Manages the state and opening of the network data source, provides the network data source
 * 
 */
public class NetSourceManager implements ICommunicateManager, ISource {

	protected static final int header = 41;
	protected static final int LoopBufsize = 512;
	protected static final int TimeOut = 4000;//2600;

	protected Socket skt;
	protected InputStream is;
	protected OutputStream os;

	protected byte[] ip;
	protected int port;

	public NetSourceManager() {
	}

	@Override
	public int retryTimes() {
		return 3;
	}

	@Override
	public boolean tryRescue() {
		logln("disconnect and ...");
		disconnect();

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return connect(ip, port);
	}

	public boolean connect(byte[] ip, int port) {
		if (SystemPropertiesUtil.isWin32System()) {
			try {
				@SuppressWarnings("unused")
				Process ProcessHandle = Runtime.getRuntime().exec(
						"cmd /c arp -d");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.ip = ip;
		this.port = port;
		try {
			logln("connect");
			skt = new Socket(InetAddress.getByAddress(ip), port);

			logln("new socket: " + PrimaryTypeUtil.getIPAddress(ip, 0)
					+ ", port: " + port);

			logln("get socket io");
			is = skt.getInputStream();
			os = skt.getOutputStream();
			skt.setSoTimeout(TimeOut);
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			logln("connect UnknownHostException");
		} catch (IOException e) {
			e.printStackTrace();
			logln("connect IOException");
		}

		return false;
	}

	public void disconnect() {
		try {
			skt.close();
		} catch (IOException e) {
			e.printStackTrace();
			logln("disconnect err");
		}
	}

	public boolean isConnected() {
		return skt.isConnected();
	}

	@Override
	public int read(byte[] arr, int start, int len) throws IOException {
		return is.read(arr, start, len);
	}

	/** The methods below are only valid when the usb connection is open */
	@Override
	public int acceptResponse(byte[] arr, int len) {
		if (isConnected()) {
			try {
				return is.read(arr, 0, len);
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		} else
			return -1;
	}

	@Override
	public int write(byte[] arr, int len) {
		if (os == null) {
			logln("Erro: os is null!");
			return -1;
		}
		try {
			os.write(arr, 0, len);
			// outArrayAlpha(arr, 0, len);
			logln("net write: " + len);
			return len;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	protected void logln(String string) {
	}

	protected void log(String string) {
	}

	protected void println(String txt) {
	// System.err.println(txt);
	}
}
