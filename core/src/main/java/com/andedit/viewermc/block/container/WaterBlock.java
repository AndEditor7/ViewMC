package com.andedit.viewermc.block.container;

import java.util.Collection;

import com.andedit.viewermc.block.BlockState;
import com.andedit.viewermc.block.Renderable;
import com.andedit.viewermc.block.TextureAtlas;
import com.andedit.viewermc.block.BlockModel;
import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.block.model.BlockModelJson;
import com.andedit.viewermc.block.state.BlockStateJson;
import com.andedit.viewermc.graphic.MeshProvider;
import com.andedit.viewermc.graphic.RenderLayer;
import com.andedit.viewermc.util.Facing;
import com.andedit.viewermc.util.Identifier;
import com.andedit.viewermc.util.ModelSupplier;
import com.andedit.viewermc.world.World;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.OrderedMap;

public class WaterBlock extends Block {
	
	private final BlockModel fullModel, haftModel;

	public WaterBlock(Identifier id, BlockStateJson state, OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		super(id, state, blockModels, textures);
		
		var texture = textures.getRegion(new Identifier(getTexture()));
		fullModel = new BlockModel();
		var cube = fullModel.cube(0, 0, 0, 16, 16, 16);
		cube.regAll(texture);
		cube.forEach(this::quad);
		
		haftModel = new BlockModel();
		cube = haftModel.cube(0, 0, 0, 16, 14, 16);
		cube.regAll(texture);
		cube.forEach(this::quad);
	}
	
	protected void quad(Quad quad) {
		quad.cullable = false;
		quad.tintIndex = 0;
	}
	
	protected String getTexture() {
		return "block/water_still";
	}
	
	@Override
	protected ModelSupplier getModelSupplier(OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		return null;
	}

	@Override
	protected Renderable getRenderable(Identifier id, BlockStateJson state, ModelSupplier supplier) {
		return null;
	}
	
	@Override
	protected RenderLayer getRenderLayer() {
		return RenderLayer.TRANS;
	}
	
	@Override
	public void build(World world, MeshProvider provider, BlockState state, int x, int y, int z) {
		getModel(world, x, y, z).build(world, provider.getBuilder(getRenderLayer()), state, x, y, z);
	}

	@Override
	public void getQuads(BlockState state, Collection<Quad> list, int x, int y, int z) {
		
	}

	@Override
	public void getBoxes(BlockState state, Collection<BoundingBox> list, int x, int y, int z) {
		
	}
	
	@Override
	public boolean canRender(BlockState primary, BlockState secondary, Facing face, int x, int y, int z) {
		return !primary.isOf(secondary.block);
	}
	
	@Override
	public boolean isFullOpaque(BlockState state, int x, int y, int z) {
		return false;
	}
	
	private BlockModel getModel(World world, int x, int y, int z) {
		return world.getBlockState(x, y+1, z).isOf(this) ? fullModel : haftModel;
	}
}
