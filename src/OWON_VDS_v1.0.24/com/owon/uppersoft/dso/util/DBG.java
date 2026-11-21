package com.owon.uppersoft.dso.util;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.owon.uppersoft.vds.util.LoggerUtil;
import com.owon.uppersoft.vds.util.LoggerUtil.Textable;

/**
 * Remember to turn off printing; after release this greatly improves efficiency, whether it is out/err, files, or string creation in method calls
 * 
 * @author Matt
 * 
 */
public class DBG {

	/** Master switch for debugging, for efficiency reasons */
	public static boolean debug = true;
	/** Print debugging 0~8, or deployment (file) debugging -1; deployment file means the exported program's console is redirected to log.txt for printing */
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
		// FINE (print)// CONFIG
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
			fine(sb.toString() + "\r\n");// warning
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
	 * Convenient for outputting some info separately while debugging
	 * 
	 * @param msg
	 */
	public static final void seversln(Object o) {
		// if (debug)
		// logger.severe(msg + "\r\n");
		System.err.println(o);
	}

	/**
	 * Convenient for outputting some info separately while debugging
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
