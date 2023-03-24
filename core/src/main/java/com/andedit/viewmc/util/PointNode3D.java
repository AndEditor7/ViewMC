package com.andedit.viewmc.util;

public record PointNode3D(int x, int y, int z) {

	public PointNode3D offset(int x, int y, int z) {
		return new PointNode3D(this.x+x, this.y+y, this.z+z);
	}
	
	public boolean pass(int xPos, int yPos, int zPos, int radius) {
		return x <= (-radius)+xPos || y <= (-radius)+yPos || z <= (-radius)+zPos || x > radius+xPos || y > radius+yPos || z > radius+zPos;
	}
}
