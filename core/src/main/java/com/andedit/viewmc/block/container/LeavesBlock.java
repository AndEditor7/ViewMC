package com.andedit.viewmc.block.container;

import com.andedit.viewmc.block.Block;
import com.andedit.viewmc.block.BlockModel;
import com.andedit.viewmc.block.BlockState;
import com.andedit.viewmc.block.TextureAtlas;
import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.resource.blockmodel.BlockModelJson;
import com.andedit.viewmc.util.Cull;
import com.andedit.viewmc.util.Facing;
import com.andedit.viewmc.util.Identifier;
import com.andedit.viewmc.util.ModelSupplier;
import com.andedit.viewmc.world.BlockView;
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
	public boolean isFullOpaque(BlockView view, BlockState state, int x, int y, int z) {
		return true;
	}
	
	@Override
	public boolean canRender(BlockState primary, BlockState secondary, Quad quad, Facing face, Cull cull, int x, int y, int z) {
		return cull.isRenderable();
	}
}
