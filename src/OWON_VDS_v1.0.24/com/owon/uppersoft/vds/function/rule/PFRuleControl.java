package com.owon.uppersoft.vds.function.rule;

import static com.owon.uppersoft.vds.function.rule.PFRuleUtil.createRulePoints;
import static com.owon.uppersoft.vds.function.rule.PFRuleUtil.paintRuleArea;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.owon.uppersoft.vds.core.aspect.help.AreaImageHelper;
import com.owon.uppersoft.vds.data.Point;

/**
 * PFRuleControl uses the rule to obtain results and optimizes the drawing of rule boundary points; it occupies many resources here, which can be released after the enable is turned off
 * 
 */
public class PFRuleControl {
	public static class ComplexBuffer {
		public boolean isByteArray;

		public IntBuffer ib;
		public ByteBuffer bb;
	}

	/** screen width */
	public static final int AREA_WIDTH = 1000;
	/** number of pixels per horizontal division */
	public static final int AREA_WIDTH_BLOCK_PIXES = 50;
	/** number of distinct adc values per vertical division */
	public static final int PIXELS_PER_DIV = 25;

	/***/
	public static final int PASS = 1, FAIL = 0, None = -1;

	private boolean enable_rule = false, prepared_rule = false;
	private int R_verticalSet = 10, R_horizontalSet = 10;

	/**
	 * this length is the used range of the array obtained after dividing the adc data equally by 1k screen pixels and taking the extreme values,
	 * 
	 * directly affects adc_max / adc_min, rule_max / rule_minadc_max / adc_min, rule_max / rule_min
	 */
	private int data_num;

	private int[] adc_min, adc_max;
	/** rule_max refers to rule_ceiling, rule_min refers to rule_floor */
	private int[] rule_max, rule_min;

	public PFRuleControl(RuleDetail rd) {
		this.rd = rd;
	}

	private int result;

	public int getResult() {
		return result;
	}

	public void resetRusult() {
		result = None;
	}

	/**
	 * called once after receiving data to generate the rule boundary points
	 * 
	 * the last frame of a transfer is used
	 * 
	 * @param wfm
	 * @param output_rule
	 *            output type
	 * @param checking
	 *            whether running
	 */
	public void receiveData(ComplexBuffer cb, boolean checking,
			int screendatalen) {
		// System.out.println("receiveData");
		if (!enable_rule)
			return;

		resetRusult();
		/**
		 * KNOW
		 * 
		 * Here, math and chl are separated because they use intbuffer and bytebuffer respectively and need different handling
		 * 
		 * For now the handling is not complicated, so they are allowed to be saved in different ways
		 */
		IntBuffer ib = cb.ib;
		ByteBuffer bb = cb.bb;
		boolean isByteArray = cb.isByteArray;

		// ByteBuffer.wrap(bytebuf.array(),bytebuf.position(),bytebuf.remaining());trimBuffer(buf);
		if (prepared_rule) {
			if (checking) {
				if (isByteArray) {
					result = checkRulePoints(bb, rule_min, rule_max, data_num);
				} else {
					result = checkRulePoints(ib, rule_min, rule_max, data_num);
				}
				// handleCheckResult(result);
			}
		} else {
			resetRulePoints();
			// DBG.config("inside\n");// Prepare Rule
			if (isByteArray) {
				data_num = createRulePoints(bb, adc_min, adc_max, rule_min,
						rule_max, R_horizontalSet, R_verticalSet);
			} else {
				data_num = createRulePoints(ib, adc_min, adc_max, rule_min,
						rule_max, R_horizontalSet, R_verticalSet);
			}
			prepared_rule = true;
			resetBI = true;
		}
	}

	public boolean isRulePrepared() {
		return prepared_rule;
	}

	private RuleDetail rd;

	public double getHor() {
		return rd.hor;
	}

	public double getVer() {
		return rd.ver;
	}

	public int getRuleChannel() {
		return rd.chl;
	}

	public RuleDetail getRuleDetail() {
		return rd;
	}

	/**
	 * Save the current rule; if already enabled, enable it again
	 * 
	 * @param chl
	 * @param hor
	 * @param ver
	 */
	public void configRule(int chl, double hor, double ver) {
		rd.set(chl, hor, ver);

		if (enable_rule) {
			prepared_rule = false;
			/**
			 * prepared_rule may be set from true back to false again here,
			 * 
			 * and the subsequent receiveData() checks it and calls the array reset method again
			 */
		}
	}

	/**
	 * @return whether enable is on
	 */
	public boolean isRuleEnable() {
		return enable_rule;
	}

	protected void trimBuffer(Buffer buf) {
		int p = buf.position();
		int l = buf.limit();
		int len = l - p;
		double v = (RuleCheckWidthPixels / (double) ScreenPixels * len);
		int length = (int) Math.round(v);

		if (length > 0 && length < len) {
			int half = length >> 1;
			int center = (p + l) >> 1;
			int start = center - half;

			p = start;
			len = length;
			l = p + len;
			buf.position(p);
			buf.limit(l);
		}
	}

