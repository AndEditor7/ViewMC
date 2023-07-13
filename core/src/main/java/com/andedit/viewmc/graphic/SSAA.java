package com.andedit.viewmc.graphic;

public enum SSAA {
	NONE("Normal", 1), SAMPLE2X("High", 2), SAMPLE4X("Ultra", 4);
	
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
