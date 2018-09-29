package com.owon.uppersoft.dso.function.measure;

import static com.owon.uppersoft.vds.core.measure.MeasureT.AVERage;

import java.beans.PropertyChangeSupport;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;

import com.owon.uppersoft.dso.model.trigger.TriggerInfoForChannel;
import com.owon.uppersoft.dso.wf.ON_WF_Iterator;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.core.aspect.help.IWF;
import com.owon.uppersoft.vds.core.measure.MeasureADC;
import com.owon.uppersoft.vds.core.measure.MeasureElem;
import com.owon.uppersoft.vds.core.measure.MeasureModel;
import com.owon.uppersoft.vds.core.measure.MeasureT;
import com.owon.uppersoft.vds.core.measure.MeasureUtil;
import com.owon.uppersoft.vds.core.measure.VR;
import com.owon.uppersoft.vds.core.measure.VerticalValueMeasure;

/**
 * MeasureManager，测量管理
 * 
 */
public class MeasureManager {
	public static final String RefreshMeasureResult = "RefreshMeasureResult";

	/** 提供给外部调用时判断是否跳过 */
	private boolean on = false;

	private PropertyChangeSupport pcs;
	private MeasureModel model;

	public MeasureManager(PropertyChangeSupport pcs, MeasureModel measMod) {
		this.pcs = pcs;
		this.model = measMod;
		setMeasureOn(measMod.enforcePermit());
	}

	public void fire_RefreshMeasureResult() {
		pcs.firePropertyChange(RefreshMeasureResult, null, null);
	}

	protected int precust(ON_WF_Iterator owi) {
		int Vnum = 0;
		VR[] vrs;
		while (owi.hasNext()) {
			IWF wf = owi.next();
			vrs = wf.getMeasureADC().vrs;
			vrs[AVERage.idx].on = true;

			if (wf.getADC_Buffer() == null) {
				System.err.println("wf.getADC_Buffer() is null");
				return -1;
			}
			Vnum = wf.getADC_Buffer().remaining();
		}
		return Vnum;
	}

	public void setMeasureOn_enforce() {
		boolean permit = model.enforcePermit();
		setMeasureOn(permit);
		pcs.firePropertyChange(MeasureModel.Refresh_MeasurePane_Selected, null,
				null);
	}

	public void setMeasureOn(boolean o) {
		if (o != on)
			on = o;
		fire_RefreshMeasureResult();
	}

	public boolean ison() {
		return on;
	}

	/**
	 * 始终都在算
	 */
	public void measure(MeasureWFSupport wfm, TriggerInfoForChannel tip,
			double freLimit, boolean mayDelayInvalid, VoltageProvider vp,
			MeasureModel measMod, int channelsNumber, TimePerPointProvider tpp) {
		if (!on)
			return;

		ON_WF_Iterator owi = wfm.on_wf_Iterator();

		int points = precust(owi);// 各通道平均值设成on，要计算
		// VR[] vrs = wf.madc.vrs;//一个与MeasureT枚举一一对应的数组
		// MeasureT[] mts = VALUES;
		// int len = mts.length;
		if (points < 0)
			return;

		double timePerPoint = tpp.getTimePerPoint(points);

		owi.reset();
		while (owi.hasNext()) {
			IWF wf = owi.next();

			if (wf.isOn()) {// && wf.getChannelNumber() == 1
				// TODO 无法避免地出现buf null
				if (wf.getADC_Buffer() == null)
					continue;

				int chl = wf.getChannelNumber();
				prepareArray(wf);
				doWaveFormMeasure(wf, tip.isChannelVideoTrg(chl), freLimit,
						timePerPoint, vp);
			}
		}

		int Delay1to2[] = { MeasureT.FAILURE, MeasureT.FAILURE };
		int Delay3to4[] = { MeasureT.FAILURE, MeasureT.FAILURE };

		LinkedList<MeasureElem> othmts = measMod.othMTlinked;
		Iterator<MeasureElem> ime = othmts.iterator();
		MeasureElem me;

		IWF wfa, wfb;
		if (channelsNumber >= 2) {
			wfa = wfm.getWaveForm(0);
			wfb = wfm.getWaveForm(1);
			if (wfa.isOn() && wfb.isOn()) {
				Delay1to2 = doDelayMeasure(wfa, wfb);

				me = ime.next();
				setDelayValue(me, Delay1to2[0], timePerPoint, mayDelayInvalid);
				me = ime.next();
				setDelayValue(me, Delay1to2[1], timePerPoint, mayDelayInvalid);
			}
		}

		// TODO 还是无法避免buf null
		if (channelsNumber >= 4) {
			wfa = wfm.getWaveForm(2);
			wfb = wfm.getWaveForm(3);
			if (wfa.isOn() && wfb.isOn()) {
				Delay3to4 = doDelayMeasure(wfa, wfb);

				me = ime.next();
				setDelayValue(me, Delay3to4[0], timePerPoint, mayDelayInvalid);
				me = ime.next();
				setDelayValue(me, Delay3to4[1], timePerPoint, mayDelayInvalid);

			}
		}

		fire_RefreshMeasureResult();
	}

