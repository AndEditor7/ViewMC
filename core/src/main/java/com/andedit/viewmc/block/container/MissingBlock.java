package com.andedit.viewmc.block.container;

import java.util.Collection;

import com.andedit.viewmc.block.Block;
import com.andedit.viewmc.block.BlockModel;
import com.andedit.viewmc.block.BlockState;
import com.andedit.viewmc.block.TextureAtlas;
import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.resource.blockmodel.BlockModelJson;
import com.andedit.viewmc.resource.blockstate.BlockStateJson;
import com.andedit.viewmc.util.Identifier;
import com.andedit.viewmc.world.Section;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.OrderedMap;

public class MissingBlock extends Block {
	
	private BlockModel model;
	
	@Override
	public void loadModel(BlockStateJson state, OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		model = BlockModel.missingModel(textures.getMissingSprite());
	}
	
	public void build(Section section, MeshProvider provider, BlockState state, int x, int y, int z) {
		model.build(section, provider, state, x, y, z);
	}

	public void getQuads(BlockState state, Collection<Quad> list, int x, int y, int z) {
		model.getQuads(list);
	}

	public void getBoxes(BlockState state, Collection<BoundingBox> list, int x, int y, int z) {
		model.getBoxes(list);
	}
	
	public boolean isFullOpaque(BlockState state, int blockLight, int x, int y, int z) {
		return true;
	}
	
	public boolean isWaterLogged(BlockState state) {
		return state.get("waterlogged", "false").equals("true");
	}
}
