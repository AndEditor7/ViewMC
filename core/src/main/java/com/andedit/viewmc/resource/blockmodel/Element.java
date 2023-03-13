package com.andedit.viewmc.resource.blockmodel;

import java.util.EnumMap;

import com.andedit.viewmc.util.Facing;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;

public class Element {
	
	public final Vector3 from, to;
	public final @Null Rotation rotation;
	public final boolean shade;
	public final EnumMap<Facing, Face> faces;
	
	public Element(JsonValue value) {
		from = new Vector3(value.get("from").asFloatArray());
		to = new Vector3(value.get("to").asFloatArray());
		
		var val = value.get("rotation");
		rotation = val == null ? null : new Rotation(val);
		
		shade = value.getBoolean("shade", true);
		
		faces = new EnumMap<>(Facing.class);
		for (var v : value.get("faces")) {
			var face = Facing.from(v.name);
			faces.put(face, new Face(face, v));
		}
	}

	Element() {
		from = new Vector3();
		to = new Vector3(16, 16, 16);
		rotation = null;
		shade = true;
		faces = new EnumMap<>(Facing.class);
		for (var face : Facing.values()) {
			faces.put(face, new Face(face));
		}
	}
}