	private void setDelayValue(MeasureElem me, int delayValue,
			double timePerPoint, boolean mayDelayInvalid) {
		if (delayValue < 0)
			me.setDelayValue(MeasureT.FAILURE);
		else if (mayDelayInvalid)
			me.setDelayValue(MeasureT.FAILURE);
		else
			me.setDelayValue(delayValue * timePerPoint);
	}

	public int[] doDelayMeasure(IWF wf1, IWF wf2) {
		MeasureADC madc1 = wf1.getMeasureADC(), madc2 = wf2.getMeasureADC();
		/* KNOW DelayP_N[0]为延迟上升沿，DelayP_N[1]为延迟下降沿 */
		int delayRaise = MeasureT.FAILURE, delayFall = MeasureT.FAILURE;

		ByteBuffer buf = wf2.getADC_Buffer();
		if (buf == null) {
			int Delay[] = { delayRaise, delayFall };
			return Delay;
		}
		VerticalValueMeasure vvc2 = madc2.vvc;
		int l = madc2.l;
		int[] array = madc2.array;

		double ampLow = vvc2.Vamp / 10 + vvc2.Vbase;
		double ampHigh = vvc2.Vamp * 9 / 10 + vvc2.Vbase;
		double ampHalf;
		// System.err.print("Vamp:"+madc2.Vamp+"ampLow:"+ampLow+",ampHigh:"+ampHigh+",");
		// 可能是嵌套太复杂，混淆通不过
		if (wf1.isOn() && wf2.isOn()) {
			// if (!wf1.isInverted() && !wf2.isInverted()) {
			if ((MeasureT.FAILURE != madc1.raiseTime)
					&& (MeasureT.FAILURE != madc2.raiseTime)) {
				delayRaise = MeasureUtil.get_delayPOS_PD_searchEdge(
						madc1.raiseStart, MeasureT.RISE, ampLow, ampHigh,
						array, l);
			} else {
				delayRaise = delayFall = MeasureT.FAILURE;
			}

			// } else if (!wf1.isInverted() && wf2.isInverted()) {
			// if ((FAIL != wf1.raiseTime) && (FAIL != wf2.fallTime)) {
			// DelayP_N[0] = MeasureUtil.get_delayPOS_PD_searchEdge(1,
			// FALL, ampLow, ampHigh, array, l);
			//
			// } else {
			// delayRaise = delayFall = FAIL;
			// }
			// } else if (wf1.isInverted() && !wf2.isInverted()) {
			// if ((FAIL != wf1.fallTime) && (FAIL != wf2.raiseTime)) {
			// DelayP_N[0] = MeasureUtil.get_delayPOS_PD_searchEdge(1,
			// RISE, ampLow, ampHigh, array, l);
			// } else {
			// delayRaise = delayFall = FAIL;
			// }
			// } else if (wf1.isInverted() && wf2.isInverted()) {
			// if ((FAIL != wf1.fallTime) && (FAIL != wf2.fallTime)) {
			// DelayP_N[0] = MeasureUtil.get_delayPOS_PD_searchEdge(1,
			// FALL, ampLow, ampHigh, array, l);
			//
			// } else {
			// delayRaise = delayFall = FAIL;
			// }
			// }

			// if (!wf1.isInverted() && !wf2.isInverted()) {
			if ((MeasureT.FAILURE != madc1.fallTime)
					&& (MeasureT.FAILURE != madc2.fallTime)) {
				delayFall = MeasureUtil.get_delayPOS_ND_searchEdge(
						madc1.fallStart, MeasureT.FALL, ampLow, ampHigh, array,
						l);
			} else {
				delayRaise = delayFall = MeasureT.FAILURE;
			}
			// } else if (!wf1.isInverted() && wf2.isInverted()) {
			// if ((FAIL != wf1.fallTime) && (FAIL != wf2.raiseTime)) {
			// DelayP_N[1] = MeasureUtil.get_delayPOS_ND_searchEdge(1,
			// RISE, ampLow, ampHigh, array, l);
			//
			// } else {
			// delayRaise = delayFall = FAIL;
			// }
			// } else if (wf1.isInverted() && !wf2.isInverted()) {
			// if ((FAIL != wf1.raiseTime) && (FAIL != wf2.fallTime)) {
			// DelayP_N[1] = MeasureUtil.get_delayPOS_ND_searchEdge(1,
			// FALL, ampLow, ampHigh, array, l);
			//
			// } else {
			// delayRaise = delayFall = FAIL;
			// }
			// } else if (wf1.isInverted() && wf2.isInverted()) {
			// if ((FAIL != wf1.raiseTime) && (FAIL != wf2.raiseTime)) {
			// DelayP_N[1] = MeasureUtil.get_delayPOS_ND_searchEdge(1,
			// RISE, ampLow, ampHigh, array, l);
			//
			// } else {
			// delayRaise = delayFall = FAIL;
			// }
			// }
		} else {
			delayRaise = delayFall = MeasureT.FAILURE;
		}

		int Delay[] = { delayRaise, delayFall };
		return Delay;
	}

