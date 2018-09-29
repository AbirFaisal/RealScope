package com.owon.uppersoft.dso.util;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.owon.uppersoft.vds.util.LoggerUtil;
import com.owon.uppersoft.vds.util.LoggerUtil.Textable;

/**
 * 记得关闭打印，在程序发布后提高很大效率，无论是out/err还是文件，还是方法调用中的字符串创建
 * 
 * @author Matt
 * 
 */
public class DBG {

	/** 总开关是否调试，效率原因 */
	public static boolean debug = true;
	/** 打印调试0~8, 还是部署(文件)调试-1 ,部署文件即导出的程序打印台重定向到log.txt打印 */
	public static int cmddebug = -1;

	private static Logger logger = LoggerUtil.getConsoleLogger(
			DBG.class.getName(), Level.ALL, new Textable() {
				@Override
				public void appendText(String msg) {
					outprint(msg);
				}
			});

	public static Logger getLogger() {
		return logger;
	}

	public static final Level[] Levels = { Level.ALL, Level.FINEST,
			Level.FINER, Level.FINE, Level.CONFIG, Level.INFO, Level.WARNING,
			Level.SEVERE, Level.OFF };

	public static void setLogType(int cmd) {
		cmddebug = cmd;

		// LoggerUtil.getFileLogger(
		// "com.owon.uppersoft.oscilloscope.data.transform.RapidDataImport",
		// Level.FINE, PropertiesItem.LogDir);

		Level l = Level.OFF;

		if (cmd >= 0 && cmd < Levels.length) {
			l = Levels[cmd];
		}
		logger.setLevel(l);
		// FINE (打印)// CONFIG
	}

	public static final void prepareLogType(int cmd, File logDir) {
		setLogType(cmd);

		if (!isCMDDEBUG())
			LoggerUtil.redirect2FileLog(logDir);
	}

	public static final boolean isCMDDEBUG() {
		return cmddebug >= 0;
	}

	private static StringBuilder sb = new StringBuilder();

	public static final void dbg(Object t) {
		if (!debug)
			return;
		sb.append(t);
	}

	public static final void dbgd(Object o) {
		if (!debug)
			return;
		flushdbg();
		String t = o.toString();
		if (t.length() > 0)
			fine(t);
	}

	public static final void dbgln(Object o) {
		flushdbg();
		String t = o.toString();
		if (t.length() > 0)
			fine(t + "\r\n");
	}

	public static final void flushdbg() {
		int len = sb.length();
		if (len > 0) {
			fine(sb + "\r\n");// warning
			sb.delete(0, len);
		}
	}

	public static final void config(Object msg) {
		if (debug)
			logger.config(msg.toString());
	}

	public static final void configln() {
		if (debug)
			logger.config("\r\n");
	}

	public static final void configln(Object msg) {
		if (debug)
			logger.config(msg + "\r\n");
	}

	public static final void fine(Object msg) {
		if (debug)
			logger.fine(msg.toString());
	}

	public static final void finer(Object msg) {
		if (debug)
			logger.finer(msg.toString());
	}

	public static final void finest(Object msg) {
		if (debug)
			logger.finest(msg.toString());
	}

	public static final void info(Object msg) {
		if (debug)
			logger.info(msg.toString());
	}

	public static final void severe(Object msg) {
		if (debug)
			logger.severe(msg.toString());
	}

	/**
	 * 调试时方便单独输出一些信息
	 * 
	 * @param msg
	 */
	public static final void seversln(Object o) {
		// if (debug)
		// logger.severe(msg + "\r\n");
		System.err.println(o);
	}

	/**
	 * 调试时方便单独输出一些信息
	 * 
	 * @param msg
	 */
	public static final void severs(Object o) {
		// if (debug)
		// logger.severe(msg + "\r\n");
		System.err.print(o);
	}

	public static final void warning(Object msg) {
		if (debug)
			logger.warning(msg.toString());
	}

	public static final void errprintln(Object o) {
		if (debug)
			System.err.println(o);
	}

	public static final void errprintln() {
		System.err.println();
	}

	public static final void errprint(Object o) {
		System.err.print(o);
	}

	public static final void outprintln(Object o) {
		System.out.println(o);
	}

	public static final void outprintln() {
		System.out.println();
	}

	public static final void outprint(Object o) {
		System.out.print(o);
	}
}
