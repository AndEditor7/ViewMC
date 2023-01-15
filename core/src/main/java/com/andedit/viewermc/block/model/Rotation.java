package com.andedit.viewermc.block.model;

import com.andedit.viewermc.util.Facing.Axis;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;

public class Rotation {
	
	private static final Quaternion IDT = new Quaternion();
	
	public final Vector3 origin;
	public final @Null Axis axis;
	public final float angle;
	public final boolean rescale;
	
	public final Quaternion quat;
	
	public Rotation(JsonValue value) {
		var array = new FloatArray(3);
		value.get("origin").forEach(v -> array.add(v.asFloat()));
		origin = new Vector3(array.items);
		
		axis = Axis.from(value.getChar("axis"));
		angle = value.getFloat("angle", 0);
		rescale = value.getBoolean("rescale", false);
		
		quat = MathUtils.isZero(angle) ? IDT : new Quaternion(axis.getVec(), angle);
	}
}
