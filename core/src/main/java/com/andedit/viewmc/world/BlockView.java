package com.andedit.viewmc.world;

import com.andedit.viewmc.biome.Biome;
import com.andedit.viewmc.block.BlockState;
import com.badlogic.gdx.utils.Null;

/** Represents a scoped, read-only view of blockstates, biomes and lights. */
public interface BlockView {
	/**
	 * Fetches a light based on a block location
	 * @return The light data.
	 */
	int getLight(int x, int y, int z);
	
	/**
	 * Fetches a block state based on a block location
	 * @return The block state data of this block.
	 */
	BlockState getBlockstate(int x, int y, int z);
	
	/**
	 * Fetches a biome based on a block location
	 * @return The biome.
	 */
	Biome getBiome(int x, int y, int z);
}
