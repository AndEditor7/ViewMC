package com.andedit.viewmc.graphic.renderer;

import static com.badlogic.gdx.Gdx.gl;

import com.andedit.viewmc.Main;
import com.andedit.viewmc.graphic.RenderLayer;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.graphics.GL20;

public class WireframeRenderer extends Renderer {

	public WireframeRenderer() {
		super(Util.newShader("shaders/mesh/wireframe"));
	}
	
	@Override
	public void enable(RenderLayer layer) {
		gl.glLineWidth(1);
		Main.api.glPolygonMode(GL20.GL_FRONT_AND_BACK, false);
	}

	@Override
	public void disable(RenderLayer layer) {
		Main.api.glPolygonMode(GL20.GL_FRONT_AND_BACK, true);
	}
	
	@Override
	public String toString() {
		return "Wireframe";
	}
}
