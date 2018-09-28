package com.owon.uppersoft.vds.util.format;

import java.math.BigDecimal;


/**
 * UnitConversionUtil，单位换算器，精确度改为3位，因为递乘/除的位数是3位
 * 
 */
/**
 * @author Administrator
 * 
 */
public class UnitConversionUtil {

	/**
	 * 接收微秒值并自行决定显示方式
	 * 
	 * @param value
	 * @return
	 */
	public static final String getTextFor_uS(double value) {
		double v = value;
		if (Double.isNaN(v) || Double.isInfinite(v))
			return "?";
		if (v <= 0.001 && v >= -0.001) {
			v = v * 1000;
			return SFormatter.UIformat("%.3f ns", v);
		}
		if (v <= 1000 && v >= -1000) {
			return SFormatter.UIformat("%.3f us", v);
		}

		v /= 1000;
		if (v <= 1000 && v >= -1000) {
			return SFormatter.UIformat("%.3f ms", v);
		}

		return SFormatter.UIformat("%.3f s", v /= 1000);
	}

	/**
	 * 1. 时基档位接收毫秒值并自行决定显示方式
	 * 
	 * @param value
	 * @return
	 */
	public static final String getTimebaseLabel_mS(double value) {
		if (value == 0)
			return ("0.0 ns");
		if (Double.isNaN(value) || Double.isInfinite(value))
			return "?";
		if (value >= 1000 || value <= -1000) {
			return SFormatter.UIformat("%.3f s", value / 1000);
		}

		if (value >= 1 || value <= -1 || value == 0) {
			return SFormatter.UIformat("%.3f ms", value);
		}

		double v = value;
		v *= 1000;
		if (v >= 1 || v <= -1) {
			return SFormatter.UIformat("%.3f us", v);
		}

		return SFormatter.UIformat("%.3f ns", v * 1000);
	}

	/**
	 * 2. 时基档位接收毫秒值并自行决定显示方式
	 * 
	 * @param value
	 * @return
	 */
	public static final String getSimplifiedTimebaseLabel_mS(double value) {
		if (value == 0)
			return ("0.0 ns");
		if (Double.isNaN(value) || Double.isInfinite(value))
			return "?";

		if (value >= 1000 || value <= -1000) {
			if (value >= 10000 || value <= -10000) {
				if (value >= 100000 || value <= -100000)
					return SFormatter.UIformat("%.1f s", value / 1000);
				return SFormatter.UIformat("%.2f s", value / 1000);
			}
			return SFormatter.UIformat("%.3f s", value / 1000);
		}

		if (value >= 1 || value <= -1) {
			if (value >= 10 || value <= -10) {
				if (value >= 100 || value <= -100)
					return SFormatter.UIformat("%.1f ms", value);
				return SFormatter.UIformat("%.2f ms", value);
			}
			return SFormatter.UIformat("%.3f ms", value);
		}

		double v = value;
		v *= 1000;
		if (v >= 1 || v <= -1) {
			if (v >= 10 || v <= -10) {
				if (v >= 100 || v <= -100)
					return SFormatter.UIformat("%.1f us", v);
				return SFormatter.UIformat("%.2f us", v);
			}
			return SFormatter.UIformat("%.3f us", v);
		}

		return SFormatter.UIformat("%.2f ns", v * 1000);
	}

	/**
	 * 1. 电压档位接收毫伏值并自行决定显示方式
	 * 
	 * @param value
	 * @return
	 */
	public static final String getVoltageLabel_mV(double value) {
		if (Double.isNaN(value) || Double.isInfinite(value))
			return "?";
		if (value >= 1000 || value <= -1000) {
			return SFormatter.UIformat("%.3f V", value / 1000);
		}
		return SFormatter.UIformat("%.3f mV", value);
	}

