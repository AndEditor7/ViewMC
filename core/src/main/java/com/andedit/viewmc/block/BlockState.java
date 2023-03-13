package com.andedit.viewmc.block;

import java.util.Collection;

import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.resource.Resources;
import com.andedit.viewmc.util.Cull;
import com.andedit.viewmc.util.Facing;
import com.andedit.viewmc.world.Section;
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
	
	public BlockState(Resources resources, CompoundTag state) {
		this.block = resources.toBlock(state.getString("Name"));
		
		var properties = state.getCompoundTag("Properties");
		props = properties == null ? EMPTY : new ObjectMap<>(24);
		if (properties != null) 
		for (var entry : properties) {
			var tag = (StringTag)entry.getValue();
			props.put(entry.getKey(), tag.getValue());
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
	
	public boolean isFullOpque(int blockLight, int x, int y, int z) {
		return block.isFullOpaque(this, blockLight, x, y, z);
	}
	
	public boolean canRender(BlockState secondary, Quad quad, Facing face, Cull cull, int x, int y, int z) {
		return block.canRender(this, secondary, quad, face, cull, x, y, z);
	}
}
