package com.andedit.viewmc.block.container;

import java.util.Collection;

import com.andedit.viewmc.block.Block;
import com.andedit.viewmc.block.BlockState;
import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.world.Section;
import com.badlogic.gdx.math.collision.BoundingBox;

public class AirBlock extends Block {
	
	public static final AirBlock INSTANCE = new AirBlock();
	
	{
		state = new BlockState(this);
		id = "minecraft:air";
	}

	@Override
	public void build(Section section, MeshProvider builder, BlockState state, int x, int y, int z) {

	}

	@Override
	public void getQuads(BlockState state, Collection<Quad> list, int x, int y, int z) {

	}

	@Override
	public void getBoxes(BlockState state, Collection<BoundingBox> list, int x, int y, int z) {

	}
	
	@Override
	public boolean isFullOpaque(BlockState state, int blockLight, int x, int y, int z) {
		return false;
	}

	@Override
	public String getId() {
		return id;
	}
}
