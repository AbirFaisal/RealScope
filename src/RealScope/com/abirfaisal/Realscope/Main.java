package com.abirfaisal.Realscope;

import com.owon.uppersoft.dso.control.IDataImporter;
import com.owon.uppersoft.dso.function.ref.IReferenceWaveForm;
import com.owon.uppersoft.dso.global.*;
import com.owon.uppersoft.dso.machine.aspect.IMultiReceiver;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.model.trigger.TrgTypeDefine;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.ref.IRefSource;
import com.owon.uppersoft.dso.source.manager.SourceManager;
import com.owon.uppersoft.vds.core.comm.ICommunicateManager;
import com.owon.uppersoft.vds.core.comm.IRuntime;
import com.owon.uppersoft.vds.core.pref.Config;
import com.owon.uppersoft.vds.source.comm.InterCommTiny;
import com.owon.uppersoft.vds.util.Pref;
import com.owon.vds.tiny.firm.FPGADownloader;
import org.usb4java.*;
import sun.nio.ch.IOUtil;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;


/**
 * Notes:
 *
 * Files of interest:
 *   CmdFactory.java
 *
 */
public class Main {

	static short VENDOR_ID = 0x5345;
	static short PRODUCT_ID = 0x1234;
	static byte USB_EP_OUT = 0x03;
	static byte USB_EP_IN = (byte) 0x81;
	static Device usbDevice;


	static int lb = -2500;
	static int ub = 2500;


    public static void main(String[] args) throws IOException, InterruptedException {

//	    Platform.launch(new Platform.PrincipleFactory() {
//		    @Override
//		    public WorkBench createWorkBench() {
//			    return new WorkBenchTiny();
//		    }
//	    });


//	    ICommunicateManager iCommunicateManager = new ICommunicateManager() {
//		    @Override
//		    public boolean tryRescue() {
//			    return false;
//		    }
//
//		    @Override
//		    public int retryTimes() {
//			    return 0;
//		    }
//
//		    @Override
//		    public boolean isConnected() {
//			    return false;
//		    }
//
//		    @Override
//		    public int write(byte[] arr, int len) {
//			    return 1;
//		    }
//
//		    @Override
//		    public int acceptResponse(byte[] arr, int len) {
//			    return 1;
//		    }
//	    };
//	    FPGADownloader fpgaDownloader = new FPGADownloader();
//	    System.out.println(fpgaDownloader.queryFPGADownloaded(iCommunicateManager));
//	    File fpgaFile = fpgaDownloader.checkFPGAAvailable("vds1022");
//	    System.out.println("FPGA File Exists:" + fpgaFile.exists());
//	    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
//		    @Override
//		    public void propertyChange(PropertyChangeEvent evt) {
//
//		    }
//	    };
//	    fpgaDownloader.downloadFPGA(
//	    		propertyChangeListener,
//			    iCommunicateManager,
//			    fpgaFile);




    }


}

