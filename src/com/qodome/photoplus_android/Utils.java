package com.qodome.photoplus_android;

public class Utils {

	public static int[] getIntArray(int size) {
		return new int[size];
	}

	public static int getMaskedValue(int i, int mask) {
		return i & mask;
	}
}