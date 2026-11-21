package com.owon.uppersoft.vds.source.front;

import com.owon.uppersoft.vds.core.aspect.base.Logable;

public class LocateTrgUtil implements Logable {
	/***************************************************************************
	 * Function name:trigpull
	 * 
	 * Description: traverse to find the nearest trigger position.
	 * 
	 * **********************************
	 * 
	 * Input::
	 * 
	 * trigbuf : the trigger data BUFFER;
	 * 
	 * trigedge: 0--rising edge, 1--falling edge;
	 * 
	 * trigvalue: trigger value;
	 * 
	 * midpos : start position of the traversal;
	 * 
	 * maxright: right limit position of the traversal;
	 * 
	 * maxleft: left limit position of the traversal;
	 * 
	 * 
	 * Output: return value: a positive value means points are added backward, a negative value means points are subtracted forward
	 * 
	 * **********************************
	 * 
	 * Author: Ge Shuihuan, 04 May 2012
	 * 
	 **************************************************************************/

	private static int RS_Finding = 0, RS_Found = 1, RS_End = 2;
	private static int LS_Finding = 0, LS_Found = 1, LS_End = 2;
	public static int Step = 4;

	public final int trigpull(byte[] trigbuf, final int trigedge,
			final int trigvalue, final int midpos, final int maxright,
			final int maxleft) {
		int right_search = RS_Finding;
		// Right-side traversal flag::
		// 0--search for rising/falling edge, 1--edge found and searching for trigger position, 2--reached the right limit position.
		int left_search = LS_Finding;
		// Left-side traversal flag::
		// 0--search for rising/falling edge, 1--edge found and searching for trigger position, 2--reached the left limit position.

		int cnt_R = 0; // right-side traversal count
		int cnt_L = 0; // left-side traversal count
		int pullvalue; // trigger locating value

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
