package com.owon.uppersoft.vds.source.front;

import com.owon.uppersoft.vds.core.aspect.base.Logable;

public class LocateTrgUtil implements Logable {
	/***************************************************************************
	 * 函数名:trigpull
	 * 
	 * 功能描述: 遍历查找最近的触发位置。
	 * 
	 * **********************************
	 * 
	 * 输入:
	 * 
	 * trigbuf : 触发数据的BUFFER;
	 * 
	 * trigedge: 0--上升沿，1--下降沿;
	 * 
	 * trigvalue: 触发值;
	 * 
	 * midpos : 开始遍历的位置；
	 * 
	 * maxright: 遍历的右侧极限位置；
	 * 
	 * maxleft: 遍历的左侧极限位置；
	 * 
	 * 
	 * 输出: 返回值: 正值代表要往后加点，负值代表要往前减点
	 * 
	 * **********************************
	 * 
	 * 作者:葛水焕 2012年5月04日
	 * 
	 **************************************************************************/

	private static int RS_Finding = 0, RS_Found = 1, RS_End = 2;
	private static int LS_Finding = 0, LS_Found = 1, LS_End = 2;
	public static int Step = 4;

	public final int trigpull(byte[] trigbuf, final int trigedge,
			final int trigvalue, final int midpos, final int maxright,
			final int maxleft) {
		int right_search = RS_Finding;
		// 右侧遍历标志:
		// 0--查找上升/下降沿，1--已找到沿并查找触发位置，2--遍历到右侧极限位置。
		int left_search = LS_Finding;
		// 左侧遍历标志:
		// 0--查找上升/下降沿，1--已找到沿并查找触发位置，2--遍历到左侧极限位置。

		int cnt_R = 0; // 右侧遍历计数
		int cnt_L = 0; // 左侧遍历计数
		int pullvalue; // 拉触发值

		if (trigedge == 0) {
			while (true) {
				if (right_search == RS_Finding) {
					if ((trigbuf[cnt_R + midpos] < trigbuf[cnt_R + midpos
							+ Step])
							&& (trigbuf[cnt_R + midpos] <= trigvalue)) {
						right_search = RS_Found;
					} else {
						cnt_R++;
						if (cnt_R >= maxright) {
							right_search = RS_End;
						}
					}
				}
				if (right_search == RS_Found) {
					if (trigbuf[cnt_R + midpos] < trigvalue) {
						if (trigbuf[cnt_R + midpos] > trigbuf[cnt_R + midpos
								+ Step]) {
							right_search = RS_Finding;
						} else {
							cnt_R++;
							if (cnt_R >= maxright) {
								right_search = RS_End;
							}
						}
					} else {
						// Uart_Printf("%s(%d): find the right[%d]=
						// %d\n",__MODULE__,__LINE__, cnt_R, trigbuf[cnt_R +
						// midpos]);
						pullvalue = cnt_R;
						break;
					}
				}

				if (left_search == LS_Finding) {
					if ((trigbuf[cnt_L + midpos - Step] < trigbuf[cnt_L
							+ midpos])
							&& (trigbuf[cnt_L + midpos] >= trigvalue)) {
						left_search = LS_Found;
					} else {
						// logln("cnt_L" + cnt_L);
						cnt_L--;
						if (cnt_L <= -maxleft) {
							left_search = LS_End;
						}
					}
				}
				if (left_search == LS_Found) {
					if (trigbuf[cnt_L + midpos] > trigvalue) {
						if (trigbuf[cnt_L + midpos - Step] > trigbuf[cnt_L
								+ midpos]) {
							left_search = LS_Finding;
						} else {
							cnt_L--;
							if (cnt_L <= -maxleft) {
								left_search = LS_End;
							}
						}
					} else {
						// Uart_Printf("%s(%d): find the left[%d]=
						// %d\n",__MODULE__,__LINE__, cnt_L, trigbuf[cnt_L +
						// midpos]);
						pullvalue = cnt_L;
						break;
					}
				}
				if ((right_search == RS_End) && (left_search == LS_End)) {
					// Uart_Printf("%s(%d): not found--end of max
					// pos\n",__MODULE__,__LINE__);
					pullvalue = 0;
					break;
				}
			}
		} else {
			while (true) {
				if (right_search == RS_Finding) {
					if ((trigbuf[cnt_R + midpos] > trigbuf[cnt_R + midpos
							+ Step])
							& (trigbuf[cnt_R + midpos] >= trigvalue)) {
						right_search = RS_Found;
					} else {
						cnt_R++;
						if (cnt_R >= maxright) {
							right_search = RS_End;
						}
					}
				}
				if (right_search == RS_Found) {
					if (trigbuf[cnt_R + midpos] > trigvalue) {
						if (trigbuf[cnt_R + midpos] < trigbuf[cnt_R + midpos
								+ Step]) {
							right_search = RS_Finding;
						} else {
							cnt_R++;
							if (cnt_R >= maxright) {
								right_search = RS_End;
							}
						}
					} else {
						// Uart_Printf("%s(%d): find the right[%d]=
						// %d\n",__MODULE__,__LINE__, cnt_R, trigbuf[cnt_R +
						// midpos]);
						pullvalue = cnt_R;
						break;
					}
				}

				if (left_search == LS_Finding) {
					if ((trigbuf[cnt_L + midpos - Step] > trigbuf[cnt_L
							+ midpos])
							& (trigbuf[cnt_L + midpos] <= trigvalue)) {
						left_search = LS_Found;
					} else {
						cnt_L--;
						if (cnt_L <= -maxleft) {
							left_search = LS_End;
						}
					}
				}
				if (left_search == LS_Found) {
					if (trigbuf[cnt_L + midpos] < trigvalue) {
						if (trigbuf[cnt_L + midpos] > trigbuf[cnt_L + midpos
								- Step]) {
							left_search = LS_Finding;
						} else {
							cnt_L--;
							if (cnt_L <= -maxleft) {
								left_search = LS_End;
							}
						}
					} else {
						// Uart_Printf("%s(%d): find the left[%d] =
						// %d\n",__MODULE__,__LINE__, cnt_L, trigbuf[cnt_L +
						// midpos]);
						pullvalue = cnt_L;
						break;
					}
				}
				if ((right_search == RS_End) && (left_search == LS_End)) {
					// Uart_Printf("%s(%d): not found - end of max
					// pos\n",__MODULE__,__LINE__);
					pullvalue = 0;
					break;
				}
			}
		}

		return pullvalue;
	}

	@Override
	public void log(Object o) {
		System.out.print(o);
	}

	@Override
	public void logln(Object o) {
		System.out.println(o);
	}
}
