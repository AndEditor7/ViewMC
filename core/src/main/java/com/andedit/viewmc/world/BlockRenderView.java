package com.andedit.viewmc.world;

import com.andedit.viewmc.resource.Resources;

/** Represents a scoped, read-only view of a block content. */
public interface BlockRenderView extends BlockView {
	
	Resources getResources();
	
	default int getGrassColor(int x, int y, int z) {
		var biome = getBiome(x, y, z);
		if (biome.isColorOverride()) return biome.color;
		return getResources().getGrassColor(biome.temperature, biome.downfall);
    }
	
	default int getFoliageColor(int x, int y, int z) {
		var biome = getBiome(x, y, z);
		if (biome.isColorOverride()) return biome.color;
		return getResources().getFoliageColor(biome.temperature, biome.downfall);
    }
}
