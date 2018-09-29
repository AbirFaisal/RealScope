package com.owon.uppersoft.vds.tool.simple;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.owon.uppersoft.dso.source.usb.USBSourceManager;
import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.vds.core.data.CByteArrayInputStream;
import com.owon.uppersoft.vds.core.usb.IDevice;
import com.owon.uppersoft.vds.ui.layout.OneColumnLayout;
import com.owon.uppersoft.vds.util.ui.ListComboBoxModel;
import com.owon.uppersoft.vds.util.ui.UIUtil;

public class UsbCommunicator {

	private JFrame frame;
	private JComboBox usbListcbb;
	private JToggleButton connectButton, acceptButton, startGetButton;
	private JLabel timelbl;
	private JButton sendButton, LoadButton;
	private JPanel viewchart;
	private JTextArea consoleArea;

	private JTextField msginputTf;
	private JTextField tf_value, tf_bytes, tf_add;
	private JTextField tf_byte, tf_Int;

	private List<IDevice> devs;

	private Thread ui;
	private UsbComControl ucc;

	public Runnable m_startGet;
	public InteractiveBranch m_fpgaSend;

	public UsbComControl getUsbComControl() {
		return ucc;
	}

	public USBSourceManager getUSBSourceManager() {
		return ucc.getUSBSourceManager();
	}

	public void re_paint() {
		viewchart.repaint();
	}

	private UCModel um = new UCModel();

	/** * 触发电平 */
	public static void main(String[] args) {
		launch();
	}

