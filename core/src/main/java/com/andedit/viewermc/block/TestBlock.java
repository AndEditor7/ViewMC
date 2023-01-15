package com.andedit.viewermc.block;

import java.util.Collection;

import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.graphic.MeshBuilder;
import com.andedit.viewermc.util.Identifier;
import com.andedit.viewermc.world.World;
import com.badlogic.gdx.math.collision.BoundingBox;

public class TestBlock implements BlockForm {
	
	private final BlockState state = new BlockState(this);
	private final BlockModel model;

	public TestBlock(TextureAtlas textures) {
		model = new BlockModel();
		model.cube(0, 0, 0, 16, 16, 16).regAll(textures.getRegion(new Identifier("block/stone")));
	}
	
	@Override
	public void build(World world, MeshBuilder builder, BlockState state, int x, int y, int z) {
		model.build(world, builder, x, y, z);
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
}
