package com.andedit.viewermc.graphic;

import java.util.EnumMap;
import java.util.List;

import com.andedit.viewermc.block.Blocks;
import com.andedit.viewermc.graphic.vertex.Vertex;
import com.badlogic.gdx.graphics.GL20;

public class MeshProvider {
	private final EnumMap<RenderLayer, MeshBuilder> builders = new EnumMap<>(RenderLayer.class);
	
	public MeshProvider(Blocks blocks) {
		for (var layer : RenderLayer.VALUES) {
			builders.put(layer, new MeshBuilder(blocks));
		}
	}
	
	public MeshBuilder getBuilder(RenderLayer layer) {
		return builders.get(layer);
	}
	
	public void build(EnumMap<RenderLayer, ? extends List<Vertex>> verts) {
		for (var entry : verts.entrySet()) {
			var builder = builders.get(entry.getKey());
			var it = entry.getValue().listIterator(builder.build(entry.getValue(), () -> Vertex.newVbo(MeshVert.context, GL20.GL_STATIC_DRAW)));
	        while (it.hasNext()) {
	            it.next().dispose();
	            it.remove();
	        }
		}
	}

	public boolean isEmpty() {
		for (var builder : builders.values()) {
			if (builder.size() != 0) {
				return false;
			}
		}
		return true;
	}
}