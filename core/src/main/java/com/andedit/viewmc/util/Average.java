package com.andedit.viewmc.util;

public class Average {

	public final int size;
	
	private final int[] array;
	private int idx, sum;
	
	public Average(int size) {
		this.size = size;
		array = new int[size];
	}
	
	public int value(int num) {
		if (idx >= size) sum -= array[idx%size];
		array[(++idx)%size] = num;
		sum += num;
		return sum / Math.min(idx, size);
	}
}
