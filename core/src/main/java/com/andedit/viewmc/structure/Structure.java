package com.andedit.viewmc.structure;

import com.andedit.viewmc.biome.Biome;
import com.andedit.viewmc.biome.Biomes;
import com.andedit.viewmc.block.BlockState;
import com.andedit.viewmc.block.container.AirBlock;
import com.andedit.viewmc.resource.Resources;
import com.andedit.viewmc.world.BlockRenderView;

import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

public class Structure implements BlockRenderView {
	
	private final short[][][] data;
	private final BlockState blocks[];
	private final Resources resources;
	
	public final int sizeX, sizeY, sizeZ;
	
	public Structure(Resources resources, NamedTag namedTag) {
		this.resources = resources;
		var compTag = (CompoundTag)namedTag.getTag();
		
		var palette = compTag.getListTag("palette").asCompoundTagList();
		var size = palette.size();
		blocks = new BlockState[size];
		for (int i = 0; i < size; i++) {
			blocks[i] = new BlockState(resources, palette.get(i));
		}
		
		var sizeList = compTag.getListTag("size").asIntTagList();
		sizeX = sizeList.get(0).asInt();
		sizeY = sizeList.get(1).asInt();
		sizeZ = sizeList.get(2).asInt();
		data = new short[sizeX][sizeY][sizeZ];
		
		for (var comp : compTag.getListTag("blocks").asCompoundTagList()) {
			var posList = comp.getListTag("pos").asIntTagList();
			int x = posList.get(0).asInt();
			int y = posList.get(1).asInt();
			int z = posList.get(2).asInt();
			data[x][y][z] = comp.getNumber("state").shortValue();
		}
	}
	
	@Override
	public Resources getResources() {
		return resources;
	}
	
	@Override
	public int getGrassColor(int x, int y, int z) {
		return 0x79C05A;
	}
	
	@Override
	public int getFoliageColor(int x, int y, int z) {
		return 0x59AE30;
	}

	@Override
	public BlockState getBlockstate(int x, int y, int z) {
		if (x < 0 || y < 0 || z < 0 || x >= sizeX || y >= sizeY || z >= sizeZ) {
			return AirBlock.INSTANCE.getState();
		}
		return blocks[data[x][y][z] & 0xFFFF];
	}

	@Override
	public Biome getBiome(int x, int y, int z) {
		return Biomes.VOID;
	}

	@Override
	public int getLight(int x, int y, int z) {
		return 0;
	}
}
