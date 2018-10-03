package com.owon.uppersoft.vds.socket.server;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.owon.uppersoft.dso.global.ControlApps;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.source.comm.AbsInterCommunicator;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.socket.ScpiPool;
import com.owon.uppersoft.vds.socket.command.CmdFactory;
import com.owon.uppersoft.vds.util.FileUtil;
import com.owon.uppersoft.vds.util.Pref;

public class ServerControl {

	/** SCPI server startup and shutdown related controls */
	private Server socketServer;
	private int mPort = 5188;
	private final int DATABLOCK = 2048;
	private final int net_dmbuflen = 4288;

	public ServerControl(Pref p) {
		load(p);
	}

	private void load(Pref p) {
		mPort = p.loadInt("ScpiPort");
	}

	public void persist(Pref p) {
		p.persistInt("ScpiPort", mPort);
	}

	public void startServer() {
		startServer(mPort);

	}

	public void startServer(int port) {
		if (port > 0 && port < 65535) {
			socketServer = new Server(this, port);
			socketServer.start();
			// fire setProt().enable(true);
			
		}
	}

	public void destroyServer() {
		if (socketServer != null) {
			socketServer.terminateServer();
		}
		// fire setProt().enable(false);
	}

	public void startSCPIConsole() {
		socketServer.setupConsole();
	}

	public boolean setPort(int port) {
		if (port == socketServer.getPort())
			return false;

		boolean b = port > 0 && port < 65535;
		if (b) {
			destroyServer();
			socketServer = null;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mPort = port;
			startServer(port);
		}
		return b;
	}

	public int getPort() {
		return socketServer.getPort();
	}

	public void writeLargeData(SocketChannel channel, CmdFactory factory,
			String cmd) throws IOException {
		byte[] buf = factory.getProvider().getData(cmd);

		if (buf != null) {
			int dataLen = buf.length;
			int p = 0, bl = DATABLOCK, wn = 0;
			ByteBuffer head = ByteBuffer.allocate(4)
					.order(ByteOrder.BIG_ENDIAN);
			head.putInt(dataLen).flip();// head.position(0);
			channel.write(head);
			while (p < dataLen) {
				if (bl > dataLen - p)
					bl = dataLen - p;
				ByteBuffer block = ByteBuffer.wrap(buf, p, bl);
				wn = channel.write(block);
				p += wn;
				println("p:" + p, false);
			}
		} else {
			ByteBuffer head = ByteBuffer.allocate(4)
					.order(ByteOrder.BIG_ENDIAN);
			head.putInt(-1).flip();
			channel.write(head);
			ByteBuffer block = ByteBuffer.wrap(ScpiPool.Failed.getBytes());
			channel.write(block);
		}
	}


	private void println(String txt, boolean onOut) {
		// if (onOut)
		// System.out.println(txt);
		// else
		// System.err.println(txt);
	}


}
