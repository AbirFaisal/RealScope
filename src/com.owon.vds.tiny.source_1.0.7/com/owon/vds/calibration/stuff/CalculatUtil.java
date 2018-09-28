package com.owon.vds.calibration.stuff;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.aspect.base.VLog;
import com.owon.uppersoft.vds.core.measure.VerticalValueMeasure;
import com.owon.uppersoft.vds.util.ArrayLogger;
import com.owon.vds.tiny.circle.TopBaseCalResult;

public class CalculatUtil {

	public static double computeAverage(ByteBuffer bb) {
		final int p = bb.position();
		final int l = bb.limit();
		byte[] arr = bb.array();
		int i = p, sum = 0;
		while (i != l) {
			sum += arr[i++];
		}
		double avg = (double) sum / (l - p);
		return avg;
	}

	public static PKCalResult computePK(ByteBuffer bb) {
		final int p = bb.position();
		final int l = bb.limit();
		byte[] arr = bb.array();
		int i = p;
		int max = arr[i], min = arr[i];
		int v;

		// ArrayLogger.outArray2Logable(new VLog(), arr, p, l - p);
		while (i != l) {
			v = arr[i++];
			if (v > max)
				max = v;
			else if (v < min)
				min = v;
		}
		return new PKCalResult(min, max);
	}

	static VerticalValueMeasure vvc = new VerticalValueMeasure();

	public static TopBaseCalResult computeTopBase(ByteBuffer bb) {
		// final int p = bb.position();
		// final int l = bb.limit();
		// byte[] arr = bb.array();
		
		// ArrayLogger.outArray2Logable(new VLog(), arr, p, l - p);
		// 计算时已移到了竖直中心
		vvc.getVtopVbase(bb, 0);
		return new TopBaseCalResult(vvc.Vtop, vvc.Vbase);
	}
}
