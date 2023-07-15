package com.andedit.viewmc.block.container;

import java.util.Collection;

import com.andedit.viewmc.block.Block;
import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.block.BlockRenderers;
import com.andedit.viewmc.block.BlockState;
import com.andedit.viewmc.block.TextureAtlas;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.resource.blockmodel.BlockModelJson;
import com.andedit.viewmc.resource.blockstate.BlockStateJson;
import com.andedit.viewmc.util.Identifier;
import com.andedit.viewmc.util.ModelSupplier;
import com.andedit.viewmc.world.BlockView;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.OrderedMap;

public class AirBlock extends Block {
	
	public static final AirBlock INSTANCE = new AirBlock();
	
	{
		state = new BlockState(this);
		id = "minecraft:air";
		renderers = BlockRenderers.BLANK;
	}

	@Override
	protected BlockRenderers getRenderers(BlockStateJson state, ModelSupplier supplier) {
		return BlockRenderers.BLANK;
	}

	@Override
	public String getId() {
		return id;
	}
}
