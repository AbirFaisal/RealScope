package com.owon.uppersoft.vds.ui.window;

import java.awt.Window;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JOptionPane;

//import com.sun.awt.AWTUtilities;


public class WindowUtil {
	/**
	 * Set the outline of the top-level window
	 * 
	 * @param window
	 * @param arc
	 */
	public static void ShapeWindow(Window window, int arc) {
		if (arc == 0)

			window.setShape(new Rectangle2D.Double(0, 0, window.getWidth(), window.getHeight()));

			//Only in JDK6
			//AWTUtilities.setWindowShape(window, new Rectangle2D.Double(0, 0, window.getWidth(), window.getHeight()));
		else

			window.setShape(new RoundRectangle2D.Double(0, 0, window.getWidth(), window.getHeight(), arc, arc));

			//JDK6
			//AWTUtilities.setWindowShape(window, new RoundRectangle2D.Double(0, 0, window.getWidth(), window.getHeight(), arc, arc));
	}

	/**
	 * @param msn
	 * @param parent A prompt to cancel the popup on this parent window, such as yes, closes the parent window
	 * @return
	 */
	public static boolean showCancelDialog(String msn, Window parent) {
		// int re = JOptionPane.showOptionDialog(Platform.getMainWindow()
		// .getFrame(), msn, null, JOptionPane.DEFAULT_OPTION,
		// JOptionPane.QUESTION_MESSAGE, null, MainWindow.JOptionBtnLabs,
		// MainWindow.JOptionBtnLabs[0]);
	
		int re = JOptionPane.showConfirmDialog(parent, msn, null, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
	
		boolean cancle = (re == JOptionPane.YES_OPTION);
		if (cancle && parent != null) {
			parent.dispose();
		}
		return cancle;
	}
}
