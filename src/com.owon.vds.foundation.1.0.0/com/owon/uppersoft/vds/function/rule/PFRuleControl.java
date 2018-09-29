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
 * PFRuleControl 使用规则得到结果，优化规则界限点的绘制；这里占用的资源多，在使能关闭后可以释放
 * 
 */
public class PFRuleControl {
	public static class ComplexBuffer {
		public boolean isByteArray;

		public IntBuffer ib;
		public ByteBuffer bb;
	}

	/** 屏幕宽度 */
	public static final int AREA_WIDTH = 1000;
	/** 水平每格对应的像素个数 */
	public static final int AREA_WIDTH_BLOCK_PIXES = 50;
	/** 垂直每格对应的adc不同值个数 */
	public static final int PIXELS_PER_DIV = 25;

	/***/
	public static final int PASS = 1, FAIL = 0, None = -1;

	private boolean enable_rule = false, prepared_rule = false;
	private int R_verticalSet = 10, R_horizontalSet = 10;

	/**
	 * 该长度是对adc数据进行按1k屏幕像素进行等分取最值之后得到的数组使用范围，
	 * 
	 * 直接影响到adc_max / adc_min, rule_max / rule_min
	 */
	private int data_num;

	private int[] adc_min, adc_max;
	/** rule_max指rule_ceiling, rule_min指rule_floor */
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
	 * 接收数据后，使用一次来生成规则界限点
	 * 
	 * 使用的是一次传输中的最后一帧
	 * 
	 * @param wfm
	 * @param output_rule
	 *            输出类型
	 * @param checking
	 *            是否运行
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
		 * 这里，分离出math和chl的做法是它们用intbuffer和bytebuffer，需要各自不同的处理
		 * 
		 * 暂时来说，处理并不复杂，故而运行它们以不同的方式保存
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
	 * 保存当前规则，如果已经使能则重新使能
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
			 * 这里可能把prepared_rule再次从true设置为false，
			 * 
			 * 而此后的receiveData()中再判断并重新调用重设数组的方法
			 */
		}
	}

	/**
	 * @return 使能是否打开
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
	 * 重设规则数组和adc数组
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
	 * 使能规则
	 * 
	 */
	public void enableRule() {
		enable_rule = true;
	}

	/**
	 * 关闭规则使能
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
	 * 准备点集
	 */
	private void prepareRulePoints(int w) {
		adc_min = new int[w];
		adc_max = new int[w];
		rule_max = new int[w];
		rule_min = new int[w];
	}

	/**
	 * 准备ADC点集
	 */
	private void prepareADCPoints(int w) {
		for (int i = 0; i < w; i++) {
			adc_min[i] = 255;// 用最大值判断，比它小的，不断得到最小值
			adc_max[i] = -255;// 用最小值判断，比它大的，不断得到最大值
		}
	}

	/**
	 * 判断数据是否符合规则
	 * 
	 * @param buf
	 * @return 通过(PASS) / 失败(FAIL)
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
			// 略过头尾空白区，在循环中判断，由于此间数据不多，性能影响不大
			if (j < ScreenEndsOffsetPixels
					|| j > ScreenPixels - ScreenEndsOffsetPixels)
				continue;
			v = arr[p];
			if ((v < rule_min[j] || v > rule_max[j])) {
				result = FAIL;
				break;
			}
		}

		// 不当的方法 int dataNum = distributeADCPoints(buf, adc_min, adc_max);
		/** 由算法决定，一般假定dataNum == data_num == adc_min.length */
		return result;
	}

	/**
	 * 判断数据是否符合规则
	 * 
	 * @param buf
	 * @return 通过(PASS) / 失败(FAIL)
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
			// 略过头尾空白区，在循环中判断，由于此间数据不多，性能影响不大
			if (j < ScreenEndsOffsetPixels
					|| j > ScreenPixels - ScreenEndsOffsetPixels)
				continue;
			v = arr[p];
			if ((v < rule_min[j] || v > rule_max[j])) {
				result = FAIL;
				break;
			}
		}

		// 不当的方法 int dataNum = distributeADCPoints_Math(buf, adc_min, adc_max);
		/** 由算法决定，一般假定dataNum == data_num == adc_min.length */
		return result;
	}

	private boolean resetBI = false;

	public void adjustView() {
		resetBI = true;
	}

	/**
	 * 使用缓存画规则界限点
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