	// private byte[] array, array1 = new byte[6000], array2 = new byte[6000];
	// private int p, l;

	/**
	 * 把采样的点的 ADC值(以垂直中心为零点，上正下负)转化为屏幕带零点位置的值
	 * 
	 * 并且后面计算各种测量值都依据转化后的值来算。
	 */
	private void prepareArray(IWF wf) {
		ByteBuffer buf = wf.getADC_Buffer();// 采样的点 ADC值，以垂直中心为零点，上正下负。
		MeasureADC madc = wf.getMeasureADC();

		byte[] array0 = buf.array();
		int pos0 = wf.getFirstLoadPos0();
		int p = buf.position();
		int l = buf.limit();
		int[] array = madc.array;

		int j = 0;
		madc.p = j;
		for (int i = p; i < l; i++, j++) {
			array[j] = array0[i] - pos0;
			// System.err.print(array[j] + "(" + array0[i] + "-" + pos0 + ")");
		}
		madc.l = j;
	}

	// private static final double H_DIV_PER_POINT = 20 / (double) 1000;

	/**
	 * ？压缩点如何处理？
	 * 
	 * @param dh
	 * @param wf
	 */
	protected void doWaveFormMeasure(IWF wf, boolean isvideo, double freLimit,
			double timePerPoint, VoltageProvider vp) {
		MeasureADC madc = wf.getMeasureADC();
		VR[] vrs = madc.vrs;// 一个与MeasureT枚举一一对应的数组
		madc.doVerticalMeasure(wf, isvideo, freLimit, vp);
		doHorizontalMeasure(wf, vrs, isvideo, freLimit, timePerPoint);// .getMeasureADC()
	}

