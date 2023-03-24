package com.andedit.viewmc.block;

import java.util.Collection;

import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.resource.blockmodel.BlockModelJson;
import com.andedit.viewmc.resource.blockstate.BlockStateJson;
import com.andedit.viewmc.util.Cull;
import com.andedit.viewmc.util.Facing;
import com.andedit.viewmc.util.Identifier;
import com.andedit.viewmc.util.ModelSupplier;
import com.andedit.viewmc.world.BlockView;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.OrderedMap;

public class Block implements BlockLike {
	
	protected BlockState state;
	protected BlockLike renderable;
	protected String id;

	public Block() {
	}
	
	public final void init(Identifier id) {
		this.id = id.full;
		if (state == null) {
			state = newBlockState();
		}
	}
	
	public void loadModel(BlockStateJson state, OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		renderable = getRenderable(state, getModelSupplier(blockModels, textures));
	}
	
	protected ModelSupplier getModelSupplier(OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		return new ModelSupplier(blockModels, textures);
	}
	
	protected BlockLike getRenderable(BlockStateJson state, ModelSupplier supplier) {
		if (state.isVariants()) {
			return new Variants(state, supplier);
		}
		
		if (state.isMultipart()) {
			return new Multipart(state, supplier);
		}
		
		throw new IllegalStateException();
	}
	
	/** new befault blockstate */
	protected BlockState newBlockState() {
		return new BlockState(this);
	}

	@Override
	public void build(MeshProvider provider, BlockView view, BlockState state, int x, int y, int z) {
		renderable.build(provider, view, state, x, y, z);
	}

	@Override
	public void getQuads(Collection<Quad> list, BlockView view, BlockState state, int x, int y, int z) {
		renderable.getQuads(list, view, state, x, y, z);
	}

	@Override
	public void getBoxes(Collection<BoundingBox> list, BlockView view, BlockState state, int x, int y, int z) {
		renderable.getBoxes(list, view, state, x, y, z);
	}
	
	@Override
	public boolean isFullOpaque(BlockView view, BlockState state, int x, int y, int z) {
		return renderable.isFullOpaque(view, state, x, y, z);
	}
	
	public boolean isWaterLogged(BlockState state) {
		return state.get("waterlogged", "false").equals("true");
	}
	
	/** @param face it can be null */
	public boolean canRender(BlockState primary, BlockState secondary, Quad quad, @Null Facing face, Cull cull, int x, int y, int z) {
		return switch (cull) {
		case CULLED -> false;
		case RENDERABLE -> true;
		case CULLED_BUT_RENDERBALE -> primary.block != secondary.block;
		};
	}

	public BlockState getState() {
		return state;
	}

	public String getId() {
		return id;
	}

	
}
