package com.andedit.viewermc.block;

import java.util.List;

import com.andedit.viewermc.block.model.BlockModelJson;
import com.andedit.viewermc.block.state.BlockStateJson;
import com.andedit.viewermc.util.Identifier;
import com.andedit.viewermc.util.Pair;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

/** Contains block models and textures. */
public class Blocks implements Disposable {
	
	private final TextureAtlas textures;
	private final ObjectMap<String, BlockForm> idToBlock;
	private final TestBlock block;

	public Blocks(List<Pair<Identifier, BlockStateJson>> blockStates, OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		this.textures = textures;
		block = new TestBlock(textures);
		
		idToBlock = new ObjectMap<>(blockStates.size());
		for (var pair : blockStates) {
			var id = pair.left;
			var state = pair.right;
			idToBlock.put(id.full, createBlock(id, state, blockModels));
		}
		
		System.out.println("Block Loader Finished");
	}
	
	private BlockForm createBlock(Identifier id, BlockStateJson state, OrderedMap<Identifier, BlockModelJson> blockModels) {
		return new Block(id, state, blockModels, textures);
	}
	
	public BlockForm toBlock(String id) {
		//return id.contains("air") ? AirBlock.INSTANCE : block;
		var block = idToBlock.get(id);
		return block == null ? AirBlock.INSTANCE : block;
	}
	
	public void init() {
		textures.createTexture();
	}
	
	public void bindTexture() {
		textures.getTexture().bind();
	}
	
	/** Only update if the texture is binded. */
	public void update() {
		textures.update();
	}

	@Override
	public void dispose() {
		textures.dispose();
	}
}
