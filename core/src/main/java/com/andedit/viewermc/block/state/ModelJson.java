package com.andedit.viewermc.block.state;

import com.andedit.viewermc.util.Identifier;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.OrderedSet;

public class ModelJson {
	public final static Matrix4 IDT = new Matrix4();
	
	public final Identifier model;
	public final int x, y;
	public final boolean uvLock;
	public final int weight;
	
	public final Matrix4 matrix;
	
	public ModelJson(JsonValue value, OrderedSet<Identifier> modelIds) {
		model = new Identifier(value.getString("model"));
		x = value.getInt("x", 0);
		y = value.getInt("y", 0);
		uvLock = value.getBoolean("uvlock", false);
		weight = value.getInt("weight", 1);
		modelIds.add(model);
		
		if (hasTransformation()) {
			var quat = new Quaternion();
			matrix = new Matrix4();
			if (y != 0) {
				matrix.rotate(quat.set(Vector3.Y, -y));
			}
			if (x != 0) {
				matrix.rotate(quat.set(Vector3.X, -x));
			} 
			
		} else matrix = IDT;
	}
	
	public boolean hasTransformation() {
		return x != 0 || y != 0;
	}
}
