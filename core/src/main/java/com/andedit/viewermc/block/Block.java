package com.andedit.viewermc.block;

import java.util.Collection;

import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.block.model.BlockModelJson;
import com.andedit.viewermc.block.state.BlockStateJson;
import com.andedit.viewermc.graphic.MeshBuilder;
import com.andedit.viewermc.util.Identifier;
import com.andedit.viewermc.world.World;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.OrderedMap;

public class Block implements BlockForm {
	
	private final BlockState state;
	private final Renderable renderable;

	public Block(Identifier id, BlockStateJson state, OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		this.state = new BlockState(this);
		
		if (state.isVariants()) {
			renderable = new Variants(state, blockModels, textures);
		} else if (state.isMultipart()) {
			renderable = new Multipart(state, blockModels, textures);
		} else {
			renderable = null; // TODO: missing block model
		}
	}

	@Override
	public void build(World world, MeshBuilder builder, BlockState state, int x, int y, int z) {
		renderable.build(world, builder, state, x, y, z);
	}

	@Override
	public void getQuads(BlockState state, Collection<Quad> list, int x, int y, int z) {
		renderable.getQuads(state, list, x, y, z);
	}

	@Override
	public void getBoxes(BlockState state, Collection<BoundingBox> list, int x, int y, int z) {
		renderable.getBoxes(state, list, x, y, z);
	}
	
	@Override
	public boolean isFullOpaque(BlockState state, int x, int y, int z) {
		return renderable.isFullOpaque(state, x, y, z);
	}

	@Override
	public BlockState getState() {
		return state;
	}

	
}
