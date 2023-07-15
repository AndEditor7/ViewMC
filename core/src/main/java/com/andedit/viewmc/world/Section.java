package com.andedit.viewmc.world;

import com.andedit.viewmc.biome.Biome;
import com.andedit.viewmc.block.BlockState;
import com.andedit.viewmc.resource.Resources;
import com.andedit.viewmc.util.Facing;

public abstract class Section implements BlockView {
	public static final int SIZE = 16;
	public static final int MASK = SIZE-1;

	protected final World world;
	protected final Chunk chunk;
	
	public boolean isDirty = true;
	
	int graph = -1;
	int steps = 1;
	
	public Section(Chunk chunk) {
		this.world = chunk.world;
		this.chunk = chunk;
	}
	
	protected void buildGrid() {
		new CaveCullingGraph(this).buildGraph();
	}
	
	public boolean canEnter(Facing fromChunk, Facing toChunk) {
		if (fromChunk.axis == toChunk.axis) {
			return CaveCullingGraph.getGraph(graph, toChunk.xOffset, toChunk.yOffset, toChunk.zOffset);
		} else {
			return CaveCullingGraph.getGraph(graph, fromChunk.xOffset+toChunk.xOffset, fromChunk.yOffset+toChunk.yOffset, fromChunk.zOffset+toChunk.zOffset);
		}
	}
	
	public int getWalkableSteps() {
		return steps;
	}
	
	public abstract byte getY();
	
	@Override
	public Resources getResources() {
		return world.getResources();
	}
	
	/**
	 * Fetches a block light based on a block location from this section.
	 * The coordinates represent the location of the block inside of this Section.
	 * @return The block light level.
	 */
	public abstract int getBlockLightAt(int sectionX, int sectionY, int sectionZ);
	
	/**
	 * Fetches a sky light based on a block location from this section.
	 * The coordinates represent the location of the block inside of this Section.
	 * @return The sky light level.
	 */
	public abstract int getSkyLightAt(int sectionX, int sectionY, int sectionZ);
	
	/**
	 * Fetches a light based on a block location from this section.
	 * The coordinates represent the location of the block inside of this Section.
	 * @return The light data.
	 */
	public int getLightAt(int sectionX, int sectionY, int sectionZ) {
		return (getBlockLightAt(sectionX, sectionY, sectionZ) << 4) | getSkyLightAt(sectionX, sectionY, sectionZ);
	}
	
	/**
	 * Fetches a block state based on a block location from this section.
	 * The coordinates represent the location of the block inside of this Section.
	 * @return The block state data of this block.
	 */
	public abstract BlockState getBlockstateAt(int sectionX, int sectionY, int sectionZ);
	
	/**
	 * Fetches a biome based on a block location from this section.
	 * The coordinates represent the location of the block inside of this Section.
	 * @return The biome.
	 */
	public abstract Biome getBiomeAt(int sectionX, int sectionY, int sectionZ);
	
	/**
	 * Fetches a light based on a block location from world space.
	 * The coordinates represent the location of the block in world space.
	 * @return The light data.
	 */
	@Override
	public int getLight(int worldX, int worldY, int worldZ) {
		var chunk = this.chunk;
		if (getY() != (worldY >> 4) || chunk.worldX != (worldX >> 4) || chunk.worldZ != (worldZ >> 4)) {
			return world.getLight(worldX, worldY, worldZ);
		}
		return getLightAt(worldX&15, worldY&15, worldZ&15);
	}
	
	/**
	 * Fetches a block state based on a block location from world space.
	 * The coordinates represent the location of the block in world space.
	 * @return The block state data of this block.
	 */
	@Override
	public BlockState getBlockstate(int worldX, int worldY, int worldZ) {
		var chunk = this.chunk;
		if (getY() != (worldY >> 4) || chunk.worldX != (worldX >> 4) || chunk.worldZ != (worldZ >> 4)) {
			return world.getBlockstate(worldX, worldY, worldZ);
		}
		return getBlockstateAt(worldX&15, worldY&15, worldZ&15);
	}
	
	/**
	 * Fetches a biome based on a block location from world space.
	 * The coordinates represent the location of the block in world space.
	 * @return The biome.
	 */
	@Override
	public Biome getBiome(int worldX, int worldY, int worldZ) {
		var chunk = this.chunk;
		if (getY() != (worldY >> 4) || chunk.worldX != (worldX >> 4) || chunk.worldZ != (worldZ >> 4)) {
			return world.getBiome(worldX, worldY, worldZ);
		}
		return getBiomeAt(worldX&15, worldY&15, worldZ&15);
	}
	
	protected int getPaletteIndex(int index, int bits, long[] data) {
		if (data.length == 0) return 0; 
		int indicesPerLong = 64 / bits;
		int longIndex = index / indicesPerLong;
		int startBit = (index % indicesPerLong) * bits;
		return (int)bitRange(data[longIndex], startBit, startBit + bits);
	}
	
	protected int getVoxelIndex(int x, int y, int z) {
		return y << 8 | z << 4 | x;
	}
	
	protected int getBiomeIndex(int x, int y, int z) {
		return y << 4 | z << 2 | x;
	}

	protected static long bitRange(long value, int from, int to) {
		final int waste = 64 - to;
		return (value << waste) >>> (waste + from);
	}
	
	protected static int fastNumberOfLeadingZeroes(int i) {
		int n = 25;
		i <<= 24;
		if (i >>> 28 == 0) {
			n += 4;
			i <<= 4;
		}
		if (i >>> 30 == 0) {
			n += 2;
			i <<= 2;
		}
		n -= i >>> 31;
		return n;
	}
}
