package com.owon.uppersoft.vds.source.comm.ext;

import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_edge_holdoff;
import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_pulse_condition;
import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_pulse_holdoff;
import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_slope_condition;
import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_slope_holdoff;
import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_slope_lowest;
import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_slope_uppest;
import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_video_holdoff;
import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_video_sync;
import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_voltsense;

import com.owon.uppersoft.dso.model.trigger.AbsTrigger;
import com.owon.uppersoft.dso.model.trigger.EdgeTrigger;
import com.owon.uppersoft.dso.model.trigger.PulseTrigger;
import com.owon.uppersoft.dso.model.trigger.SlopeTrigger;
import com.owon.uppersoft.dso.model.trigger.TriggerControl;
import com.owon.uppersoft.dso.model.trigger.TriggerSet;
import com.owon.uppersoft.dso.model.trigger.VideoTrigger;
import com.owon.uppersoft.dso.model.trigger.common.Thredshold;
import com.owon.uppersoft.dso.model.trigger.helper.ETV_Holdoff;
import com.owon.uppersoft.dso.model.trigger.helper.ETV_TrgConditon;
import com.owon.uppersoft.vds.device.interpret.util.CBitSet;
import com.owon.uppersoft.vds.source.comm.Submitor2;

public class TinyTrgSubmitHandler {
	/** 可考虑转译 */
	private static final int[] Tiny_TrgType = { 0, 2, 1, 3 };
	private static final int[] Tiny_Sweep = { 0, 1, 2 };
	private static final int[] Tiny_SYNC = { 0, 1, 2, 3, 4 };

	private static final int[] Tiny_CONDITIONS_PULSE = { 0, 1, 2, 4, 5, 6 };
	private static final int[] Tiny_CONDITIONS_SLOPE = { 0, 1, 2, 4, 5, 6 };

	public void selfSubmit_Tiny(Submitor2 sbm, TriggerControl tc) {
		byte alpha = tc.getChannelModeAlpha();

		boolean single;
		if (tc.getChannelMode() == 1) {
			single = false;
			int size = tc.getChannelsNumber();

			/** 通道没开也会发... */
			for (int i = 0; i < size; i++) {
				TriggerSet ts = tc.getAlternateTriggerSet(i);
				if (ts != null) {
					handleSubmit_trg_type(alpha, i, sbm, single, ts, tc);
					sbm.wrEmpty();
				}
			}
		} else {
			/** 留意外部触发时 */
			single = true;

			TriggerSet ts = tc.getSingleTriggerSet();
			handleSubmit_trg_type(alpha, ts.getChannel(), sbm, single, ts, tc);
		}
	}

	private void handleSubmit_trg_type(int mode, int chl, Submitor2 sbm,
			boolean single, TriggerSet ts, TriggerControl tc) {
		CBitSet bs = new CBitSet(16);
		bs.clear();
		boolean alt = !single;

		AbsTrigger trigger = ts.getTrigger();
		int channel = ts.getChannel();
		// 触发类型，和index对应的方式是高低位颠倒
		int type = Tiny_TrgType[trigger.type.ordinal()];
		if (single) {
			/** 留意外部触发时 */
			bs.set1(alt, 15);
			bs.setN(type, 8, 14);

			if (tc.isExtTrg(channel)) {
				bs.set1(0, 13);
				bs.set1(1, 0);
			} else {
				bs.set1(channel, 13);
				bs.set1(0, 0);
			}
		} else {
			bs.set1(alt, 15);
			bs.setN(type, 13, 8);
			bs.set1(channel, 14);
		}
		if (trigger instanceof VideoTrigger) {
			VideoTrigger vt = (VideoTrigger) trigger;
			bs.setN(vt.module, 7, 9);
		}
		submit_trg_type(trigger, bs, single);
		sbm.wrTrg(bs.getValue());

		submit_trg_addition(mode, chl, trigger, sbm);
	}

