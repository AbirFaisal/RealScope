package com.owon.uppersoft.vds.core.measure;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.core.aspect.help.IWF;
import com.owon.uppersoft.vds.util.ArrayLogger;

public class VerticalValueMeasure {

	public int Vbase;
	public int Vtop;
	public int Vamp;

	public int VmaxPos;
	public int VminPos;
	public int Vmax;
	public int Vmin;
	public int Vpp;

	private int Vsum, Vnum;
	public double Vavg, Vrms;

	private float Overshoot, Preshoot;

	private static int kk = 1 << 8;
	private static int[] MEM_1k;

	public VerticalValueMeasure() {
	}

	public void getVtopVbase(ByteBuffer buf, int pos0) {
		rewind();// MEM_1k全部置0。
		int v, Vsum = 0, Vnum;
		/** 在1024个不同的值中，找出出现频率最多的，按其大小，作为顶端值和底端值 */
		byte array[] = buf.array();
		int p = buf.position();
		int l = buf.limit();
		Vnum = l - p;

		while (p < l) {
			v = array[p];// v指采集来的垂直电压adc点， v为正负数
			Vsum += v;
			/* v范围在-128~127，建立等大小字节数组MEM_1k,相同电压的点出现越多,(以该电压加128为索引的)字节标记越大 */
			MEM_1k[v + 128]++;
			p++;
		}
		
		// ArrayLogger.outArray2Logable(new VLog(), MEM_1k, 0, MEM_1k.length);

		int len1k = MEM_1k.length;
		double Vavg = (double) Vsum / (double) Vnum;
		int tmp = (int) (Vavg + 128);
		int v1 = MEM_1k[0], v2 = MEM_1k[tmp];
		int i1 = tmp, i2 = tmp;
		/** 在低于平均值的一半MEM_1k[]中出现最多的v，既是底端值Vbase */
		for (int i = 0; i < tmp; i++) {
			v = MEM_1k[i];
			if (v > v1) {
				v1 = v;
				i1 = i; // 在MEM_1k数组中遍历0到tmp，i1最大，即i1代表的那个点出现最多次。
			}
		}
		/** 在高于平均值的一半MEM_1k[]中出现最多的v，既是顶端值Vbase */
		for (int i = tmp; i < len1k; i++) {
			v = MEM_1k[i];
			if (v > v2) {
				v2 = v;
				i2 = i;
			}
		}
		// System.out.println(i2 +", "+i1);
		// 如果重复值小于一定程度20，那么不认为存在底端值
		// if (v1 < 4)
		// i1 = Vmin + 128;
		// if (v2 < 4)
		// i2 = Vmax + 128;

		/** 此处为扣去零点的adc纯采样数值 */
		/** 从索引值转回原数据,原数据未考虑pos0,需要补上 */
		this.Vtop = i2 - 128 - pos0;
		this.Vbase = i1 - 128 - pos0;

		this.Vamp = Math.abs(this.Vtop - this.Vbase);
		// System.out.println(String.format("Vtop:%d,Vbase:%d,Vmax:%d,Vmin:%d",
		// Vtop, Vbase, Vmax, Vmin));
	}

	private static void rewind() {
		if (MEM_1k == null || MEM_1k.length < kk)
			MEM_1k = new int[kk];
		Arrays.fill(MEM_1k, (int) 0);
	}

	public void getVmaxVmin(IWF wf) {
		int v;
		MeasureADC madc = wf.getMeasureADC();
		int[] array = madc.array;
		int l = madc.l, p = madc.p;

		int VmaxPos = p, VminPos = p;
		int Vmax, Vmin, Vsum = 0, Vnum = l - p;// Vnum：有效波形中 ，点的个数。
		long Vsquare_sum = 0;
		Vmax = Vmin = array[p];
		// rewind(MEM_1k);// MEM_1k全部置0。

		/** 找出max，min；计算平方和，累计1024种[-511~512]不同值出现的次数 */
		int tmp_p = p;
		while (p < l) {
			v = array[p];// array[p],v指采集来的垂直电压adc点，已经把pos0考虑在内 v为正负数
			if (v > Vmax) {
				Vmax = v;
				VmaxPos = p;
			} else if (v < Vmin) {
				Vmin = v;
				VminPos = p;
			}
			Vsum += v;
			Vsquare_sum += v * v;
			/* v范围在-128~127，建立等大小字节数组MEM_1k,相同电压的点出现越多,(以该电压加128为索引的)字节标记越大 */
			// MEM_1k[v + 128]++;
			p++;
		}

		p = tmp_p;

		this.Vmax = Vmax;
		this.Vmin = Vmin;
		this.VmaxPos = VmaxPos;
		this.VminPos = VminPos;
		this.Vpp = Math.abs(this.Vmax - this.Vmin);

		// System.err.println(String.format("Vmax:%d,Vmin:%d", Vmax, Vmin));
		/** 计算平均值和均方根 */
		this.Vsum = Vsum;
		this.Vnum = Vnum;
		this.Vavg = (double) Vsum / (double) Vnum;
		this.Vrms = Math.sqrt((double) Vsquare_sum / (double) Vnum);
	}

