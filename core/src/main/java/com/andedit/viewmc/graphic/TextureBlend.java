package com.andedit.viewmc.graphic;

public enum TextureBlend {
	SOLID, TRANSPARENT, TRANSLUCENT;
	
	public boolean isSolid() {
		return this == SOLID;
	}
	
	public boolean isTrans() {
		return this != SOLID;
	}
	
	public RenderLayer getRenderLayer() {
		return this == TRANSLUCENT ? RenderLayer.TRANS : RenderLayer.SOLID;
	}
}