	public static final int ScreenEndsOffsetPixels = 20;
	public static final int ScreenPixels = AREA_WIDTH;
	public static final int RuleCheckWidthPixels = ScreenPixels
			- (ScreenEndsOffsetPixels << 1);

	/**
	 * Reset the rule arrays and the adc arrays
	 * 
	 */
	private void resetRulePoints() {
		int w = ScreenPixels;// RuleCheckWidthPixels;
		if (adc_min == null || adc_min.length != w) {
			prepareRulePoints(w);
		}

		prepareADCPoints(w);

		R_horizontalSet = (int) (rd.hor * AREA_WIDTH_BLOCK_PIXES);
		R_verticalSet = (int) (rd.ver * PIXELS_PER_DIV);
	}

	/**
	 * Enable the rule
	 * 
	 */
	public void enableRule() {
		enable_rule = true;
	}

	/**
	 * Disable the rule
	 */
	public void disableRule() {
		enable_rule = false;
		prepared_rule = false;

		adc_min = null;
		adc_max = null;
		rule_max = null;
		rule_min = null;

		rule_bi = null;
	}

	/**
	 * Prepare the point set
	 */
	private void prepareRulePoints(int w) {
		adc_min = new int[w];
		adc_max = new int[w];
		rule_max = new int[w];
		rule_min = new int[w];
	}

	/**
	 * Prepare the ADC point set
	 */
	private void prepareADCPoints(int w) {
		for (int i = 0; i < w; i++) {
			adc_min[i] = 255;// compare against the maximum value; smaller values keep updating to get the minimum
			adc_max[i] = -255;// compare against the minimum value; larger values keep updating to get the maximum
		}
	}

	/**
	 * Determine whether the data conforms to the rule
	 * 
	 * @param buf
	 * @return pass (PASS) / fail (FAIL)(FAIL)
	 */
	private int checkRulePoints(ByteBuffer buf, int[] rule_min, int[] rule_max,
			int data_num) {
		int result;
		int p = buf.position(), l = buf.limit();
		int len = l - p;
		byte[] arr = buf.array();

		int j = 0;
		double g = 0, r = data_num / (double) len;
		int v;// dataNum = data_num

		result = PASS;
		for (; p < l; p++, g = g + r, j = (int) g) {
			// skip the blank areas at the head and tail, checked inside the loop; since there is little data in between, the performance impact is small
			if (j < ScreenEndsOffsetPixels
					|| j > ScreenPixels - ScreenEndsOffsetPixels)
				continue;
			v = arr[p];
			if ((v < rule_min[j] || v > rule_max[j])) {
				result = FAIL;
				break;
			}
		}

		// improper approach: int dataNum = distributeADCPoints(buf, adc_min, adc_max);
		/** determined by the algorithm; generally dataNum == data_num == adc_min.length is assumeddataNum == data_num == adc_min.length */
		return result;
	}

	/**
	 * Determine whether the data conforms to the rule
	 * 
	 * @param buf
	 * @return pass (PASS) / fail (FAIL)(FAIL)
	 */
	private int checkRulePoints(IntBuffer buf, int[] rule_min, int[] rule_max,
			int data_num) {
		int result;
		int p = buf.position(), l = buf.limit();
		int len = l - p;
		int[] arr = buf.array();

		int j = 0;
		double g = 0, r = data_num / (double) len;
		int v;// dataNum = data_num

		result = PASS;
		for (; p < l; p++, g = g + r, j = (int) g) {
			// skip the blank areas at the head and tail, checked inside the loop; since there is little data in between, the performance impact is small
			if (j < ScreenEndsOffsetPixels
					|| j > ScreenPixels - ScreenEndsOffsetPixels)
				continue;
			v = arr[p];
			if ((v < rule_min[j] || v > rule_max[j])) {
				result = FAIL;
				break;
			}
		}

		// improper approach: int dataNum = distributeADCPoints_Math(buf, adc_min, adc_max);
		/** determined by the algorithm; generally dataNum == data_num == adc_min.length is assumeddataNum == data_num == adc_min.length */
		return result;
	}

	private boolean resetBI = false;

	public void adjustView() {
		resetBI = true;
	}

	/**
	 * Draw the rule boundary points using the cache
	 * 
	 * @param g2d
	 * @param pc
	 */
	public void paintRulePoints(Graphics2D g2d, boolean isScreenMode_3,
			Rectangle r, AreaImageHelper gd) {
		boolean b = enable_rule && prepared_rule;
		if (!b)
			return;

		if (rule_bi == null) {
			rule_bi = gd.createARGBScreenBufferedImage();
			resetBI = true;
		}
		if (resetBI) {
			gd.resetARGBBufferImage(rule_bi);
			paintRuleArea(rule_bi.createGraphics(), isScreenMode_3, r,
					rule_min, rule_max, data_num, ScreenEndsOffsetPixels,
					RuleCheckWidthPixels);
			resetBI = false;
		}
		Point sz = gd.getDrawSize();
		int w = sz.x, h = sz.y;
		g2d.drawImage(rule_bi, 0, 0, w, h, 0, 0, w, h, null);
	}

	private BufferedImage rule_bi;

}