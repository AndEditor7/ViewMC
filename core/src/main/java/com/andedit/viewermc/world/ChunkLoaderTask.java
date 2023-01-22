package com.andedit.viewermc.world;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import com.badlogic.gdx.utils.Array;

import net.querz.mca.MCAUtil;

public class ChunkLoaderTask implements Runnable {

	private final World world;
	private final Array<ChunkToLoad> array;
	private final int regionX, regionZ;
	
	public ChunkLoaderTask(World world, Array<ChunkToLoad> array, int regionX, int regionZ) {
		this.world = world;
		this.array = new Array<>(array);
		this.regionX = regionX;
		this.regionZ = regionZ;
		
	}
	
	@Override
	public void run() {
		var regionFile = world.worldFolder.child("region/" + MCAUtil.createNameFromRegionLocation(regionX, regionZ));
		
		int s = array.size;
		int i = 0;
		try (var raf = new RandomAccessFile(regionFile.file(), "r")) {
			for (; i < s; i++) {
				var chunkToLoad = array.get(i);
				if (chunkToLoad.isCancelled) continue;
				
				raf.seek(chunkToLoad.toIndex() * 4);
				int offset = raf.read() << 16;
				offset |= (raf.read() & 0xFF) << 8;
				offset |= raf.read() & 0xFF;
				if (raf.readByte() == 0) continue;
				raf.seek(4096 * offset + 4); //+4: skip data size
				
				var chunk = new Chunk(raf, world.blocks, chunkToLoad.worldX, chunkToLoad.worldZ);
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
			array.get(j).isCancelled = true;
		}
	}
}