	protected void doHorizontalMeasure(IWF wf, VR[] vrs, boolean isvideo,
			double freLimit, double timePerPoint) {
		MeasureADC madc = wf.getMeasureADC();
		VerticalValueMeasure vvc = madc.vvc;

		int l = madc.l, p = madc.p;
		int[] array = madc.array;
		int Vbase = vvc.Vbase, Vamp = vvc.Vamp;// ,Vtop = vvc.Vtop;
		int Vnum = l - p;

		/** 计算周期 */
		double Vavg = vvc.Vavg;
		int Vmid = (vvc.Vmax + vvc.Vmin) >> 1;// Vmax-(Vmax-Vmin)/2,最大值-幅值一半的位置
		// int Vmid = 0;// adc点已扣除pos0，窗口中点位置固定垂直中部
		int initPos;

		/**
		 * 用于在测量周期时转换一个周期起始的adc点到屏幕像素点
		 * 
		 * 算法还不确定，且基本没有实现，所以暂时屏蔽，只使用固定的方式返回
		 * 
		 * @param pos
		 * @return
		 */

		if (Math.abs(vvc.Vmax - Vavg) >= Math.abs(vvc.Vmin - Vavg)) {
			initPos = vvc.VmaxPos % 4;
		} else {
			initPos = vvc.VminPos % 4;
		}

		float peroid = computePeroid(initPos, Vmid, Vnum, array, l);// p+initPos?initPos?
		// System.out.println(peroid);
		/** 计算上升下降时间 */
		// float raiseTime = 0, fallTime = 0, pWidth = 0, nWidth = 0, pDuty,
		// nDuty;
		// int raiseStart = 0, fallStart = 0, raiseFlag = 0, fallFlag = 0;
		//
		// float ampLow = (float) Vamp / 10 + Vbase;
		// float ampHigh = (float) Vamp * 9 / 10 + Vbase;
		//
		// float ampHalf;
		// int i = p;
		// int t1 = 0;
		// int firstEdge = FAILURE;
		// for (; i < l; i++) {
		// v = array[i];
		// // 若点电压值从开始就比amLow小,则第一波形边是从下而上的。
		// if (ampLow > v) {
		// firstEdge = RISE;
		// break;
		// }
		// // 若点电压值从开始就比ampHigh大,则第一波形边是从上而下的。
		// if (ampHigh < v) {
		// firstEdge = FALL;
		// break;
		// }
		// }
		//
		// if (firstEdge == RISE) {
		// for (; i < l; i++) {
		// v = array[i];
		// if (ampLow > v) {
		// t1 = i;
		// }
		// if (ampHigh <= v) {
		// raiseTime = i - t1;
		// raiseStart = t1;
		// break;
		// }
		// }
		// for (; i < l; i++) {
		// v = array[i];
		// if (ampHigh <= v) {
		// t1 = i;
		// }
		// if (ampLow > v) {
		// fallTime = i - t1;
		// fallStart = t1;
		// break;
		// }
		// }
		// }
		// if (firstEdge == FALL) {
		// // FALL时，从ampHigh到ampLow，之间的点走过的时间为fallTime
		// for (; i < l; i++) {
		// v = array[i];
		// if (ampHigh < v) {
		// t1 = i;
		// }
		// if (ampLow >= v) {
		// fallTime = i - t1;
		// fallStart = t1;
		// break;
		// }
		// }
		// // FALL时，再从ampLow到ampHigh，之间的点走过的时间为raiseTime
		// for (; i < l; i++) {
		// v = array[i];
		// if (ampLow > v) {
		// t1 = i;
		// }
		// if (ampHigh <= v) {
		// raiseTime = i - t1;
		// raiseStart = t1;
		// break;
		// }
		// }
		// }
		// if (i > l) {
		// fallTime = -1;
		// raiseTime = -1;
		// }
		//
		// if (raiseTime >= 0 && raiseTime <= 4)
		// raiseFlag = 1;
		// else
		// raiseFlag = 0;
		//
		// if (fallTime >= 0 && fallTime <= 4)
		// fallFlag = 1;
		// else
		// fallFlag = 0;
		//
		// madc.raiseTime = raiseTime;
		// madc.fallTime = fallTime;
		// madc.raiseStart = raiseStart;
		// madc.fallStart = fallStart;
		// //
		// System.out.println("raiseStart:"+raiseStart+",fallStart:"+fallStart);
		getRiseFallTime(madc);

		/** 计算正负脉宽 */
		int i;
		int v;
		float ampHalf = (float) Vamp / 2 + Vbase;
		float ampLow = ampHalf - 5;
		float ampHigh = ampHalf + 5;
		for (i = p; i < l; i++) {
			v = array[i];
			if (ampLow > v) {
				getFirstPwidth(i, ampHalf, ampHigh, ampLow, madc, p, l, array);
				break;
			} else
				madc.pWidth = -1;

			if (ampHigh < v) {
				getFirstNwidth(i, ampHalf, ampHigh, ampLow, madc, p, l, array);
				break;
			} else
				madc.nWidth = -1;
		}
		// madc.pWidth = pWidth;
		// madc.nWidth = nWidth;
		// System.out.println(String.format("pWidth:%f,nWidth:%f", pWidth,
		// nWidth));

		/** 计算正负占空比 */
		float pDuty, nDuty;
		if (madc.pWidth == -1 || madc.nWidth == -1) {
			pDuty = nDuty = -1;
		} else {
			pDuty = madc.pWidth / (madc.pWidth + madc.nWidth);
			nDuty = madc.nWidth / (madc.pWidth + madc.nWidth);
		}
		ampLow = Vamp / 10 + Vbase;
		ampHigh = Vamp * 9 / 10 + Vbase;
		boolean invalid = false;// vvc.Vmax_VminBeyond;

		// Vmax_VminBeyond = wf.isADCBeyondMax() || wf.isADCBeyondMin();
		VR.setHorizontalValue(vrs, MeasureT.PERiod, (peroid * timePerPoint),
				invalid, isvideo, freLimit);
		VR.setHorizontalValue(vrs, MeasureT.FREQuency,
				(1 / vrs[MeasureT.PERiod.idx].v), invalid, isvideo, freLimit);
		VR.setRise_FallTimeValue(vrs, MeasureT.RTime,
				(madc.raiseTime * timePerPoint), invalid, madc.raiseFlag,
				isvideo);
		VR.setRise_FallTimeValue(vrs, MeasureT.FTime,
				(madc.fallTime * timePerPoint), invalid, madc.fallFlag, isvideo);
		VR.setHorizontalValue(vrs, MeasureT.PWIDth,
				(madc.pWidth * timePerPoint), invalid, isvideo, freLimit);
		VR.setHorizontalValue(vrs, MeasureT.NWIDth,
				(madc.nWidth * timePerPoint), invalid, isvideo, freLimit);
		VR.setHorizontalValue(vrs, MeasureT.PDUTy, pDuty, invalid, isvideo,
				freLimit);
		VR.setHorizontalValue(vrs, MeasureT.NDUTy, nDuty, invalid, isvideo,
				freLimit);

		// System.out.print("[" + wf.toString() + "]");
		// System.out.print(" peroid:" + vrs[MeasureT.Peroid.idx].vu);
		// System.out.print("\t raiseTime:" + vrs[MeasureT.RaiseTime.idx].vu);
		// System.out.print("\t fallTime:" + vrs[MeasureT.FallTime.idx].vu);
		// System.out.print("\t pWidth:" + vrs[MeasureT.PulseWidth.idx].vu);
		// System.out.print("\t nWidth:" + vrs[MeasureT._PlusWidth.idx].vu);
		// System.out.print("\t pDuty:" + vrs[MeasureT.DutyCycle.idx].vu);
		// System.out.print("\t nDuty:" + vrs[MeasureT._DutyCycle.idx].vu);
		// System.out.print("\t Vpp:" + vrs[MeasureT.Vpp.idx].vu);
		// System.out.print("\t Vmax:" + vrs[MeasureT.Vmax.idx].vu);
		// System.out.print("\t Vmin:" + vrs[MeasureT.Vmin.idx].vu);
		// System.out.print("\t Vamp:" + vrs[MeasureT.Vamp.idx].vu);
		// System.out.print("\t Vtop:" + vrs[MeasureT.Vtop.idx].vu);
		// System.out.print("\t Vbase:" + vrs[MeasureT.Vbase.idx].vu);
		// System.out.print("\t Overshoot:" + vrs[MeasureT.Overshoot.idx].vu);
		// System.out.print("\t Preshoot:" + vrs[MeasureT.Preshoot.idx].vu);
		// System.out.print("\t Vavg:" + vrs[MeasureT.Average.idx].vu);
		// System.out.print("\t Vrms:" + vrs[MeasureT.Vrms.idx].vu);
		// System.out.println("");
	}

