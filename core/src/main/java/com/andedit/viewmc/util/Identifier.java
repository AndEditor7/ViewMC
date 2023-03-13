package com.andedit.viewmc.util;

public final class Identifier {
	public final String name, path, full;
	
	public Identifier(String string) {
		var array = Util.split(string, ':');
		if (array.size() == 1) {
			name = "minecraft";
			path = string;
			full = name + ':' + string;
		} else {
			name = array.get(0);
			path = array.get(1);
			full = string;
		}
	}
	
	public Identifier(String name, String path) {
		this.name = name;
		this.path = path;
		full = name + ':' + path;
	}
	
	public Identifier subfix(String string) {
		return new Identifier(name, path.concat(string));
	}
	
	@Override
	public int hashCode() {
		return full.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (obj.getClass() == getClass()) {
			return ((Identifier)obj).full.equals(full);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return full;
	}
}
