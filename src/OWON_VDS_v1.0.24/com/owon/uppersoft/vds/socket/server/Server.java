package com.owon.uppersoft.vds.socket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.owon.uppersoft.vds.socket.ScpiPool;
import com.owon.uppersoft.vds.socket.command.CmdFactory;
import com.owon.uppersoft.vds.socket.command.CommandKey;

public class Server extends Thread {
	/** customize */
	public static final int DEFAULT_PORT = 5188;

	private int mPort;
	private CmdFactory factory;
	private ServerControl svCtr;
	private ScpiConsole dbgView;
	// TODO Secondary method: here to determine whether the trans tag is passed, until the end of the register (selector, OP_WRITE), currently using segmentation to send adc
	private boolean onlargeTransmit = false, onDmTransmit = false;
	protected boolean isRemoteTransmit = false;
	private boolean isExit = false;

	/** Necessary definition */
	protected ByteBuffer clientBuffer = ByteBuffer.allocate(1024);// equivalent to reciveBuffer, receives client commands
	// protected CharsetDecoder decoder;
	protected Selector selector;
	ServerSocketChannel server;

	public Server(ServerControl sc, int port) {
		this.mPort = port;
		this.svCtr = sc;
		factory = new CmdFactory();
		
	}

	public void terminateServer() {
		if (selector != null)
			selector.wakeup();
		isExit = true;

		try {
			server.socket().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			selector = getSelector(mPort);
			listen();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	// get the SelectorSelector
	protected Selector getSelector(int port) throws IOException {
		// ServerSocketChannel
		server = ServerSocketChannel.open();
		Selector sel = Selector.open();// static method, creates a selector instance
		server.socket().bind(new InetSocketAddress(port));
		server.configureBlocking(false);
		server.register(sel, SelectionKey.OP_ACCEPT);
		return sel;
	}

	// listen on the port
	public void listen() {
		try {
			while (!isExit) {
				selector.select();
				Iterator<SelectionKey> iter = selector.selectedKeys()
						.iterator();
				while (iter.hasNext()) {
					SelectionKey key = (SelectionKey) iter.next();
					iter.remove();
					process(key);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Handling events */
	protected void process(SelectionKey key) throws IOException {
		if (key.isAcceptable()) { // Receive the request. Test if the channel for this key is ready to accept a new socket connection.
			// Returns the channel that created this key.
			ServerSocketChannel server = (ServerSocketChannel) key.channel();
			// Accept the connection to this channel socket. The returned socket channel (if any) will be in blocking mode.
			SocketChannel client = server.accept();
			// Set non-blocking mode
			client.configureBlocking(false);
			// register with the selector, wait for connections
			client.register(selector, SelectionKey.OP_READ);
		} else if (key.isReadable()) {
			SocketChannel client = (SocketChannel) key.channel();
			int count = -1;
			try {
				/** The read function must catch the exception and close the SocketChannel when the client is abnormally disconnected, otherwise the server can no longer connect. */
				count = client.read(clientBuffer);// read the data sent by the client into the buffer
			} catch (Exception e) {
				client.close();
			}
			if (count > 0) {
				clientBuffer.flip();
				// byte[] buf = new byte[count];
				// clientBuffer.get(buf);
				byte[] buf = clientBuffer.array();
				String cmd = new String(buf, 0, count).trim();

				if (dbgView != null)
					dbgView.addReadText(cmd);

				cmd += "\r";// used as the terminator
				String trans = translation(cmd);
				if (trans != null && !trans.equalsIgnoreCase("")) {
					SelectionKey sKey = client.register(selector,
							SelectionKey.OP_WRITE);
					sKey.attach(trans);
				}
			} else {
				client.close();
			}
			clientBuffer.clear();
		} else if (key.isWritable()) { // write event
			SocketChannel client = (SocketChannel) key.channel();
			String cmd = (String) key.attachment();

			if (onlargeTransmit) {
				svCtr.writeLargeData(client, factory, cmd);
				onlargeTransmit = false;
			} else if (onDmTransmit) {
				onDmTransmit = false;
				isRemoteTransmit = false;
			} else {
				ByteBuffer block = ByteBuffer.wrap(cmd.getBytes());
				// System.out.println(" " + cmd.trim());
				if (dbgView != null)
					dbgView.addWriteText(cmd);

				client.write(block);// write/send data to the client
			}
			client.register(selector, SelectionKey.OP_READ);
			// client.close();
		}
	}

	protected String translation(String cmd) {
		String[] splits = cmd.split(";");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < splits.length; i++) {
			sb.append(analyse(splits[i]));
		}
		return sb.toString();
	}

	private String analyse(String line) {
		line = line.trim();// remove /r/n
		String str = (String) analyseCMDs_1(line);
		return str + "\r";
	}

	public Object analyseCMDs_1(String line) {
		line = line.toUpperCase().replaceAll("\\s*", "");

		if (dealADC_CMDs(line))
			return line;

		/** dealNormal_CMDs */
		CommandKey trunk = pickOutTrunk(line);

		CommandKey node = null;
		if (trunk != null)
			node = trunk.hasKey(line, null);
		if (node != null)
			return node.handle();
		return ScpiPool.ErrAnalyse;
	}

	private boolean dealADC_CMDs(String line) {
		boolean a = line.startsWith(CmdFactory.ADC);
		boolean b = line.startsWith(CmdFactory.LDM_ADC);
		boolean c = line.startsWith(CmdFactory.RDM_ADC);
		if (a) {
			onlargeTransmit = true;
			return true;
		} else if (b) {
			onDmTransmit = true;
			isRemoteTransmit = false;
			return true;
		} else if (c) {
			onDmTransmit = true;
			isRemoteTransmit = true;
			return true;
		} else
			return false;
	}

	private CommandKey pickOutTrunk(String line) {
		CommandKey cmd = null;
		String line_head = line;
		if ((line.startsWith(":")))
			line_head = line.split(":", 3)[1];//split 3-1 times, into 3 groups

		// System.err.println(line_head);
		for (CommandKey ck : factory.getKeys()) {
			if (line_head.startsWith(ck.getFitPre())
					|| line_head.startsWith(ck.getShortPre())) {
				cmd = ck;
				break;
			}
		}

		return cmd;
	}

	public void setupConsole() {
		dbgView = ScpiConsole.getInstance();
	}

	public int getPort() {
		return mPort;
	}

}
