package com.owon.uppersoft.dso.model.trigger;

import com.owon.uppersoft.dso.control.TrgLevelCheckHandler;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.model.trigger.common.Voltsensor;
import com.owon.uppersoft.dso.model.trigger.helper.PaintChannelTrgLabelContext;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.util.Pref;

public class TriggerSet {
	// 标记通道名
	private int channel;

	public int getChannel() {
		return channel;
	}

	public void setChannel(int v) {
		this.channel = v;
	}

	public final SlopeTrigger slope;
	public final PulseTrigger pulse;
	public final VideoTrigger video;
	public final EdgeTrigger edge;

	private AbsTrigger trigger;

	public AbsTrigger getTrigger() {
		return trigger;
	}

	public AbsTrigger getTriggerSet(byte type) {
		switch (type) {
		case 'e':
			return edge;
		case 'p':
			return pulse;
		case 's':
			return slope;
		case 'v':
			return video;
		default:
			return null;
		}
	}

	public AbsTrigger getTriggerSet(TrgTypeDefine type) {
		switch (type) {
		case Edge:
			return edge;
		case Pulse:
			return pulse;
		case Slope:
			return slope;
		case Video:
			return video;
		default:
			return null;
		}
	}

	/**
	 * 画触发电平标志
	 * 
	 * @param wfm
	 * @param lr
	 * @param g2d
	 * @param pc
	 * @param ci
	 * @param rtlip
	 */
	public void paintTriggerSet(WaveFormManager wfm,
			PaintChannelTrgLabelContext pctlc) {
		int channel = this.channel;
		WaveForm wf = wfm.getWaveForm(channel);
		if (wf == null || !wf.isOn())
			return;

		pctlc.setChannelInfo(wf.wfi.ci);
		trigger.paintChannelTrgLabel(pctlc);
	}

	/**
	 * 
	 * @return
	 */
	public boolean isVoltsenseSupport() {
		return trigger instanceof VoltsensableTrigger;
	}

	/**
	 * 判断y所在位置是否有可以点击的触发参数
	 * 
	 * @return
	 */
	public boolean checkTrgLevel(TrgLevelCheckHandler checkHandler) {
		return trigger.doCheckTrgLevel(checkHandler);
	}

	/**
	 * 对指定参数进行增减
	 * 
	 * @param del
	 *            不为0才发指令改变
	 * @param type
	 * @return 触发的事件类名
	 */
	public boolean handelIncr(int del, TrgCheckType type) {
		return trigger.handelIncr(del, type);
	}

	/**
	 * 
	 * @return
	 */
	public int getVoltsenseHalfRange() {
		if (trigger == pulse)
			return PulseTrigger.VoltSenseHalfRange;
		else if (trigger == edge)
			return EdgeTrigger.VoltSenseHalfRange;
		else
			return -1;
	}

	/**
	 * 内部处理和绘图使用的是绝对值，不改变它，仅在示数和下传时使用相对值
	 * 
	 * @return
	 */
	public Voltsensor getVoltsense() {
		Voltsensor vs;
		if (trigger == pulse)
			vs = pulse.getVoltsensor();
		else if (trigger == edge)
			vs = edge.getVoltsensor();
		else {
			return null;
		}
		return vs;
	}

	/**
	 * 尝试设置为单次触发
	 */
	void trySweepOnce(TriggerControl tc) {
		trigger.c_setSweep(TriggerDefine.TrigOnceIndex, tc);
	}

	/**
	 * @return 是否单次触发
	 */
	boolean isSweepOnce() {
		return trigger.getSweep() == TriggerDefine.TrigOnceIndex;
	}

	/**
	 * @return 是否正常触发
	 */
	boolean isSweepNormal() {
		return trigger.getSweep() == TriggerDefine.TrigNormalIndex;
	}

	/**
	 * @return 是否自动触发
	 */
	boolean isSweepAuto() {
		return trigger.getSweep() == TriggerDefine.TrigSweepAutoIndex;
	}

	/**
	 * 重置为自动触发
	 */
	void resumeAuto(TriggerControl tc) {
		trigger.c_setSweep(TriggerDefine.TrigSweepAutoIndex, tc);
	}

	/**
	 * @return
	 */
	public boolean isCurrentTrigger_Video() {
		return trigger == video;
	}

	/**
	 * @return
	 */
	public boolean isCurrentTrigger_Slope() {
		return trigger == slope;
	}

	/**
	 * @return
	 */
	public boolean isCurrentTrigger_Edge() {
		return trigger == edge;
	}

	/**
	 * 用于打印识别
	 */
	private int category;

	public String category() {
		switch (category) {
		case 0:
			return "single";
		case 1:
		case 2:
		case 3:
		case 4:
			return "alt_" + category;
		default:
			return "ts_?";
		}
	}

	private int mode_single_alt;

	public boolean isSingle() {
		return mode_single_alt == TriggerDefine.TrgModeSingleIndex;
	}

	public boolean isAlternate() {
		return mode_single_alt == TriggerDefine.TrgModeAltIndex;
	}

	public TriggerSet(int mode_single_alt) {
		this.mode_single_alt = mode_single_alt;
		slope = new SlopeTrigger();
		pulse = new PulseTrigger();
		video = new VideoTrigger();
		edge = new EdgeTrigger();
	}

	public void initTrigger(TrgTypeDefine idx, int category) {
		this.category = category;

		switch (idx) {
		case Slope:
			trigger = slope;
			break;
		case Edge:
			trigger = edge;
			break;
		case Video:
			trigger = video;
			break;
		case Pulse:
			trigger = pulse;
			break;
		}
	}

	public void load(String pre, Pref p) {
		slope.load(pre, p);
		edge.load(pre, p);
		video.load(pre, p);
		pulse.load(pre, p);
	}

	public void setTrigger(AbsTrigger at) {
		trigger = at;
	}

}