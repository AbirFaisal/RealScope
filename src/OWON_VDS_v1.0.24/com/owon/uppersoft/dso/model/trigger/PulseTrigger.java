package com.owon.uppersoft.dso.model.trigger;

import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_pulse_condition;
import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_pulse_holdoff;
import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_voltsense;

import java.awt.Graphics2D;

import com.owon.uppersoft.dso.model.trigger.condition.TrgConditionDelegate;
import com.owon.uppersoft.dso.model.trigger.helper.ETV_TrgConditon;
import com.owon.uppersoft.dso.view.sub.Label;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.util.Pref;

public class PulseTrigger extends VoltsensableTrigger {
	/** 控件为对称限位而设计 */
	public static final int VoltSenseHalfRange = GDefine.TriggerLevelDivRange
			* GDefine.PIXELS_PER_DIV;

	public int condition = 0;

	public ETV_TrgConditon trgcondition = TrgConditionDelegate.createETV();

	public PulseTrigger() {
		super(TrgTypeDefine.Pulse, VoltSenseHalfRange);
	}

	@Override
	public void loadProperties(String prefix, Pref p) {
		super.loadProperties(prefix, p);

		loadETV(prefix, p, trgcondition);
		String txt = getName();
		condition = p.loadInt(prefix + txt + ".condition");
	}

	@Override
	public void persistProperties(String prefix, Pref p) {
		persistETV(prefix, p, trgcondition);
		String txt = getName();
		p.persistInt(prefix + txt + ".condition", condition);

		super.persistProperties(prefix, p);
	}

	@Override
	public String getIconKey() {
		String k = null;
		switch (condition) {
		case 0:
		case 1:
		case 2:
			k = "pr.png";
			break;
		case 3:
		case 4:
		case 5:
			k = "pf.png";
			break;
		default:
			break;
		}
		return k;
	}

	@Override
	public void paintIcon(Graphics2D g2d) {
		int x = Label.trgmodstart + 8, y = 12;
		switch (condition) {
		case 0:
		case 3:
			g2d.drawString(">", x, y);
			break;
		case 1:
		case 4:
			g2d.drawString("=", x, y);
			break;
		case 2:
		case 5:
			g2d.drawString("<", x, y);
			break;
		}
	}

	public String getConditionText() {
		String k;
		switch (condition) {
		case 0:
		case 3:
			k = ">";
			break;
		case 1:
		case 4:
			k = "=";
			break;
		case 2:
		case 5:
			k = "<";
			break;
		default:
			return null;
		}
		return k + " " + trgcondition;
	}

	public int toRelateSense(int pos0) {
		return c_getVoltsense() - pos0;
	}

	@Override
	public void nextStatus() {
		condition = (condition + 1) % TriggerDefine.CONDITIONS_PULSE.length;
	}

	@Override
	public void submitVoltsense(int mode, int chl, Submitable sbm) {
		sbm.c_trg_pulse(mode, chl, trg_voltsense, c_getVoltsense(),
				getRaiseNFall());
	}

	public int getRaiseNFall() {
		return condition <= 2 ? 0 : 1;
	}

	@Override
	public void submitHoldOff(int mode, int chl, Submitable sbm) {
		sbm.c_trg_pulse(mode, chl, trg_pulse_holdoff, etvho.toInt(),
				etvho.getValueDivTimeOnStage(), etvho.enumPart());
	}

	public void submitCondition(int mode, int chl, Submitable sbm) {
		sbm.c_trg_pulse(mode, chl, trg_pulse_condition, condition,
				trgcondition.toInt(), trgcondition.getValueDivTimeOnStage(),
				trgcondition.enumPart());
	}

	public static final boolean isCondition_Equal(int condition) {
		return condition == 1 || condition == 4;
	}

}