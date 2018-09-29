package com.owon.uppersoft.vds.core.tune;

public interface ICal {
	int getId();

	Cending cending();

	int[][] getArgs();

	String getType();
	
	// double getK();
}