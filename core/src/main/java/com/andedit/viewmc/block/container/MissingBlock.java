package com.andedit.viewmc.block.container;

import java.util.Collection;

import com.andedit.viewmc.block.Block;
import com.andedit.viewmc.block.BlockModel;
import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.block.BlockState;
import com.andedit.viewmc.block.TextureAtlas;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.resource.blockmodel.BlockModelJson;
import com.andedit.viewmc.resource.blockstate.BlockStateJson;
import com.andedit.viewmc.util.Identifier;
import com.andedit.viewmc.world.BlockView;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.OrderedMap;

public class MissingBlock extends Block {
	
	private BlockModel model;
	
	@Override
	public void loadModel(BlockStateJson state, OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		model = BlockModel.missingModel(textures.getMissingSprite());
	}
	
	@Override
	public void build(MeshProvider provider, BlockView view, BlockState state, int x, int y, int z) {
		model.build(provider, view, state, x, y, z);
	}

	@Override
	public void getQuads(Collection<Quad> list, BlockView view, BlockState state, int x, int y, int z) {
		model.getQuads(list, view, state, x, y, z);
	}

	@Override
	public void getBoxes(Collection<BoundingBox> list, BlockView view, BlockState state, int x, int y, int z) {
		model.getBoxes(list, view, state, x, y, z);
	}
	
	@Override
	public boolean isFullOpaque(BlockView view, BlockState state, int x, int y, int z) {
		return true;
	}
	
	@Override
	public boolean isWaterLogged(BlockState state) {
		return false;
	}
}