	/**
	 * 预留的用于在原始adc上去除零点的方法
	 * 
	 * @param wf
	 * @deprecated
	 */
	protected void hV(IWF wf) {
		MeasureADC madc = wf.getMeasureADC();
		VerticalValueMeasure vvc = madc.vvc;

		int pos0 = 0;
		int Vsquare_sum = 0, Vnum = 0, Vsum = 0;
		/*
		 * KNOW array每点减Pos0后的修改， Vmax、Vmin、Vsum、Vsquare_sum
		 * 
		 * MEM_1k[v +128](无法立即处理)
		 */
		vvc.Vmax -= pos0;
		vvc.Vmin -= pos0;
		/* [ pos0*pos0*Vnum+(-2*pos0)*∑(p->l)v ]为[(v-pos0)*(v-pos0)]与[v * v]的差 */
		Vsquare_sum = Vsquare_sum + (pos0 * pos0 * Vnum - 2 * Vsum * pos0);
		Vsum -= (Vnum * pos0);
		// MEM_1k[v + 128]++; MEM_1k产生的变动，后面处理

		/* KNOW array每点减Pos0后,MEM_1k相应改变后的修改 */
		vvc.Vtop -= pos0;
		vvc.Vbase -= pos0;
		// ----------------------------------------------------
		/* KNOW array每点减Pos0后, ampLow、ampHigh补回pos0后的修改 */
		float ampLow = (float) vvc.Vamp / 10 + vvc.Vbase + pos0;
		float ampHigh = (float) vvc.Vamp * 9 / 10 + vvc.Vbase + pos0;
		/* KNOW array每点减Pos0后, ampHalf补回pos0后的修改 */
		float ampHalf = vvc.Vamp / 2 + vvc.Vbase + pos0;
		// Vmid += pos0;
	}

