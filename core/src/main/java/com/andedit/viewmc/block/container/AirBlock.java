package com.andedit.viewmc.block.container;

import java.util.Collection;

import com.andedit.viewmc.block.Block;
import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.block.BlockState;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.world.BlockView;
import com.badlogic.gdx.math.collision.BoundingBox;

public class AirBlock extends Block {
	
	public static final AirBlock INSTANCE = new AirBlock();
	
	{
		state = new BlockState(this);
		id = "minecraft:air";
	}

	@Override
	public void build(MeshProvider builder, BlockView view, BlockState state, int x, int y, int z) {

	}

	@Override
	public void getQuads(Collection<Quad> list, BlockView view, BlockState state, int x, int y, int z) {

	}

	@Override
	public void getBoxes(Collection<BoundingBox> list, BlockView view, BlockState state, int x, int y, int z) {

	}
	
	@Override
	public boolean isFullOpaque(BlockView view, BlockState state, int x, int y, int z) {
		return false;
	}

	@Override
	public String getId() {
		return id;
	}
}
