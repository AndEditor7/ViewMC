package com.andedit.viewermc.block.container;

import java.util.Collection;

import com.andedit.viewermc.block.Block;
import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.block.BlockState;
import com.andedit.viewermc.graphic.MeshProvider;
import com.andedit.viewermc.world.Section;
import com.badlogic.gdx.math.collision.BoundingBox;

public class AirBlock extends Block {
	
	public static final AirBlock INSTANCE = new AirBlock();
	
	{
		state = new BlockState(this);
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
	public boolean isFullOpaque(BlockState state, int x, int y, int z) {
		return false;
	}

	@Override
	public String getId() {
		return "minecraft:air";
	}
}
