package com.andedit.viewermc.util;

public record PointNode(int x, int z) {

	public PointNode offset(int x, int z) {
		return new PointNode(this.x+x, this.z+z);
	}
	
	public boolean pass(int xPos, int zPos, int radius) {
		return x <= (-radius)+xPos|| z <= (-radius)+zPos || x > radius+xPos || z > radius+zPos;
	}
}
