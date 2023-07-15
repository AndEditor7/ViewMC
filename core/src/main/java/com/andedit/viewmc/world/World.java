package com.andedit.viewmc.world;

import static com.andedit.viewmc.Statics.chunkExe;

import com.andedit.viewmc.biome.Biome;
import com.andedit.viewmc.biome.Biomes;
import com.andedit.viewmc.block.BlockState;
import com.andedit.viewmc.block.container.AirBlock;
import com.andedit.viewmc.graphic.Camera;
import com.andedit.viewmc.resource.Resources;
import com.andedit.viewmc.util.AtomicArray;
import com.andedit.viewmc.util.FloodFill2D;
import com.andedit.viewmc.util.Pair;
import com.andedit.viewmc.util.PointNode2D;
import com.andedit.viewmc.util.Progress;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.OrderedSet;
import com.badlogic.gdx.utils.Queue;

import net.querz.nbt.io.NBTUtil;

public class World implements WorldView {
	
	public static final int LOAD_CHUNK_OFFSET = 4;
	public static final int DELETE_CHUNK_OFFSET = 8;
	
	public final Resources resources;
	
	public final LevelDat level;
	
	/** World Save Folder */
	final FileHandle worldFolder;
	
	// TODO Make it thread-safe
	private final AtomicArray<Region> regions = new AtomicArray<>(50);
	
	public World(Resources resources, FileHandle worldFolder) throws Exception {
		this.resources = resources;
		this.worldFolder = worldFolder;
		this.level = new LevelDat(NBTUtil.read(worldFolder.child("level.dat").file()));
	}
	
	// Chunk Loader start
	
	private final Queue<Pair<Chunk, ChunkToLoad>> chunkLoaded = new Queue<>(300);
	private final OrderedSet<ChunkToLoad> pendingChunks = new OrderedSet<>(300);
	private final Array<ChunkToLoad> pendingChunksToRemove = new Array<ChunkToLoad>();
	
	private final ChunkSorter sorter = new ChunkSorter();
	private final Queue<PointNode2D> queue = new Queue<>(300);
	private final GridPoint2 lastPos = new GridPoint2(Integer.MIN_VALUE, Integer.MIN_VALUE);
	
	public void startLoadingChunks(int chunkX, int chunkZ, @Null Progress progress) {
		sorter.clear();
		
		final int size = WorldRenderer.RADIUS_H + LOAD_CHUNK_OFFSET;
		new FloodFill2D(queue, size*2, node -> {
			var chunk = new ChunkToLoad(chunkX+node.x(), chunkZ+node.z());
			
			if (shouldLoadChunk(chunk)) {
				setChunkToLoad(false, chunkX, chunkZ);
				pendingChunks.add(chunk);
				sorter.add(chunk);
			}
		}).run();
		
		if (progress != null) {
			progress.newProgess(1);
			progress.newStep(pendingChunks.size);
		}
		
		for (var entry : sorter) {
			var point = entry.key;
			//new ChunkLoaderTask(this, entry.value, point.x(), point.z()).run();
			chunkExe.execute(new ChunkLoaderTask(this, entry.value, point.x(), point.z(), progress));
		}
	}
	
	public void update(Camera camera) {
		final var camPos = camera.position;
		final int chunkX = camPos.floorX()>>4;
		final int chunkZ = camPos.floorZ()>>4;
		
		int rad = LOAD_CHUNK_OFFSET;
		if (lastPos.x < (-rad)+chunkX 
		 || lastPos.y < (-rad)+chunkZ 
		 || lastPos.x > rad+chunkX 
		 || lastPos.y > rad+chunkZ) {
			lastPos.set(chunkX, chunkZ);
			startLoadingChunks(chunkX, chunkZ, null);
		}
		
		updateChunk(chunkX, chunkZ);
		
		for (int i = 0; i < regions.size(); i++) {
			var region = regions.get(i);
			if (region.pass(chunkX, chunkZ)) {
				regions.remove(i--);
				continue;
			}
			region.update(chunkX, chunkZ);
		}
	}
	
