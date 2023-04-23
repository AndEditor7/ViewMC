package com.andedit.viewmc.graphic;

public enum SSAA {
	NONE("Normal", 1), SAMPLE4X("High", 2), SAMPLE8X("Ultra", 4);
	
	public final String name;
	public final int gridSize;
	
	private SSAA(String name, int gridSize) {
		this.name = name;
		this.gridSize = gridSize;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
