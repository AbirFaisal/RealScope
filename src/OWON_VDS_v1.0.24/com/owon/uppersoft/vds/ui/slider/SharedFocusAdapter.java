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
		this.on = true;
	}

	@Override
	public void focusLost(FocusEvent e) {
		/**
		 * The custom slider closes after the button is clicked, to avoid refreshing internal state, especially since the value corresponding to 50% currently cannot be set inside the slider but is changed externally
		 * 
		 * Under java7 this code keeps the slider from closing when the button is clicked; the reason is that under java7 the focus-lost event now occurs before the button click,
		 * so the button click cannot be generated and the corresponding action cannot be triggered
		 * , as a temporary fix at the time, the action triggered by focus loss is masked at the right moment by a check; in the future the button operation can be adjusted so that it does not trigger focus loss
		 * */
		// if (!isOn())
		// return;

		/** Focus is lost after the click, so the action can no longer occur */
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