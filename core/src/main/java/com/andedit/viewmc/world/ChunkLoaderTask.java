package com.andedit.viewmc.world;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Properties;

import com.andedit.viewmc.util.Progress;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

import net.querz.mca.MCAUtil;

public class ChunkLoaderTask implements Runnable {

	private final World world;
	private final ChunkToLoad[] array;
	private final int regionX, regionZ;
	private final @Null Progress progress;
	
	public ChunkLoaderTask(World world, Array<ChunkToLoad> array, int regionX, int regionZ, @Null Progress progress) {
		this.world = world;
		this.array = array.toArray(ChunkToLoad.class);
		this.regionX = regionX;
		this.regionZ = regionZ;
		this.progress = progress;
	}
	
	@Override
	public void run() {
		var regionFile = world.worldFolder.child("region/" + MCAUtil.createNameFromRegionLocation(regionX, regionZ));
		
		int s = array.length;
		int i = 0;
		try (var raf = new RandomAccessFile(regionFile.file(), "r")) {
			for (; i < s; i++) {
				var chunkToLoad = array[i];
				if (chunkToLoad.isCancelled) continue;
				
				raf.seek(chunkToLoad.toIndex() * 4L);
				int offset = raf.read() << 16;
				offset |= (raf.read() & 0xFF) << 8;
				offset |= raf.read() & 0xFF;
				if (raf.read() <= 0) {
					chunkToLoad.isCancelled = true;
					continue;
				}
				
				raf.seek(4096L * offset + 4); //+4: skip data size
				
				var chunk = new Chunk(raf, world, world.resources, chunkToLoad.worldX, chunkToLoad.worldZ);
				if (progress != null) {
					progress.incStep();
				}
				
				if (chunk.isEmpty()) {
					chunkToLoad.isCancelled = true;
					continue;
				}
				
				//chunk.init(world);
				world.addChunk(chunk, chunkToLoad);
			}
			return;
		} catch (FileNotFoundException e) {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// cancel chunks
		for (int j = i; j < s; j++) {
			array[j].isCancelled = true;
		}
		
		if (progress != null) progress.incStep(s - i);
	}
}
