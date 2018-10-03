package com.owon.uppersoft.vds.ui.widget.help;

import java.awt.Graphics;

import javax.swing.JButton;

public interface ShareRollNStopInfoPane {

	void setRsRollover(boolean b);

	void repaint();

	void askStop(boolean b);

	void paintForButton(JButton btn, Graphics g);

}
