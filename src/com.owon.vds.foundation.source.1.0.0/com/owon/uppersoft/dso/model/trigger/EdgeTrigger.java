package com.owon.uppersoft.dso.model.trigger;

import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_edge_holdoff;
import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_voltsense;

import com.owon.uppersoft.dso.data.LObject;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.socket.ScpiPool;
import com.owon.uppersoft.vds.util.Pref;

public class EdgeTrigger extends VoltsensableTrigger {
	/** 控件为对称限位而设计 */
	public static final int VoltSenseHalfRange = GDefine.TriggerLevelDivRange
			* GDefine.PIXELS_PER_DIV;

	public static final LObject[] RiseFall = { new LObject("M.Trg.Edge.Rise"),
			new LObject("M.Trg.Edge.Fall") };

	public int raisefall = 0;

	public EdgeTrigger() {
		super(TrgTypeDefine.Edge, VoltSenseHalfRange);
	}

	@Override
	public void loadProperties(String prefix, Pref p) {
		super.loadProperties(prefix, p);
		String txt = getName();
		raisefall = p.loadInt(prefix + txt + ".raisefall");
	}

	@Override
	public void persistProperties(String prefix, Pref p) {
		String txt = getName();
		p.persistInt(prefix + txt + ".raisefall", raisefall);

		super.persistProperties(prefix, p);
	}

	public String getSlope() {
		return raisefall == 0 ? "RISE" : "FALL";
	}

	public String setSlope(String risefall) {
		if (risefall.equalsIgnoreCase("RISE"))
			raisefall = 0;
		else if (risefall.equalsIgnoreCase("FALL"))
			raisefall = 1;
		else
			return ScpiPool.Failed;
		return ScpiPool.Success;
	}

	@Override
	public String getIconKey() {
		return raisefall == 0 ? "er.png" : "ef.png";
	}

	@Override
	public void nextStatus() {
		raisefall = (raisefall + 1) % RiseFall.length;
	}

	@Override
	public void submitVoltsense(int mode, int chl, Submitable sbm) {
		sbm.c_trg_edge(mode, chl, trg_voltsense, c_getVoltsense(), raisefall);
	}

	@Override
	public void submitHoldOff(int mode, int chl, Submitable sbm) {
		sbm.c_trg_edge(mode, chl, trg_edge_holdoff, etvho.toInt(),
				etvho.getValueDivTimeOnStage(), etvho.enumPart());
	}

}