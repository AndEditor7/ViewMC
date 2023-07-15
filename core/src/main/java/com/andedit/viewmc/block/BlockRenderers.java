package com.andedit.viewmc.block;

import com.andedit.viewmc.world.BlockView;

public interface BlockRenderers {

	BlockRenderers BLANK = of(new BlankRenderer());

	BlockRenderer getRenderer(BlockView view, BlockState state, int x, int y, int z);

	static BlockRenderers of(BlockRenderer renderer) {
		return (view, state, x, y, z) -> renderer;
	}
}