	public static void launch() {
		UIUtil.modifyui();

		final UsbCommunicator window = new UsbCommunicator();
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					window.ui = Thread.currentThread();
				}
			});
			window.ui.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public UsbCommunicator() {
		this.ucc = new UsbComControl(this);
		m_startGet = new InfiniteGetData(this);
		m_fpgaSend = new InteractiveBranch(this, getUsbComControl()
				.getUSBSourceManager());
		prepare();
		initialize();
		frame.setVisible(true);
		// frame.setAlwaysOnTop(true);
		// frame.setResizable(false);
	}

	private void prepare() {
		devs = ucc.refreshUSBPort();
	}

	private void initialize() {
		DBG.prepareLogType(4, new File("log"));
		frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		CreateSouthPane();

		final JPanel northpanel = new JPanel();
		frame.getContentPane().add(northpanel, BorderLayout.NORTH);
		northpanel.setLayout(new OneColumnLayout());// new BorderLayout(0, 0)

		CreatePortPanel(northpanel);
		CreateTxtPanel(northpanel);
		CreateAddrPanel(northpanel);
		CreateHexPanel(northpanel);

		updateElapsedTimeLab(0);

		// final JButton disconnectButton = new JButton();
		// panel_1.add(disconnectButton);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				// m_fpgaSend.setCycleAcceptOn(false);
				setGetDataKeep(false);
				DBG.errprintln("exit");
			}
		});
		DBG.dbgln("chart w=" + viewchart.getWidth() + ",chart y="
				+ viewchart.getHeight());
	}

	private void CreateSouthPane() {
		final JPanel southpanel = new JPanel();
		southpanel.setLayout(new OneColumnLayout());

		final JScrollPane scrollPane1 = new JScrollPane();
		// scrollPane1.setPreferredSize(new Dimension(500, 300));
		scrollPane1.setBorder(new CompoundBorder(
				new EmptyBorder(0, 10, 10, 10), new LineBorder(Color.black, 1,
						false)));

		final JScrollPane scrollPane2 = new JScrollPane();
		scrollPane2.setPreferredSize(new Dimension(1000, 200));
		scrollPane2.setBorder(new CompoundBorder(
				new EmptyBorder(0, 10, 10, 10), new LineBorder(Color.black, 1,
						false)));

		consoleArea = new JTextArea();
		consoleArea.setLineWrap(true);
		consoleArea.setWrapStyleWord(true);
		scrollPane2.setViewportView(consoleArea);

		viewchart = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				um.paint((Graphics2D) g);
			}
		};
		viewchart.setBackground(Color.black);
		viewchart.setPreferredSize(new Dimension(1000, 251));
		scrollPane1.setViewportView(viewchart);

		southpanel.add(scrollPane1);
		southpanel.add(scrollPane2);
		frame.getContentPane().add(southpanel, BorderLayout.SOUTH);

		um.viewchartClear();
	}

	private void CreatePortPanel(JPanel northpanel) {
		final JPanel portpanel = new JPanel();
		portpanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		northpanel.add(portpanel, BorderLayout.NORTH);

		final JLabel usbLabel = new JLabel();
		usbLabel.setPreferredSize(new Dimension(52, 15));
		portpanel.add(usbLabel);
		usbLabel.setText("USBPort");

		usbListcbb = new JComboBox(new ListComboBoxModel(devs));
		usbListcbb.setPreferredSize(new Dimension(120, 28));
		portpanel.add(usbListcbb);

		final JButton refreshButton = new JButton();
		portpanel.add(refreshButton);
		refreshButton.setText("Refresh");
		refreshButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}

		});

		connectButton = new JToggleButton();
		portpanel.add(connectButton);
		connectButton.setText("Connect");
		connectButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean select = connectButton.isSelected();
				refresh();
				if (select) {
					int id = usbListcbb.getSelectedIndex();
					ucc._getUSBSource(devs.get(id));
				} else {
					ucc.releaseUSBSource();
					// refresh();
					setGetDataKeep(false);
				}
				updateConnectStatus();

			}

		});

		final JButton clearButton = new JButton();
		clearButton.setText("clear");
		portpanel.add(clearButton);
		clearButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// 用于textArea清屏
				consoleArea.setText(null);

				// 用于viewchart清屏
				viewchartClear();

			}

		});
		timelbl = new JLabel();
		portpanel.add(timelbl);

	}

	private void CreateTxtPanel(JPanel northpanel) {
		final JPanel txtpanel = new JPanel();
		final FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		txtpanel.setLayout(flowLayout);
		northpanel.add(txtpanel, BorderLayout.CENTER);

		final JLabel textLabel = new JLabel();
		txtpanel.add(textLabel);
		textLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		textLabel.setPreferredSize(new Dimension(52, 15));
		textLabel.setComponentPopupMenu(null);
		textLabel.setText("Message");

		// msginputTf = new JTextField();
		// msginputTf.setPreferredSize(new Dimension(180, 28));
		// txtpanel.add(msginputTf);

		sendButton = new JButton();
		txtpanel.add(sendButton);
		sendButton.setText("FPGAsend");

		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// acceptButton.setEnabled(false);
				Thread sendtrd = new Thread(m_fpgaSend);// comSend
				sendtrd.start();
			}
		});

		startGetButton = new JToggleButton();
		startGetButton.setText("StartGet");
		startGetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean get = startGetButton.isSelected();
				startGetButton.setText(get ? "stop" : "StartGet");

				if (get) {
					setGetDataKeep(true);
					Thread gt = new Thread(m_startGet);
					gt.start();
				} else {
					setGetDataKeep(false);
				}

			}
		});
		final JSpinner prd = new JSpinner(new SpinnerNumberModel(
				InfiniteGetData.LTimeOut, 1, 10000, 1));
		prd.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				InfiniteGetData.LTimeOut = (Integer) prd.getValue();
			}
		});
		prd.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				InfiniteGetData.LTimeOut = (Integer) prd.getValue();
			}
		});

		acceptButton = new JToggleButton();
		// txtpanel.add(acceptButton);
		acceptButton.setText("accept");
		acceptButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean accepting = acceptButton.isSelected();
				sendButton.setEnabled(false);

				Thread t = new Thread(m_fpgaSend.acp);
				if (accepting) {
					if (!ucc.isConnect()) {
						int id = usbListcbb.getSelectedIndex();
						ucc._getUSBSource(devs.get(id));
					}
					// byte[] slrun = InterCommunicator.slrun;
					// write(slrun, slrun.length);
					t.start();
					addTextArea("start to accept...");
				} else {
					try {
						t.join();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					addTextArea("stop accepting...");
					connectButton.setSelected(accepting);
					updateConnectStatus();
				}
			}

		});
		LoadButton = new JButton("Load");
		LoadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int fileLen;
				String sendFileName;
				byte[] sendFileBuf;
				CByteArrayInputStream ba;

				JFileChooser jfc = new JFileChooser();
				int rsl = jfc.showOpenDialog(frame);
				if (rsl == JFileChooser.APPROVE_OPTION) {
					File sf = jfc.getSelectedFile();
					fileLen = (int) sf.length();
					// if (sendFileBuf != null && sendFileBuf.length < len)
					sendFileBuf = new byte[fileLen];

					ba = new CByteArrayInputStream(sf);
					// sendFileBuf = ba.buf().array();//为什么下句可以，这句不行？
					ba.get(sendFileBuf, 0, fileLen);

					if (ba != null)
						ba.dispose();

					sendFileName = sf.getName();
					msginputTf.setText(sendFileName);
					addTextArea("LoadFileSize:" + fileLen);
				}
			}
		});
		// timelbl = new JLabel();
		// txtpanel.add(LoadButton);
		txtpanel.add(startGetButton);
		txtpanel.add(prd);
	}

	private void CreateAddrPanel(JPanel northpanel) {
		JPanel addrpanel = new JPanel();
		addrpanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		northpanel.add(addrpanel, BorderLayout.SOUTH);

		JLabel lblOx = new JLabel("addr 0x");
		addrpanel.add(lblOx);

		tf_add = new JTextField();
		addrpanel.add(tf_add);
		tf_add.setColumns(6);

		JLabel label = new JLabel("bytes 0x");
		addrpanel.add(label);

		tf_bytes = new JTextField();
		addrpanel.add(tf_bytes);
		tf_bytes.setColumns(6);

		JLabel label_1 = new JLabel("value 0x");
		addrpanel.add(label_1);

		tf_value = new JTextField();
		addrpanel.add(tf_value);
		tf_value.setColumns(6);

		JButton btnAdd = new JButton("add");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				um.setNdrawTrglevel(tf_add.getText(), tf_bytes.getText(),
						tf_value.getText());
				re_paint();
				ucc.add2CMDList(tf_add, tf_bytes, tf_value);
				setInAddcmd(true);
			}

		});
		addrpanel.add(btnAdd);

		JButton btnSend = new JButton("send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				um.setNdrawTrglevel(tf_add.getText(), tf_bytes.getText(),
						tf_value.getText());
				re_paint();
				if (isKeepGet()) {
					// 在拿波形中，send只把命令添加到队列 ，由守护线程发送
					ucc.add2CMDList(tf_add, tf_bytes, tf_value);
				} else {
					ucc.sendNAcceptOneCMD(tf_add, tf_bytes, tf_value);// 不用手动拿波形时，单独发送控制命令
					// ucc.sendNAcceptOneFrame();//手动拿波形时开启
				}
				setInAddcmd(false);
			}

		});
		addrpanel.add(btnSend);

	}

	private void CreateHexPanel(JPanel northpanel) {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEADING));

		northpanel.add(panel, BorderLayout.SOUTH);

		JLabel lblfo = new JLabel(" HEX byte 0x");
		panel.add(lblfo);
		tf_byte = new JTextField();
		tf_byte.setColumns(5);
		panel.add(tf_byte);

		JLabel lblint = new JLabel(" HEX Int ");
		panel.add(lblint);
		tf_Int = new JTextField();
		tf_Int.setColumns(6);
		panel.add(tf_Int);

		JButton btnSend2 = new JButton("send");
		btnSend2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ucc.sendByte_Int(tf_byte, tf_Int);
			}
		});
		panel.add(btnSend2);
	}

	public String getMsgInputText() {
		return msginputTf.getText();
	}

	// private List<ByteBuffer> bbl = new LinkedList<ByteBuffer>();

	public boolean keepGet = false, addingcmd = false;

	public void setGetDataKeep(boolean b) {
		keepGet = b;
	}

	public boolean isKeepGet() {
		return keepGet;
	}

	public void setInAddcmd(boolean b) {
		addingcmd = b;
	}

	public boolean canAddcmd() {
		return addingcmd;
	}

	public void updateElapsedTimeLab(long elapsedTime) {
		timelbl.setText("t : " + elapsedTime + " ms");
	}

	protected void viewchartClear() {
		um.viewchartClear();
		viewchart.repaint();
	}

	public JFrame getFrame() {
		return frame;
	}

	private void updateConnectStatus() {
		boolean select = connectButton.isSelected();
		if (select) {
			if (ucc.isConnect()) {
				connectButton.setText("DisConnect");
				addTextArea("-----Conneted successfully-----");
			} else {
				addTextArea("-----Connection failure----");
			}
		} else {
			connectButton.setText("Connect");
			addTextArea("releaseUSBSource----");
			sendButton.setEnabled(true);
			acceptButton.setEnabled(true);
		}
	}

	protected void addTextArea(String txt) {
		consoleArea.append(txt + "\r\n");
	}

	protected void appendTextArea(String txt) {
		consoleArea.append(txt);
	}

	protected void addwordinLine(byte txt) {
		consoleArea.append(txt + ",");
	}

	private void refresh() {
		devs = ucc.refreshUSBPort();
		usbListcbb.removeAllItems();
		if (devs.size() <= 0) {
			addTextArea("*****No USB Detected*****");
			return;
		}
		for (IDevice dev : devs) {
			usbListcbb.addItem(dev);
		}
	}

	public int acceptResponse(byte[] arr, int len) {
		return ucc.acceptResponse(arr, len);
	}

	public int write(byte[] arr, int len) {
		return ucc.write(arr, len);
	}

	public UCModel getUCModel() {
		return um;
	}

}
