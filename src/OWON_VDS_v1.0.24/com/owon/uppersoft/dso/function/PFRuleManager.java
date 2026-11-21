package com.owon.uppersoft.dso.function;

import static com.owon.uppersoft.vds.function.rule.PFRuleControl.FAIL;
import static com.owon.uppersoft.vds.function.rule.PFRuleControl.PASS;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.owon.uppersoft.dso.function.perspective.CompositeWaveForm;
import com.owon.uppersoft.dso.global.ControlApps;
import com.owon.uppersoft.dso.global.Platform;
import com.owon.uppersoft.dso.model.WaveFormManager;
import com.owon.uppersoft.dso.util.DBG;
import com.owon.uppersoft.dso.wf.WaveForm;
import com.owon.uppersoft.vds.core.aspect.help.AreaImageHelper;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.function.rule.PFRuleControl;
import com.owon.uppersoft.vds.function.rule.PFRuleControl.ComplexBuffer;
import com.owon.uppersoft.vds.function.rule.RuleDetail;
import com.owon.uppersoft.vds.util.Pref;
import com.owon.uppersoft.vds.util.format.SFormatter;

/**
 * PFRuleManager, manages the running state, result output, saving and loading; this is where the configuration info is stored, no resource release is needed
 * 
 */
public class PFRuleManager {
	public static final int max_rule_num = 8;

	private List<RuleDetail> rule_list;
	private PFRuleControl pfrc;

	public PFRuleManager(Pref p, int channelNumber) {
		rule_list = new ArrayList<RuleDetail>(max_rule_num);
		load(p, channelNumber);
	}

	public List<RuleDetail> getRuleList() {
		return rule_list;
	}

	public RuleDetail load(Pref p, int channelNumber) {
		List<String> list = p.loadStringList("Rule.rules", ";", max_rule_num);
		if (!rule_list.isEmpty())
			rule_list.clear();
		RuleDetail rd;
		for (String t : list) {
			rd = RuleDetail.getInstance(t, channelNumber);
			if (rd != null)
				rule_list.add(rd);
		}

		show_msg = p.loadBoolean("Rule.msg");
		ring = p.loadBoolean("Rule.ring");
		stop_on_output = p.loadBoolean("Rule.stopOnOutput");
		setOutputRule(p.loadInt("Rule.outputRule"));

		rd = RuleDetail.getInstanceWithAdjust(p.getProperty("Rule.current"),
				channelNumber);
		pfrc = new PFRuleControl(rd);
		return rd;
	}

	public void persist(Pref p) {
		p.setProperty("Rule.current", pfrc.getRuleDetail().toPersist());

		p.persistBoolean("Rule.msg", show_msg);
		p.persistBoolean("Rule.ring", ring);
		p.persistBoolean("Rule.stopOnOutput", stop_on_output);
		p.persistInt("Rule.outputRule", output_rule);

		List<String> list = new LinkedList<String>();
		for (RuleDetail rd : rule_list) {
			if (rd != null)
				list.add(rd.toPersist());
		}
		p.persistStringList("Rule.rules", list, ";");
	}

	public PFRuleControl getPFRuleControl() {
		return pfrc;
	}

	public void receiveData(WaveFormManager wfm, int screendatalen) {
		if (!pfrc.isRuleEnable())
			return;

		pfrc.resetRusult();
		int chl = pfrc.getRuleChannel();

		ComplexBuffer cb = new ComplexBuffer();
		boolean pass2Rule = true;
		if (wfm.isMath(chl)) {
			CompositeWaveForm cwf = wfm.getCompositeWaveForm();
			if (cwf == null || !cwf.isOn())
				pass2Rule = false;

			cb.ib = cwf.getADC_Buffer();
			cb.isByteArray = false;
		} else {
			WaveForm wf = wfm.getWaveForm(chl);
			if (wf == null || !wf.isOn())
				pass2Rule = false;

			cb.bb = wf.getADC_Buffer();
			cb.isByteArray = true;
		}

		if (pass2Rule)
			pfrc.receiveData(cb, checking, screendatalen);

		handleCheckResult(pfrc.getResult());
	}

	private int output_rule = PASS;

