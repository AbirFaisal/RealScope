package com.owon.uppersoft.vds.socket;

public class NB {
	public static void main(String[] args) {
		String str1="1122.2.2";
		String str2="111";
		String str3="43211.2";
		String str4="111s";
		String str5="111.s";
		String str6="1s11";
		System.out.println(str1+":"+isNum(str1));
		System.out.println(str2+":"+isNum(str2));
		System.out.println(str3+":"+isNum(str3));
		System.out.println(str4+":"+isNum(str4));
		System.out.println(str5+":"+isNum(str5));
		System.out.println(str6+":"+isNum(str6));
	}
	public static boolean isNum(String str){
		return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	}
}