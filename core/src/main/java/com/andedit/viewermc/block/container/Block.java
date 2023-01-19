package com.andedit.viewermc.block.container;

import java.util.Collection;

import com.andedit.viewermc.block.BlockForm;
import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.block.BlockState;
import com.andedit.viewermc.block.Multipart;
import com.andedit.viewermc.block.Renderable;
import com.andedit.viewermc.block.TextureAtlas;
import com.andedit.viewermc.block.Variants;
import com.andedit.viewermc.block.model.BlockModelJson;
import com.andedit.viewermc.block.state.BlockStateJson;
import com.andedit.viewermc.graphic.MeshProvider;
import com.andedit.viewermc.graphic.RenderLayer;
import com.andedit.viewermc.util.Identifier;
import com.andedit.viewermc.util.ModelSupplier;
import com.andedit.viewermc.world.Section;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.OrderedMap;

public class Block implements BlockForm {
	
	protected final BlockState state;
	protected final Renderable renderable;
	protected final String id;

	public Block(Identifier id, BlockStateJson state, OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		this.id = id.full;
		this.state = new BlockState(this);
		renderable = getRenderable(id, state, getModelSupplier(blockModels, textures));
	}
	
	protected ModelSupplier getModelSupplier(OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		return new ModelSupplier(blockModels, textures);
	}
	
	protected Renderable getRenderable(Identifier id, BlockStateJson state, ModelSupplier supplier) {
		if (state.isVariants()) {
			return new Variants(state, supplier);
		} else if (state.isMultipart()) {
			return new Multipart(state, supplier);
		} else {
			return null; // TODO: missing block model
		}
	}
	
	protected RenderLayer getRenderLayer() {
		return RenderLayer.SOILD;
	}

	@Override
	public void build(Section section, MeshProvider provider, BlockState state, int x, int y, int z) {
		renderable.build(section, provider.getBuilder(getRenderLayer()), state, x, y, z);
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

	@Override
	public String getId() {
		return id;
	}
}
