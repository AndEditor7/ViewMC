package com.andedit.viewmc.graphic;

public enum TextureBlend {
	SOILD, TRANSPARENT, TRANSLUCENT;
	
	public boolean isSoild() {
		return this == SOILD;
	}
	
	public boolean isTrans() {
		return this != SOILD;
	}
	
	public RenderLayer getRenderLayer() {
		return this == TRANSLUCENT ? RenderLayer.TRANS : RenderLayer.SOILD;
	}
}
