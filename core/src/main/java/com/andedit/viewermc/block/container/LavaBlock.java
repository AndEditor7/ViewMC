package com.andedit.viewermc.block.container;

import com.andedit.viewermc.block.TextureAtlas;
import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.block.model.BlockModelJson;
import com.andedit.viewermc.block.state.BlockStateJson;
import com.andedit.viewermc.graphic.RenderLayer;
import com.andedit.viewermc.util.Identifier;
import com.badlogic.gdx.utils.OrderedMap;

public class LavaBlock extends WaterBlock {

	public LavaBlock(Identifier id, BlockStateJson state, OrderedMap<Identifier, BlockModelJson> blockModels,
			TextureAtlas textures) {
		super(id, state, blockModels, textures);
	}
	
	@Override
	protected void quad(Quad quad) {
		quad.cullable = false;
	}
	
	@Override
	protected RenderLayer getRenderLayer() {
		return RenderLayer.SOILD;
	}
	
	@Override
	protected String getTexture() {
		return "block/lava_still";
	}
}
