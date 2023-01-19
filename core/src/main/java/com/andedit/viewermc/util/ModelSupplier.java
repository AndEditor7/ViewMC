package com.andedit.viewermc.util;

import com.andedit.viewermc.block.BlockModel;
import com.andedit.viewermc.block.TextureAtlas;
import com.andedit.viewermc.block.model.BlockModelJson;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * A Model Supplier so the BlockModel can be reuse. Extend it and override the
 * config method to config models.
 */
public class ModelSupplier {

	public final TextureAtlas textures;

	private final ObjectMap<Identifier, BlockModel> map = new ObjectMap<>(20);
	private final OrderedMap<Identifier, BlockModelJson> blockModels;

	public ModelSupplier(OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		this.blockModels = blockModels;
		this.textures = textures;
	}

	public BlockModel config(Identifier id, BlockModel model) {
		return model;
	}

	public final BlockModel get(Identifier id) {
		var model = map.get(id);
		if (model == null) {
			map.put(id, model = new BlockModel(blockModels.get(id), textures));
		}
		return model;
	}
}
