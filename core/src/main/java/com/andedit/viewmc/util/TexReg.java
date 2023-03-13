package com.andedit.viewmc.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** The Immutable Texture Region, it defines a rectangular area of a texture. */
public class TexReg {
	
	public static final TexReg FULL = new TexReg(0, 0, 1, 1); 
	
	public final float u1, v1, u2, v2;
	
	public TexReg(TextureRegion region) {
		this(region.getU(), region.getV(), region.getU2(), region.getV2());
	}
	
	public TexReg(float u1, float v1, float u2, float v2) {
		this.u1 = u1;
		this.v1 = v1;
		this.u2 = u2;
		this.v2 = v2;
	}
	
	/** To libGDX"s TextureRegion. */
	public TextureRegion toGdxTR(Texture texture) {
		return new TextureRegion(texture, u1, v1, u2, v2);
	}
}
