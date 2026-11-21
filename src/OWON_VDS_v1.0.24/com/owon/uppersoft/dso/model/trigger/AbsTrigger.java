package com.owon.uppersoft.dso.model.trigger;

import java.awt.Graphics2D;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import com.owon.uppersoft.dso.control.TrgLevelCheckHandler;
import com.owon.uppersoft.dso.model.trigger.helper.ETV_Holdoff;
import com.owon.uppersoft.dso.model.trigger.helper.PaintChannelTrgLabelContext;
import com.owon.uppersoft.dso.model.trigger.holdoff.HoldoffDelegate;
import com.owon.uppersoft.vds.core.comm.effect.Submitable;
import com.owon.uppersoft.vds.core.trigger.help.EnumNValue;
import com.owon.uppersoft.vds.util.Pref;
import com.owon.uppersoft.vds.util.format.EndianUtil;

public abstract class AbsTrigger {
	public static final BigInteger bi100 = BigInteger.valueOf(100);
	public static final BigInteger bi30 = BigInteger.valueOf(30);
	public static final BigInteger bi10 = BigInteger.valueOf(10);

	public final TrgTypeDefine type;

	public ETV_Holdoff etvho = HoldoffDelegate.createETV();

	public AbsTrigger(TrgTypeDefine idx) {
		type = idx;
	}

	public String getName() {
		return type.name();
	}

	/** What is saved to disk is in units of 100ns */
	protected void loadETV(String prefix, Pref p, EnumNValue etv) {
		etv.fromInt(p.loadInt(prefix + getName() + "." + etv.itemName()));
	}

	protected void persistETV(String prefix, Pref p, EnumNValue etv) {
		p.persistInt(prefix + getName() + "." + etv.itemName(), etv.toInt());
	}

	/** The value used to communicate with the device is in units of 100ns */
	protected void addETV(ByteBuffer bbuf, EnumNValue etv) {
		int v = etv.toInt();
		bbuf.putInt(v);
		dbg(etv.itemName() + ":" + v);
	}

	public int applyETV(byte[] arr, int p, EnumNValue etv) {
		int v = EndianUtil.nextIntB(arr, p);
		etv.fromInt(v);
		dbg(etv.itemName() + ":" + etv);
		return p + 4;
	}

	public int parseETV(byte[] arr, int p, EnumNValue etv) {
		int ho = EndianUtil.nextIntB(arr, p);
		etv.fromInt(ho);
		dbg(etv.toString());
		return p + 4;
	}

	private void dbg(String string) {
		System.out.println(string);
	}

	@Override
	public String toString() {
		return getName();
	}

	public void load(String prefix, Pref p) {
		loadETV(prefix, p, etvho);
		loadProperties(prefix, p);
	}

	public void persist(String prefix, Pref p) {
		persistETV(prefix, p, etvho);
		persistProperties(prefix, p);
	}

	protected abstract void loadProperties(String prefix, Pref p);

	protected abstract void persistProperties(String prefix, Pref p);

	public abstract String getIconKey();

	public abstract String getLabelText(boolean inverse, int voltbase, int pos0);

	public abstract void c_setSweep(int sweep, TriggerControl tc);

	public abstract int getSweep();

	public abstract void nextStatus();

	public abstract void paintIcon(Graphics2D g2d);

	/**
	 * Increment or decrement the specified parameter. This is an active change and must be based on valid values, so invalid internal values can be forcibly converted, such as an out-of-range trigger level and threshold upper/lower limits
	 * 
	 * @param del
	 *            Send a command to change only if non-zero
	 * @param type
	 * @return class name of the trigger event
	 */
	public abstract boolean handelIncr(int del, TrgCheckType type);

	public abstract boolean doCheckTrgLevel(TrgLevelCheckHandler rsg);

	/**
	 * Draw the trigger level marker
	 * 
	 */
	public abstract void paintChannelTrgLabel(PaintChannelTrgLabelContext pctlc);

	public abstract void submitHoldOff(int mode, int chl, Submitable sbm);

}
