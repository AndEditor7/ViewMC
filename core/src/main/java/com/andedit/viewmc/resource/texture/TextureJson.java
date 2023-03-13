package com.andedit.viewmc.resource.texture;

import com.andedit.viewmc.util.IncompatibleException;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;

public class TextureJson {
	
	public final int frametime;
	public final int width, height;
	public final boolean interpolate;
	public final @Null int frames[];
	
	public TextureJson(JsonValue value) throws IncompatibleException {
		value = value.get("animation");
		if (value == null) {
			throw new IncompatibleException();
		}
		this.frametime = value.getInt("frametime", 1);
		this.width = value.getInt("width", -1);
		this.height = value.getInt("height", -1);
		this.interpolate = value.getBoolean("interpolate", false);
		var frames = value.get("frames");
		var array = frames == null ? null : frames.asIntArray();
		this.frames = array == null || array.length == 0 ? null : array;
	}
	
	public int width(Pixmap pixmap) {
		return width == -1 ? pixmap.getWidth() : width;
	}
	
	public int height(Pixmap pixmap) {
		return height == -1 ? pixmap.getHeight() / (pixmap.getHeight() / width(pixmap)) : height;
	}
}
