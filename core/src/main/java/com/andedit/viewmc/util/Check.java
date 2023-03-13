package com.andedit.viewmc.util;

class Check {
	final int haft, size;
	final boolean check[][];
	
	public Check(int size) {
		this.haft = size / 2;
		this.size = size;
		this.check = new boolean[size][size];
	}
	
	public boolean isVaild(int x, int z) {
		x += haft;
		z += haft;
		if (x < 0 || z < 0 || x >= size || z >= size) {
			return false;
		}
		return !check[x][z];
	}
	
	public PointNode invaildate(PointNode point) {
		invaildate(point.x(), point.z());
		return point;
	}
	
	public void invaildate(int x, int z) {
		check[x+haft][z+haft] = true;
	}
}
