package com.andedit.viewermc.world;

import com.andedit.viewermc.biome.Biome;
import com.andedit.viewermc.biome.Biomes;
import com.andedit.viewermc.block.BlockState;
import com.andedit.viewermc.block.Blocks;
import com.andedit.viewermc.block.container.AirBlock;
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
		region.load(worldFolder.child("region/" + MCAUtil.createNameFromRegionLocation(-7, -5)), blocks);
		regions.add(region);
		System.gc();
	}
	
	public int getGrassColor(int x, int y, int z) {
		var biome = getBiome(x, y, z);
		return blocks.getGrassColor(biome.temperature, biome.downfall);
    }
	
	public int getFoliageColor(int x, int y, int z) {
		var biome = getBiome(x, y, z);
		return blocks.getFoliageColor(biome.temperature, biome.downfall);
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
		var region = getRegion(x>>9, z>>9);
		return region == null ? Lights.DEFAULT_BLOCK : region.getBlockLight(x&511, y, z&511);
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
		var region = getRegion(x>>9, z>>9);
		return region == null ? Lights.DEFAULT_SKY : region.getSkyLight(x&511, y, z&511);
	}
	
	/**
	 * Fetches a light based on a block location from this world.
	 * The coordinates represent the location of the block inside of this World.
	 * @param x The x-coordinate of the block in this World
	 * @param y The y-coordinate of the block in this World
	 * @param z The z-coordinate of the block in this World
	 * @return The light data.
	 */
	public int getLight(int x, int y, int z) {
		var region = getRegion(x>>9, z>>9);
		return region == null ? Lights.DEFAULT_LIGHT : region.getLight(x&511, y, z&511);
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
	 * Fetches a biome based on a block location from this section.
	 * The coordinates represent the location of the block inside of this Section.
	 * @param x The x-coordinate of the block in this Section
	 * @param y The y-coordinate of the block in this Section
	 * @param z The z-coordinate of the block in this Section
	 * @return The biome.
	 */
	public Biome getBiome(int x, int y, int z) {
		var region = getRegion(x>>9, z>>9);
		return region == null ? Biomes.VOID : region.getBiome(x&511, y, z&511);
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
