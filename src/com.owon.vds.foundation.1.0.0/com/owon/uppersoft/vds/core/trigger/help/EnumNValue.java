package com.owon.uppersoft.vds.core.trigger.help;

/**
 * enum+value，双保存
 * 
 */
public abstract class EnumNValue {

	/** 单位部分，同时是步进判断的依据，当然这里还可以加速步进 */
	protected int en;

	/** 值部分 */
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
	 * @return 是否修改了原值
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
	 * 返回下一个可用值
	 * 
	 * @return
	 */
	public abstract EnumNValue getNext();

	/**
	 * 返回上一个可用值
	 * 
	 * @return
	 */
	public abstract EnumNValue getPrevious();

	/**
	 * 由所含的内容en和v，转换为int值，单位由子类定义
	 * 
	 * @return
	 */
	public abstract int toInt();

	/**
	 * 从int中读取值设置en和v，单位由子类定义
	 * 
	 * @param v
	 */
	public abstract void fromInt(int v);

	/**
	 * 配置文件中的参数项名字
	 * 
	 * @return
	 */
	public abstract String itemName();
}