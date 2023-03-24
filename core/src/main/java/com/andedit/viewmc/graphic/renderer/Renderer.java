package com.andedit.viewmc.graphic.renderer;

import com.andedit.viewmc.graphic.RenderLayer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

public abstract class Renderer implements Disposable {
	
	public final ShaderProgram shader;
	
	public Renderer(ShaderProgram shader) {
		this.shader = shader;
	}
	
	public abstract void enable(RenderLayer layer);
	
	public abstract void disable(RenderLayer layer);
	
	@Override
	public void dispose() {
		shader.dispose();
	}
}
