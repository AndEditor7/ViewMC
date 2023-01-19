package com.andedit.viewermc.graphic;

import com.andedit.viewermc.util.Facing;
import com.badlogic.gdx.utils.Null;

public class Lighting {

	public static float getShade(@Null Facing face) {
		return face == null ? 1 : switch (face) {
		case NORTH, SOUTH -> 0.8f;
		case EAST, WEST -> 0.6f;
		case DOWN -> 0.5f;
		default -> 1.0f;
		};
	}
	
	public static float getAmbient(int level) {
		return switch (level) {
		case 0 -> 0.3f;
		case 1 -> 0.45f;
		case 2 -> 0.65f;
		case 3 -> 0.8f;
		default -> 1.0f; // and 3
		};
	}
}
