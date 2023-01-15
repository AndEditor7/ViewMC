package com.andedit.viewermc.block;

import com.andedit.viewermc.util.Facing;

public interface BlockForm extends Renderable {
	
	static float getShade(Facing face) {
		return switch (face) {
		case NORTH, SOUTH -> 0.8f;
		case EAST, WEST -> 0.7f;
		case DOWN -> 0.6f;
		default -> 1.0f;
		};
	}
	
	/** Contains the property key */
	default boolean containsKey(String key) {
		return true;
	}
	
	/** Get default blockstate */
	BlockState getState();
}
