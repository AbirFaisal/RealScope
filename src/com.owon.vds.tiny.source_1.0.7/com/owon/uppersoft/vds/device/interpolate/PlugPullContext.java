package com.owon.uppersoft.vds.device.interpolate;

import java.nio.ByteBuffer;

import com.owon.uppersoft.vds.core.aspect.base.Logable;
import com.owon.uppersoft.vds.util.format.SFormatter;

public class PlugPullContext implements Logable {

	private boolean forTrg;

	public PlugPullContext(boolean forTrg, int chlid) {
		this.forTrg = forTrg;
		bufferId = chlid;
	}

	public boolean isForTrg() {
		return forTrg;
	}

	private int bufferId;

	public int getPlugedBuffer_Id() {
		return bufferId;
	}

	/** 未加工 */
	private ByteBuffer raw_adc;

	public ByteBuffer getRaw_adc() {
		return raw_adc;
	}

	public void setRaw_adc(ByteBuffer raw_adc) {
		this.raw_adc = raw_adc;
	}

	/** 插值后 */
	private ByteBuffer pluged_adc;

	public ByteBuffer getPluged_adc() {
		return pluged_adc;
	}

	public void setPluged_adc(ByteBuffer pluged_adc) {
		this.pluged_adc = pluged_adc;
		markRange();
	}

	private void markRange() {
		basePos = pluged_adc.position();
		baseLimit = pluged_adc.limit();
		logln(SFormatter.UIformat("[LowRange]basePos: %d, baseLimit: %d",
				basePos, baseLimit));
	}

	/** 拉触发后 */
	private int skip, demand;
	private int basePos, baseLimit;

	public int getDemand() {
		return demand;
	}

	public int getSkip() {
		return skip;
	}

	public int getBaseLimit() {
		return baseLimit;
	}

	public void arRange() {
		pluged_adc.position(skip);
		pluged_adc.limit(skip + demand);
	}

	public void markDemandRange(int skip, int demand) {
		this.skip = skip;
		this.demand = demand;
		logln(SFormatter.UIformat("[DemandRange]skip: %d, demand: %d", skip,
				demand));
	}

	protected void limitRange() {
		if (skip < basePos) {
			skip = basePos;
		}
		if (skip + demand > baseLimit) {
			skip = baseLimit - demand;
		}
	}

	public void shift(int del) {
		skip += del;
		logln("shift: " + del + ", new skip: " + skip);
		limitRange();
		arRange();
	}

	@Override
	public void log(Object o) {
	}

	@Override
	public void logln(Object o) {
		System.out.println(o);
	}

	public void logInfo() {
		logln(SFormatter.UIformat("[DemandRange]skip: %d, demand: %d", skip,
				demand));
		logln(SFormatter.UIformat("[LowRange]basePos: %d, baseLimit: %d",
				basePos, baseLimit));
	}

}