	/**
	 * Output rules
	 * 
	 * @param rule
	 *            PASS or FAIL
	 */
	public void setOutputRule(int rule) {
		boolean b = rule == PASS || rule == FAIL;
		if (!b) {
			return;
		}
		output_rule = rule;
	}

	public int getOutputRule() {
		return output_rule;
	}

	/**
	 * Reset the output count
	 */
	public void resetPFCounts() {
		pf_counts[PASS] = pf_counts[FAIL] = 0;
	}

	private int[] pf_counts = new int[2];
	private boolean stop_on_output = false, ring, show_msg;

	public void paintRulePoints(Graphics2D g2d, boolean isScreenMode_3,
			Rectangle r, AreaImageHelper gd) {
		pfrc.paintRulePoints(g2d, isScreenMode_3, r, gd);
	}

	public void adjustView() {
		pfrc.adjustView();
	}

	/**
	 * Draw the rule check result info
	 * 
	 * @param g2d
	 * @param pc
	 */
	public void paintPFLabel(Graphics2D g2d, ScreenContext pc) {
		if (show_msg && pfrc.isRuleEnable()) {
			int ac = pf_counts[PASS], fc = pf_counts[FAIL];

			int tt = ac + fc;
			int x = 20, y = 20, rt = 20, w = 85, h = 65, arc = 10;

			g2d.setColor(Color.BLACK);
			g2d.fillRoundRect(x, y, w, h, arc, arc);
			g2d.setColor(Color.RED);
			g2d.drawRoundRect(x, y, w, h, arc, arc);

			x += 5;
			y = 35;
			g2d.drawString(SFormatter.UIformat("pass: %d", ac), x, y);
			y += rt;
			g2d.drawString(SFormatter.UIformat("fail: %d", fc), x, y);
			y += rt;
			g2d.drawString(SFormatter.UIformat("total: %d", tt), x, y);
		}
	}

	private void logResult(int r) {
		if (r == PASS) {
			DBG.outprintln("output: PASS");
		} else {
			DBG.errprintln("output: FAIL");
		}
	}

	/**
	 * Handle the output result
	 * 
	 * @param result
	 */
	private void handleCheckResult(int result) {
		if (result == PASS || result == FAIL) {
			pf_counts[result]++;
		} else {// None
			return;
		}
		// logResult(result);
		if (result == output_rule) {
			if (ring) {
				Toolkit.getDefaultToolkit().beep();
			}
			if (stop_on_output) {
				/**
				 * KNOW
				 * 
				 * Do not stop checking, so that clicking run can continue to judge pass/fail and accumulate the counts
				 * 
				 * Enable single-frame receive mode on Pass/Fail, saving only the last frame of data, but still receiving all frames and discarding the others
				 * 
				 * Since stopping works as an immediate stop, and the frame just stopped on can yield the device's deep memory data, the handling here is convenient
				 * 
				 */

				Platform.getControlApps().interComm.statusStop(false);
			}
		}

		/** 25M the device has a problem when the stop command is sent for stop-on-output after pf judgment */
		ca.interComm.sync_pf(result);
	}

	/** Rule output settings */
	/**
	 * @param show
	 *            info display
	 */
	public void setShowMsg(boolean show) {
		show_msg = show;
	}

	public boolean isShowMsg() {
		return show_msg;
	}

	/**
	 * @param r
	 *            beep
	 */
	public void setRing(boolean r) {
		ring = r;
	}

	public boolean isRing() {
		return ring;
	}

	/**
	 * @param b
	 *            stop on output
	 */
	public void setStopOnOutput(boolean b) {
		stop_on_output = b;
	}

	public boolean isStopOnOutput() {
		return stop_on_output;
	}

	/** Handle the output result */
	private boolean checking = false;

	/** Start/stop rule checking */
	/**
	 * @return whether checking is in progress
	 */
	public boolean isChecking() {
		return checking;
	}

	/**
	 * Start rule checking
	 */
	public void runCheck() {
		checking = true;
		resetPFCounts();

		ca = Platform.getControlApps();
	}

	private ControlApps ca;

	/**
	 * Stop rule checking
	 */
	public void stopCheck() {
		checking = false;
		// resetPFCounts();
	}

}
