package com.andedit.viewmc.resource;

import com.andedit.viewmc.resource.blockmodel.BlockModelJson;
import com.andedit.viewmc.resource.blockstate.BlockStateJson;
import com.andedit.viewmc.resource.texture.TextureJson;
import com.andedit.viewmc.util.Identifier;
import com.andedit.viewmc.util.Progress;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

public class RawResources {
	public final OrderedMap<Identifier, BlockStateJson> blockStates = new OrderedMap<>(4000);
	public OrderedMap<Identifier, BlockModelJson> blockModels = new OrderedMap<>(4000);
	
	public OrderedMap<Identifier, byte[]> blockTextures = new OrderedMap<>(4000); // raw png data
	public final ObjectMap<Identifier, TextureJson> blockTextureMetas = new ObjectMap<>(4000);
	
	public final ObjectMap<Identifier, byte[]> textures = new ObjectMap<>(200);
	
	void finalize(boolean cleanUp, Progress progress) {
		progress.newStep(blockModels.size);
		for (var model : blockModels.values()) {
			try {
				model.load(blockModels);
			} catch (Exception e) {
				continue;
			} finally {
				progress.incStep();
			}
		}
		
		if (!cleanUp) return;
		
		var newBlockModels = new OrderedMap<Identifier, BlockModelJson>(blockModels.size);
		for (var statejson : blockStates.values()) {
			statejson.addAll(blockModels, newBlockModels);
		}
		blockModels = newBlockModels;
		
		var newBlockTextures = new OrderedMap<Identifier, byte[]>(blockTextures.size);
		for (var modeljson : blockModels.values()) {
			modeljson.addAll(blockTextures, newBlockTextures);
		}
		blockTextures = newBlockTextures;
	}
}