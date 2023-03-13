package com.andedit.viewmc.block;

import java.util.Collection;

import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.graphic.MeshBuilder;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.world.Section;
import com.andedit.viewmc.world.World;
import com.badlogic.gdx.math.collision.BoundingBox;

/** Renderable Block*/
public interface Renderable {
	void build(Section section, MeshProvider provider, BlockState state, int x, int y, int z);
	
	void getQuads(BlockState state, Collection<Quad> collection, int x, int y, int z);
	
	void getBoxes(BlockState state, Collection<BoundingBox> collection, int x, int y, int z);
	
	/** Is full cube and opaque. Used for ambient occlusion. */
	boolean isFullOpaque(BlockState state, int x, int y, int z);
}
