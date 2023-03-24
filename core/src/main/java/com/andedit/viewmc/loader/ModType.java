package com.andedit.viewmc.loader;

public enum ModType {
	VANILLA("Vanilla"), 
	FORGE("Forge"), 
	FABIC("Fabric"), 
	QUILT("Quilt");
	
	private final String name;
	
	private ModType(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
