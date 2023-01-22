package com.andedit.viewermc.graphic;

public enum Rendering {
	SIMPLE(false, false, true), 
	MINECRAFT(true, true, true);
	
	public final boolean ao, lighting, reloadMesh;
	
	private Rendering(boolean ao, boolean lighting, boolean reloadMesh) {
		this.ao = ao;
		this.lighting = lighting;
		this.reloadMesh = reloadMesh;
	}
	
	public Rendering cycle() {
		var array = values();
		return array[(ordinal()+1)%array.length];
	}
	
	@Override
	public String toString() {
		return switch (this) {
		case SIMPLE -> "Simple";
		case MINECRAFT -> "Minecraft";
		};
	}
}
