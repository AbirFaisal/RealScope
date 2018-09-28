package com.owon.uppersoft.dso.model.trigger;

import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_slope_condition;
import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_slope_holdoff;
import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_slope_lowest;
import static com.owon.uppersoft.dso.source.comm.CProtocol.trg_slope_uppest;

import java.awt.Color;
import java.awt.Graphics2D;

import com.owon.uppersoft.dso.control.TrgLevelCheckHandler;
import com.owon.uppersoft.dso.model.trigger.common.Thredshold;
import com.owon.uppersoft.dso.model.trigger.condition.TrgConditionDelegate;
import com.owon.uppersoft.dso.model.trigger.helper.ETV_TrgConditon;
import com.owon.uppersoft.dso.model.trigger.helper.PaintChannelTrgLabelContext;
import com.owon.uppersoft.dso.source.comm.CProtocol;
import com.owon.uppersoft.dso.util.ui.LineUtil;
import com.owon.uppersoft.dso.wf.ChannelInfo;
import com.owon.uppersoft.vds.core.GDefine;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.data.LocRectangle;
import com.owon.uppersoft.vds.util.Pref;

public class SlopeTrigger extends AbsTrigger {
	/** 控件为对称限位而设计 */
	public static final int ThredsholdHalfRange = GDefine.TriggerLevelDivRange
			* GDefine.PIXELS_PER_DIV;

	public int condition = 0;
	private Thredshold ts;

	public ETV_TrgConditon trgcondition = TrgConditionDelegate.createETV();

	private int sweep = 0;

	public Thredshold getThredshold() {
		return ts;
	}

	public boolean c_setUppestOrLowest(TrgCheckType type, int v) {
		switch (type) {
		case UppOver:
			return ts.c_setUppest(v);
		case LowOver:
			return ts.c_setLowest(v);
		default:
			return false;
		}
	}

	public boolean c_addUppestOrLowest(TrgCheckType type, int del) {
		switch (type) {
		case UppOver:
			return ts.c_setUppest(ts.c_getUppest() + del);
		case LowOver:
			return ts.c_setLowest(ts.c_getLowest() + del);
		default:
			return false;
		}
	}

	public int c_getLowest() {
		return ts.c_getLowest();
	}

	public int c_getUppest() {
		return ts.c_getUppest();
	}

	public boolean c_setUppestWithoutSync(int v) {
		return ts.c_setUppest(v);
	}

	public boolean c_setLowestWithoutSync(int v) {
		return ts.c_setLowest(v);
	}

	public String getUppestLabel(boolean inverse, int voltbase, int pos0) {
		return ts.getUppestLabel(inverse, voltbase, pos0);
	}

	public String getLowestLabel(boolean inverse, int voltbase, int pos0) {
		return ts.getLowestLabel(inverse, voltbase, pos0);
	}

	public SlopeTrigger() {
		super(TrgTypeDefine.Slope);
		ts = new Thredshold(ThredsholdHalfRange);
	}

	@Override
	public void paintIcon(Graphics2D g2d) {
	}

	@Override
	public void loadProperties(String prefix, Pref p) {
		String txt = getName();
		loadETV(prefix, p, trgcondition);
		condition = p.loadInt(prefix + txt + ".condition");

		/** load的时候允许无限界 */
		c_setUppestWithoutSync(p.loadInt(prefix + txt + ".uppest"));
		c_setLowestWithoutSync(p.loadInt(prefix + txt + ".lowest"));

		setSweep(p.loadInt(prefix + txt + ".sweep"));
	}

	@Override
	public void persistProperties(String prefix, Pref p) {
		String txt = getName();
		persistETV(prefix, p, trgcondition);
		p.persistInt(prefix + txt + ".condition", condition);

		p.persistInt(prefix + txt + ".uppest", ts.c_getUppest());

		p.persistInt(prefix + txt + ".lowest", ts.c_getLowest());
		p.persistInt(prefix + txt + ".sweep", getSweep());
	}

	@Override
	public String getIconKey() {
		String k = null;
		switch (condition) {
		case 0:
		case 1:
		case 2:
			k = "sr.png";
			break;
		case 3:
		case 4:
		case 5:
			k = "sf.png";
			break;
		default:
			break;
		}
		return k;
	}

	@Override
	public String getLabelText(boolean inverse, int voltbase, int pos0) {
		// 在slope的时候作为条件文本
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
		return k + ' ' + trgcondition;
	}

	@Override
	public void nextStatus() {
		condition = (condition + 1) % TriggerDefine.CONDITIONS_SLOPE.length;
	}

	/**
	 * 在选择并修改的时候，内部保存该类型(上限或是下限或是无)，以备画图的时候选择作为画在上面的
	 */
	private TrgCheckType thredsholdType = TrgCheckType.NotOver;

	/**
	 * 对指定参数进行增减
	 * 
	 * @param del
	 *            不为0才发指令改变
	 * @param type
	 * @return 触发的事件类名
	 */
	public boolean handelIncr(int del, TrgCheckType type) {
		thredsholdType = type;
		return c_addUppestOrLowest(type, del);
	}

