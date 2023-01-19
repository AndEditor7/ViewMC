package com.andedit.viewermc.block.container;

import java.util.Collection;

import com.andedit.viewermc.block.BlockForm;
import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.block.BlockState;
import com.andedit.viewermc.graphic.MeshProvider;
import com.andedit.viewermc.world.World;
import com.badlogic.gdx.math.collision.BoundingBox;

public class AirBlock implements BlockForm {
	
	public static final AirBlock INSTANCE = new AirBlock();
	
	private final BlockState state = new BlockState(this);

	@Override
	public void build(World world, MeshProvider builder, BlockState state, int x, int y, int z) {

	}

	@Override
	public void getQuads(BlockState state, Collection<Quad> list, int x, int y, int z) {

	}

	@Override
	public void getBoxes(BlockState state, Collection<BoundingBox> list, int x, int y, int z) {

	}
	
	@Override
	public boolean isFullOpaque(BlockState state, int x, int y, int z) {
		return false;
	}

	@Override
	public BlockState getState() {
		return state;
	}

	@Override
	public String getId() {
		return "minecraft:air";
	}
}
