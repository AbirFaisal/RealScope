package com.owon.uppersoft.vds.ui.window;

import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JFrame;

public class ExtendedStataJFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 600368153239543324L;

	// private GlassPane glassPane;

	public ExtendedStataJFrame() {
		// glassPane = new GlassPane();
		// setGlassPane(glassPane);
		// glassPane.setVisible(false);

		// setMinimumSize(new Dimension(600, 400));
		// setMaximumSize(new Dimension(800, 600));

		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//DISPOSE_ON_CLOSE
//		setUndecorated(true);
		// setIconImage(null);
	}

	/**
	 * Fix the bug &quot;jframe undecorated cover taskbar when maximized&quot;.
	 * See: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4737788
	 * 
	 * @param state
	 */
	@Override
	public void setExtendedState(int state) {
		if ((state & java.awt.Frame.MAXIMIZED_BOTH) == java.awt.Frame.MAXIMIZED_BOTH) {
			Rectangle bounds = getGraphicsConfiguration().getBounds();
			Rectangle maxBounds = null;
			// Check to see if this is the 'primary' monitor
			// The primary monitor should have screen coordinates of
			// (0,0)
			if (bounds.x == 0 && bounds.y == 0) {
				Insets screenInsets = getToolkit().getScreenInsets(
						getGraphicsConfiguration());
				maxBounds = new Rectangle(screenInsets.left, screenInsets.top,
						bounds.width - screenInsets.right - screenInsets.left,
						bounds.height - screenInsets.bottom - screenInsets.top);
			} else {
				// Not the primary monitor, reset the maximized
				// bounds...
				maxBounds = null;
			}
			super.setMaximizedBounds(maxBounds);
		}
		super.setExtendedState(state);
	}
}