	public boolean doCheckTrgLevel(TrgLevelCheckHandler checkHandler) {
		int v0, v1;
		TrgCheckType t0, t1;
		switch (thredsholdType) {
		default:
		case UppOver:
			v0 = ts.c_getUppest();
			v1 = ts.c_getLowest();
			t0 = TrgCheckType.UppOver;
			t1 = TrgCheckType.LowOver;
			break;
		case LowOver:
			v1 = ts.c_getUppest();
			v0 = ts.c_getLowest();
			t0 = TrgCheckType.LowOver;
			t1 = TrgCheckType.UppOver;
			break;
		}

		/**
		 * 先检测上限，再检测下限，
		 * 
		 * 上限如果判断为真，则不再判断下限
		 */
		boolean b = checkHandler.checkAroundTrgAndHandleOnTrue(v0, t0);
		if (b) {
			return b;
		}

		return checkHandler.checkAroundTrgAndHandle(v1, t1,
				TrgCheckType.NotOver);
	}

	/**
	 * 画触发电平标志
	 * 
	 */
	public void paintChannelTrgLabel(PaintChannelTrgLabelContext pctlc) {
		Graphics2D g2d = pctlc.g2d;
		ScreenContext pc = pctlc.pc;

		ChannelInfo ci = pctlc.ci;
		LocRectangle lr = pctlc.lr;
		boolean lineLevel = pctlc.lineLevel;
		boolean inverted = ci.isInverse();
		int pos0 = ci.getPos0();
		Color c = ci.getColor();

		boolean showDetail = lineLevel && !pctlc.cssm.isTrgInfoControlActive();

		int y = 0;

		// 绘画框值信息
		int voltbase = ci.getVoltageLabel().getValue();
		int p0 = ci.getPos0();
		boolean inverse = ci.isInverse();

		String detailInfo = null;
		// 阈值上下限，用限制的值，仅用在位置的绘制上
		int l = ts.c_getLowest(), u = ts.c_getUppest();

		// 反相画图倒一下就行了，在3in1前面
		if (inverted) {
			u = ChannelInfo.getLevelFromPos0(u, pos0);
			l = ChannelInfo.getLevelFromPos0(l, pos0);
		}

		if (pc.isScreenMode_3()) {
		} else {
			l = l << 1;
			u = u << 1;
		}

		/** 操作的时候默认会选定为当前通道，这样可以保证其它通道不会也跟着显示两条线 */
		int y1, y2;
		boolean higtlightTb = false;
		String txt, txt_operate;
		int hcenter = pc.getHcenter();
		switch (thredsholdType) {
		case LowOver:
			if (showDetail) {// 阈值上下限，可以用无限制的值显示，一改变这个值也被限制到有限的范围内了
				detailInfo = getLowestLabel(inverse, voltbase, p0);
			}

			y1 = hcenter - u;
			txt = "T1";
			y2 = hcenter - l;
			txt_operate = "T2";
			higtlightTb = true;
			break;
		case UppOver:
			if (showDetail) {// 阈值上下限，可以用无限制的值显示，一改变这个值也被限制到有限的范围内了
				detailInfo = getUppestLabel(inverse, voltbase, p0);
			}

			y1 = hcenter - l;
			txt = "T2";
			y2 = hcenter - u;
			txt_operate = "T1";
			higtlightTb = true;
			break;
		default:
			// 默认先画上限，再画下限
			y1 = hcenter - u;
			txt = "T1";
			y2 = hcenter - l;
			txt_operate = "T2";
			higtlightTb = false;
		}
		higtlightTb &= lineLevel;

		// System.out.println(b1 + ", " + b2);
		LineUtil.paintThredsholds(y1, lr, g2d, lineLevel, false, c, txt);
		LineUtil.paintThredsholds(y2, lr, g2d, lineLevel, higtlightTb, c,
				txt_operate);
		y = y2;// y2为对应type的位置

		/**
		 * 画触发电平标志的信息框，通过y的设置来判断出哪一个需要画框
		 */
		if (detailInfo != null && showDetail) {
			LineUtil.paintTrgLevelDetail(g2d, lr, y, detailInfo, c);
		}
	}

	public void submitUpper_Lower(int mode, int chl, Submitable sbm) {
		sbm.c_trg_slope(mode, chl, trg_slope_uppest, ts.c_getUppest());
		sbm.c_trg_slope(mode, chl, trg_slope_lowest, ts.c_getLowest());
	}

	public void submitUpper_Lower(int mode, int chl, Submitable sbm,
			TrgCheckType trg_slope_type) {
		if (trg_slope_type.code == CProtocol.trg_slope_lowest) {
			sbm.c_trg_slope(mode, chl, trg_slope_lowest, ts.c_getLowest());
		} else if (trg_slope_type.code == CProtocol.trg_slope_uppest) {
			sbm.c_trg_slope(mode, chl, trg_slope_uppest, ts.c_getUppest());
		}
	}

	@Override
	public void submitHoldOff(int mode, int chl, Submitable sbm) {
		sbm.c_trg_slope(mode, chl, trg_slope_holdoff, etvho.toInt(),
				etvho.getValueDivTimeOnStage(), etvho.enumPart());
	}

	public void submitCondition(int mode, int chl, Submitable sbm) {
		sbm.c_trg_slope(mode, chl, trg_slope_condition, condition,
				trgcondition.toInt(), trgcondition.getValueDivTimeOnStage(),
				trgcondition.enumPart());
	}

	public static final boolean isCondition_Equal(int condition) {
		return condition == 1 || condition == 4;
	}

	public void c_setSweep(int sweep, TriggerControl tc) {
		this.sweep = sweep;
		tc.doSumbitTrgSweep(sweep);
	}

	public int setSweep(int sweep) {
		return this.sweep = sweep;
	}

	public int getSweep() {
		return sweep;
	}
}