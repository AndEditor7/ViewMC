package com.andedit.viewermc.block.model;

import com.badlogic.gdx.utils.JsonValue;

public class UV {
	public final float x1, y1, x2, y2;
	
	public UV(JsonValue value) {
		var array = value.asFloatArray();
		x1 = array[0];
		y1 = array[1];
		x2 = array[2];
		y2 = array[3];
	}
}
