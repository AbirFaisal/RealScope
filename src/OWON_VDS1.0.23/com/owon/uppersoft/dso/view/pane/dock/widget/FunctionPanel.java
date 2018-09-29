package com.owon.uppersoft.dso.view.pane.dock.widget;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.i18n.I18nProvider;
import com.owon.uppersoft.dso.pref.Define;
import com.owon.uppersoft.vds.core.aspect.Localizable;
import com.owon.uppersoft.vds.ui.layout.OneColumnLayout;
import com.owon.uppersoft.vds.ui.widget.IPPane;
import com.owon.uppersoft.vds.ui.widget.custom.CButton;
import com.owon.uppersoft.vds.ui.widget.custom.CCheckBox;
import com.owon.uppersoft.vds.ui.widget.custom.CComboBox;
import com.owon.uppersoft.vds.ui.widget.custom.CLabel;
import com.owon.uppersoft.vds.ui.widget.custom.CToggleButton;
import com.owon.uppersoft.vds.ui.widget.custom.HCLabel;
import com.owon.uppersoft.vds.ui.widget.custom.NLabel;
import com.owon.uppersoft.vds.ui.widget.help.ToggleListener;
import com.owon.uppersoft.vds.util.ui.ListComboBoxModel;

/**
 * FunctionPanel，针对不同功能的通用面板
 * 
 * @author Matt
 * 
 */
