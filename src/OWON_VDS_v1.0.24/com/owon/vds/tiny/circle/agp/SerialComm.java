package com.owon.vds.tiny.circle.agp;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.util.ArrayLogger;

public class SerialComm implements SerialPortEventListener {

	public static final int parity = 0;
	public static final int baudRate = 115200;
	public static final int stopBits = 1;
	public static final int dataBits = 8;

	private CommPortIdentifier portId;
	private SerialPort SrPt;

	private InputStream is;
	private OutputStream os;

	public SerialComm() {
		sb = new StringBuffer();
	}

	public boolean openPort(String port) {
		int len = port.getBytes().length;
		if (len < 0) {
			return false;
		}
		try {
			portId = CommPortIdentifier.getPortIdentifier(port);
			SrPt = (SerialPort) portId.open("OWON_SerialPort", 2000);

			init();
			return true;
		} catch (NoSuchPortException e) {
			config("NoSuchPortException" + "\r\n");
			e.printStackTrace();
		} catch (PortInUseException e) {
			config("PortInUseException" + "\r\n");
			e.printStackTrace();
		}
		return false;
	}
	

	protected void init() {
		try {
			is = SrPt.getInputStream();
			os = SrPt.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			SrPt.addEventListener(this);
		} catch (TooManyListenersException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			SrPt.setSerialPortParams(baudRate, dataBits, stopBits, parity);
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		SrPt.notifyOnDataAvailable(true);
	}

	private StringBuffer sb;
	private VLog vl = new VLog();

	public String comm(byte[] send) {
		flag = 0;
		sb.delete(0, sb.length());
		try {
			os.write(send);
			os.flush();
			ArrayLogger.outArray2Logable(vl, send, 0, send.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int timeout = 50;
		while (true) {
			waitToReceiveAll(100);
			if (flag >= 2) {
				configln();
				String s = sb.substring(sb.indexOf("$"), sb.indexOf("#") + 1);
				config("return: " + s + "\r\n");
				return s;
			}
			if (timeout-- < 0) {
				break;
			}
		}
		return "";
	}

	public void release() {
		try {
			if (os != null) {
				os.close();
			}
			if (is != null) {
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (SrPt != null && portId != null && portId.isCurrentlyOwned()) {
				SrPt.close();
			}
		}
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		/* Break interrupt */
		case SerialPortEvent.BI:
			break;
		/* Overrun error */
		case SerialPortEvent.OE:
			break;
		/* Framing error */
		case SerialPortEvent.FE:
			break;
		/* Parity error */
		case SerialPortEvent.PE:
			break;
		/* Carrier detect */
		case SerialPortEvent.CD:
			break;
		/* Clear to send */
		case SerialPortEvent.CTS:
			break;
		/* Data set ready */
		case SerialPortEvent.DSR:
			break;
		/* Ring indicator */
		case SerialPortEvent.RI:
			break;
		/* Output buffer empty */
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		/* Data available */
		case SerialPortEvent.DATA_AVAILABLE:
			// System.err.println(event.getOldValue() + "|" +
			// event.getNewValue());
			responseOnDataAvaible();
			break;
		default:
		}

	}

	int flag = 0;

	/*
	 * 在数据可用时处理
	 */
	private void responseOnDataAvaible() {
		config("responseOnDataAvaible\r\n");
		waitToReceiveAll(150);
		int size = 0;
		byte[] b = new byte[1];
		while (true) {
			try {
				size = is.read(b);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (flag >= 2) {
				configln();
				return;
			}

			char c = (char) b[0];

			if (c == '$' || c == '#') {
				flag++;
			}
			config((int) c + ", ");
			sb.append(c);
		}
	}

	private void configln() {
		System.out.println();
	}

	private void config(String string) {
		System.out.print(string);
	}

	/*
	 * 延迟一定时间以继续处理通讯
	 */
	private void waitToReceiveAll(int delay) {
		int delayRead = delay;
		try {
			Thread.sleep(delayRead);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}