	/**
	 * 2. 电压档位接收毫伏值并自行决定显示方式
	 * 
	 * @param value
	 * @return
	 */
	public static final String getSimplifiedVoltLabel_mV(double value) {
		// if(true)return SFormatter.UIformat("%.0f", value);

		if (Double.isNaN(value) || Double.isInfinite(value))
			return "?";
		if (value >= 1000000 || value <= -1000000) {
			if (value >= 10000000 || value <= -10000000) {
				if (value >= 100000000 || value <= -100000000)
					return SFormatter.UIformat("%.1f kV", value / 1000000);
				return SFormatter.UIformat("%.2f kV", value / 1000000);
			}
			return SFormatter.UIformat("%.3f kV", value / 1000000);
		}

		if (value >= 1000 || value <= -1000) {
			if (value >= 10000 || value <= -10000) {
				if (value >= 100000 || value <= -100000)
					return SFormatter.UIformat("%.1f V", value / 1000);
				return SFormatter.UIformat("%.2f V", value / 1000);
			}
			return SFormatter.UIformat("%.3f V", value / 1000);
		}
		if (value >= 1 || value <= -1) {
			if (value >= 10 || value <= -10) {
				if (value >= 100 || value <= -100)
					return SFormatter.UIformat("%.1f mV", value);
				return SFormatter.UIformat("%.2f mV", value);
			}
		}
		return SFormatter.UIformat("%.3f mV", value);
	}

	/**
	 * 3. 电压档位接收mV的数值并自行决定显示方式
	 * 
	 * @param value
	 * @return
	 */
	public static final String getIntVoltageLabel_mV(int value) {
		if (value >= 1000 || value <= -1000) {
			return SFormatter.UIformat("%d V", value / 1000);
		}
		return SFormatter.UIformat("%d mV", value);
	}

	/**
	 * 3. 电压档位接收mV的数值并自行决定显示方式，单位和数值间无空格
	 * 
	 * @param value
	 * @return
	 */
	public static final String getIntVoltageLabel_xmV(int value) {
		if (value >= 1000 || value <= -1000) {
			return SFormatter.UIformat("%dV", value / 1000);
		}
		return SFormatter.UIformat("%dmV", value);
	}

	/**
	 * 4.下位机代码 电压档位接收毫伏值并自行决定显示方式
	 * 
	 * @param value
	 * @return
	 */
	private static final String getVoltageLabel_mV2(double value) {
		if (Double.isNaN(value) || Double.isInfinite(value))
			return "?";
		boolean isPositive = true;
		String str;
		if (value < 0) {
			isPositive = false;
			value = 0 - value;
		}
		if (value >= 1000000) {
			str = SFormatter.UIformat("%.2fkV", value / 1000000);
		} else if (value >= 1000) {
			if (value >= 100000) {
				str = SFormatter.UIformat("%.1fV", value / 1000);
			} else if (value >= 10000) {
				str = SFormatter.UIformat("%.2fV", value / 1000);
			} else {
				str = SFormatter.UIformat("%.3fV", value / 1000);
			}
		} else {
			if (value >= 100) {
				str = SFormatter.UIformat("%.1fmV", value);
			} else if (value >= 10) {
				str = SFormatter.UIformat("%.2fmV", value);
			} else {
				str = SFormatter.UIformat("%.3fmV", value);
			}
		}
		if (isPositive)
			return str;
		else
			return '-' + str;
	}

	/**
	 * 电压档位接收伏值并自行决定显示方式
	 * 
	 * @param value
	 * @return
	 */
	public static final String getVoltageLabel_V(double value) {
		if (Double.isNaN(value) || Double.isInfinite(value))
			return "?";
		if (value >= 1 || value <= -1) {
			return SFormatter.UIformat("%.3f V", value);
		}
		return SFormatter.UIformat("%.3f mV", value * 1000);
	}

	public static final String FrequencyLabel_Hz_BTW_Question = "?";
	public static final String FrequencyLabel_Hz_BTW_Empty = "";

	/**
	 * 1. 接收频率并自行决定显示方式
	 * 
	 * @param value
	 * @return
	 */
	public static final String getFrequencyLabel_Hz_withRestrict(double value,
			double restrict) {
		if (Double.isNaN(value) || Double.isInfinite(value))
			return "?";
		double v = value;
		double rstr = restrict;
		String str;
		if (v > -1000 && v < 1000) {
			str = SFormatter.UIformat("%.3f Hz", v);
			str = distrustCheck(v, rstr, str);
			return str;
		}
		v /= 1000;
		rstr /= 1000;

		if (v > -1000 && v < 1000) {
			str = SFormatter.UIformat("%.3f kHz", v);
			str = distrustCheck(v, rstr, str);
			return str;
		}
		v /= 1000;
		rstr /= 1000;

		if (v > -1000 && v < 1000) {
			str = SFormatter.UIformat("%.3f MHz", v);
			str = distrustCheck(v, rstr, str);
			return str;
		}
		v /= 1000;
		rstr /= 1000;

		str = SFormatter.UIformat("%.3f GHz", v);
		str = distrustCheck(v, rstr, str);
		return str;
	}

