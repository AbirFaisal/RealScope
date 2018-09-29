package com.owon.uppersoft.vds.util.ui;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.*;

import com.owon.uppersoft.vds.data.RGB;
import com.sun.java.swing.plaf.motif.MotifLookAndFeel;
import com.sun.java.swing.plaf.gtk.GTKLookAndFeel;


/**
 * Note: JSpinner is calling the non-constructor new JSpinner (SpinnerNumberModel
 * snm), the background color of the border may occur, it is suspected that setUIProperty("opaque", true); updateUI(); is not called
 *
 * @author Matt
 */
public class UIUtil {
	public static void modifylaf() {
		try {
			String lafn = MotifLookAndFeel.class.getName();
			//UIManager.setLookAndFeel(lafn);

			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}catch (Exception e){
			System.out.println(e);
		}
	}

	public static void modifyui() {
		modifylaf();

		String sk;
		UIDefaults ud = UIManager.getLookAndFeelDefaults();

		if (true) return;

		enumui();
		sk = "nimbusFocus";
		ud.put(sk, Color.LIGHT_GRAY);

		sk = "ComboBox.background";
		ud.put(sk, new RGB("070906").getColor());

		sk = "List.background";
		ud.put(sk, new RGB("2a2a2a").getColor());

		sk = "List[Selected].textBackground";
		ud.put(sk, new RGB("39698A").getColor());

		sk = "textForeground";
		ud.put(sk, Color.white);

		// sk = "background";
		// ud.put(sk, Color.darkGray);
	}

	public static void enumui() {
		UIDefaults ud = UIManager.getLookAndFeelDefaults();
		Set<Map.Entry<Object, Object>> set = ud.entrySet();

		boolean b;
		Object k;
		Object v;

		for (Map.Entry<Object, Object> m : set) {
			k = m.getKey();
			b = k.toString().startsWith("ProgressMonitor");
			if (b) {
				v = m.getValue();
			}
		}
	}

	public static void hackLazyPainter(Object v, String fn) {
		try {
			Field field = v.getClass().getDeclaredField(fn);
			field.setAccessible(true);
			field
					.set(
							v,
							"com.owon.uppersoft.dso.test.Custom.SpinnerPanelSpinnerFormattedTextFieldPainter");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		enumui();
	}

	public static void restartApplication() throws IOException {
		String SUN_JAVA_COMMAND = "sun.java.command";
		try {
			// java binary
			String java = System.getProperty("java.home") + "/bin/java";
			// vm arguments
			List<String> vmArguments = ManagementFactory.getRuntimeMXBean()
					.getInputArguments();
			StringBuffer vmArgsOneLine = new StringBuffer();
			for (String arg : vmArguments) {
				// if it's the agent argument : we ignore it otherwise the
				// address of the old application and the new one will be in
				// conflict
				if (!arg.contains("-agentlib")) {
					vmArgsOneLine.append(arg);
					vmArgsOneLine.append(" ");
				}
			}
			// init the command to execute, add the vm args
			final StringBuffer cmd = new StringBuffer("\"" + java + "\" "
					+ vmArgsOneLine);

			// program main and program arguments
			String[] mainCommand = System.getProperty(SUN_JAVA_COMMAND).split(
					" ");
			// program main is a jar
			if (mainCommand[0].endsWith(".jar")) {
				// if it's a jar, add -jar mainJar
				cmd.append("-jar " + new File(mainCommand[0]).getPath());
			} else {
				// else it's a .class, add the classpath and mainClass
				cmd.append("-cp \"" + System.getProperty("java.class.path")
						+ "\" " + mainCommand[0]);
			}
			// finally add program arguments
			for (int i = 1; i < mainCommand.length; i++) {
				cmd.append(" ");
				cmd.append(mainCommand[i]);
			}
			// execute the command in a shutdown hook, to be sure that all the
			// resources have been disposed before restarting the application
			try {
				Runtime.getRuntime().exec(cmd.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			// something went wrong
		}
	}
}
