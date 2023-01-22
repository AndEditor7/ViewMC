package com.andedit.viewermc.block;

import java.util.Collection;

import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.block.model.BlockModelJson;
import com.andedit.viewermc.block.state.BlockStateJson;
import com.andedit.viewermc.graphic.MeshProvider;
import com.andedit.viewermc.graphic.RenderLayer;
import com.andedit.viewermc.util.Cull;
import com.andedit.viewermc.util.Facing;
import com.andedit.viewermc.util.Identifier;
import com.andedit.viewermc.util.ModelSupplier;
import com.andedit.viewermc.world.Section;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.OrderedMap;

public class Block {
	
	protected BlockState state;
	protected Renderable renderable;
	protected String id;

	public Block() {
	}
	
	void init(Identifier id) {
		this.id = id.full;
		if (state == null) {
			state = newBlockState();
		}
	}
	
	protected void loadModel(BlockStateJson state, OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
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
		
		return null; // TODO: missing block model
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
	
	public boolean isFullOpaque(BlockState state, int x, int y, int z) {
		return renderable.isFullOpaque(state, x, y, z);
	}
	
	public boolean isWaterLogged(BlockState state) {
		return state.get("waterlogged", "false").equals("true");
	}
	
	/**
	 * @param quad TODO
	 * @param face it can be null */
	public boolean canRender(BlockState primary, BlockState secondary, Quad quad, @Null Facing face, Cull cull, int x, int y, int z) {
		if (cull == Cull.RENDERABLE) {
			return true;
		}
		
		if (cull == Cull.CULLED) {
			return false;
		}
		
		if (cull == Cull.CULLED_BUT_RENDERBALE) {
			return primary.block != secondary.block;
		}
		
		return false;
	}

	public BlockState getState() {
		return state;
	}

	public String getId() {
		return id;
	}
}