	public void updateChunk(int chunkX, int chunkZ) {
		pendingChunksToRemove.clear();
		for (var chunkLoaded : pendingChunks.orderedItems()) {
			if (chunkLoaded.isCancelled) {
				pendingChunksToRemove.add(chunkLoaded);
				setChunkToLoad(false, chunkLoaded.worldX, chunkLoaded.worldZ);
			}
		}
		pendingChunksToRemove.forEach(pendingChunks::remove);
		
		synchronized (chunkLoaded) {
			while (chunkLoaded.notEmpty()) {
				Pair<Chunk, ChunkToLoad> pair = chunkLoaded.removeFirst();
				var chunkToLoad = pair.right;
				pendingChunks.remove(chunkToLoad);
				
				var chunk = pair.left;
				if (chunk.pass(chunkX, chunkZ)) {
					continue;
				}

				putChunk(chunk);
			}
		}
	}
	
	public boolean isFinishLoading() {
		return pendingChunks.isEmpty();
	}
	
	/** region will be created if return true. */
	private boolean shouldLoadChunk(ChunkToLoad chunkToLoad) {
		if (pendingChunks.contains(chunkToLoad)) {
			return false;
		}
		
		return shouldLoadChunk(chunkToLoad.worldX, chunkToLoad.worldZ);
	}
	
	private void putChunk(Chunk chunk) {
		getOrCreateRegion(chunk.worldX>>5, chunk.worldZ>>5).putChunk(chunk);
	}
	
	private void setChunkToLoad(boolean bool, int chunkX, int chunkZ) {
		var region = getRegion(chunkX>>5, chunkZ>>5);
		if (region != null) {
			region.setChunkToLoad(bool, chunkX&31, chunkZ&31);
		}
	}
	
	void addChunk(Chunk chunk, ChunkToLoad chunkToLoad) {
		synchronized (chunkLoaded) {
			chunkLoaded.addLast(new Pair<>(chunk, chunkToLoad));
		}
	}
	
	public boolean shouldLoadChunk(int chunkX, int chunkZ) {
		var region = getOrCreateRegion(chunkX>>5, chunkZ>>5);
		return region.shouldLoadChunk(chunkX&31, chunkZ&31);
	}
	
	// Chunk Loader end
	
	/**
	 * Fetches a light based on a block location from this world.
	 * The coordinates represent the location of the block inside of this World.
	 * @return The light data.
	 */
	@Override
	public int getLight(int worldX, int worldY, int worldZ) {
		var region = getRegion(worldX>>9, worldZ>>9);
		return region == null ? Lights.DEFAULT_LIGHT : region.getLight(worldX&511, worldY, worldZ&511);
	}
	
	/**
	 * Fetches a block state based on a block location from this world.
	 * The coordinates represent the location of the block inside of this World.
	 * @return The block state data of this block.
	 */
	@Override
	public BlockState getBlockstate(int worldX, int worldY, int worldZ) {
		var region = getRegion(worldX>>9, worldZ>>9);
		return region == null ? AirBlock.INSTANCE.getState() : region.getBlockState(worldX&511, worldY, worldZ&511);
	}
	
	/**
	 * Fetches a biome based on a block location from this section.
	 * The coordinates represent the location of the block inside of this World.
	 * @return The biome.
	 */
	@Override
	public Biome getBiome(int worldX, int worldY, int worldZ) {
		var region = getRegion(worldX>>9, worldZ>>9);
		return region == null ? Biomes.VOID : region.getBiome(worldX&511, worldY, worldZ&511);
	}
	
	/**
	 * Fetches a block state based on a section location from this world.
	 * The coordinates represent the location of the section inside of this World.
	 * @return The block state data of this block.
	 */
	@Null
	public Section getSection(int worldX, int worldY, int worldZ) {
		var region = getRegion(worldX>>5, worldZ>>5);
		return region == null ? null : region.getSection(worldX&31, worldY, worldZ&31);
	}
	
	@Null
	public Chunk getChunk(int worldX, int worldZ) {
		var region = getRegion(worldX>>5, worldZ>>5);
		return region == null ? null : region.getChunk(worldX&31, worldZ&31);
	}

	@Null
	private Region getRegion(int worldX, int worldZ) {
		for (int i = 0; i < regions.size(); i++) {
			var region = regions.get(i);
			if (region == null) continue;
			if (region.equals(worldX, worldZ)) return region;
		}
		return null;
	}
	
	private Region getOrCreateRegion(int worldX, int worldZ) {
		var region = getRegion(worldX, worldZ);
		if (region == null) {
			region = new Region(this, worldX, worldZ);
			regions.add(region);
		}
		return region;
	}

	@Override
	public Resources getResources() {
		return resources;
	}
}
