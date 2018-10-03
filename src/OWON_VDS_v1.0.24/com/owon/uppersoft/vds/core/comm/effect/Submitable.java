package com.owon.uppersoft.vds.core.comm.effect;

import com.owon.uppersoft.vds.core.comm.job.JobUnit;

/**
 * Submitable，其中一些方法无法仅通过给定的参数实现指令控制，这是由机型不同导致的指令控制所需的信息的范围不同导致的，
 * 
 * 解决的方法是由实现方自己去寻找这些参数
 * 
 * 
 * 
 * 指令的发送是按照JobQueue的执行顺序依次进行的，所以设置仅代表的添加任务，任务会自动在空闲的时候得到执行。
 * 
 * 可注册完成后动作，在添加的任务完成后被调用，从而有序化波形在指令生效后的效果
 * 
 * 可建议优化，在添加的任务被多次设置后仅执行最后一次，从而优化鼠标拖拽和滚轮操作的多次触发操作
 * 
 */
public interface Submitable extends JobUnitDealer {
	void prepare();

	/**
	 * 对Tiny暂时是调用对应的设置api就会发送，apply无动作
	 */
	void apply();

	/**
	 * @param pf
	 *            1通过，0失败
	 */
	void sync_pf(int pf);

	void sendRun();

	void sendStopThen(final Runnable r);

	void apply_trgThen(Runnable r);

	void apply_trgSweep(int sweepIndex);

	void apply_trg();

	/**
	 * 进行优化指令发送 level
	 * 
	 * @param item
	 * @param arr
	 */
	void c_trg_edge(int mode, int chl, final byte item, final int... arr);

	/**
	 * 进行优化指令发送 阈值
	 * 
	 * @param item
	 * @param arr
	 */
	void c_trg_slope(int mode, int chl, final byte item, final int... arr);

	void c_trg_video(int mode, int chl, final byte item, final int... arr);

	/**
	 * 进行优化指令发送 level
	 * 
	 * @param item
	 * @param arr
	 */
	void c_trg_pulse(int mode, int chl, final byte item, final int... arr);

	/**
	 * 进行优化指令发送
	 * 
	 * @param tb
	 * @param htp
	 */
	void c_tb_htp(final int tb, final int htp);

	/**
	 * 进行优化指令发送
	 * 
	 * @param htp
	 */
	void c_htp(final int htp);

	/**
	 * 切换fft的过程设计通道开关，分频系数调整，采集深度调整，通常借助syncDetail完成，这里只是做特定设置
	 * 
	 * @param ffton
	 * @param fftchl
	 */
	void c_fft(final boolean ffton, final int fftchl);

	void c_network_off();

	void c_network(final boolean netOn, final byte[] ipaddress, final int port,
			final byte[] gwaddress, final byte[] smaddress,
			final byte[] macaddress);

	void c_sync_output(final int sync_output);

	/**
	 * 进行优化指令发送
	 * 
	 * @param deepIdx
	 */
	void c_dm(final int deepIdx);

	void c_sample(final int modelIdx);

	/**
	 * 尽量针对设置电压档位、零点位置或是开关通道的情况，进行优化指令发送，及发送完毕的任务执行
	 * 
	 * @param mode
	 * @param chl
	 * @param arr
	 */
	void c_chl(final P_Channel mode, final int chl, final int... arr);

	void forceTrg();

	void applyThen(Runnable run);

	void recommendOptimize();

	void d_chl_vb(int chl, int vbIndex, Runnable r);

	void d_chl_pos0(int chl, int pos0, Runnable r);

}