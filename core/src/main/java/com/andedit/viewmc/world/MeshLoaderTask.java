package com.andedit.viewmc.world;

import java.util.concurrent.Callable;

import com.andedit.viewmc.graphic.MeshProvider;

public class MeshLoaderTask implements Callable<Void> {

	public final MeshProvider provider;
	public final Section section;
	public final MeshToLoad mesh;
	
	public MeshLoaderTask(MeshProvider provider, MeshToLoad mesh) {
		this.provider = provider;
		this.section = mesh.section;
		this.mesh = mesh;
	}
	
	@Override
	public Void call() throws Exception {
		int xPos = mesh.chunkX<<4;
		int yPos = mesh.chunkY<<4;
		int zPos = mesh.chunkZ<<4;
		
		synchronized (provider) {
			var water = provider.resources.getWaterBlock();
			provider.clear();
			for (int x = 0; x < Section.SIZE; x++)
			for (int y = 0; y < Section.SIZE; y++)
			for (int z = 0; z < Section.SIZE; z++) {
				var state = section.getBlockstateAt(x, y, z);
				state.build(provider, section, xPos+x, yPos+y, zPos+z);
				if (state.isWaterlogged() && state.block != water) {
					water.getState().build(provider, section, xPos+x, yPos+y, zPos+z);
				}
			}
		}
		
		return null;
	}
}
