package com.andedit.viewmc.graphic;

import java.util.ArrayList;
import java.util.EnumMap;

import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.resource.Resources;

public class MeshProvider {
	private final EnumMap<RenderLayer, MeshBuilder> builders = new EnumMap<>(RenderLayer.class);
	
	public final Resources resources;
	
	public MeshProvider(Resources resources) {
		this.resources = resources;
		for (var layer : RenderLayer.VALUES) {
			builders.put(layer, new MeshBuilder());
		}
	}
	
	public MeshBuilder getBuilder(RenderLayer layer) {
		return builders.get(layer);
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
