package com.owon.uppersoft.dso.function.ref;

/**
 * ReferenceWaveControl, another kind of on-screen waveform effect different from the WaveForm implementation
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

	/** Get the letter with the same index as the target */
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
