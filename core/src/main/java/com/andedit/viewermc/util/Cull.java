package com.andedit.viewermc.util;

public enum Cull {
	CULLED, CULLED_BUT_RENDERBALE, RENDERABLE;
	
	public boolean isRenderable() {
		return this == RENDERABLE || this == CULLED_BUT_RENDERBALE;
	}
	
	public boolean isCulled() {
		return this == CULLED || this == CULLED_BUT_RENDERBALE;
	}
}