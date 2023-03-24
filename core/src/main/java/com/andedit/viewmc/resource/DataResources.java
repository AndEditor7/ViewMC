package com.andedit.viewmc.resource;

import com.andedit.viewmc.block.Block;
import com.andedit.viewmc.util.Identifier;

public interface DataResources {
	
	default Block getBlock(Identifier id) {
		return getBlock(id.full);
	}
	
	Block getBlock(String id);
	
	int getGrassColor(float temperature, float humidity);
	
	int getFoliageColor(float temperature, float humidity);
}
