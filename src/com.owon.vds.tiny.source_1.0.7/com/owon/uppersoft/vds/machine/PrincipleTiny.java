package com.owon.uppersoft.vds.machine;

import java.awt.Window;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.dso.global.Principle;
import com.owon.uppersoft.dso.global.WorkBenchTiny;
import com.owon.uppersoft.dso.model.trigger.condition.TrgCondition;
import com.owon.uppersoft.vds.core.comm.JobQueueDispatcher;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.core.machine.MachineType;
import com.owon.uppersoft.vds.core.tune.ICal;
import com.owon.uppersoft.vds.device.interpret.LowerTranslator;
import com.owon.uppersoft.vds.source.comm.Submitor2;
import com.owon.uppersoft.vds.util.Pref;
import com.owon.vds.calibration.stuff.CalArgTypeProvider;
import com.owon.vds.tiny.firm.pref.PrefControl;
import com.owon.vds.tiny.firm.pref.model.ArgType;
import com.owon.vds.tiny.tune.TinyTuneFunction;

public class PrincipleTiny implements Principle {

	private TinyTuneFunction tune;
	private TinyMachine mt;

	public PrincipleTiny(String macpara) {
		/** 似乎只用在机型查找表 */
		machTypIdMap = new HashMap<Integer, MachineType>();
		machTypIdMap.put(100, new VDS1022());
		machTypIdMap.put(102, new VDS2052());

		machineChange(macpara);

		catp = new CalArgTypeProvider() {
			@Override
			public ICal getSimpleAdjustCMDType(ArgType at) {
				return tune.getTuneModel().getSimpleAdjustCMDType(at.ordinal());
			}
		};
		// ControlManager.isNetConnectSupport = false;
	}

	private CalArgTypeProvider catp;

	public CalArgTypeProvider getCalArgTypeProvider() {
		return catp;
	}

	public TinyTuneFunction getTuneFunction() {
		return tune;
	}

	@Override
	public String getConfigurationDirectory() {
		return WorkBenchTiny.CONFIGURATION_DIR;
	}

	private HashMap<Integer, MachineType> machTypIdMap;

	public String getMachineTypeName(int id) {
		return machTypIdMap.get(id).name();
	}

	public int getMachineID(String machName) {
		Iterator<Entry<Integer, MachineType>> iter = machTypIdMap.entrySet()
				.iterator();
		while (iter.hasNext()) {
			MachineType val = iter.next().getValue();
			if (val.name().equals(machName)) {
				// System.out.println("PrincipleSamrt.getMachinID:" + machName);
				return val.saveID();
			}
		}
		return -1;
	}

	@Override
	public TinyMachine getMachineType() {
		return mt;
	}

	public void machineChange(String machineparam) {
		machine = new MachineInfo_Tiny(
				MachineInfo_Tiny.class
						.getResourceAsStream("/com/owon/uppersoft/dso/model/machine/params/"
								+ machineparam + ".txt"));

		TrgCondition.resumeDefaultSet();
		if (machineparam.startsWith("VDS1022")) {
			mt = new VDS1022();
		} else if (machineparam.startsWith("VDS2052")) {
			mt = new VDS2052();
			TrgCondition.resumeSet2();
		}

		// mt.prepareCommAddress();

		int chls = machine.getChannelNumbers();
		int[] volts = machine.intVOLTAGE[0];

		tune = mt.createTinyTuneFunction(chls, volts);
	}

	@Override
	public void prepareTuneFunction(Pref p) {
	}

	@Override
	public void openTuneDialog(final Window wnd, PropertyChangeSupport pcs) {
		tune.open(wnd);
	}

	@Override
	public void persist(Pref p) {
		tune.persist(p);
	}

	private MachineInfo_Tiny machine;

	@Override
	public MachineInfo_Tiny getMachine() {
		return machine;
	}

	private void createLowControlManger(ControlManager cm) {
		if (lcm == null)
			lcm = mt.createLowControlManger(cm);
	}

	public PrefControl getPrefControl() {
		return tune.getDefaultPrefControl();
	}

	private Submitor2 sb;
	private LowControlManger lcm;

	public LowControlManger getLowControlManger(ControlManager cm) {
		createLowControlManger(cm);
		return lcm;
	}

	@Override
	public Submitable createSubmitor(JobQueueDispatcher df, ControlManager cm) {
		createLowControlManger(cm);

		LowerTranslator lt = mt.createLowerTranslator(catp);
		return sb = mt.createSubmitor(df, lt, lcm, cm);
	}

	public String getMachineSeries(int id) {
		return machTypIdMap.get(id).series();
	}

	public boolean isSameSeries(int loadID) {
		String s = mt.series();
		String ls = getMachineSeries(loadID);
		return s.equals(ls);
	}

}