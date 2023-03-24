package com.andedit.viewmc.world;

import com.andedit.viewmc.util.IntsFunction;

public class Lights {
	
	public static final int DEFAULT_BLOCK = 0;
	public static final int DEFAULT_SKY = 0;
	public static final int DEFAULT_LIGHT = (DEFAULT_BLOCK << 4) | DEFAULT_SKY;
	
	public static final int MAX = 15;
	public static final float SCL = (float)MAX;
	
	public static final IntsFunction BLOCK = Lights::toBlock;
	public static final IntsFunction SKY = Lights::toSky;
	
	public static int toBlock(int light) {
		return light >>> 4;
	}
	
	public static int toSky(int light) {
		return light & 0xF;
	}
	
	public static float toBlockF(int light) {
		return toBlock(light) / 15f;
	}
	
	public static float toSkyF(int light) {
		return toSky(light) / 15f;
	}
}
