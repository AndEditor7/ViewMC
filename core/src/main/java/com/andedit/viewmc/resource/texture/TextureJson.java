package com.andedit.viewmc.resource.texture;

import java.util.OptionalInt;

import com.andedit.viewmc.util.IncompatibleException;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.JsonValue;

public class TextureJson {
	
	public final int frametime;
	public final int width, height;
	public final boolean interpolate;
	public final Frame[] frames;
	
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
		if (frames != null) {
			this.frames = new Frame[frames.size];
			for (int i = 0; i < frames.size; i++) {
				this.frames[i] = new Frame(frames.get(i), i);
			}
		} else this.frames = new Frame[0];
	}
	
	public int width(Pixmap pixmap) {
		return width == -1 ? pixmap.getWidth() : width;
	}
	
	public int height(Pixmap pixmap) {
		return height == -1 ? pixmap.getHeight() / (pixmap.getHeight() / width(pixmap)) : height;
	}
	
	public static class Frame {
		public final int index;
		public final OptionalInt time;
		
		private Frame(JsonValue value, int idx) {
			var val = value.getInt("index", -1);
			index = val == -1 ? value.asInt() : val;
			val = value.getInt("time", -1);
			time = val == -1 ? OptionalInt.empty() : OptionalInt.of(val);
		}
		
		public int getFrametime(int frametime) {
			return time.isEmpty() ? frametime : time.getAsInt();
		}
	}
}
