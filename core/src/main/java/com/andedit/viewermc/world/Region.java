package com.andedit.viewermc.world;

import java.io.RandomAccessFile;

import com.andedit.viewermc.block.AirBlock;
import com.andedit.viewermc.block.BlockState;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Null;

import net.querz.mca.MCAFile;

/** Chunk Region, AKA MCA */
public class Region {
	
	public static final int SIZE = 32;

	public final World world;
	public final int x, z;
	
	private final Chunk[] chunks;
	
	public Region(World world, int x, int z) {
		this.world = world;
		this.x = x;
		this.z = z;
		chunks = new Chunk[SIZE * SIZE];
	}
	
	/**
	 * Fetches a block light based on a block location from this region.
	 * The coordinates represent the location of the block inside of this Region.
	 * @param x The x-coordinate of the block in this Region
	 * @param y The y-coordinate of the block in this Region
	 * @param z The z-coordinate of the block in this Region
	 * @return The block light level.
	 */
	public int getBlockLight(int x, int y, int z) {
		var chunk = getChunk(x>>4, z>>4);
		return chunk == null ? 0 : chunk.getBlockLight(x&15, y, z&15);
	}
	
	/**
	 * Fetches a sky light based on a block location from this region.
	 * The coordinates represent the location of the block inside of this Region.
	 * @param x The x-coordinate of the block in this Region
	 * @param y The y-coordinate of the block in this Region
	 * @param z The z-coordinate of the block in this Region
	 * @return The sky light level.
	 */
	public int getSkyLight(int x, int y, int z) {
		var chunk = getChunk(x>>4, z>>4);
		return chunk == null ? 15 : chunk.getSkyLight(x&15, y, z&15);
	}
	
	/**
	 * Fetches a block state based on a block location from this region.
	 * The coordinates represent the location of the block inside of this Region.
	 * @param x The x-coordinate of the block in this Region
	 * @param y The y-coordinate of the block in this Region
	 * @param z The z-coordinate of the block in this Region
	 * @return The block state data of this block.
	 */
	public BlockState getBlockState(int x, int y, int z) {
		var chunk = getChunk(x>>4, z>>4);
		return chunk == null ? AirBlock.INSTANCE.getState() : chunk.getBlockState(x&15, y, z&15);
	}
	
	@Null
	public Section getSection(int x, int y, int z) {
		var chunk = getChunk(x, z);
		return chunk == null ? null : chunk.getSection(y);
	}
	
	@Null
	public Chunk getChunk(int x, int y) {
		if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) {
			return null;
		}
		return chunks[MCAFile.getChunkIndex(x, y)];
	}
	
	public boolean equals(int x, int z) {
		return this.x == x && this.z == z;
	}
	
	public void load(FileHandle file) throws Exception {
		try (var raf = new RandomAccessFile(file.file(), "r")) {
			for (int i = 0; i < 1024; i++) {
				raf.seek(i * 4);
				int offset = raf.read() << 16;
				offset |= (raf.read() & 0xFF) << 8;
				offset |= raf.read() & 0xFF;
				if (raf.readByte() == 0) {
					continue;
				}
				raf.seek(4096 * offset + 4); //+4: skip data size
				var chunk = new Chunk(this, i & 31, i >> 5);
				chunk.load(raf);
				chunks[i] = chunk;
			}
		}
	}

	
}
