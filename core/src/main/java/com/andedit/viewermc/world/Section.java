package com.andedit.viewermc.world;

import java.lang.reflect.Array;
import java.util.Arrays;

import com.andedit.viewermc.block.AirBlock;
import com.andedit.viewermc.block.BlockState;
import com.badlogic.gdx.utils.Null;

import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.LongArrayTag;

public class Section implements Comparable<Section> {
	public static final int SIZE = 16;
	public static final int MASK = SIZE-1;

	public final Chunk chunk;
	public final byte y;
	
	private final byte[] blockLight;
	private final byte[] skyLight;
	private final long[] blockStates;
	private final @Null BlockState[] palette;
	
	public boolean isDirty = true;
	
	public Section(Chunk chunk, CompoundTag data) {
		this.chunk = chunk;
		this.y = data.getByte("Y");
		this.blockLight = data.getByteArray("BlockLight");
		this.skyLight = data.getByteArray("SkyLight");
		
		var block_states = data.getCompoundTag("block_states");
		if (block_states != null) {
			var rawPalette = block_states.getListTag("palette");
			if (rawPalette == null) {
				palette = null;
			} else {
				palette = new BlockState[rawPalette.size()];
				var list = rawPalette.asCompoundTagList();
				var blocks = chunk.region.world.blocks;
				for (int i = 0; i < palette.length; i++) {
					palette[i] = new BlockState(blocks, list.get(i));
				}
			}
			
			blockStates = block_states.getLongArray("data");
		} else {
			blockStates = LongArrayTag.ZERO_VALUE;
			palette = null;
		}
	}
	
	/**
	 * Fetches a block light based on a block location from this section.
	 * The coordinates represent the location of the block inside of this Section.
	 * @param x The x-coordinate of the block in this Section
	 * @param y The y-coordinate of the block in this Section
	 * @param z The z-coordinate of the block in this Section
	 * @return The block light level.
	 */
	public int getBlockLight(int x, int y, int z) {
		if (blockLight.length == 0) return 0;
		final int index = getLightIndex(x, y, z);
        return blockLight[index >> 1] >> 4 * (index & 1) & 0xF;
	}
	
	/**
	 * Fetches a sky light based on a block location from this section.
	 * The coordinates represent the location of the block inside of this Section.
	 * @param x The x-coordinate of the block in this Section
	 * @param y The y-coordinate of the block in this Section
	 * @param z The z-coordinate of the block in this Section
	 * @return The sky light level.
	 */
	public int getSkyLight(int x, int y, int z) {
		if (skyLight.length == 0) return 15;
		final int index = getLightIndex(x, y, z);
        return skyLight[index >> 1] >> 4 * (index & 1) & 0xF;
	}
	
	/**
	 * Fetches a block state based on a block location from this section.
	 * The coordinates represent the location of the block inside of this Section.
	 * @param x The x-coordinate of the block in this Section
	 * @param y The y-coordinate of the block in this Section
	 * @param z The z-coordinate of the block in this Section
	 * @return The block state data of this block.
	 */
	public BlockState getBlockState(int x, int y, int z) {
		if (palette == null) return AirBlock.INSTANCE.getState(); 
		int index = getBlockIndex(x, y, z);
		int paletteIndex = getPaletteIndex(index);
		return palette[paletteIndex];
	}
	
	/**
	 * Returns the index of the block data in the palette.
	 * @param blockStateIndex The index of the block in this section, ranging from 0-4095.
	 * @return The index of the block data in the palette.
	 * */
	public int getPaletteIndex(int blockStateIndex) {
		if (blockStates.length == 0) return 0; 
		int bits = blockStates.length >> 6;
		int indicesPerLong = (int) (64D / bits);
		int blockStatesIndex = blockStateIndex / indicesPerLong;
		int startBit = (blockStateIndex % indicesPerLong) * bits;
		return (int) bitRange(blockStates[blockStatesIndex], startBit, startBit + bits);
	}
	
	public static int getBlockIndex(int x, int y, int z) {
		return (y & 0xF) * 256 + (z & 0xF) * 16 + (x & 0xF);
	}

	public static int getLightIndex(int x, int y, int z) {
		return y << 8 | z << 4 | x;
	}

	private static long bitRange(long value, int from, int to) {
		int waste = 64 - to;
		return (value << waste) >>> (waste + from);
	}

	@Override
	public int compareTo(Section s) {
		return Byte.compare(y, s.y);
	}
}
