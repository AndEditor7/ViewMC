package com.andedit.viewermc.block.container;

import com.andedit.viewermc.block.Block;
import com.andedit.viewermc.block.BlockModel;
import com.andedit.viewermc.block.BlockState;
import com.andedit.viewermc.block.TextureAtlas;
import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.block.model.BlockModelJson;
import com.andedit.viewermc.util.Cull;
import com.andedit.viewermc.util.Facing;
import com.andedit.viewermc.util.Identifier;
import com.andedit.viewermc.util.ModelSupplier;
import com.badlogic.gdx.utils.OrderedMap;

public class LeavesBlock extends Block {

	public LeavesBlock() {
		super();
	}
	
	@Override
	protected ModelSupplier getModelSupplier(OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		return new ModelSupplier(blockModels, textures) {
			@Override
			public BlockModel config(Identifier id, BlockModel model) {
				for (var quad : model.quads) {
					quad.allowRender = true;
				}
				return model;
			}
		};
	}
	
	@Override
	public boolean isFullOpaque(BlockState state, int x, int y, int z) {
		return true;
	}
	
	@Override
	public boolean canRender(BlockState primary, BlockState secondary, Quad quad, Facing face, Cull cull, int x, int y, int z) {
		return cull.isRenderable();
	}
}