	protected void getFirstNwidth(int start, double ampHalf, double ampHigh,
			double ampLow, MeasureADC madc, int p, int l, int[] array) {
		int p1 = p;
		int condition = 0;
		int v;

		for (int i = start; i < l; i++) {
			v = array[i];
			switch (condition) {
			case 0:
				if (ampHalf >= v) {
					p1 = i;
					condition++;
				}
				break;
			case 1:
				if (ampLow >= v) {
					condition++;
				}
				break;
			case 2:
				if (ampHalf <= v) {
					madc.nWidth = i - p1;
					if (i <= p1)
						madc.nWidth = -1;
					p1 = i;
					condition++;
				}
				break;
			case 3:
				if (ampHigh <= v) {
					condition++;
				}
				break;
			case 4:
				if (ampHalf >= v) {
					madc.pWidth = i - p1;
					if (i <= p1)
						madc.pWidth = -1;
					return;
				}
				break;
			}
		}
	}

	protected void getFirstPwidth(int start, double ampHalf, double ampHigh,
			double ampLow, MeasureADC madc, int p, int l, int[] array) {
		int p1 = p;
		int condition = 0;

		int v;
		for (int i = start; i < l; i++) {
			v = array[i];
			switch (condition) {
			case 0:
				if (ampHalf <= v) {
					p1 = i;
					condition++;
				}
				break;
			case 1:
				if (ampHigh <= v) {
					condition++;
				}
				break;
			case 2:
				if (ampHalf >= v) {
					madc.pWidth = i - p1;
					if (i <= p1)
						madc.pWidth = -1;
					p1 = i;
					condition++;
				}

				break;
			case 3:
				if (ampLow >= v) {
					condition++;
				}
				break;
			case 4:
				if (ampHalf <= v) {
					madc.nWidth = i - p1;
					if (i <= p1)
						madc.nWidth = -1;
					return;
				}
				break;
			}
		}
	}

