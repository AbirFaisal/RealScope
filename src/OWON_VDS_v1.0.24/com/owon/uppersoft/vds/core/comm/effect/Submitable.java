package com.owon.uppersoft.vds.core.comm.effect;

import com.owon.uppersoft.vds.core.comm.job.JobUnit;

/**
 * Submitable, some of whose methods cannot implement command control from the given parameters alone; this is because different models need a different range of information for command control,
 * 
 * The solution is for the implementer to find these parameters itself
 * 
 * 
 * 
 * Commands are sent in the execution order of JobQueue, so a setting only represents adding a task; the task is executed automatically when idle.
 * 
 * A post-completion action can be registered, called after the added task completes, so that the effect on the waveform after the command takes effect is ordered
 * 
 * Optimization could be suggested: when an added task is set multiple times, only execute the last one, thereby optimizing the repeated triggering from mouse dragging and wheel operations
 * 
 */
public interface Submitable extends JobUnitDealer {
	void prepare();

	/**
	 * For Tiny, calling the corresponding setting api sends immediately for now; apply does nothing
	 */
	void apply();

	/**
	 * @param pf
	 *            1 = pass, 0 = fail
	 */
	void sync_pf(int pf);

	void sendRun();

	void sendStopThen(final Runnable r);

	void apply_trgThen(Runnable r);

	void apply_trgSweep(int sweepIndex);

	void apply_trg();

	/**
	 * Optimized command sending level
	 * 
	 * @param item
	 * @param arr
	 */
	void c_trg_edge(int mode, int chl, final byte item, final int... arr);

	/**
	 * Optimized command sending threshold
	 * 
	 * @param item
	 * @param arr
	 */
	void c_trg_slope(int mode, int chl, final byte item, final int... arr);

	void c_trg_video(int mode, int chl, final byte item, final int... arr);

	/**
	 * Optimized command sending level
	 * 
	 * @param item
	 * @param arr
	 */
	void c_trg_pulse(int mode, int chl, final byte item, final int... arr);

	/**
	 * Optimized command sending
	 * 
	 * @param tb
	 * @param htp
	 */
	void c_tb_htp(final int tb, final int htp);

	/**
	 * Optimized command sending
	 * 
	 * @param htp
	 */
	void c_htp(final int htp);

	/**
	 * Switching fft involves channel switches, divider coefficient adjustment, and acquisition depth adjustment, usually done via syncDetail; here only specific settings are made
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
	 * Optimized command sending
	 * 
	 * @param deepIdx
	 */
	void c_dm(final int deepIdx);

	void c_sample(final int modelIdx);

	/**
	 * Optimized command sending, as far as possible for setting voltage scale, zero position or switching channels, and execution of the task once sending completes
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