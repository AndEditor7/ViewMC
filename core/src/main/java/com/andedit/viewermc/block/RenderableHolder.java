package com.andedit.viewermc.block;

import java.util.Collection;

import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.graphic.MeshBuilder;
import com.andedit.viewermc.graphic.MeshProvider;
import com.andedit.viewermc.world.World;
import com.badlogic.gdx.math.collision.BoundingBox;

/** Used for overriding the specific methods. */
public class RenderableHolder implements Renderable {
	
	private final Renderable renderable;
	
	public RenderableHolder(Renderable renderable) {
		this.renderable = renderable;
	}

	@Override
	public void build(World world, MeshBuilder builder, BlockState state, int x, int y, int z) {
		renderable.build(world, builder, state, x, y, z);
	}

	@Override
	public void getQuads(BlockState state, Collection<Quad> collection, int x, int y, int z) {
		renderable.getQuads(state, collection, x, y, z);
	}

	@Override
	public void getBoxes(BlockState state, Collection<BoundingBox> collection, int x, int y, int z) {
		renderable.getBoxes(state, collection, x, y, z);
	}

	@Override
	public boolean isFullOpaque(BlockState state, int x, int y, int z) {
		return renderable.isFullOpaque(state, x, y, z);
	}

}
