package com.andedit.viewmc.util;

import com.badlogic.gdx.utils.ObjectMap;

public class Properties extends ObjectMap<String, Object> {
	
	public void putIfAbsent(String key, Object value) {
		if (!containsKey(key)) {
			put(key, value);
		}
	}
	
	public Number getNumber(String key) {
		return (Number)get(key);
	}
	
	public int getInt(String key) {
		return getNumber(key).intValue();
	}
	
	public float getFloat(String key) {
		return getNumber(key).floatValue();
	}
	
	public double getDouble(String key) {
		return getNumber(key).doubleValue();
	}
	
	public boolean getBool(String key) {
		return (boolean)get(key);
	}
}
