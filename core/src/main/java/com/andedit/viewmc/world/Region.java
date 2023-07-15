package com.andedit.viewmc.world;

import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceArray;

import com.andedit.viewmc.biome.Biome;
import com.andedit.viewmc.biome.Biomes;
import com.andedit.viewmc.block.BlockState;
import com.andedit.viewmc.block.container.AirBlock;
import com.andedit.viewmc.resource.Resources;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Null;

/** Chunk Region, AKA MCA */
public class Region {
	
	public static final int SIZE = 32;

	public final World world;
	public final int x, z;
	
	private final AtomicReferenceArray<Chunk> chunks;
	private final boolean[] chunksToLoad;
	
	public Region(World world, int x, int z) {
		this.world = world;
		this.x = x;
		this.z = z;
		chunks = new AtomicReferenceArray<>(SIZE * SIZE);
		chunksToLoad = new boolean[SIZE * SIZE];
		Arrays.fill(chunksToLoad, true);
	}
	
	public void update(int chunkX, int chunkZ) {
		for (int i = 0; i < chunks.length(); i++) {
			var chunk = chunks.get(i);
			if (chunk != null && chunk.pass(chunkX, chunkZ)) {
				chunks.set(i, null);
				chunksToLoad[i] = true;
			}
		}
	}
	
	/**
	 * Fetches a block light based on a block location from this region.
	 * The coordinates represent the location of the block inside of this Region.
	 * @return The block light level.
	 */
	public int getBlockLight(int regionX, int regionY, int regionZ) {
		var chunk = getChunk(regionX>>4, regionZ>>4);
		return chunk == null ? Lights.DEFAULT_BLOCK : chunk.getBlockLight(regionX&15, regionY, regionZ&15);
	}
	
	/**
	 * Fetches a sky light based on a block location from this region.
	 * The coordinates represent the location of the block inside of this Region.
	 * @return The sky light level.
	 */
	public int getSkyLight(int regionX, int regionY, int regionZ) {
		var chunk = getChunk(regionX>>4, regionZ>>4);
		return chunk == null ? Lights.DEFAULT_SKY : chunk.getSkyLight(regionX&15, regionY, regionZ&15);
	}
	
	/**
	 * Fetches a light based on a block location from this region.
	 * The coordinates represent the location of the block inside of this Region.
	 * @return The light data.
	 */
	public int getLight(int regionX, int regionY, int regionZ) {
		var chunk = getChunk(regionX>>4, regionZ>>4);
		return chunk == null ? Lights.DEFAULT_LIGHT : chunk.getLight(regionX&15, regionY, regionZ&15);
	}
	
	/**
	 * Fetches a block state based on a block location from this region.
	 * The coordinates represent the location of the block inside of this Region.
	 * @return The block state data of this block.
	 */
	public BlockState getBlockState(int regionX, int regionY, int regionZ) {
		var chunk = getChunk(regionX>>4, regionZ>>4);
		return chunk == null ? AirBlock.INSTANCE.getState() : chunk.getBlockState(regionX&15, regionY, regionZ&15);
	}
	
	/**
	 * Fetches a Biome based on a block location from this Section.
	 * The coordinates represent the location of the block inside of this Region.
	 * @return The Biome.
	 */
	public Biome getBiome(int regionX, int regionY, int regionZ) {
		var chunk = getChunk(regionX>>4, regionZ>>4);
		return chunk == null ? Biomes.VOID : chunk.getBiome(regionX&15, regionY, regionZ&15);
	}
	
	/**
	 * Fetches a Section based on a Section location from this Region.
	 * The coordinates represent the location of the section inside of this Region.
	 * @return The Section.
	 */
	@Null
	public Section getSection(int regionX, int regionY, int regionZ) {
		var chunk = getChunk(regionX, regionZ);
		return chunk == null ? null : chunk.getSection(regionY);
	}
	
	/**
	 * Fetches a Chunk based on a Chunk location from this region.
	 * The coordinates represent the location of the section inside of this Region.
	 * @return The Chunk.
	 */
	@Null
	public Chunk getChunk(int regionX, int regionZ) {
		if (regionX < 0 || regionZ < 0 || regionX >= SIZE || regionZ >= SIZE) {
			return null;
		}
		return chunks.get(regionX + (regionZ << 5));
	}
	
	public boolean shouldLoadChunk(int regionX, int regionZ) {
		if (regionX < 0 || regionZ < 0 || regionX >= SIZE || regionZ >= SIZE) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return chunksToLoad[regionX + (regionZ << 5)];
	}
	
	public void setChunkToLoad(boolean bool, int regionX, int regionZ) {
		if (regionX < 0 || regionZ < 0 || regionX >= SIZE || regionZ >= SIZE) {
			throw new ArrayIndexOutOfBoundsException();
		}
		chunksToLoad[regionX + (regionZ << 5)] = bool;
	}
	
	void putChunk(Chunk chunk) {
		chunks.set(chunk.getIndex(), chunk);
		chunksToLoad[chunk.getIndex()] = false;
	}
	
	public boolean pass(int chunkX, int chunkZ) {
		chunkX >>= 5;
		chunkZ >>= 5;
		final int rad = (WorldRenderer.RADIUS_H + World.DELETE_CHUNK_OFFSET) >> 5;
		if (x+1 < (-rad)+chunkX
		|| z+1 < (-rad)+chunkZ 
		|| x-1 > rad+chunkX 
		|| z-1 > rad+chunkZ) {
			return true;
		}
		return false;
	}
	
	public boolean equals(int x, int z) {
		return this.x == x && this.z == z;
	}
	
	public void load(FileHandle file, Resources resources) throws Exception {
		try (var raf = new RandomAccessFile(file.file(), "r")) {
			for (int i = 0; i < 1024; i++) {
				raf.seek(i * 4);
				int offset = raf.read() << 16;
				offset |= (raf.read() & 0xFF) << 8;
				offset |= raf.read() & 0xFF;
				if (raf.readByte() == 0) {
					continue;
				}
				raf.seek(4096L * offset + 4); //+4: skip data size
				var chunk = new Chunk(raf, world, resources, (i & 31) + (x>>4), (i >> 5) + (z>>4));
				chunks.set(i, chunk);
			}
		}
	}
}