	/**
	 * @param start
	 * @param ADMiddle
	 *            (Vmax + Vmin) >> 1
	 * @param Vnum
	 * @param array
	 * @param l
	 * @param pos0
	 * @return cyc_show 既周期
	 */
	protected float computePeroid(int start, int ADMiddle, int Vnum,
			int[] array, int l) {
		final int step = 2;
		int winLow = ADMiddle - 7, winHigh = ADMiddle + 7;// 窗口门限，参考FPGA
		// 原来的门限值,即窗口中间值的上下限
		// int largerthan, lessthan
		int status = MeasureT.FAILURE, edge = MeasureT.FAILURE;// status_flag,edgeSelect;
		int start_counter = 0, win_cyc_counter = 0, win_data_counter = 0;
		for (int j = start; j < l; j += step) {

			if (array[j] > winLow && array[j] >= winHigh) {
				if (status == MeasureT.RISE) {
					if (edge == MeasureT.RISE) {
						win_cyc_counter++;
						win_data_counter = start_counter;
					}
					if (edge == MeasureT.FAILURE) {
						edge = MeasureT.RISE;
						start_counter = 0;
					}
				}
				status = MeasureT.FALL;
			}
			if (array[j] <= winLow && array[j] < winHigh) {
				if (status == MeasureT.FALL) {
					if (edge == MeasureT.FALL) {
						win_cyc_counter++;
						win_data_counter = start_counter;
					}
					if (edge == MeasureT.FAILURE) {
						edge = MeasureT.FALL;
						start_counter = 0;
					}
				}
				status = MeasureT.RISE;
			}
			start_counter += step;
		}
		// System.out.println(win_cyc_counter + ">?" + (1024 / step));
		if (win_cyc_counter != 0 && win_cyc_counter <= 1024 / step) {
			// 单位是ms,要换算成s
			// FFT时间间隔不一样!
			float cyc_show = (win_data_counter / (float) win_cyc_counter);
			// * step * (1000 / (float)Vnum)

			// double cyc_show = step * dblTimeInterval * win_data_counter
			// / win_cyc_counter;
			// System.out.println("period:" + 1000/cyc_show);
			return cyc_show;
		} else {
			return -1;// 0
		}
	}

	void getRiseFallTime(MeasureADC madc) {
		int v;
		int l = madc.l, p = madc.p;
		int[] array = madc.array;
		VerticalValueMeasure vvc = madc.vvc;
		int Vbase = vvc.Vbase, Vamp = vvc.Vamp;// ,Vtop = madc.Vtop;
		float raiseTime = 0, fallTime = 0;
		int raiseStart = 0, fallStart = 0, raiseFlag = 0, fallFlag = 0;

		float ampLow = (float) Vamp / 10 + Vbase;
		float ampHigh = (float) Vamp * 9 / 10 + Vbase;

		int i = p;
		int t1 = 0;
		int firstEdge = MeasureT.FAILURE;
		for (; i < l; i++) {
			v = array[i];
			// 若点电压值从开始就比amLow小,则第一波形边是从下而上的。
			if (ampLow > v) {
				firstEdge = MeasureT.RISE;
				break;
			}
			// 若点电压值从开始就比ampHigh大,则第一波形边是从上而下的。
			if (ampHigh < v) {
				firstEdge = MeasureT.FALL;
				break;
			}
		}

		if (firstEdge == MeasureT.RISE) {
			for (; i < l; i++) {
				v = array[i];
				if (ampLow > v) {
					t1 = i;
				}
				if (ampHigh <= v) {
					raiseTime = i - t1;
					raiseStart = t1;
					break;
				}
			}
			for (; i < l; i++) {
				v = array[i];
				if (ampHigh <= v) {
					t1 = i;
				}
				if (ampLow > v) {
					fallTime = i - t1;
					fallStart = t1;
					break;
				}
			}
		}
		if (firstEdge == MeasureT.FALL) {
			// FALL时，从ampHigh到ampLow，之间的点走过的时间为fallTime
			for (; i < l; i++) {
				v = array[i];
				if (ampHigh < v) {
					t1 = i;
				}
				if (ampLow >= v) {
					fallTime = i - t1;
					fallStart = t1;
					break;
				}
			}
			// FALL时，再从ampLow到ampHigh，之间的点走过的时间为raiseTime
			for (; i < l; i++) {
				v = array[i];
				if (ampLow > v) {
					t1 = i;
				}
				if (ampHigh <= v) {
					raiseTime = i - t1;
					raiseStart = t1;
					break;
				}
			}
		}
		if (i > l) {
			fallTime = -1;
			raiseTime = -1;
		}

		if (raiseTime >= 0 && raiseTime <= 4)
			raiseFlag = 1;
		else
			raiseFlag = 0;

		if (fallTime >= 0 && fallTime <= 4)
			fallFlag = 1;
		else
			fallFlag = 0;

		madc.raiseTime = raiseTime;
		madc.fallTime = fallTime;
		madc.raiseStart = raiseStart;
		madc.fallStart = fallStart;
		madc.raiseFlag = raiseFlag;
		madc.fallFlag = fallFlag;
		// System.out.println("raiseStart:"+raiseStart+",fallStart:"+fallStart);
	}
}
