package com.owon.uppersoft.dso.view.pane;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.text.Document;

import com.owon.uppersoft.dso.util.DBG;

public class LogDialog extends JDialog {
	private final class DialogHandler extends Handler {
		@Override
		public void publish(LogRecord record) {
			String t = record.getMessage();
			append(t);
		}

		@Override
		public void close() throws SecurityException {
			removeHandler();
		}

		@Override
		public void flush() {

		}
	}

	private JTextArea textArea;
	private DialogHandler dialogHandler;
	private AbstractButton ab;

	public LogDialog(Window w, final AbstractButton ab) {
		super(w);

		this.ab = ab;
		dialogHandler = new DialogHandler();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				ab.setSelected(false);
				removeHandler();
			}
		});

		setLayout(new BorderLayout());
		JPanel sp = new JPanel();
		final JComboBox jcb = new JComboBox(DBG.Levels);
		jcb.setSelectedItem(DBG.getLogger().getLevel());
		jcb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				DBG.setLogType(jcb.getSelectedIndex());
			}
		});
		sp.add(jcb);

		String[] ms = new String[] { "VDS3102ONE", "VDS2062ONE", "VDS1022ONE" };
		final JComboBox jcb2 = new JComboBox();
		// jcb2.setSelectedItem(Platform.getControlManager().getCoreControl()
		// .getProductParam());
		jcb2.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// Platform.getControlManager().setProductParam(
				// jcb2.getSelectedItem().toString());
			}
		});
		sp.add(jcb2);

//		final JCheckBox jcb3 = new JCheckBox("fps");
//		jcb3.setSelected(ControlManager.dbgbtns);
//		jcb3.addItemListener(new ItemListener() {
//			@Override
//			public void itemStateChanged(ItemEvent e) {
//				ControlManager.dbgbtns = jcb3.isSelected();
//			}
//		});
//		defaultSystemPrefrences.add(jcb3);

		JButton jbtn = new JButton("clear");
		jbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.setText("");
			}
		});

		add(sp, BorderLayout.NORTH);

		textArea = new JTextArea();

		DBG.getLogger().addHandler(dialogHandler);
		JScrollPane jsp = new JScrollPane();
		JViewport jv = new JViewport();
		jv.add(textArea);
		jsp.setViewport(jv);
		add(jsp, BorderLayout.CENTER);
		setBounds(100, 100, 500, 375);

		setVisible(true);
	}

	private void removeHandler() {
		DBG.getLogger().removeHandler(dialogHandler);
	}

	public void append(String txt) {
		Document doc = textArea.getDocument();
		int len = doc.getLength();
		if (len >= 50000) {
			textArea.replaceRange("", 0, 20000);
		}
		textArea.append(txt);
		len = doc.getLength();
		textArea.setCaretPosition(len);
	}
}