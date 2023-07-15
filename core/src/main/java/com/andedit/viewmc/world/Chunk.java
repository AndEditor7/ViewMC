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
	
	protected final World world;
	
	public Chunk(RandomAccessFile raf, World world, Resources resources, int worldX, int worldZ) throws IOException {
		this.world = world;
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
			var section = new Section19(this, resources, compound);
			sections.add(section);
		}
		
		//new Sort().sort(sections.items, 0, sections.size);
		min = sections.first().getY();
	}
	
	public boolean canBuild() {
		return world.getChunk(worldX+1, worldZ) != null && world.getChunk(worldX-1, worldZ) != null && world.getChunk(worldX, worldZ+1) != null && world.getChunk(worldX, worldZ-1) != null;
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
	public int getBlockLight(int chunkX, int chunkY, int chunkZ) {
		var section = getSectionIn(chunkY);
		return section == null ? Lights.DEFAULT_BLOCK : section.getBlockLightAt(chunkX, chunkY&15, chunkZ);
	}
	
	/**
	 * Fetches a sky light based on a block location from this chunk.
	 * The coordinates represent the location of the block inside of this Chunk.
	 * @return The sky light level.
	 */
	public int getSkyLight(int chunkX, int chunkY, int chunkZ) {
		var section = getSectionIn(chunkY);
		return section == null ? Lights.DEFAULT_SKY : section.getSkyLightAt(chunkX, chunkY&15, chunkZ);
	}
	
	/**
	 * Fetches a light based on a block location from this chunk.
	 * The coordinates represent the location of the block inside of this Chunk.
	 * @return The light data.
	 */
	public int getLight(int chunkX, int chunkY, int chunkZ) {
		var section = getSectionIn(chunkY);
		return section == null ? Lights.DEFAULT_LIGHT : section.getLightAt(chunkX, chunkY&15, chunkZ);
	}
	
	/**
	 * Fetches a block state based on a block location from this chunk.
	 * The coordinates represent the location of the block inside of this Chunk.
	 * @return The block state data of this block.
	 */
	public BlockState getBlockState(int chunkX, int chunkY, int chunkZ) {
		var section = getSectionIn(chunkY);
		return section == null ? AirBlock.INSTANCE.getState() : section.getBlockstateAt(chunkX, chunkY&15, chunkZ);
	}
	
	/**
	 * Fetches a biome based on a block location from this section.
	 * The coordinates represent the location of the block inside of this Section.
	 * @return The biome.
	 */
	public Biome getBiome(int chunkX, int chunkY, int chunkZ) {
		var section = getSectionIn(chunkY);
		return section == null ? Biomes.VOID : section.getBiomeAt(chunkX, chunkY&15, chunkZ);
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
