package com.andedit.viewmc.graphic.renderer;

import static com.badlogic.gdx.Gdx.gl;

import com.andedit.viewmc.graphic.RenderLayer;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.graphics.GL20;

public class ViewRenderer extends Renderer {

	public ViewRenderer() {
		super(Util.newShader("shaders/mesh/view"));
	}

	@Override
	public void enable(RenderLayer layer) {
		if (layer == RenderLayer.TRANS) {
			gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			gl.glEnable(GL20.GL_BLEND);
		}
	}

	@Override
	public void disable(RenderLayer layer) {
		if (layer == RenderLayer.TRANS) {
			gl.glDisable(GL20.GL_BLEND);
		}
	}
}
