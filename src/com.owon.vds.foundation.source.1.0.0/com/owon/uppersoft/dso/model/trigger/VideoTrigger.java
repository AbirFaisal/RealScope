package com.owon.uppersoft.dso.model.trigger;

import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_video_holdoff;

import java.awt.Graphics2D;

import com.owon.uppersoft.dso.control.TrgLevelCheckHandler;
import com.owon.uppersoft.dso.data.LObject;
import com.owon.uppersoft.dso.model.trigger.helper.PaintChannelTrgLabelContext;
import com.owon.uppersoft.dso.view.sub.Label;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.socket.ScpiPool;
import com.owon.uppersoft.vds.util.Pref;

public class VideoTrigger extends AbsTrigger {
	public static final String[] MODULE = new String[] { "NTSC", "PAL", "SECAM" };
	public static final String[] SYNC_TXT = { "LINE", "FIELd", "ODD", "EVEN",
			"LNUM" };
	public static final LObject[] SYNC = { new LObject("M.Trg.Sync.Line"),
			new LObject("M.Trg.Sync.Field"),
			new LObject("M.Trg.Sync.OddField"),
			new LObject("M.Trg.Sync.EvenField"),
			new LObject("M.Trg.Sync.LineNum") };
	public static final int linenumber = 4;

	public int module = 0;
	public int sync = 0;
	public int syncValue = 1;

	public int getMax(int mod) {
		return mod == 0 ? 525 : 625;
	}

	public VideoTrigger() {
		super(TrgTypeDefine.Video);
	}

	public boolean isSyncValueEnable() {
		return sync == linenumber;
	}

	public String getMod() {
		return MODULE[module];
	}

	public boolean setMod(String args) {
		for (int i = 0; i < MODULE.length; i++) {
			if (args.equalsIgnoreCase(MODULE[i])) {
				module = i;
				return true;
			}
		}
		return false;
	}

	public String getSync() {
		return SYNC_TXT[sync];
	}

	public boolean setSync(String args) {
		for (int i = 0; i < SYNC_TXT.length; i++) {
			if (args.equalsIgnoreCase(SYNC_TXT[i])) {
				sync = i;
				return true;
			}
		}
		return false;
	}

	public String getLnum() {
		return String.valueOf(syncValue);
	}

	@Override
	public void loadProperties(String prefix, Pref p) {
		String txt = getName();
		module = p.loadInt(prefix + txt + ".module");
		sync = p.loadInt(prefix + txt + ".sync");
		syncValue = p.loadInt(prefix + txt + ".syncValue");
	}

	@Override
	public void persistProperties(String prefix, Pref p) {
		String txt = getName();
		p.persistInt(prefix + txt + ".module", module);
		p.persistInt(prefix + txt + ".sync", sync);
		p.persistInt(prefix + txt + ".syncValue", syncValue);
	}

	@Override
	public String getIconKey() {
		if (sync == 0 || sync == 4)
			return "line.png";
		else
			return "field.png";
	}

	@Override
	public void paintIcon(Graphics2D g2d) {
		int x = Label.trgmodstart, y = 7;
		switch (sync) {
		case 1:
			// g2d.drawString("", x, y);
			break;
		case 2:
			g2d.drawString("O", x, y);
			break;
		case 3:
			g2d.drawString("E", x, y);
			break;
		}
	}

	@Override
	public void c_setSweep(int sweep, TriggerControl tc) {
	}

	@Override
	public int getSweep() {
		return TriggerDefine.TrigSweepAutoIndex;
	}

	@Override
	public String getLabelText(boolean inverse, int voltbase, int pos0) {
		// 在video的时候作为行数文本
		if (sync == 4)
			return "#" + syncValue;
		else
			return "ALL";
	}

	@Override
	public void nextStatus() {
		sync = (sync + 1) % SYNC.length;
	}

	/**
	 * 对指定参数进行增减
	 * 
	 * @param del
	 *            不为0才发指令改变
	 * @param type
	 * @return 触发的事件类名
	 */
	@Override
	public boolean handelIncr(int del, TrgCheckType type) {
		return false;
	}

	@Override
	public boolean doCheckTrgLevel(TrgLevelCheckHandler checkHandler) {
		// checkHandler.checkAroundTrgAndHandle(-1, NotOver, NotOver);
		return false;
	}

	/**
	 * 画触发电平标志
	 * 
	 * @param g2d
	 * @param pc
	 * @param cm
	 * @param lr
	 * @param select
	 * @param arrow
	 * @param c
	 */
	@Override
	public void paintChannelTrgLabel(PaintChannelTrgLabelContext pctlc) {
	}

	@Override
	public void submitHoldOff(int mode, int chl, Submitable sbm) {
		sbm.c_trg_video(mode, chl, trg_video_holdoff, etvho.toInt(),
				etvho.getValueDivTimeOnStage(), etvho.enumPart());
	}

}