package com.andedit.viewermc.world;

import static com.andedit.viewermc.Statics.chunkExe;

import com.andedit.viewermc.biome.Biome;
import com.andedit.viewermc.biome.Biomes;
import com.andedit.viewermc.block.BlockState;
import com.andedit.viewermc.block.Blocks;
import com.andedit.viewermc.block.container.AirBlock;
import com.andedit.viewermc.graphic.Camera;
import com.andedit.viewermc.util.AtomicArray;
import com.andedit.viewermc.util.FloodFill;
import com.andedit.viewermc.util.Pair;
import com.andedit.viewermc.util.PointNode;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.OrderedSet;
import com.badlogic.gdx.utils.Queue;

public class World {
	
	public static final int LOAD_CHUNK_OFFSET = 4;
	public static final int DELETE_CHUNK_OFFSET = 8;
	
	public final Blocks blocks;
	
	public boolean isDirty = false;
	
	/** World Save Folder */
	final FileHandle worldFolder;
	
	// TODO Make it thread-safe
	private final AtomicArray<Region> regions = new AtomicArray<>(50);
	
	
	public World(Blocks blocks, FileHandle worldFolder) throws Exception {
		this.blocks = blocks;
		this.worldFolder = worldFolder;
		//var region = new Region(this, 0, 0);
		//region.load(worldFolder.child("region/" + MCAUtil.createNameFromRegionLocation(-7, -5)), blocks);
		//regions.add(region);
		//System.gc();
	}
	
	// Chunk Loader start
	
	private final Queue<Pair<Chunk, ChunkToLoad>> chunkLoaded = new Queue<>(300);
	private final OrderedSet<ChunkToLoad> pendingChunks = new OrderedSet<>(300);
	private final Array<ChunkToLoad> pendingChunksToRemove = new Array<ChunkToLoad>();
	
	private final ChunkSorter sorter = new ChunkSorter();
	private final Queue<PointNode> queue = new Queue<>(300);
	private final GridPoint2 lastPos = new GridPoint2(Integer.MIN_VALUE, Integer.MIN_VALUE);
	
