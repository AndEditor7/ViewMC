package com.andedit.viewmc.block;

import java.util.Collection;

import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.world.BlockView;
import com.badlogic.gdx.math.collision.BoundingBox;

public interface BlockLike {
	void build(MeshProvider provider, BlockView view, BlockState state, int x, int y, int z);
	
	void getQuads(Collection<Quad> collection, BlockView view, BlockState state, int x, int y, int z);
	
	void getBoxes(Collection<BoundingBox> collection, BlockView view, BlockState state, int x, int y, int z);
	
	/** Is full cube and opaque. Used for ambient occlusion. */
	boolean isFullOpaque(BlockView view, BlockState state, int x, int y, int z);
}
