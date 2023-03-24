package com.andedit.viewmc.world;

import com.andedit.viewmc.biome.Biome;
import com.andedit.viewmc.biome.Biomes;
import com.andedit.viewmc.block.BlockState;
import com.andedit.viewmc.block.container.AirBlock;
import com.andedit.viewmc.resource.Resources;
import com.badlogic.gdx.utils.Null;

import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.LongArrayTag;

public class Section19 extends Section {
	
	private final byte y;
	
	private final byte[] blockLight;
	private final byte[] skyLight;
	
	private final @Null BlockState[] blockPalette;
	private final long[] blockStates;
	private final int blockBits;
	
	private final @Null Biome[] biomePalette;
	private final long[] biomes;
	private final int biomeBits;
	
	public Section19(Resources resources, CompoundTag data) {
		this.y = data.getNumber("Y").byteValue();
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
					blockPalette[i] = new BlockState(resources, list.get(i));
				}
			}
			
			blockStates = block_states.getLongArray("data");
			blockBits = blockStates.length >> 6;
		} else {
			blockStates = LongArrayTag.ZERO_VALUE;
			blockPalette = null;
			blockBits = 0;
			isDirty = false;
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
		
		if (blockPalette != null) {
			if (blockPalette.length == 1) {
				graph = blockPalette[0].isFullOpaque(this, 0, 0, 0) ? 0 : -1;
			} else {
				buildGrid(); // TODO optimize it
			}
			if (graph != -1 && skyLight.length == 0) {
				steps = 2;
			}
		}
	}
	
	@Override
	public byte getY() {
		return y;
	}
	
	@Override
	public int getBlockLightAt(int x, int y, int z) {
		if (blockLight.length == 0) return Lights.DEFAULT_BLOCK;
		final int index = getBlockIndex(x, y, z);
        return blockLight[index >> 1] >> 4 * (index & 1) & 0xF;
	}
	
	@Override
	public int getSkyLightAt(int x, int y, int z) {
		if (skyLight.length == 0) return Lights.DEFAULT_SKY;
		final int index = getBlockIndex(x, y, z);
        return skyLight[index >> 1] >> 4 * (index & 1) & 0xF;
	}
	
	@Override
	public BlockState getBlockstateAt(int x, int y, int z) {
		if (blockPalette == null) return AirBlock.INSTANCE.getState(); 
		return blockPalette[getPaletteIndex(getBlockIndex(x, y, z), blockBits, blockStates)];
	}
	
	@Override
	public Biome getBiomeAt(int x, int y, int z) {
		if (biomePalette == null) return Biomes.VOID; 
		return biomePalette[getPaletteIndex(getBiomeIndex(x>>2, y>>2, z>>2), biomeBits, biomes)];
	}
}
