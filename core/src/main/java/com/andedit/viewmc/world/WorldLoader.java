package com.andedit.viewmc.world;

import com.andedit.viewmc.resource.Resources;
import com.andedit.viewmc.util.LoaderTask;
import com.andedit.viewmc.util.Progress;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Null;

public class WorldLoader implements LoaderTask<World> {
	
	public final @Null World world;
	private final Progress progress = new Progress();
	private final int chunkX, chunkZ;

	public WorldLoader(Resources resources, FileHandle worldFolder) {
		World world = null;
		int chunkX = 0, chunkZ = 0;
		try {
			world = new World(resources, worldFolder);
			var player = world.level.player;
			world.startLoadingChunks(chunkX = Util.floor(player.xPos)>>4, chunkZ = Util.floor(player.zPos)>>4, progress);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.world = world;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		progress.setStatus("Loading World");
	}
	
	@Override
	public String getStatus() {
		return progress.getStatus();
	}
	
	@Override
	public float getProgress() {
		return progress.getProgress();
	}
	
	@Override
	public World call() throws Exception {
		throw new UnsupportedOperationException();
	}

	public boolean update() {
		if (world == null) return true; 
		world.updateChunk(chunkX, chunkZ);
		return world.isFinishLoading();
	}

}
