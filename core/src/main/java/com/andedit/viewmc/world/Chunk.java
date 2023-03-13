package com.andedit.viewmc.world;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.andedit.viewmc.biome.Biome;
import com.andedit.viewmc.biome.Biomes;
import com.andedit.viewmc.block.BlockState;
import com.andedit.viewmc.block.container.AirBlock;
import com.andedit.viewmc.resource.Resources;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

import net.querz.mca.CompressionType;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

/** Chunk Column */
public class Chunk {
	
	public final Array<Section> sections;
	/** chunk in world coordinate. */
	public final int worldX, worldZ;
	public byte min;
	
	private volatile World world;
	
	public Chunk(RandomAccessFile raf, Resources resources, int worldX, int worldZ) throws IOException {
		this.sections = new Array<>(30);
		this.worldX = worldX;
		this.worldZ = worldZ;
		
		byte compressionTypeByte = raf.readByte();
		CompressionType compressionType = CompressionType.getFromID(compressionTypeByte);
		if (compressionType == null) {
			throw new IOException("invalid compression type " + compressionTypeByte);
		}
		BufferedInputStream dis = new BufferedInputStream(compressionType.decompress(new FileInputStream(raf.getFD())));
		NamedTag tag = new NBTDeserializer(false).fromStream(dis);
		
		final CompoundTag data;
		if (tag != null && tag.getTag() instanceof CompoundTag) {
			data = (CompoundTag)tag.getTag();
		} else {
			throw new IOException("invalid data tag: " + (tag == null ? "null" : tag.getClass().getName()));
		}
		
		var list = data.getListTag("sections");
		if (list == null) return; 
		for (var compound : list.asCompoundTagList()) {
			var seletion = new Section(resources, compound);
			sections.add(seletion);
		}
		
		//new Sort().sort(sections.items, 0, sections.size);
		min = sections.first().y;
	}
	
	public boolean canBuild() {
		var world = this.world;
		return world.getChunk(worldX+1, worldZ) != null && world.getChunk(worldX-1, worldZ) != null && world.getChunk(worldX, worldZ+1) != null && world.getChunk(worldX, worldZ-1) != null;
	}
	
	public void init(World world) {
		this.world = world;
		for (var section : sections) {
			section.init(world, this);
		}
	}
	
	public boolean isEmpty() {
		return sections.isEmpty();
	}
	
	public int localX() {
		return worldX & 31;
	}
	
	public int localZ() {
		return worldZ & 31;
	}
	
	public int getIndex() {
		return localX() + (localZ() << 5);
	}
	
	/**
	 * Fetches a block light based on a block location from this chunk.
	 * The coordinates represent the location of the block inside of this Chunk.
	 * @return The block light level.
	 */
	public int getBlockLight(int x, int y, int z) {
		var seletion = getSectionIn(y);
		return seletion == null ? Lights.DEFAULT_BLOCK : seletion.getBlockLight(x, y&15, z);
	}
	
	/**
	 * Fetches a sky light based on a block location from this chunk.
	 * The coordinates represent the location of the block inside of this Chunk.
	 * @return The sky light level.
	 */
	public int getSkyLight(int x, int y, int z) {
		var seletion = getSectionIn(y);
		return seletion == null ? Lights.DEFAULT_SKY : seletion.getSkyLight(x, y&15, z);
	}
	
	/**
	 * Fetches a light based on a block location from this chunk.
	 * The coordinates represent the location of the block inside of this Chunk.
	 * @return The light data.
	 */
	public int getLight(int x, int y, int z) {
		var seletion = getSectionIn(y);
		return seletion == null ? Lights.DEFAULT_LIGHT : seletion.getLight(x, y&15, z);
	}
	
	/**
	 * Fetches a block state based on a block location from this chunk.
	 * The coordinates represent the location of the block inside of this Chunk.
	 * @return The block state data of this block.
	 */
	public BlockState getBlockState(int x, int y, int z) {
		var seletion = getSectionIn(y);
		return seletion == null ? AirBlock.INSTANCE.getState() : seletion.getBlockState(x, y&15, z);
	}
	
	/**
	 * Fetches a biome based on a block location from this section.
	 * The coordinates represent the location of the block inside of this Section.
	 * @return The biome.
	 */
	public Biome getBiome(int x, int y, int z) {
		var seletion = getSectionIn(y);
		return seletion == null ? Biomes.VOID : seletion.getBiome(x, y&15, z);
	}
	
	/** zero-based 
	 * @param y The y-coordinate of the block in this Chunk*/
	@Null
	private Section getSectionIn(int y) {
		y >>= 4;
		y -= min;
		return y < 0 || y >= sections.size ? null : sections.get(y);
	}
	
	@Null
	public Section getSection(int y) {
		y -= min;
		return y < 0 || y >= sections.size ? null : sections.get(y);
	}

	public boolean pass(int chunkX, int chunkZ) {
		final int rad = WorldRenderer.RADIUS_H + World.DELETE_CHUNK_OFFSET;
		return worldX <= (-rad)+chunkX|| worldZ <= (-rad)+chunkZ || worldX > rad+chunkX || worldZ > rad+chunkZ;
	}
}