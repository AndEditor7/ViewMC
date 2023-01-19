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
	Renderable INSTANCE = new Renderable() {
		@Override
		public void build(Section section, MeshBuilder builder, BlockState state, int x, int y, int z) {}
		@Override
		public void getQuads(BlockState state, Collection<Quad> collection, int x, int y, int z) {}
		@Override
		public void getBoxes(BlockState state, Collection<BoundingBox> collection, int x, int y, int z) {}
		@Override
		public boolean isFullOpaque(BlockState state, int x, int y, int z) { return false; }
	};
	
	void build(Section section, MeshBuilder builder, BlockState state, int x, int y, int z);
	
	void getQuads(BlockState state, Collection<Quad> collection, int x, int y, int z);
	
	void getBoxes(BlockState state, Collection<BoundingBox> collection, int x, int y, int z);
	
	/** Is full cube and opaque. Used for ambient occlusion. */
	boolean isFullOpaque(BlockState state, int x, int y, int z);
}
