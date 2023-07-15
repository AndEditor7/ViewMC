package com.andedit.viewmc.block;

import java.util.Collection;

import com.andedit.viewmc.Main;
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

public class Block implements BlockRenderers {
	
	protected BlockState state;
	protected BlockRenderers renderers = BlockRenderers.BLANK;
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
		renderers = getRenderers(state, getModelSupplier(blockModels, textures));
	}

	@Override
	public BlockRenderer getRenderer(BlockView view, BlockState state, int x, int y, int z) {
		return renderers.getRenderer(view, state, x, y, z);
	}
	
	protected ModelSupplier getModelSupplier(OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		return new ModelSupplier(blockModels, textures);
	}
	
	protected BlockRenderers getRenderers(BlockStateJson state, ModelSupplier supplier) {
		if (state.isVariants()) {
			return new Variants(state, supplier);
		}
		
		if (state.isMultipart()) {
			return new Multipart(state, supplier);
		}
		
		throw new IllegalStateException();
	}
	
	/** new default blockstate */
	protected BlockState newBlockState() {
		return new BlockState(this);
	}
	
	public boolean isWaterLogged(BlockState state) {
		return state.get("waterlogged", "false").equals("true");
	}

	/** @param face it can be null */
	public boolean canRender(BlockState primary, @Null BlockState secondary, Quad quad, @Null Facing face, Cull cull, int x, int y, int z) {
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
