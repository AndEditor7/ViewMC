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
import com.andedit.viewmc.world.Section;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.OrderedMap;

public class Block {
	
	protected BlockState state;
	protected Renderable renderable;
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
	
	protected Renderable getRenderable(BlockStateJson state, ModelSupplier supplier) {
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

	public void build(Section section, MeshProvider provider, BlockState state, int x, int y, int z) {
		renderable.build(section, provider, state, x, y, z);
	}

	public void getQuads(BlockState state, Collection<Quad> list, int x, int y, int z) {
		renderable.getQuads(state, list, x, y, z);
	}

	public void getBoxes(BlockState state, Collection<BoundingBox> list, int x, int y, int z) {
		renderable.getBoxes(state, list, x, y, z);
	}
	
	public boolean isFullOpaque(BlockState state, int blockLight, int x, int y, int z) {
		return renderable.isFullOpaque(state, x, y, z) && blockLight == 0;
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
