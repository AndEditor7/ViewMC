package com.andedit.viewermc.block;

import java.util.Collection;

import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.graphic.MeshProvider;
import com.andedit.viewermc.util.Facing;
import com.andedit.viewermc.world.Section;
import com.andedit.viewermc.world.World;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Null;

public interface BlockForm {
	
	void build(Section section, MeshProvider builder, BlockState state, int x, int y, int z);
	
	void getQuads(BlockState state, Collection<Quad> collection, int x, int y, int z);
	
	void getBoxes(BlockState state, Collection<BoundingBox> collection, int x, int y, int z);
	
	/** Is full cube and opaque. Used for ambient occlusion. */
	boolean isFullOpaque(BlockState state, int x, int y, int z);
	
	default boolean canRender(BlockState primary, BlockState secondary, Facing face, int x, int y, int z) {
		return true;
	}
	
	/** Get default blockstate */
	BlockState getState();
	
	String getId();
}
