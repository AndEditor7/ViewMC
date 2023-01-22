package com.andedit.viewermc.block.container;

import java.util.Collection;

import com.andedit.viewermc.block.Block;
import com.andedit.viewermc.block.BlockModel;
import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.block.BlockState;
import com.andedit.viewermc.block.TextureAtlas;
import com.andedit.viewermc.block.model.BlockModelJson;
import com.andedit.viewermc.block.state.BlockStateJson;
import com.andedit.viewermc.graphic.MeshProvider;
import com.andedit.viewermc.util.Cull;
import com.andedit.viewermc.util.Facing;
import com.andedit.viewermc.util.Identifier;
import com.andedit.viewermc.util.Facing.Axis;
import com.andedit.viewermc.world.Section;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.OrderedMap;

public class WaterBlock extends Block {
	
	protected BlockModel fullModel, haftModel;
	
	@Override
	protected void loadModel(BlockStateJson state, OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) {
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
		
		float o = 0.02f;
		var face = quad.getFace();
		for (int i = 0; i < 4; i++) {
			quad.getVert(i).sub(o*face.xOffset, o*face.yOffset, o*face.zOffset);
		}
	}
	
	protected String getTexture() {
		return "block/water_still";
	}
	
	@Override
	public void build(Section section, MeshProvider provider, BlockState state, int x, int y, int z) {
		getModel(section, x, y, z).build(section, provider, state, x, y, z);
	}

	@Override
	public void getQuads(BlockState state, Collection<Quad> list, int x, int y, int z) {
		
	}

	@Override
	public void getBoxes(BlockState state, Collection<BoundingBox> list, int x, int y, int z) {
		
	}
	
	@Override
	public boolean canRender(BlockState primary, BlockState secondary, Quad quad, @Null Facing face, Cull cull, int x, int y, int z) {
		if (secondary.isWaterlogged()) {
			return false;
		}
		
		return cull.isRenderable();
	}
	
	@Override
	public boolean isFullOpaque(BlockState state, int x, int y, int z) {
		return false;
	}
	
	@Override
	public boolean isWaterLogged(BlockState state) {
		return true;
	}
	
	protected BlockModel getModel(Section section, int x, int y, int z) {
		return section.getBlockStateAt(x, y+1, z).isWaterlogged() ? fullModel : haftModel;
	}
}
