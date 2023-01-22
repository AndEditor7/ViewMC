package com.andedit.viewermc.block;

import java.util.Collection;

import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.graphic.MeshProvider;
import com.andedit.viewermc.util.Cull;
import com.andedit.viewermc.util.Facing;
import com.andedit.viewermc.world.Section;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectMap;

import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.StringTag;

public class BlockState {
	
	private static final ObjectMap<String, String> EMPTY = new ObjectMap<String, String>(0);
	
	public final Block block;
	
	private final ObjectMap<String, String> props;
	
	public BlockState(Block block) {
		this(block, EMPTY);
	}
	
	public BlockState(Block block, ObjectMap<String, String> props) {
		this.block = block;
		this.props = props;
	}
	
	public BlockState(Blocks blocks, CompoundTag state) {
		this.block = blocks.toBlock(state.getString("Name"));
		
		var props = state.getCompoundTag("Properties");
		this.props = props == null ? EMPTY : new ObjectMap<>(24);
		if (props != null) 
		for (var entry : props) {
			var tag = (StringTag)entry.getValue();
			this.props.put(entry.getKey(), tag.getValue());
		}
	}
	
	public boolean isOf(Block block) {
		return this.block == block;
	}
	
	public boolean isWaterlogged() {
		return block.isWaterLogged(this);
	}
	
	/** Get the property value */
	@Null
	public String get(String key) {
		return props.get(key);
	}
	
	public String get(String key, String defaultValue) {
		return props.get(key, defaultValue);
	}
	
	/** Get the property value */
	public int getInt(String key) {
		return Integer.parseInt(get(key));
	}
	
	/** Contains the property key */
	public boolean contains(String key) {
		return props.containsKey(key);
	}
	
	public void build(Section section, MeshProvider builder, int x, int y, int z) {
		block.build(section, builder, this, x, y, z);
	}
	
	public void getQuads(Collection<Quad> collection, int x, int y, int z) {
		block.getQuads(this, collection, x, y, z);
	}
	
	public void getBoxes(Collection<BoundingBox> collection, int x, int y, int z) {
		block.getBoxes(this, collection, x, y, z);
	}
	
	public boolean isFullOpque(int x, int y, int z) {
		return block.isFullOpaque(this, x, y, z);
	}
	
	public boolean canRender(BlockState secondary, Quad quad, Facing face, Cull cull, int x, int y, int z) {
		return block.canRender(this, secondary, quad, face, cull, x, y, z);
	}
}
