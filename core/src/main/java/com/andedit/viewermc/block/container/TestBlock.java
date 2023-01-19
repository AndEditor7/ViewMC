package com.andedit.viewermc.block.container;

import java.util.Collection;

import com.andedit.viewermc.block.BlockForm;
import com.andedit.viewermc.block.BlockModel;
import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.block.BlockState;
import com.andedit.viewermc.block.TextureAtlas;
import com.andedit.viewermc.graphic.MeshProvider;
import com.andedit.viewermc.graphic.RenderLayer;
import com.andedit.viewermc.util.Identifier;
import com.andedit.viewermc.world.Section;
import com.badlogic.gdx.math.collision.BoundingBox;

public class TestBlock implements BlockForm {
	
	private final BlockState state = new BlockState(this);
	private final BlockModel model;

	public TestBlock(TextureAtlas textures) {
		model = new BlockModel();
		model.cube(0, 0, 0, 16, 16, 16).regAll(textures.getRegion(new Identifier("block/stone")));
	}
	
	@Override
	public void build(Section section, MeshProvider provider, BlockState state, int x, int y, int z) {
		model.build(section, provider.getBuilder(RenderLayer.SOILD), state, x, y, z);
	}

	@Override
	public void getQuads(BlockState state, Collection<Quad> list, int x, int y, int z) {
		model.getQuads(list);
	}

	@Override
	public void getBoxes(BlockState state, Collection<BoundingBox> list, int x, int y, int z) {
		model.getBoxes(list);
	}
	
	@Override
	public boolean isFullOpaque(BlockState state, int x, int y, int z) {
		return model.isFullOpaque();
	}

	@Override
	public BlockState getState() {
		return state;
	}
	
	@Override
	public String getId() {
		return "unqiue:test_block";
	}
}
