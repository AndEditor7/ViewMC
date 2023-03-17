package com.andedit.viewmc.resource.blockmodel;

import com.andedit.viewmc.util.Facing.Axis;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.JsonValue;

public class Rotation {
	
	private static final Matrix4 IDT = new Matrix4();
	
	public final Vector3 origin;
	public final Axis axis;
	public final float angle;
	public final boolean rescale;
	
	public final Matrix4 matrix;
	
	public Rotation(JsonValue value) {
		var array = new FloatArray(3);
		value.get("origin").forEach(v -> array.add(v.asFloat()));
		origin = new Vector3(array.items);
		
		axis = Axis.from(value.getChar("axis"));
		angle = value.getFloat("angle", 0);
		rescale = value.getBoolean("rescale", false);
		
		matrix = MathUtils.isZero(angle) ? IDT : new Matrix4(new Quaternion(axis.getVec(), angle));
	}
}
