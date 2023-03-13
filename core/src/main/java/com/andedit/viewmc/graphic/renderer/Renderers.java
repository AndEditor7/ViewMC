package com.andedit.viewmc.graphic.renderer;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectIntMap;

public class Renderers implements Disposable {

	private final Array<Renderer> renderers = new Array<>();
	private final ObjectIntMap<Renderer> rendererToIndex = new ObjectIntMap<>();
	private int index;
	
	public final Renderer minecraft;
	public final Renderer simple;
	public final Renderer noTexture;
	public final Renderer wireframe;
	
	public Renderers() {
		addRender(minecraft = new MinecraftRenderer());
		addRender(simple = new SimpleRenderer());
		addRender(noTexture = new NoTextureRenderer());
		addRender(wireframe = new WireframeRenderer());
	}
	
	private void addRender(Renderer renderer) {
		rendererToIndex.put(renderer, renderers.size);
		renderers.add(renderer);
	}
	
	public void cycle() {
		index = (index + 1) % renderers.size;
	}
	
	public void setRenderer(Renderer renderer) {
		index = rendererToIndex.get(renderer, -1);
	}
	
	public Renderer getRenderer() {
		return renderers.get(index);
	}
	
	@Override
	public void dispose() {
		renderers.forEach(Renderer::dispose);
	}
}
