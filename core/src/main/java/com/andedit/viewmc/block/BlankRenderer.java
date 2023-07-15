package com.andedit.viewmc.block;

import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.world.BlockView;
import com.badlogic.gdx.math.collision.BoundingBox;

import java.util.Collection;

public class BlankRenderer implements BlockRenderer {
	@Override
	public void build(MeshProvider provider, BlockView view, BlockState state, int x, int y, int z) {

	}

	@Override
	public void getQuads(Collection<BlockModel.Quad> collection, BlockView view, BlockState state, int x, int y, int z) {

	}

	@Override
	public void getBoxes(Collection<BoundingBox> collection, BlockView view, BlockState state, int x, int y, int z) {

	}

	@Override
	public boolean isFullOpaque(BlockView view, BlockState state, int x, int y, int z) {
		return false;
	}
}
