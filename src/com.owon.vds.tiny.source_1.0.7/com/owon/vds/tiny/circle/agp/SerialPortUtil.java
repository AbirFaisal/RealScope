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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

public class SerialPortUtil implements SerialPortEventListener {
	/**
	 * 装载可用的串口
	 * 
	 * @return 可用串口的文本列表
	 */
	public static List<String> loadAvailablePort() {
		Enumeration portList;
		List<String> avaliblePortList;

		avaliblePortList = new ArrayList<String>(5);
		portList = CommPortIdentifier.getPortIdentifiers();

		CommPortIdentifier portId;
		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				avaliblePortList.add(portId.getName());
			}
		}
		return avaliblePortList;
	}

	public SerialPortUtil() {
	}

	private CommPortIdentifier portId;
	private SerialPort SrPt;

	public SerialPort openPort(String port) {
		try {
			portId = CommPortIdentifier.getPortIdentifier(port);
			SrPt = (SerialPort) portId.open("OWON_SerialPort", 1000);
			return SrPt;
		} catch (NoSuchPortException e) {
			e.printStackTrace();
		} catch (PortInUseException e) {
			e.printStackTrace();
		}
		return null;
	}

	private InputStream is;
	private OutputStream os;

	public InputStream getInputStream() {
		return is;
	}

	public OutputStream getOutputStream() {
		return os;
	}

	public void loadPort(int baudRate, int dataBits, int stopBits, int parity) {
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

	public void closeCOM() {
		try {
			if (os != null) {
				os.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (SrPt != null && portId != null && portId.isCurrentlyOwned()) {
				SrPt.close();
			}
		}
	}

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
			// l.handleEvent(null);

			break;

		default:
		}
	}
}
