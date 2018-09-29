package com.abirfaisal.Realscope;

import com.oracle.tools.packager.IOUtils;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.global.WorkBench;
import com.owon.uppersoft.dso.global.WorkBenchTiny;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
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

import javax.swing.*;
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
	static SwingNode swingNode = new SwingNode();

	@Override
    public void start(Stage primaryStage) throws Exception{

	    zeroAnchor(swingNode);

	    AnchorPane anchorPane = new AnchorPane(swingNode);

        primaryStage.setTitle("RealScope");
        primaryStage.setScene(new Scene(anchorPane, 1280, 720));
        primaryStage.show();
    }

    void zeroAnchor(Node node){
	    AnchorPane.setTopAnchor(node,0.0);
	    AnchorPane.setBottomAnchor(node,0.0);
	    AnchorPane.setLeftAnchor(node,0.0);
	    AnchorPane.setRightAnchor(node,0.0);
    }


	static WorkBench workBench;
	static Platform.PrincipleFactory principleFactory;
	static WorkBenchTiny workBenchTiny;

	static void launchVDS(){

		principleFactory = new Platform.PrincipleFactory() {
			@Override
			public WorkBench createWorkBench() {
				workBenchTiny = new WorkBenchTiny();
				return workBenchTiny;
			}
		};
		launch(principleFactory);
	}


	public static final void launch(Platform.PrincipleFactory pf) {
		principleFactory = pf;
		workBench = null;
		try {
			workBench = pf.createWorkBench();
			workBench.join();

		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

    public static void main(String[] args) throws IOException, InterruptedException {

	    launchVDS();


		//createWorkBench();



        launch(args);
    }

}
