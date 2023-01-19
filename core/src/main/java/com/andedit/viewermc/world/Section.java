package com.andedit.viewermc.world;

import com.andedit.viewermc.biome.Biome;
import com.andedit.viewermc.biome.Biomes;
import com.andedit.viewermc.block.BlockState;
import com.andedit.viewermc.block.Blocks;
import com.andedit.viewermc.block.container.AirBlock;
import com.badlogic.gdx.utils.Null;

import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.LongArrayTag;

public class Section implements Comparable<Section> {
	public static final int SIZE = 16;
	public static final int MASK = SIZE-1;

	public final byte y;
	
	private final byte[] blockLight;
	private final byte[] skyLight;
	
	private final @Null BlockState[] blockPalette;
	private final long[] blockStates;
	private final int blockBits;
	
	private final @Null Biome[] biomePalette;
	private final long[] biomes;
	private final int biomeBits;
	
	public boolean isDirty = true;
	
	private volatile World world;
	private volatile Chunk chunk;
	
	public Section(Blocks blocks, CompoundTag data) {
		this.y = data.getByte("Y");
		this.blockLight = data.getByteArray("BlockLight");
		this.skyLight = data.getByteArray("SkyLight");
		
		var block_states = data.getCompoundTag("block_states");
		if (block_states != null) {
			var rawPalette = block_states.getListTag("palette");
			if (rawPalette == null) {
				blockPalette = null;
			} else {
				blockPalette = new BlockState[rawPalette.size()];
				var list = rawPalette.asCompoundTagList();
				for (int i = 0; i < blockPalette.length; i++) {
					blockPalette[i] = new BlockState(blocks, list.get(i));
				}
			}
			
			blockStates = block_states.getLongArray("data");
			blockBits = blockStates.length >> 6;
		} else {
			blockStates = LongArrayTag.ZERO_VALUE;
			blockPalette = null;
			blockBits = 0;
		}
		
		var biomes = data.getCompoundTag("biomes");
		if (biomes != null) {
			var rawPalette = biomes.getListTag("palette");
			if (rawPalette == null) {
				biomePalette = null;
			} else {
				biomePalette = new Biome[rawPalette.size()];
				var list = rawPalette.asStringTagList();
				for (int i = 0; i < biomePalette.length; i++) {
					biomePalette[i] = Biomes.toBiome(list.get(i).getValue());
				}
			}
			
			this.biomes = biomes.getLongArray("data");
			biomeBits = 32 - fastNumberOfLeadingZeroes(Math.max(this.biomes.length-1, 1));
		} else {
			this.biomes = LongArrayTag.ZERO_VALUE;
			biomePalette = null;
			biomeBits = 0;
		}
	}
	
	public void init(World world, Chunk chunk) {
		this.world = world;
		this.chunk = chunk;
	}
	
	public World getWorld() {
		return world;
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
		if (blockLight.length == 0) return Lights.DEFAULT_BLOCK;
		final int index = getBlockIndex(x, y, z);
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
		if (skyLight.length == 0) return Lights.DEFAULT_SKY;
		final int index = getBlockIndex(x, y, z);
        return skyLight[index >> 1] >> 4 * (index & 1) & 0xF;
	}
	
	/**
	 * Fetches a light based on a block location from this section.
	 * The coordinates represent the location of the block inside of this Section.
	 * @param x The x-coordinate of the block in this Section
	 * @param y The y-coordinate of the block in this Section
	 * @param z The z-coordinate of the block in this Section
	 * @return The light data.
	 */
	public int getLight(int x, int y, int z) {
		return (getBlockLight(x, y, z) << 4) | getSkyLight(x, y, z);
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
		if (blockPalette == null) return AirBlock.INSTANCE.getState(); 
		return blockPalette[getPaletteIndex(getBlockIndex(x, y, z), blockBits, blockStates)];
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
		if (biomePalette == null) return Biomes.VOID; 
		return biomePalette[getPaletteIndex(getBiomeIndex(x>>2, y>>2, z>>2), biomeBits, biomes)];
	}
	
	/**
	 * Fetches a light based on a block location from world space.
	 * The coordinates represent the location of the block inside of world space.
	 * @param x The x-coordinate of the block in this world space
	 * @param y The y-coordinate of the block in this world space
	 * @param z The z-coordinate of the block in this world space
	 * @return The light data.
	 */
	public int getLightAt(int x, int y, int z) {
		var chunk = this.chunk;
		if (this.y != (y >> 4) || chunk.worldX != (x >> 4) || chunk.worldZ != (z >> 4)) {
			return world.getLight(x, y, z);
		}
		return getLight(x&15, y&15, z&15);
	}
	
	/**
	 * Fetches a block state based on a block location from world space.
	 * The coordinates represent the location of the block inside of world space.
	 * @param x The x-coordinate of the block in world space
	 * @param y The y-coordinate of the block in world space
	 * @param z The z-coordinate of the block in world space
	 * @return The block state data of this block.
	 */
	public BlockState getBlockStateAt(int x, int y, int z) {
		if (blockPalette == null) return AirBlock.INSTANCE.getState(); 
		var chunk = this.chunk;
		if (this.y != (y >> 4) || chunk.worldX != (x >> 4) || chunk.worldZ != (z >> 4)) {
			return world.getBlockState(x, y, z);
		}
		return getBlockState(x&15, y&15, z&15);
	}
	
	/**
	 * Fetches a biome based on a block location from world space.
	 * The coordinates represent the location of the block inside of world space.
	 * @param x The x-coordinate of the block in this world space
	 * @param y The y-coordinate of the block in this world space
	 * @param z The z-coordinate of the block in this world space
	 * @return The biome.
	 */
	public Biome getBiomeAt(int x, int y, int z) {
		if (biomePalette == null) return Biomes.VOID;
		var chunk = this.chunk;
		if (this.y != (y >> 4) || chunk.worldX != (x >> 4) || chunk.worldZ != (z >> 4)) {
			return world.getBiome(x, y, z);
		}
		return getBiome(x&15, y&15, z&15);
	}
	
	private static int getPaletteIndex(int index, int bits, long[] data) {
		if (data.length == 0) return 0; 
		int indicesPerLong = 64 / bits;
		int longIndex = index / indicesPerLong;
		int startBit = (index % indicesPerLong) * bits;
		return (int)bitRange(data[longIndex], startBit, startBit + bits);
	}
	
	private static int getBlockIndex(int x, int y, int z) {
		return y << 8 | z << 4 | x;
	}
	
	private static int getBiomeIndex(int x, int y, int z) {
		return y << 4 | z << 2 | x;
	}

	private static long bitRange(long value, int from, int to) {
		final int waste = 64 - to;
		return (value << waste) >>> (waste + from);
	}
	
	private static int fastNumberOfLeadingZeroes(int i) {
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

	@Override
	public int compareTo(Section s) {
		return Byte.compare(y, s.y);
	}
}
