/**
 * 
 */
package com.owon.uppersoft.vds.ui.slider;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JDialog;

public class SharedFocusAdapter extends FocusAdapter {
	private final SliderDisposeDelegate delegate;
	private final JDialog dlg;

	private boolean on;

	public SharedFocusAdapter(SliderDisposeDelegate delegate, JDialog dlg) {
		this.delegate = delegate;
		this.dlg = dlg;
		setOn(true);
	}

	@Override
	public void focusLost(FocusEvent e) {
		/**
		 * 自定义slider在点击按钮后关闭，避免刷新内部状态，特别是50%的对应值目前无法设置到slider内部而是在外部改变
		 * 
		 * 在java7下借由此代码实现点击按钮的时候不关闭slider，不关闭的原因是在java7下焦点丢失的事件改成了在按钮点击之前，
		 * 所以无法产生按钮点击而无法触发对应的动作
		 * ，当时为了临时解决只有通过判断适时屏蔽掉焦点丢失触发的动作，今后可以把按钮的操作方式作调整，使其不触发焦点丢失
		 * */
		// if (!isOn())
		// return;

		/** 点击后失去焦点，无法再让action发生 */
		dlg.dispose();
		delegate.onDispose();

	}

	public void setOn(boolean on) {
		this.on = on;
	}

	public boolean isOn() {
		return on;
	}
}