	private void submit_trg_type(AbsTrigger at, CBitSet bs, boolean single) {
		switch (at.type) {
		case Edge: {
			EdgeTrigger et = (EdgeTrigger) at;
			int AC = 0;
			int sweepidx = Tiny_Sweep[et.getSweep()];
			if (single) {
				bs.set1(et.raisefall, 12);
				bs.setN(sweepidx, 10, 11);
				bs.set1(AC, 9);
			} else {
				bs.set1(et.raisefall, 12);
			}
		}
			break;
		case Video: {
			VideoTrigger vt = (VideoTrigger) at;
			int syncidx = Tiny_SYNC[vt.sync];
			if (single) {
				bs.setN(syncidx, 10, 11, 12);
			} else {
				bs.setN(syncidx, 10, 11, 12);
			}
		}
			break;
		case Slope: {
			SlopeTrigger st = (SlopeTrigger) at;
			int sweepidx = Tiny_Sweep[st.getSweep()];
			int ct = Tiny_CONDITIONS_SLOPE[st.condition];
			// DBG.configln("ConditionTypeOfTiny: "+ct);
			if (single) {
				bs.setN(sweepidx, 10, 11);
				bs.setN(ct, 5, 6, 7);
			} else {
				bs.setN(ct, 5, 6, 7);
			}
		}
			break;
		case Pulse: {
			PulseTrigger pt = (PulseTrigger) at;
			int sweepidx = Tiny_Sweep[pt.getSweep()];
			int ct = Tiny_CONDITIONS_PULSE[pt.condition];
			// DBG.configln("ConditionTypeOfTiny: "+ct);
			if (single) {
				bs.setN(sweepidx, 10, 11);
				bs.setN(ct, 5, 6, 7);
			} else {
				bs.setN(ct, 5, 6, 7);
			}
		}
			break;
		}
	}

	private void submit_trg_addition(int mode, int chl, AbsTrigger at,
			Submitor2 sbm) {
		ETV_Holdoff etvho = at.etvho;
		switch (at.type) {
		case Edge: {
			EdgeTrigger et = (EdgeTrigger) at;
			sbm.c_trg_edge(mode, chl, trg_edge_holdoff, etvho.toInt(),
					etvho.getValueDivTimeOnStage(), etvho.enumPart());
			sbm.c_trg_edge(mode, chl, trg_voltsense, et.c_getVoltsense(),
					et.raisefall);
		}
			break;
		case Video: {
			VideoTrigger vt = (VideoTrigger) at;
			sbm.c_trg_video(mode, chl, trg_video_holdoff, etvho.toInt(),
					etvho.getValueDivTimeOnStage(), etvho.enumPart());
			sbm.c_trg_video(mode, chl, trg_video_sync, vt.sync, vt.syncValue);
		}
			break;
		case Slope: {
			SlopeTrigger st = (SlopeTrigger) at;
			ETV_TrgConditon trgcondition = st.trgcondition;
			sbm.c_trg_slope(mode, chl, trg_slope_holdoff, etvho.toInt(),
					etvho.getValueDivTimeOnStage(), etvho.enumPart());
			sbm.c_trg_slope(mode, chl, trg_slope_condition, st.condition,
					trgcondition.toInt(),
					trgcondition.getValueDivTimeOnStage(),
					trgcondition.enumPart());
			Thredshold ts = st.getThredshold();
			sbm.c_trg_slope(mode, chl, trg_slope_uppest, ts.c_getUppest());
			sbm.c_trg_slope(mode, chl, trg_slope_lowest, ts.c_getLowest());
		}
			break;
		case Pulse: {
			PulseTrigger pt = (PulseTrigger) at;
			ETV_TrgConditon trgcondition = pt.trgcondition;
			sbm.c_trg_pulse(mode, chl, trg_pulse_holdoff, etvho.toInt(),
					etvho.getValueDivTimeOnStage(), etvho.enumPart());
			sbm.c_trg_pulse(mode, chl, trg_voltsense, pt.c_getVoltsense(),
					pt.getRaiseNFall());
			sbm.c_trg_pulse(mode, chl, trg_pulse_condition, pt.condition,
					trgcondition.toInt(),
					trgcondition.getValueDivTimeOnStage(),
					trgcondition.enumPart());
		}
			break;
		}
	}
}