	private void getShoots() {
		if (Vamp == 0) {
			Overshoot = Preshoot = -1;
		} else {
			Overshoot = (Vmax - Vtop) / (float) Vamp;
			Preshoot = (Vbase - Vmin) / (float) Vamp;
		}
	}

	public boolean Vmax_VminBeyond;

	private void rest(boolean isvideo, double freLimit, double Vconst,
			VR[] vrs, boolean adcBeyondmax, boolean adcBeyondMin) {
		this.Vmax_VminBeyond = adcBeyondmax || adcBeyondMin;

		boolean noSense = Vmax == Vmin;
		boolean VtopInvalid = Vtop > Vmax || noSense || Vtop == Vbase;
		boolean VbaseInvalid = Vbase < Vmin || noSense || Vtop == Vbase;
		boolean VampInvalid = VtopInvalid || VbaseInvalid;

		VR.setVerticalValue(vrs, MeasureT.PKPK, Vpp * Vconst, Vmax_VminBeyond,
				noSense, isvideo);
		VR.setVerticalValue(vrs, MeasureT.MAX, Vmax * Vconst, Vmax_VminBeyond,
				noSense, isvideo);// adcBeyondmax
		VR.setVerticalValue(vrs, MeasureT.MIN, Vmin * Vconst, Vmax_VminBeyond,
				noSense, isvideo);// adcBeyondMin
		VR.setVerticalValue(vrs, MeasureT.AVERage, this.Vavg * Vconst,
				Vmax_VminBeyond, noSense, isvideo);
		VR.setVerticalValue(vrs, MeasureT.CYCRms, this.Vrms * Vconst,
				Vmax_VminBeyond, noSense, isvideo);

		VR.setVerticalValue(vrs, MeasureT.VAMP, Vamp * Vconst, Vmax_VminBeyond,
				VampInvalid, isvideo);
		VR.setVerticalValue(vrs, MeasureT.VTOP, Vtop * Vconst, Vmax_VminBeyond,
				VtopInvalid, isvideo);
		VR.setVerticalValue(vrs, MeasureT.VBASe, Vbase * Vconst,
				Vmax_VminBeyond, VbaseInvalid, isvideo);
		VR.setVerticalValue(vrs, MeasureT.OVERshoot, Overshoot,
				Vmax_VminBeyond, VampInvalid, isvideo);// OvershootBeyond
		VR.setVerticalValue(vrs, MeasureT.PREShoot, Preshoot, Vmax_VminBeyond,
				VampInvalid, isvideo);// PreshootBeyond

	}

	public void doMeasure(VR[] vrs, IWF wf, boolean isvideo, double freLimit,
			double Vconst, ByteBuffer bb) {
		/** 计算最大值和最小值,及平均值和均方根 */
		getVmaxVmin(wf);

		/** 计算顶端值和底端值 */
		getVtopVbase(bb, wf.getFirstLoadPos0());

		getShoots();

		// System.out.println(String.format("Vamp:%d,Vpp:%d", Vamp, Vpp));
		// System.out.println("Vmax:"+Vmax+",Vtop:"+Vtop+",Vamp:"+Vamp+",Vbase:"+Vbase+",Vmin:"+Vmin);

		rest(isvideo, freLimit, Vconst, vrs, wf.isADCBeyondMax(),
				wf.isADCBeyondMin());
	}
}