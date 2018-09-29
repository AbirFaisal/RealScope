package com.abirfaisal.Realscope;

import com.oracle.tools.packager.IOUtils;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.global.WorkBench;
import com.owon.uppersoft.dso.global.WorkBenchTiny;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.usb4java.*;
import sun.nio.ch.IOUtil;

import java.io.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class Main  extends Application {

	static short VENDOR_ID = 0x5345;
	static short PRODUCT_ID = 0x1234;
	static byte USB_EP_OUT = 0x03;
	static byte USB_EP_IN = (byte) 0x81;
	static Device usbDevice;


	static int lb = -2500;
	static int ub = 2500;

	static NumberAxis xAxis = new NumberAxis(lb,ub,250);
	static NumberAxis yAxis = new NumberAxis(-3,3,1);
	static LineChart lineChart = new LineChart(xAxis,yAxis);
	static XYChart.Series series = new XYChart.Series();
	static XYChart.Series minMarker = new XYChart.Series();
	static XYChart.Series maxMarker = new XYChart.Series();
	SwingNode swingNode = new SwingNode();

	@Override
    public void start(Stage primaryStage) throws Exception{



    	lineChart.getData().add(series);

    	lineChart.getData().add(minMarker);
    	lineChart.getData().add(maxMarker);

    	AnchorPane.setTopAnchor(lineChart,0.0);
	    AnchorPane.setBottomAnchor(lineChart,0.0);
	    AnchorPane.setLeftAnchor(lineChart,0.0);
	    AnchorPane.setRightAnchor(lineChart,0.0);
	    lineChart.setCreateSymbols(false);



	    AnchorPane anchorPane = new AnchorPane(lineChart);

        primaryStage.setTitle("RealScope");
        primaryStage.setScene(new Scene(anchorPane, 1280, 720));
        primaryStage.show();
    }



	static void createSwingContent(final SwingNode swingNode){

		Platform.launch(new Platform.PrincipleFactory() {

			@Override
			public WorkBench createWorkBench() {
				return new WorkBenchTiny();
			}

		});
	}

    public static void main(String[] args) throws IOException, InterruptedException {



		//createWorkBench();

    	File file = new File("data.csv");
	    Scanner scanner = new Scanner(file);
	    //scanner.useDelimiter(",");

	    double dataArray[] = new double[5000];

	    String s;
	    double value;
	    double minValue = 0.0;
	    double maxValue = 0.0;

	    double values[] = new double[5000];

	    for (int i = lb; i < ub; i++) {
	    	s = scanner.next();

	    	s = s.substring(s.lastIndexOf(",") + 1); // get number after comma in CSV file

		    value = Float.valueOf(s) / 1000.0; //convert mV to Volts

		    values[i + Math.abs(lb)] = value;

		    series.getData().add(new XYChart.Data(i, value));

		    //Keep Track of min and max values
		    if (minValue > value) minValue = value;
		    if (maxValue < value) maxValue = value;
	    }

	    //lineChart.setStyle("-fx-stroke-width: 0;");

	    //Min Max Markers
	    minMarker.getData().add(new XYChart.Data(lb, minValue));
	    minMarker.getData().add(new XYChart.Data(ub, minValue));

	    maxMarker.getData().add(new XYChart.Data(lb, maxValue));
	    maxMarker.getData().add(new XYChart.Data(ub, maxValue));



	    //Output Some Measurements

	    double VAVG = 0.0;
	    double VAVG_DIVISOR = 5000.0;
	    for (int i = 0; i < VAVG_DIVISOR; i++) {
	    	VAVG = VAVG + (double)Math.abs(values[i]);
	    }
	    VAVG = VAVG / (double) VAVG_DIVISOR;
	    System.out.println("DC VAVG = " + VAVG);

	    double VPP = Math.abs(minValue)+Math.abs(maxValue);
	    System.out.println("VPP = " + BigDecimal.valueOf(VPP).setScale(4,BigDecimal.ROUND_UP));

	    double VRMS = VPP / Math.sqrt(2.0); //This only works for sine waves
	    System.out.println("VRMS = " + BigDecimal.valueOf(VRMS).setScale(4,BigDecimal.ROUND_UP));



	    //1KHz unit=mV
//	    for (int i = 0; i < 10; i++) {
//
//
//	    }







    	//Initialize LibUSB
	    //Context context = new Context();
//	    int result = LibUsb.init(context);
//	    if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to initialize libusb.", result);

//	    //Find USBDevice
//	    usbDevice = findDevice(VENDOR_ID, PRODUCT_ID);
//	    System.out.println(usbDevice);
//
//	    //Claim USB Device
//	    DeviceHandle deviceHandle = new DeviceHandle();
//	    LibUsb.open(usbDevice, deviceHandle);
//	    result = LibUsb.claimInterface(deviceHandle, 0);
//	    if (result == 0 ) { System.out.println("Claimed Interface: " + result); }
//
//	    //
//
//
//
//	    byte[] data = new byte[]{1, 64, 0, 0, 1, 86};
//	    sendUsbData(deviceHandle, data);
//	    recieveUsbData(deviceHandle);
//
//	    data = new byte[]{1, 64, 0, 0, 1, 86};
//	    sendUsbData(deviceHandle, data);
//	    recieveUsbData(deviceHandle);
//
//
//	    data = new byte[]{35, 2, 0, 0, 1};
//	    sendUsbData(deviceHandle, data);
//	    recieveUsbData(deviceHandle);
//
//	    //Get Flash Data size = 2002
//	    data = new byte[]{-80, 1, 0, 0, 1, 1};
//	    sendUsbData(deviceHandle, data);
//	    for (int i = 0; i < 32; i++) {
//		    recieveUsbData(deviceHandle);
//	    }
//
//
//	    //Prepare for FPGA Send
//	    data = new byte[]{35, 2, 0, 0, 1};
//	    sendUsbData(deviceHandle, data);
//	    recieveUsbData(deviceHandle);
//
//	    //Send FPGA data
//	    //10236 bytes
//	    sendFPGA(deviceHandle);
//
//
//
//	    //De initialize LibUSB
//	    LibUsb.releaseInterface(deviceHandle, 0);
//	    LibUsb.exit(context);

        launch(args);
    }

    static void sendFPGA(DeviceHandle deviceHandle) throws IOException, InterruptedException {

	    byte[] data = new byte[]{0, 0, 0, 0};
	    long filesize;

	    File fpgaFile = new File("fpga/vds1022/VDS1022_FPGA_V3.5.bin");
	    System.out.println("FPGA File Size: " + fpgaFile.length() + " Bytes");
	    int fpgaFileSize = (int) fpgaFile.length();
	    byte buffer[] = new byte[64];
	    byte largeBuffer[] = new byte[fpgaFileSize];


	    buffer = make_int32(buffer, fpgaFileSize);
	    //sendPackedCommand(deviceHandle,'D',);


	    //sendPackedCommand(deviceHandle, 0x4000, 4);


    }

    static int getResponse(DeviceHandle deviceHandle, char responseCode){
    	int response = -1;
    	byte buffer[] = new byte[5];

    	buffer = recieveBulkCommand(deviceHandle, 5);

	    response = buffer[1] | (buffer[2] << 8) | (buffer[3] << 16) | (buffer[4] << 24);

    	return response;
    }

    static byte[] recieveBulkCommand(DeviceHandle deviceHandle, int bufferLength){

	    int response, tmp;
	    byte buffer[] = new byte[bufferLength];

		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bufferLength);
		IntBuffer intBuffer = IntBuffer.allocate(bufferLength);

		response = LibUsb.bulkTransfer(deviceHandle, USB_EP_IN, byteBuffer, intBuffer, timeout);


	    for (int i = 0; i < bufferLength; i++) {
	    	buffer[i] = byteBuffer.get(i);
	    }

	    System.out.println("Recieved: ");
	    hexdump(buffer, bufferLength);
	    System.out.println("\n");

	    return buffer;
    }


    static void sendPackedCommand(DeviceHandle deviceHandle, int cmd, byte data, int commandLength){

	    int ret;
	    byte buffer[] = new byte[32];

	    if (commandLength < 0 || commandLength > 24)
		    System.out.println("sendPackedCommand Error: Out of Bounds");

	    make_int32(buffer, cmd);

	    buffer[4] = (byte) commandLength;


	    System.arraycopy(data, 5,buffer,0,commandLength);


	    //ret = sendUsbBulkTransfer(deviceHandle, buffer, commandLength + 5);


    }

	static byte[] make_int32(byte buffer[], int val) {
		buffer[0] = (byte) (val & 0xff);
		buffer[1] = (byte) ((val >> 8) & 0xff);
		buffer[2] = (byte) ((val >> 16) & 0xff);
		buffer[3] = (byte) ((val >> 24) & 0xff);

		return buffer;
	}

	static void sendUsbBulkTransfer(DeviceHandle deviceHandle, byte[] b, int length){




	}

	static void hexdump(byte buffer[], int bufferLength) {
		for (int i = 0; i < bufferLength; ++i)
			System.out.print(" " + buffer[i]);
	}


    static int timeout = 100;

    static void sendUsbData(DeviceHandle handle, byte[] bytes) throws LibUsbException {

	    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(8); //TODO Make Global Variable
	    IntBuffer transfered = IntBuffer.allocate(1);
	    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
	    byte b = 0;
	    int i;

    	if (bytes.length > 8){
		    int cycles = bytes.length / 8;

    		for (i = 0; i < cycles; i++) {
			    //put 8 bytes into buffer
			    for (int j = 0; j < 8; j++) {
				    b = (byte) byteArrayInputStream.read();
				    byteBuffer.put(b);
			    }

			    //Send Data
			    int result = LibUsb.bulkTransfer(
			    		handle,
					    USB_EP_OUT,
					    byteBuffer,
					    transfered,
					    timeout); //TODO make address global


			    //if (result != LibUsb.SUCCESS) System.out.println("Send Control transfer failed " + transfered);

			    //Print Results
			    int[] array = new int[byteBuffer.capacity()];
			    for (int k = 0; k < array.length; k++) array[i] = byteBuffer.get(i);
			    System.out.println(transfered.get() + " bytes sent     " + Arrays.toString(array));

			    transfered = IntBuffer.allocate(1);
			    byteBuffer = ByteBuffer.allocateDirect(8);
		    }
	    }else {
		    byteBuffer.put(bytes);
		    int result = LibUsb.bulkTransfer(handle, USB_EP_OUT, byteBuffer, transfered, timeout); //TODO make address global
		    if (result != LibUsb.SUCCESS) System.out.println("Send Control transfer failed " + transfered);
		    System.out.println(transfered.get() + " bytes sent     " + Arrays.toString(bytes));
	    }

    }


	static void recieveUsbData(DeviceHandle handle) throws LibUsbException {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(64);
		//buffer.put(bytes);
		IntBuffer transfered = IntBuffer.allocate(64);

		int result = LibUsb.bulkTransfer(
				handle,
				USB_EP_IN,
				byteBuffer,
				transfered,
				timeout);

		if (result != LibUsb.SUCCESS) System.out.println("Receive Control transfer failed " + transfered);
		//byte[] output = byteBuffer.get;

		//Printout Received Data:
		int[] array = new int[byteBuffer.capacity()];
		for (int i = 0; i < array.length; i++) array[i] = byteBuffer.get(i);
		System.out.println(transfered.get() + " bytes recieved " + Arrays.toString(array));

    }





	public static Device findDevice(short vendorId, short productId) {
		// Read the USB device list
		DeviceList list = new DeviceList();
		int result = LibUsb.getDeviceList(null, list);
		if (result < 0) throw new LibUsbException("Unable to get device list", result);

		try { // Iterate over all devices and scan for the right one
			for (Device device : list) {
				DeviceDescriptor descriptor = new DeviceDescriptor();
				result = LibUsb.getDeviceDescriptor(device, descriptor);
				if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to read device descriptor", result);
				if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) return device;
			}
		} finally { // Ensure the allocated device list is freed
			LibUsb.freeDeviceList(list, true);
		} // Device not found
		return null;
	}


}
