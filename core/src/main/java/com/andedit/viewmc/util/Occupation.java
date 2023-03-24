package com.andedit.viewmc.util;

class Occupation {
	final int haft, size;
	final boolean check[][];
	
	public Occupation(int size) {
		this.haft = size / 2;
		this.size = size;
		this.check = new boolean[size][size];
	}
	
	public boolean isAvailable(int x, int z) {
		x += haft;
		z += haft;
		if (x < 0 || z < 0 || x >= size || z >= size) {
			return false;
		}
		return !check[x][z];
	}
	
	public PointNode2D occupy(PointNode2D point) {
		occupy(point.x(), point.z());
		return point;
	}
	
	public void occupy(int x, int z) {
		check[x+haft][z+haft] = true;
	}
}