	private static String distrustCheck(double v, double restrict, String str) {
		/** 各自精简到三位小数,具体频率如果保留到3位仍超出最大值才加问号。过滤后做比较，增强容错性 */
		v = BigDecimal.valueOf(v).setScale(3, BigDecimal.ROUND_HALF_UP)
				.doubleValue();
		restrict = BigDecimal.valueOf(restrict)
				.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
		/** 判断是否超限 */
		if (v > restrict)
			str += FrequencyLabel_Hz_BTW_Question;
		return str;
	}

	/**
	 * 2. 接收频率并自行决定显示方式
	 * 
	 * @param value
	 * @return
	 */
	public static final String getSimplifiedFrequencyLabel_Hz(double value) {
		if (Double.isNaN(value) || Double.isInfinite(value))
			return "?";
		double v = BigDecimal.valueOf(value)
				.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
		if (v > -1000 && v < 1000) {
			if (v > -100 && v < 100) {
				if (v > -10 && v < 10)
					return SFormatter.UIformat("%.3f Hz", v);
				return SFormatter.UIformat("%.2f Hz", v);
			}
			return SFormatter.UIformat("%.1f Hz", v);
		}
		v /= 1000;
		if (v > -1000 && v < 1000) {
			if (v > -100 && v < 100) {
				if (v > -10 && v < 10)
					return SFormatter.UIformat("%.3f kHz", v);
				return SFormatter.UIformat("%.2f kHz", v);
			}
			return SFormatter.UIformat("%.1f kHz", v);
		}
		v /= 1000;
		if (v > -1000 && v < 1000) {
			if (v > -100 && v < 100) {
				if (v > -10 && v < 10)
					return SFormatter.UIformat("%.3f MHz", v);
				return SFormatter.UIformat("%.2f MHz", v);
			}
			return SFormatter.UIformat("%.1f MHz", v);
		}
		v /= 1000;
		return SFormatter.UIformat("%.3f GHz", v);
	}

	public static final String getSimplifiedFrequencyLabel_Hz2(double value) {
		if (Double.isNaN(value) || Double.isInfinite(value))
			return "?";
		/** value四舍五入5位给v,为了防止v=999.99999Hz被格式化成1000.0Hz返回 */
		double v = BigDecimal.valueOf(value)
				.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
		if (v < 1000) {
			if (v < 100) {
				if (v < 10)
					return SFormatter.UIformat("%.3f Hz", v);
				return SFormatter.UIformat("%.2f Hz", v);
			}
			return SFormatter.UIformat("%.1f Hz", v);
		}
		v /= 1000;
		if (v < 1000) {
			if (v < 100) {
				if (v < 10)
					return SFormatter.UIformat("%.3f kHz", v);
				return SFormatter.UIformat("%.2f kHz", v);
			}
			return SFormatter.UIformat("%.1f kHz", v);
		}
		v /= 1000;
		if (v < 1000) {
			if (v < 100) {
				if (v < 10)
					return SFormatter.UIformat("%.3f MHz", v);
				return SFormatter.UIformat("%.2f MHz", v);
			}
			return SFormatter.UIformat("%.1f MHz", v);
		}
		v /= 1000;
		return SFormatter.UIformat("%.3f GHz", v);
	}

	public static final String getBytesNumber(int number) {
		int oneK = 1 << 10;
		if (number < oneK) {
			return number + "B";
		} else {
			return (number >> 10) + "kB";
		}
	}

	public static final String getPercent(int v, int l) {
		if (v >= l) {
			return "100%";
		}
		return SFormatter.UIformat("%d%%", v * 100 / l);
	}

	public static final String getPercent(double value) {
		if (Double.isNaN(value) || Double.isInfinite(value))
			return "?";
		if (value < 0)
			return "?";
		if (value == 0)
			return SFormatter.UIformat("%.0f%%", value);
		return SFormatter.UIformat("%.2f%%", value * 100);
	}

	public static final String getSpeed(int v, long t) {
		if (v <= 0 || t <= 0) {
			return "-%";
		}
		return SFormatter.UIformat("%.2f kB/s", v / (double) t);
	}

	public static void main(String[] args) {
		double i = 1000.000;
		System.out.println(i <= 1000);
	}
}
