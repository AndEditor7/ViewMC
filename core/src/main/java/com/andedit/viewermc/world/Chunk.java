package com.andedit.viewermc.world;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.andedit.viewermc.block.AirBlock;
import com.andedit.viewermc.block.BlockState;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Sort;

import net.querz.mca.CompressionType;
import net.querz.mca.MCAUtil;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

/** Chunk Column */
public class Chunk {
	
	public final Region region;
	public final Array<Section> sections;
	public final byte x, z;
	public byte min;
	
	public Chunk (Region region, int x, int z) {
		this.region = region;
		this.sections = new Array<>(30);
		this.x = (byte) x;
		this.z = (byte) z;
	}
	
	/**
	 * Fetches a block light based on a block location from this chunk.
	 * The coordinates represent the location of the block inside of this Chunk.
	 * @param x The x-coordinate of the block in this Chunk
	 * @param y The y-coordinate of the block in this Chunk
	 * @param z The z-coordinate of the block in this Chunk
	 * @return The block light level.
	 */
	public int getBlockLight(int x, int y, int z) {
		var seletion = getSeletion(y);
		return seletion == null ? 0 : seletion.getBlockLight(x, y&15, z);
	}
	
	/**
	 * Fetches a sky light based on a block location from this chunk.
	 * The coordinates represent the location of the block inside of this Chunk.
	 * @param x The x-coordinate of the block in this Chunk
	 * @param y The y-coordinate of the block in this Chunk
	 * @param z The z-coordinate of the block in this Chunk
	 * @return The sky light level.
	 */
	public int getSkyLight(int x, int y, int z) {
		var seletion = getSeletion(y);
		return seletion == null ? 15 : seletion.getSkyLight(x, y&15, z);
	}
	
	/**
	 * Fetches a block state based on a block location from this chunk.
	 * The coordinates represent the location of the block inside of this Chunk.
	 * @param x The x-coordinate of the block in this Chunk
	 * @param y The y-coordinate of the block in this Chunk
	 * @param z The z-coordinate of the block in this Chunk
	 * @return The block state data of this block.
	 */
	public BlockState getBlockState(int x, int y, int z) {
		var seletion = getSeletion(y);
		return seletion == null ? AirBlock.INSTANCE.getState() : seletion.getBlockState(x, y&15, z);
	}
	
	/** zero-based 
	 * @param y The y-coordinate of the block in this Chunk*/
	@Null
	private Section getSeletion(int y) {
		y >>= 4;
		y -= min;
		return y < 0 || y >= sections.size ? null : sections.get(y);
	}
	
	@Null
	public Section getSection(int y) {
		y -= min;
		return y < 0 || y >= sections.size ? null : sections.get(y);
	}
	
	public int getWorldChunkX() {
		return MCAUtil.regionToChunk(region.x) + x;
	}
	
	public int getWorldChunkZ() {
		return MCAUtil.regionToChunk(region.z) + z;
	}

	public void load(RandomAccessFile raf) throws Exception {
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
			var seletion = new Section(this, compound);
			sections.add(seletion);
		}
		
		new Sort().sort(sections.items, 0, sections.size);
		min = sections.first().y;
	}

	
}
