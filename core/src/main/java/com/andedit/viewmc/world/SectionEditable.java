package com.andedit.viewmc.world;

import com.andedit.viewmc.biome.Biome;
import com.andedit.viewmc.biome.Biomes;
import com.andedit.viewmc.block.BlockState;

public class SectionEditable extends Section {

	private byte y;
	
	public SectionEditable(int y) {
		this.y = (byte)y;
	}
	
	@Override
	public byte getY() {
		return y;
	}

	@Override
	public int getBlockLightAt(int sectionX, int sectionY, int sectionZ) {
		return 15;
	}

	@Override
	public int getSkyLightAt(int sectionX, int sectionY, int sectionZ) {
		return 15;
	}

	@Override
	public BlockState getBlockstateAt(int sectionX, int sectionY, int sectionZ) {
		return null;
	}

	@Override
	public Biome getBiomeAt(int sectionX, int sectionY, int sectionZ) {
		return Biomes.VOID;
	}
	
	public void setBlockstateAt(BlockState state, int sectionX, int sectionY, int sectionZ) {
		
	}

	public void packBlockstates() {
		
	}
	
	
}
