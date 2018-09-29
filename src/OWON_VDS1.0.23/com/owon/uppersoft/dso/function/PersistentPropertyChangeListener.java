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
				/** KNOW 在恢复到运行时，刷新状态以配合总览区画图 */
				// dataHouse.setStatus(RT_Normal);
				// 因为其它原因必须注释：正常或单次触发时没有新波形进来还画dm必须保持为RT_DM
				int idx = cm.displayControl.getPersistenceIndex();
				pd.fadeThdOn_Off_UI(idx);
				/** 如果启动运行后仍占用dm.bin文件，则释放它 */
				// if(cba!=null)
				// cba.dispose();
			} else {
				/** KNOW 在停下后，并不设置改变，等波形载入自然会变化 */
				// sta = RT_DM;
				// pd.fadeThdOn_Off_UI(0);
			}
		} else if (n.equalsIgnoreCase(PropertiesItem.FFT_ON)) {
			pd.fadeThdOn_Off_UI(cm.displayControl.setPersistenceIndex(0));
		} else if (n.equalsIgnoreCase(PropertiesItem.FFT_OFF)) {
			/** 这里不在关闭fft的时候再恢复余辉原本的状态了 */
		} else if (n.equalsIgnoreCase(PersistentDisplay.PERSISTENCE_RESET)) {
			pd.resetPersistence();
		}
	}
}