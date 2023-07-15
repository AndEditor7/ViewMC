package com.andedit.viewmc.block.container;

import java.util.Collection;

import com.andedit.viewmc.block.*;
import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.resource.blockmodel.BlockModelJson;
import com.andedit.viewmc.resource.blockstate.BlockStateJson;
import com.andedit.viewmc.util.Identifier;
import com.andedit.viewmc.world.BlockView;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.OrderedMap;

public class MissingBlock extends Block {
	
	@Override
	public void loadModel(BlockStateJson state, OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		renderers = BlockRenderers.of(BlockModel.missingModel(textures.getMissingSprite()));
	}
	
	@Override
	public boolean isWaterLogged(BlockState state) {
		return false;
	}
}
