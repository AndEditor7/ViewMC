package com.andedit.viewmc.graphic;

import java.util.ArrayList;
import java.util.EnumMap;

import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.graphic.vertex.Vertex;
import com.andedit.viewmc.resource.Resources;
import com.badlogic.gdx.graphics.GL20;

public class MeshProvider {
	private final EnumMap<RenderLayer, MeshBuilder> builders = new EnumMap<>(RenderLayer.class);
	
	public final Resources resources;
	
	public MeshProvider(Resources resources) {
		this.resources = resources;
		for (var layer : RenderLayer.VALUES) {
			builders.put(layer, new MeshBuilder(resources));
		}
	}
	
	public MeshBuilder getBuilder(RenderLayer layer) {
		return builders.get(layer);
	}
	
	public void build(Mesh mesh) {
		for (var entry : mesh.verts.entrySet()) {
			var builder = builders.get(entry.getKey());
			var it = entry.getValue().listIterator(builder.build(entry.getValue(), () -> Vertex.newVbo(MeshVert.attributes, GL20.GL_STATIC_DRAW)));
	        while (it.hasNext()) {
	            it.next().dispose();
	            it.remove();
	        }
		}
		
		for (var entry : mesh.textureToAnimate.entrySet()) {
			var builder = builders.get(entry.getKey());
			var array = entry.getValue();
			array.clear();
			array.addAll(builder.textureToAnimate.orderedItems());
			builder.textureToAnimate.clear();
		}
	}
	
	public void clear() {
		builders.values().forEach(MeshBuilder::clear);
	}

	public boolean isEmpty() {
		for (var builder : builders.values()) {
			if (builder.size() != 0) {
				return false;
			}
		}
		return true;
	}
	
	/* A cached instances temporary uses. */
	
	public final ArrayList<Quad> quads = new ArrayList<>();
	
	public final Lighting lighting = new Lighting();
}