public abstract class FunctionPanel extends JPanel implements Localizable,
		PropertyChangeListener {
	protected ControlManager cm;
	protected GroupPane gp;
	protected ItemPane ip;
	protected List<Localizable> list;

	public FunctionPanel(ControlManager cm) {
		this.cm = cm;
		setBackground(Define.def.style.CO_DockBack);
		setLayout(new OneColumnLayout(new Insets(5, 5, 5, 5), 5));
	}

	public void set_all_visible(boolean b) {
		set_all_visible(this, b);
		// setVisible(b);
	}

	public static void set_all_visible(Container ctn, boolean b) {
		int cc = ctn.getComponentCount();
		// System.out.println(cc);
		for (int i = 0; i < cc; i++) {
			Component c = ctn.getComponent(i);
			// System.out.println(c);
			c.setVisible(b);
		}
	}

	/**
	 * 在当前pane离开之前调用，用于保存信息等
	 */
	public void beforeLeave() {
	}

	/**
	 * GroupPane是圆角边容器，可横可竖，可添加容器或组件
	 * 
	 * ItemPane是附着在GroupPane上的透明容器，横，添加组件
	 * 
	 * 容器被创建会自动存入对象内部的当前引用
	 * 
	 * 组件会自动添加到相应容器
	 * 
	 * 继承自Localizable的组件会自动被添加到List<Localizable>
	 * list并自动调用其Localize(ResourceBundle bundle)方法
	 * 
	 * 
	 */
	/**
	 * Column
	 * 
	 * @return
	 */
	public GroupPane ncgp() {
		gp = new GroupPane();
		add(gp);
		return gp;
	}

	/**
	 * Row
	 * 
	 * @return
	 */
	public ItemPane nrip() {
		return ip = gp.nrip();
	}

	public ItemPane nrip_notitle() {
		return ip = gp.nrip_notitle();
	}

	/**
	 * 创建i18n多行文本框
	 * 
	 * @param key
	 * @return
	 */
	public HCLabel nlbld(String key) {
		HCLabel lbl = new HCLabel(Define.def.CO_DockFore,
				Define.def.style.CO_DockContainer,
				Define.def.style.CO_DockTitle);
		lbl.setName(key);
		ip.add(lbl);
		addLocalizable(lbl);
		return lbl;
	}

	/**
	 * 创建多行文本框
	 * 
	 * @param t
	 * @return
	 */
	public HCLabel nlbldt(String t) {
		HCLabel lbl = new HCLabel(Define.def.CO_DockFore,
				Define.def.style.CO_DockContainer,
				Define.def.style.CO_DockTitle);
		lbl.setText(t);
		ip.add(lbl);
		return lbl;
	}

	/**
	 * 创建i18n带冒号后缀单行文本
	 * 
	 * @param key
	 * @return
	 */
	public CLabel nlbl(String key) {
		CLabel lbl = new CLabel();
		lbl.setName(key);
		ip.add(lbl);
		addLocalizable(lbl);
		return lbl;
	}

	/**
	 * 创建i18n单行文本
	 * 
	 * @param key
	 * @return
	 */
	public NLabel nnlbl(String key) {
		NLabel lbl = new NLabel(Define.def.CO_DockFore);
		lbl.setName(key);
		ip.add(lbl);
		addLocalizable(lbl);
		return lbl;
	}

	/**
	 * 创建既没i18n也没冒号的单行文本
	 * 
	 * @param key
	 * @return
	 */
	public NLabel nnnlbl(String key) {
		NLabel lbl = new NLabel(Define.def.CO_DockFore);
		lbl.setText(key);
		ip.add(lbl);
		return lbl;
	}

	/**
	 * 创建带冒号后缀单行文本
	 * 
	 * @param t
	 * @return
	 */
	public CLabel nlblt(String t) {
		CLabel lbl = new CLabel();
		lbl.setText(t);
		ip.add(lbl);
		return lbl;
	}

	public JTextField ntf(String txt) {
		JTextField lbl = new JTextField();
		lbl.setText(txt);
		ip.add(lbl);
		return lbl;
	}

	public JTextField[] ntfip(String lab, String txt) {
		nlbl(lab).setPreferredSize(new Dimension(86, 0));
		JTextField jtf[] = new JTextField[4];
		for (int i = 0; i < 4; i++) {
			JTextField jt = jtf[i] = new JTextField(2);
			jt.setText(txt);
			ip.add(jt);
			if (i == 3)
				continue;
			nlblt(".").setPreferredSize(new Dimension(2, 0));
		}
		return jtf;
	}

	public IPPane nip4net(String lab, byte[] ip) {
		IPPane ipp = new IPPane(I18nProvider.bundle());
		ipp.setLabelName(lab);
		ipp.setIP(ip);
		addLocalizable(ipp);
		gp.add(ipp);
		return ipp;
	}

	public CComboBox nccb() {
		CComboBox lbl = new CComboBox();
		ip.add(lbl);
		// addLocalizable(lbl);
		return lbl;
	}

	public CComboBox nccb(ComboBoxModel o) {
		CComboBox lbl = new CComboBox(o);
		ip.add(lbl);
		// addLocalizable(lbl);
		return lbl;
	}

	public CComboBox nccb(List o) {
		CComboBox lbl = new CComboBox(new ListComboBoxModel(o));
		ip.add(lbl);
		// addLocalizable(lbl);
		return lbl;
	}

	public CComboBox nccb(Object[] o) {
		CComboBox lbl = new CComboBox(o);
		ip.add(lbl);
		// addLocalizable(lbl);
		return lbl;
	}

	public CComboBox nccb(Object[] o, int w, int h) {
		CComboBox lbl = new CComboBox(o);
		ip.add(lbl);
		lbl.setPreferredSize(new Dimension(w, h));
		// addLocalizable(lbl);
		return lbl;
	}

	/**
	 * 创建i18n单选框
	 * 
	 * @param key
	 * @return
	 */
	public CCheckBox ncb(String key) {
		CCheckBox lbl = new CCheckBox();
		lbl.setName(key);
		ip.add(lbl);
		addLocalizable(lbl);
		return lbl;
	}

	/**
	 * 创建单选框
	 * 
	 * @param t
	 * @return
	 */
	public CCheckBox ncbt(String t) {
		CCheckBox lbl = new CCheckBox();
		lbl.setText(t);
		ip.add(lbl);
		return lbl;
	}

	/**
	 * 创建i18n按钮
	 * 
	 * @param key
	 * @return
	 */
	public CButton nbtn(String key) {
		CButton lbl = new CButton();
		lbl.setName(key);
		ip.add(lbl);
		addLocalizable(lbl);
		return lbl;
	}

	/**
	 * 创建按钮
	 * 
	 * @param t
	 * @return
	 */
	public CButton nbtnt(String t) {
		CButton lbl = new CButton();
		lbl.setText(t);
		ip.add(lbl);
		return lbl;
	}

	/**
	 * 创建按钮
	 * 
	 * @param t
	 * @return
	 */
	public CToggleButton ntbtn(String selkey, String uskey, int w, int h,
			boolean sel) {
		return ntbtn(selkey, uskey, null, w, h, sel);
	}

	/**
	 * 创建按钮
	 * 
	 * @param t
	 * @return
	 */
	public CToggleButton ntbtn(String selkey, String uskey, ToggleListener al,
			int w, int h, boolean sel) {
		CToggleButton lbl = new CToggleButton(selkey, uskey, al, sel,
				I18nProvider.bundle());
		lbl.setPreferredSize(new Dimension(w, h));
		ip.add(lbl);
		return lbl;
	}

	/**
	 * 根据个数填充数值，从1开始
	 * 
	 * @param ofs
	 * @return
	 */
	public static final Object[] fillTextFrom1(Object[] ofs) {
		int len = ofs.length + 1;
		for (int i = 1; i < len; i++) {
			ofs[i - 1] = String.valueOf(i);
		}
		return ofs;
	}

	/**
	 * 在已有内容数组最后添加一项
	 * 
	 * @param os
	 * @param last
	 * @return
	 */
	public static final String[] getComposite_suffix(Object[] os, String last) {
		String[] ss = new String[os.length + 1];
		int len = os.length;
		for (int i = 0; i < len; i++) {
			ss[i] = os[i].toString();
		}
		ss[len] = last;
		return ss;
	}

	/**
	 * 在已有内容数组开头添加一项
	 * 
	 * @param first
	 * @param os
	 * @return
	 */
	public static final String[] getComposite_prefix(String first, Object[] os) {
		String[] ss = new String[os.length + 1];
		int len = os.length + 1;
		ss[0] = first;
		for (int i = 1; i < len; i++) {
			ss[i] = os[i - 1].toString();
		}
		return ss;
	}

	public void localizeSelf() {
		I18nProvider.LocalizeSelf(this);
	}

	public void addLocalizable(Localizable l) {
		if (list == null) {
			list = new LinkedList<Localizable>();
		}
		list.add(l);
	}

	@Override
	public void localize(ResourceBundle rb) {
		if (list != null) {
			for (Localizable l : list) {
				l.localize(rb);
			}
		}
	}

	public ControlManager getControlManager() {
		return cm;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	}
}
