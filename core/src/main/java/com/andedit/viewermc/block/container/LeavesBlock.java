package com.andedit.viewermc.block.container;

import com.andedit.viewermc.block.BlockModel;
import com.andedit.viewermc.block.BlockState;
import com.andedit.viewermc.block.Renderable;
import com.andedit.viewermc.block.RenderableHolder;
import com.andedit.viewermc.block.TextureAtlas;
import com.andedit.viewermc.block.model.BlockModelJson;
import com.andedit.viewermc.block.state.BlockStateJson;
import com.andedit.viewermc.util.Identifier;
import com.andedit.viewermc.util.ModelSupplier;
import com.badlogic.gdx.utils.OrderedMap;

public class LeavesBlock extends Block {

	public LeavesBlock(Identifier id, BlockStateJson state, OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		super(id, state, blockModels, textures);
	}
	
	@Override
	protected ModelSupplier getModelSupplier(OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		return new ModelSupplier(blockModels, textures) {
			@Override
			public BlockModel config(Identifier id, BlockModel model) {
				for (var quad : model.quads) {
					//quad.culling = false;
					quad.cullable = false;
				}
				return model;
			}
		};
	}
	
	@Override
	protected Renderable getRenderable(Identifier id, BlockStateJson state, ModelSupplier supplier) {
		return new RenderableHolder(super.getRenderable(id, state, supplier)) {
			@Override
			public boolean isFullOpaque(BlockState state, int x, int y, int z) {
				return true;
			}
		};
	}
}
