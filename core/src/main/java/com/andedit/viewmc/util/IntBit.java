package com.andedit.viewmc.util;

public class IntBit {
	
	public static int set(int bits, int index, boolean bool) {
		return bool ? set(bits, index) : clear(bits, index);
	}
	
	public static int set(int bits, int index) {
		return bits | (1 << index);
	}
	
	public static int clear(int bits, int index) {
		return bits & ~(1 << index);
	}
	
	public static boolean get(int bits, int index) {
		return ((bits >>> index) & 1) == 1;
	}
}
