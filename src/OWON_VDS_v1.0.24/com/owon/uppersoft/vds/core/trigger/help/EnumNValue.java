package com.owon.uppersoft.vds.core.trigger.help;

/**
 * enum+value, dual storage
 * 
 */
public abstract class EnumNValue {

	/** Unit part, also the basis for step judgment; acceleration of stepping could also be added here */
	protected int en;

	/** Value part */
	protected int v;

	public EnumNValue() {
		this(0, 100);
	}

	public EnumNValue(int en, int v) {
		this.en = en;
		this.v = v;
	}

	@Override
	public boolean equals(Object obj) {
		// System.out.println("compare: " + this + ":" + obj);
		if (!(obj instanceof EnumNValue))
			return false;
		EnumNValue that = (EnumNValue) obj;

		return en == that.en && v == that.v;
	}

	/**
	 * @param etv
	 * @return Whether the original value was modified
	 */
	public boolean set(EnumNValue etv) {
		if (this.equals(etv))
			return false;

		en = etv.en;
		v = etv.v;

		return true;
	}

	public EnumNValue set(int en, int v) {
		this.en = en;
		this.v = v;
		return this;
	}

	public int enumPart() {
		return en;
	}

	/**
	 * Return the next available value
	 * 
	 * @return
	 */
	public abstract EnumNValue getNext();

	/**
	 * Return the previous available value
	 * 
	 * @return
	 */
	public abstract EnumNValue getPrevious();

	/**
	 * Convert the contained en and v to an int value; the unit is defined by subclasses
	 * 
	 * @return
	 */
	public abstract int toInt();

	/**
	 * Read the value from an int and set en and v; the unit is defined by subclasses
	 * 
	 * @param v
	 */
	public abstract void fromInt(int v);

	/**
	 * Name of the parameter item in the configuration file
	 * 
	 * @return
	 */
	public abstract String itemName();
}