	public void startLoadingChunks(int chunkX, int chunkZ) {
		sorter.clear();
		//System.out.println("Start loading chunks!");
		
		final int size = WorldRenderer.RADIUS_H + LOAD_CHUNK_OFFSET;
		new FloodFill(queue, size*2, node -> {
			var chunk = new ChunkToLoad(chunkX+node.x(), chunkZ+node.z());
			
			if (shouldLoadChunk(chunk)) {
				setChunkToLoad(false, chunkX, chunkZ);
				pendingChunks.add(chunk);
				sorter.add(chunk);
			}
		}).run();
		
		for (var entry : sorter) {
			var point = entry.key;
			//new ChunkLoaderTask(this, entry.value, point.x(), point.z()).run();
			chunkExe.execute(new ChunkLoaderTask(this, entry.value, point.x(), point.z()));
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
			startLoadingChunks(chunkX, chunkZ);
		}
		
		pendingChunksToRemove.clear();
		for (var chunkLoaded : pendingChunks.orderedItems()) {
			if (chunkLoaded.isCancelled) {
				pendingChunksToRemove.add(chunkLoaded);
				setChunkToLoad(false, chunkX, chunkZ);
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
				
				chunk.init(this);
				putChunk(chunk);
			}
		}
		
		for (int i = 0; i < regions.size(); i++) {
			var region = regions.get(i);
			if (region.pass(chunkX, chunkZ)) {
				regions.remove(i--);
				continue;
			}
			//System.out.println("remove region: " + region.x + " " + region.z);
			region.update(chunkX, chunkZ);
		}
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
		isDirty = true;
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
	
	public int getGrassColor(int x, int y, int z) {
		var biome = getBiome(x, y, z);
		return blocks.getGrassColor(biome.temperature, biome.downfall);
    }
	
	public int getFoliageColor(int x, int y, int z) {
		var biome = getBiome(x, y, z);
		return blocks.getFoliageColor(biome.temperature, biome.downfall);
    }
	
	/**
	 * Fetches a block light based on a block location from this world.
	 * The coordinates represent the location of the block inside of this World.
	 * @param x The x-coordinate of the block in this World
	 * @param y The y-coordinate of the block in this World
	 * @param z The z-coordinate of the block in this World
	 * @return The block light level.
	 */
	public int getBlockLight(int x, int y, int z) {
		var region = getRegion(x>>9, z>>9);
		return region == null ? Lights.DEFAULT_BLOCK : region.getBlockLight(x&511, y, z&511);
	}
	
	/**
	 * Fetches a sky light based on a block location from this world.
	 * The coordinates represent the location of the block inside of this World.
	 * @param x The x-coordinate of the block in this World
	 * @param y The y-coordinate of the block in this World
	 * @param z The z-coordinate of the block in this World
	 * @return The sky light level.
	 */
	public int getSkyLight(int x, int y, int z) {
		var region = getRegion(x>>9, z>>9);
		return region == null ? Lights.DEFAULT_SKY : region.getSkyLight(x&511, y, z&511);
	}
	
	/**
	 * Fetches a light based on a block location from this world.
	 * The coordinates represent the location of the block inside of this World.
	 * @param x The x-coordinate of the block in this World
	 * @param y The y-coordinate of the block in this World
	 * @param z The z-coordinate of the block in this World
	 * @return The light data.
	 */
	public int getLight(int x, int y, int z) {
		var region = getRegion(x>>9, z>>9);
		return region == null ? Lights.DEFAULT_LIGHT : region.getLight(x&511, y, z&511);
	}
	
	/**
	 * Fetches a block state based on a block location from this world.
	 * The coordinates represent the location of the block inside of this World.
	 * @param x The x-coordinate of the block in this World
	 * @param y The y-coordinate of the block in this World
	 * @param z The z-coordinate of the block in this World
	 * @return The block state data of this block.
	 */
	public BlockState getBlockState(int x, int y, int z) {
		var region = getRegion(x>>9, z>>9);
		return region == null ? AirBlock.INSTANCE.getState() : region.getBlockState(x&511, y, z&511);
	}
	
	/**
	 * Fetches a biome based on a block location from this section.
	 * The coordinates represent the location of the block inside of this Section.
	 * @param x The x-coordinate of the block in this Section
	 * @param y The y-coordinate of the block in this Section
	 * @param z The z-coordinate of the block in this Section
	 * @return The biome.
	 */
	public Biome getBiome(int x, int y, int z) {
		var region = getRegion(x>>9, z>>9);
		return region == null ? Biomes.VOID : region.getBiome(x&511, y, z&511);
	}
	
	/**
	 * Fetches a block state based on a section location from this world.
	 * The coordinates represent the location of the section inside of this World.
	 * @param x The x-coordinate of the section in this World
	 * @param y The y-coordinate of the section in this World 
	 * @param z The z-coordinate of the section in this World
	 * @return The block state data of this block.
	 */
	@Null
	public Section getSection(int x, int y, int z) {
		var region = getRegion(x>>5, z>>5);
		return region == null ? null : region.getSection(x&31, y, z&31);
	}
	
	@Null
	public Chunk getChunk(int x, int z) {
		var region = getRegion(x>>5, z>>5);
		return region == null ? null : region.getChunk(x&31, z&31);
	}

	@Null
	private Region getRegion(int x, int z) {
		for (int i = 0; i < regions.size(); i++) {
			var region = regions.get(i);
			if (region == null) continue;
			if (region.equals(x, z)) return region;
		}
		return null;
	}
	
	private Region getOrCreateRegion(int x, int z) {
		var region = getRegion(x, z);
		if (region == null) {
			region = new Region(this, x, z);
			regions.add(region);
		}
		return region;
	}
}
