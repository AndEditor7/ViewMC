package com.andedit.viewmc.resource.blockmodel;

import com.andedit.viewmc.util.Facing;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;

public class Face {
	
	public final Facing face;
	public final @Null UV uv;
	public final String texture;
	public final Facing cullFace;
	public final int rotation;
	public final int tintIndex;
	
	public final boolean culling;
	
	public Face(Facing face, JsonValue value) {
		this.face = face;
		var val = value.get("uv");
		uv = val == null ? null : new UV(val);
		texture = value.getString("texture");
		var str = value.get("cullface");
		culling = str != null;
		cullFace = str == null ? face : Facing.from(str.asString());
		rotation = value.getInt("rotation", 0);
		tintIndex = value.getInt("tintindex", -1);
	}

	Face(Facing face) {
		this.face = face;
		uv = null;
		texture = "missing";
		cullFace = face;
		rotation = 0;
		tintIndex = -1;
		
		culling = true;
	}
}
