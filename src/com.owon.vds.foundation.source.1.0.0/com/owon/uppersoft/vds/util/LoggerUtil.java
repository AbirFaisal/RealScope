package com.owon.uppersoft.vds.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class LoggerUtil {
	public interface Textable {
		void appendText(String msg);
	}

	public static final int LogStrategy_FILEASCONSOLE = 1;
	public static int LogStrategy = 0;

	public static final String LogDir = "log.txt";
	public static final String ErrDir = "err.txt";

	/**
	 * 在发布后开启，开发时关闭，只是将System.in,
	 * System.out的打印内容输出到文件，而非所有debug信息，只用于辅助查找发布后出现的意外问题
	 */
	public static final void redirect2FileLog(File logDir) {
		String errPath = ErrDir;
		String logPath = LogDir;

		File ef = new File(logDir, errPath);
		File of = new File(logDir, logPath);
		FileUtil.checkPath(ef);
		FileUtil.checkPath(of);
		PrintStream errps = null, outps = null;
		try {
			errps = new PrintStream(ef);
			outps = new PrintStream(of);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (outps != null) {
			System.setOut(outps);
			System.out.println("start log:");
		}
		if (errps != null) {
			System.setErr(errps);
		}
	}

	/**
	 * 使用文件作为输出方式，用在无法开启eclipse的内部调试中，给测试人员使用
	 * 
	 * @param className
	 * @param level
	 * @param dirPath
	 * @return
	 */
	public static Logger getFileLogger(String className, Level level,
			String dirPath) {
		Logger logger = Logger.getLogger(className);
		StreamHandler sh = null;
		SimpleFormatter formatter = new SimpleFormatter();
		File logDir = new File(dirPath);
		logDir.mkdirs();
		try {
			sh = new StreamHandler(new FileOutputStream(new File(logDir,
					className)), formatter);
			sh.setLevel(level);
			logger.addHandler(sh);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		logger.setLevel(level);
		return logger;
	}

	/**
	 * eclipse的内部调试中，打印信息重定向到System.out
	 * 
	 * @param className
	 * @param level
	 * @return
	 */
	public static Logger getConsoleLogger(String className, Level level,
			final Textable t) {
		Logger logger = Logger.getLogger(className);
		SimpleFormatter formatter = new SimpleFormatter();
		Handler sh = new Handler() {
			public synchronized void publish(LogRecord record) {
				String msg = record.getMessage();
				t.appendText(msg);
			}

			public void close() {
				flush();
			}

			@Override
			public void flush() {
				System.out.flush();
			}
		};
		sh.setLevel(level);
		logger.addHandler(sh);
		logger.setLevel(level);
		return logger;
	}
}
