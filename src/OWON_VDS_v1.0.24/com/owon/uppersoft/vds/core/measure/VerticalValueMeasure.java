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
		rewind();// MEM_1k all set to 0.
		int v, Vsum = 0, Vnum;
		/** Among 1024 different values, find the most frequent ones and use their magnitudes as the top value and the base value */
		byte array[] = buf.array();
		int p = buf.position();
		int l = buf.limit();
		Vnum = l - p;

		while (p < l) {
			v = array[p];// v is the vertical voltage adc point acquired, v can be positive or negative
			Vsum += v;
			/* v ranges from -128 to 127; build an equal-size byte array MEM_1k; the more points of the same voltage, the larger the byte mark (indexed by that voltage plus 128) */
			MEM_1k[v + 128]++;
			p++;
		}
		
		// ArrayLogger.outArray2Logable(new VLog(), MEM_1k, 0, MEM_1k.length);

		int len1k = MEM_1k.length;
		double Vavg = (double) Vsum / (double) Vnum;
		int tmp = (int) (Vavg + 128);
		int v1 = MEM_1k[0], v2 = MEM_1k[tmp];
		int i1 = tmp, i2 = tmp;
		/** The v that occurs most in the half of MEM_1k[] below the average is the base value Vbase */
		for (int i = 0; i < tmp; i++) {
			v = MEM_1k[i];
			if (v > v1) {
				v1 = v;
				i1 = i; // Traverse MEM_1k from 0 to tmp; the largest i1 is the point that appears the most times.
			}
		}
		/** The v that occurs most in the half of MEM_1k[] above the average is the top value Vbase */
		for (int i = tmp; i < len1k; i++) {
			v = MEM_1k[i];
			if (v > v2) {
				v2 = v;
				i2 = i;
			}
		}
		// System.out.println(i2 +", "+i1);
		// If the repeat count is below a certain level (20), the base value is considered nonexistent
		// if (v1 < 4)
		// i1 = Vmin + 128;
		// if (v2 < 4)
		// i2 = Vmax + 128;

		/** Here is the pure adc sampling value with the zero point removed */
		/** Convert from index back to original data; the original data did not account for pos0, so it needs to be added back */
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
		int Vmax, Vmin, Vsum = 0, Vnum = l - p;// Vnum: number of points in the valid waveform.
		long Vsquare_sum = 0;
		Vmax = Vmin = array[p];
		// rewind(MEM_1k);// MEM_1k all set to 0.

		/** Find max and min; compute the sum of squares and accumulate the occurrence counts of the 1024 different values [-511~512] */
		int tmp_p = p;
		while (p < l) {
			v = array[p];// array[p],v is the vertical voltage adc point acquired, pos0 already taken into account, v can be positive or negative
			if (v > Vmax) {
				Vmax = v;
				VmaxPos = p;
			} else if (v < Vmin) {
				Vmin = v;
				VminPos = p;
			}
			Vsum += v;
			Vsquare_sum += v * v;
			/* v ranges from -128 to 127; build an equal-size byte array MEM_1k; the more points of the same voltage, the larger the byte mark (indexed by that voltage plus 128) */
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
		/** Compute the average and RMS */
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
		/** Compute max and min, and the average and RMS */
		getVmaxVmin(wf);

		/** Compute top value and base value */
		getVtopVbase(bb, wf.getFirstLoadPos0());

		getShoots();

		// System.out.println(String.format("Vamp:%d,Vpp:%d", Vamp, Vpp));
		// System.out.println("Vmax:"+Vmax+",Vtop:"+Vtop+",Vamp:"+Vamp+",Vbase:"+Vbase+",Vmin:"+Vmin);

		rest(isvideo, freLimit, Vconst, vrs, wf.isADCBeyondMax(),
				wf.isADCBeyondMin());
	}
}