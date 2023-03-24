package com.andedit.viewmc.util;

public record PointNode2D(int x, int z) {

	public PointNode2D offset(int x, int z) {
		return new PointNode2D(this.x+x, this.z+z);
	}
	
	public boolean pass(int xPos, int zPos, int radius) {
		return x <= (-radius)+xPos|| z <= (-radius)+zPos || x > radius+xPos || z > radius+zPos;
	}
}
