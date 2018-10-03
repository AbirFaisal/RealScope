package com.owon.uppersoft.vds.core.measure;

public class MeasureUtil {
	private final static int FALL = MeasureT.FALL;
	private final static int RISE = MeasureT.RISE;
	private final static int FAILURE = MeasureT.FAILURE;
	private final static int StepValue = 3;

	public static int get_delayPOS_PD_searchEdge(int start, int edgeSelect,
			double ampLow, double ampHigh, int[] array, int l) {
		int pDelay;
		int i;
		// if (RISE == edgeSelect) {
		for (i = start; i < l; i += StepValue) {
			if (ampLow > array[i]) {
				break;
			}
		}
		// } else {
		// for (i = start; i < l; i++) {
		// if (ampHigh < array[i]) {
		// break;
		// }
		// }
		// }

		if (i >= l) {// array[i]
			pDelay = FAILURE;
			return pDelay;
		}
		int P1 = 0, P2;
		// if (RISE == edgeSelect) {
		for (; i < l; i += StepValue) {
			if (ampLow > array[i]) {
				P1 = i;
			}

			if (ampHigh <= array[i]) {
				P2 = i;
				break;
			}
		}
		// } else {
		// for (; i < l; i+=2) {//
		// if (ampHigh < array[i]) {
		// P1 = i;
		// }
		//
		// if (ampLow >= array[i]) {
		// P2 = i;
		// break;
		// }
		// }
		// }

		if (i >= l) {
			pDelay = FAILURE;
		} else {
			pDelay = P1 - start;
		}
		// System.out.println("P1:"+P1+",raiseStart:"+start+",pDelay:"+pDelay);
		return pDelay;
	}

	public static int get_delayPOS_ND_searchEdge(int start, int edgeSelect,
			double ampLow, double ampHigh, int[] array, int l) {
		int nDelay;
		int i;
		// if (RISE == edgeSelect) {
		// for (i = start; i < l; i++) {
		// if (ampLow > array[i]) {
		// break;
		// }
		// }
		// } else {
		for (i = start; i < l; i += StepValue) {
			if (ampHigh < array[i]) {
				break;
			}
		}
		// }

		if (i >= l) {// array[i]
			nDelay = FAILURE;
			return nDelay;
		}
		int P1 = 0, P2;
		// if (RISE == edgeSelect) {
		// for (; i < l; i += 2) {
		// if (ampLow > array[i]) {
		// P1 = i;
		// }
		//
		// if (ampHigh <= array[i]) {
		// P2 = i;
		// break;
		// }
		// }
		// } else {
		for (; i < l; i += StepValue) {
			if (ampHigh < array[i]) {
				P1 = i;
			}

			if (ampLow >= array[i]) {
				P2 = i;
				break;
			}
		}
		// }

		if (i >= l) {
			nDelay = FAILURE;
		} else {
			nDelay = P1 - start;
		}
		// System.out.println("P1:"+P1+",fallStart:"+start+",nDelay:"+nDelay);
		return nDelay;
	}
}
