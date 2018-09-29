package com.owon.uppersoft.dso.view.sub;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import com.owon.uppersoft.dso.global.DataHouse;
import com.owon.uppersoft.dso.page.TriggerPage;
import com.owon.uppersoft.dso.source.comm.effect.SubmitorFactory;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.ui.resource.FontCenter;
import com.owon.uppersoft.vds.ui.widget.LightenButton;

public class TrgToolBar extends JPanel implements Localizable {
	private LightenButton trgbtn;
	private LightenButton forcebtn;

	public TrgToolBar(final DataHouse dh) {
		setOpaque(false);
		setLayout(new BorderLayout());

		// Image img = SwingResourceManager.m_ClassImageMap
		// .get(TriggerPage.M_TRG_NAME);
		// trgbtn = new LightenIconButton(new ImageIcon(img));

		trgbtn = new LightenButton();
		trgbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dh.controlManager.getDockControl().dockDialogQuickOpenHide(
						TriggerPage.Name);
			}
		});

		forcebtn = new LightenButton();
		forcebtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// if (dh.controlManager.isRuntimeStop()) {
				// Platform.getMainWindow().getTitlePane()
				// .askRun(false, false);
				// }
				SubmitorFactory.getSubmitable().forceTrg();
			}
		});

		add(forcebtn, BorderLayout.EAST);
		add(trgbtn, BorderLayout.WEST);
	}

	public void setTrgInfPaneEnable(boolean b) {
		trgbtn.setEnabled(b);
		forcebtn.setEnabled(b);
		// Labels设置Enable(b)时,直接各个地方调用isFFTon,设完直接repaint(),这个以后得改。
		repaint();
	}

	@Override
	public void localize(ResourceBundle rb) {
		Font fn = FontCenter.getTitleFont();
		trgbtn.setFont_Text(fn, rb.getString("M.Trg.TrgVolt"));
		forcebtn.setFont_Text(fn, rb.getString("M.Trg.Force"));
		repaint();
	}

}