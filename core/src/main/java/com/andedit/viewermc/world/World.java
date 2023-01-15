package com.andedit.viewermc.world;

import com.andedit.viewermc.block.AirBlock;
import com.andedit.viewermc.block.BlockState;
import com.andedit.viewermc.block.Blocks;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

import net.querz.mca.MCAUtil;

public class World {
	
	private final Array<Region> regions = new Array<>();
	
	public final Blocks blocks;
	
	/** World Save Folder */
	private final FileHandle folder;
	
	public World(Blocks blocks, FileHandle worldFolder) throws Exception {
		this.blocks = blocks;
		this.folder = worldFolder;
		var region = new Region(this, 0, 0);
		region.load(worldFolder.child("region/" + MCAUtil.createNameFromRegionLocation(-7, -5)));
		regions.add(region);
		System.gc();
	}
	
	/**
	 * Fetches a block light based on a block location from this world.
	 * The coordinates represent the location of the block inside of this World.
	 * @param x The x-coordinate of the block in this World
	 * @param y The y-coordinate of the block in this World
	 * @param z The z-coordinate of the block in this World
	 * @return The block light level.
	 */
	public int getBlockLight(int x, int y, int z) {
		var region = getRegion(x>>4, z>>4);
		return region == null ? 0 : region.getBlockLight(x&511, y, z&511);
	}
	
	/**
	 * Fetches a sky light based on a block location from this world.
	 * The coordinates represent the location of the block inside of this World.
	 * @param x The x-coordinate of the block in this World
	 * @param y The y-coordinate of the block in this World
	 * @param z The z-coordinate of the block in this World
	 * @return The sky light level.
	 */
	public int getSkyLight(int x, int y, int z) {
		var region = getRegion(x>>4, z>>4);
		return region == null ? 15 : region.getSkyLight(x&511, y, z&511);
	}
	
	/**
	 * Fetches a block state based on a block location from this world.
	 * The coordinates represent the location of the block inside of this World.
	 * @param x The x-coordinate of the block in this World
	 * @param y The y-coordinate of the block in this World
	 * @param z The z-coordinate of the block in this World
	 * @return The block state data of this block.
	 */
	public BlockState getBlockState(int x, int y, int z) {
		var region = getRegion(x>>9, z>>9);
		return region == null ? AirBlock.INSTANCE.getState() : region.getBlockState(x&511, y, z&511);
	}
	
	/**
	 * Fetches a block state based on a section location from this world.
	 * The coordinates represent the location of the section inside of this World.
	 * @param x The x-coordinate of the section in this World
	 * @param y The y-coordinate of the section in this World 
	 * @param z The z-coordinate of the section in this World
	 * @return The block state data of this block.
	 */
	@Null
	public Section getSection(int x, int y, int z) {
		var region = getRegion(x>>5, z>>5);
		return region == null ? null : region.getSection(x&31, y, z&31);
	}
	
	@Null
	public Chunk getChunk(int x, int z) {
		var region = getRegion(x>>5, z>>5);
		return region == null ? null : region.getChunk(x&31, z&31);
	}

	@Null
	private Region getRegion(int x, int z) {
		for (int i = 0; i < regions.size; i++) {
			var region = regions.get(i);
			if (region.equals(x, z)) return region;
		}
		return null;
	}
}
