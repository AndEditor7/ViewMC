package com.andedit.viewermc.util;

import com.andedit.viewermc.block.BlockModel;
import com.andedit.viewermc.block.TextureAtlas;
import com.andedit.viewermc.block.model.BlockModelJson;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

/** A Model Supplier so the BlockModel can be reuse.*/
public class ModelSupplier {

	private final ObjectMap<Identifier, BlockModel> map = new ObjectMap<>(20);
	private final OrderedMap<Identifier, BlockModelJson> blockModels;
	private final TextureAtlas textures;
	
	public ModelSupplier(OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		this.blockModels = blockModels;
		this.textures = textures;
	}
	
	protected void config(Identifier id, BlockModel model) {
		
	}
	
	public final BlockModel get(Identifier id) {
		var model = map.get(id);
		if (model == null) {
			map.put(id, model = new BlockModel(blockModels.get(id), textures));
			config(id, model);
		}
		return model;
	}
}
