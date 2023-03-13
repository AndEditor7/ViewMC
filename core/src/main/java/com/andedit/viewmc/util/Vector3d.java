package com.andedit.viewmc.util;

import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector3;

public class Vector3d {
	public double x, y, z;

	public void set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public int floorX() {
		return Util.floor(x);
	}
	
	public int floorY() {
		return Util.floor(y);
	}
	
	public int floorZ() {
		return Util.floor(z);
	}

	public void add(Vector3 vec) {
		x += vec.x;
		y += vec.y;
		z += vec.z;
	}
	
	public GridPoint3 toGrid() {
		return new GridPoint3(floorX(), floorY(), floorZ());
	}
	
	public Vector3 toVecF() {
		return new Vector3((float)x, (float)y, (float)z);
	}
	
	public Vector3 toVecF(Vector3 vec) {
		return vec.set((float)x, (float)y, (float)z);
	}
	
	public float floatFactX() {
		double f = (double)(float)x;
		return (float)(f - x);
	}
	
	public float floatFactZ() {
		double f = (double)(float)z;
		return (float)(f - z);
	}

	
}
