package com.andedit.viewermc.block;

import java.util.Collection;

import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.graphic.MeshBuilder;
import com.andedit.viewermc.graphic.MeshProvider;
import com.andedit.viewermc.world.Section;
import com.andedit.viewermc.world.World;
import com.badlogic.gdx.math.collision.BoundingBox;

/** Renderable Block*/
public interface Renderable {
	void build(Section section, MeshProvider provider, BlockState state, int x, int y, int z);
	
	void getQuads(BlockState state, Collection<Quad> collection, int x, int y, int z);
	
	void getBoxes(BlockState state, Collection<BoundingBox> collection, int x, int y, int z);
	
	/** Is full cube and opaque. Used for ambient occlusion. */
	boolean isFullOpaque(BlockState state, int x, int y, int z);
}
