package com.andedit.viewmc.graphic.renderer;

import com.andedit.viewmc.graphic.RenderLayer;
import com.andedit.viewmc.util.Util;

public class NoTextureRenderer extends Renderer {

	public NoTextureRenderer() {
		super(Util.newShader("shaders/mesh/no_texture"));
	}

	@Override
	public void enable(RenderLayer layer) {
		
	}

	@Override
	public void disable(RenderLayer layer) {
		
	}
	
	@Override
	public String toString() {
		return "No Texture";
	}
}
