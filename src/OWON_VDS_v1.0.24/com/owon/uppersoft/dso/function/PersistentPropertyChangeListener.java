package com.owon.uppersoft.dso.function;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.util.PropertiesItem;

public class PersistentPropertyChangeListener implements PropertyChangeListener {
	private ControlManager cm;
	private PersistentDisplay pd;

	public PersistentPropertyChangeListener(ControlManager cm,
			PersistentDisplay pd) {
		this.cm = cm;
		this.pd = pd;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String n = evt.getPropertyName();
		if (n.equalsIgnoreCase(PropertiesItem.KEEPGET)) {
			boolean keepget = (Boolean) evt.getNewValue();
			if (keepget) {
				/** KNOW KNOW When resuming to running, refresh the state to coordinate with the overview area drawing */
				// dh.setStatus(RT_Normal);
				// Must stay commented out for other reasons: in normal or single trigger with no new waveform coming in, drawing dm must remain RT_DM
				int idx = cm.displayControl.getPersistenceIndex();
				pd.fadeThdOn_Off_UI(idx);
				/** If the dm.bin file is still occupied after starting to run, release it */
				// if(cba!=null)
				// cba.dispose();
			} else {
				/** KNOW KNOW After stopping, no change is set; it will change naturally when a waveform is loaded */
				// sta = RT_DM;
				// pd.fadeThdOn_Off_UI(0);
			}
		} else if (n.equalsIgnoreCase(PropertiesItem.FFT_ON)) {
			pd.fadeThdOn_Off_UI(cm.displayControl.setPersistenceIndex(0));
		} else if (n.equalsIgnoreCase(PropertiesItem.FFT_OFF)) {
			/** Here, when fft is turned off, the original persistence state is no longer restored */
		} else if (n.equalsIgnoreCase(PersistentDisplay.PERSISTENCE_RESET)) {
			pd.resetPersistence();
		}
	}
}