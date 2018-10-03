package com.owon.uppersoft.dso.function.ref;

/**
 * ReferenceWaveControl，是不同于WaveForm实现的另一种屏幕波形效果
 * 
 */
public class ReferenceFile {
	public static final String RefFileSuffix = ".bin";
	public static final int RefFile_Normal = 0, RefFile_Math = 1;

	public ReferenceFile(int id) {
		this.id = id;
		name = "";
	}

	public int id;
	public boolean use;
	private String name;

	/** 获得与目标相同序号的字母 */
	public Character getLetter() {
		return (char) (id + 97);
	}

	public String getPath() {
		return getLetter() + RefFileSuffix;
	}

	@Override
	public String toString() {
		return getComplexName();// getLetter() + "";//
	}

	public void setName(String n) {
		name = n;
	}

	public String getName() {
		return name;
	}

	public String getComplexName() {
		if (name.equalsIgnoreCase("")) {
			return getLetter() + "";
		}
		return getLetter() + "(" + name + ")";
	}
}
