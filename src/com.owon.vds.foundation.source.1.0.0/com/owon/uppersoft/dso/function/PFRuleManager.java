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
 * PFRuleManager，管理运行状态，结果的输出，存盘和读取；这里是配置信息存放的位置，无需释放资源
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

		setShowMsg(p.loadBoolean("Rule.msg"));
		setRing(p.loadBoolean("Rule.ring"));
		setStopOnOutput(p.loadBoolean("Rule.stopOnOutput"));
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
	 * 输出规则
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
	 * 重置输出计数
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
	 * 画规则检测结果信息
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
	 * 处理输出结果
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
				 * 不停止checking，这样点运行可以继续判断通过失败，累加计数
				 * 
				 * 在Pass/Fail时开启单帧接收模式，只保存最后一帧数据，但仍是所有帧都接收，其它帧丢弃
				 * 
				 * 由于停止的做法为即停，并就停下的那副可以得到下位机的深存储数据，所以这边的处理方便了
				 * 
				 */

				Platform.getControlApps().interComm.statusStop(false);
			}
		}

		/** 25M 判断pf后输出即停发停止指令下位机有问题 */
		ca.interComm.sync_pf(result);
	}

	/** 规则输出设置 */
	/**
	 * @param show
	 *            信息显示
	 */
	public void setShowMsg(boolean show) {
		show_msg = show;
	}

	public boolean isShowMsg() {
		return show_msg;
	}

	/**
	 * @param r
	 *            响铃
	 */
	public void setRing(boolean r) {
		ring = r;
	}

	public boolean isRing() {
		return ring;
	}

	/**
	 * @param b
	 *            输出即停
	 */
	public void setStopOnOutput(boolean b) {
		stop_on_output = b;
	}

	public boolean isStopOnOutput() {
		return stop_on_output;
	}

	/** 处理输出结果 */
	private boolean checking = false;

	/** 开始停止规则检测 */
	/**
	 * @return 是否正在检测
	 */
	public boolean isChecking() {
		return checking;
	}

	/**
	 * 开始规则检测
	 */
	public void runCheck() {
		checking = true;
		resetPFCounts();

		ca = Platform.getControlApps();
	}

	private ControlApps ca;

	/**
	 * 停止规则检测
	 */
	public void stopCheck() {
		checking = false;
		// resetPFCounts();
	}

}
