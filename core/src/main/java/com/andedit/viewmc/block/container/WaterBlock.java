package com.andedit.viewmc.block.container;

import java.util.Collection;

import com.andedit.viewmc.block.Block;
import com.andedit.viewmc.block.BlockModel;
import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.block.BlockState;
import com.andedit.viewmc.block.TextureAtlas;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.resource.blockmodel.BlockModelJson;
import com.andedit.viewmc.resource.blockstate.BlockStateJson;
import com.andedit.viewmc.util.Cull;
import com.andedit.viewmc.util.Facing;
import com.andedit.viewmc.util.Identifier;
import com.andedit.viewmc.world.BlockView;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.OrderedMap;

public class WaterBlock extends Block {
	
	protected BlockModel fullModel, haftModel;
	
	@Override
	public void loadModel(BlockStateJson state, OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
		var sprite = textures.getSprite(new Identifier(getTexture()));
		fullModel = newBlockModel();
		var cube = fullModel.cube(0, 0, 0, 16, 16, 16);
		cube.regAll(sprite);
		cube.forEach(this::quad);
		
		haftModel = newBlockModel();
		cube = haftModel.cube(0, 0, 0, 16, 14, 16);
		cube.regAll(sprite);
		cube.forEach(this::quad);
	}
	
	protected BlockModel newBlockModel() {
		return new BlockModel();
	}
	
	protected void quad(Quad quad) {
		quad.allowRender = true;
		quad.tintIndex = 0;
		
		float o = 0.00f;
		var face = quad.getFace();
		for (int i = 0; i < 4; i++) {
			quad.getVert(i).sub(o*face.xOffset, o*face.yOffset, o*face.zOffset);
		}
	}
	
	protected String getTexture() {
		return "block/water_still";
	}
	
	@Override
	public void build(MeshProvider provider, BlockView view, BlockState state, int x, int y, int z) {
		getModel(view, x, y, z).build(provider, view, state, x, y, z);
	}

	@Override
	public void getQuads(Collection<Quad> list, BlockView view, BlockState state, int x, int y, int z) {
		
	}

	@Override
	public void getBoxes(Collection<BoundingBox> list, BlockView view, BlockState state, int x, int y, int z) {
		
	}
	
	@Override
	public boolean canRender(BlockState primary, BlockState secondary, Quad quad, @Null Facing face, Cull cull, int x, int y, int z) {
		if (secondary.isWaterlogged()) {
			return false;
		}
		
		return cull.isRenderable();
	}
	
	@Override
	public boolean isFullOpaque(BlockView view, BlockState state, int x, int y, int z) {
		return false;
	}
	
	@Override
	public boolean isWaterLogged(BlockState state) {
		return true;
	}
	
	protected BlockModel getModel(BlockView view, int x, int y, int z) {
		return view.getBlockstate(x, y+1, z).isWaterlogged() ? fullModel : haftModel;
	}
}
