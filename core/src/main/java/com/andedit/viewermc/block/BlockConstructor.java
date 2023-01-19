package com.andedit.viewermc.block;

import com.andedit.viewermc.block.model.BlockModelJson;
import com.andedit.viewermc.block.state.BlockStateJson;
import com.andedit.viewermc.util.Identifier;
import com.badlogic.gdx.utils.OrderedMap;

@FunctionalInterface
public interface BlockConstructor {
	BlockForm create(Identifier id, BlockStateJson state, OrderedMap<Identifier, BlockModelJson> models, TextureAtlas textures);
}
