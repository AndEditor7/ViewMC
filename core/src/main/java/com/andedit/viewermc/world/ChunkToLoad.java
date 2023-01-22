package com.andedit.viewermc.world;

/** Chunk to load in world coordinate. */
public final class ChunkToLoad {
	
	public final int worldX, worldZ;
	public volatile boolean isCancelled;
	
	public ChunkToLoad(int worldX, int worldZ) {
		this.worldX = worldX;
		this.worldZ = worldZ;
	}
	
	public int localX() {
		return worldX & 31;
	}
	
	public int localZ() {
		return worldZ & 31;
	}
	
	public int toIndex() {
		return localX() + (localZ() << 5);
	}
	
	@Override
	public int hashCode() {
		int s = 1337;
		s ^= worldX * 0x1827F5 ^ worldZ * 0x123C21;
	    return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 24;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof ChunkToLoad chunk) {
			return chunk.worldX == worldX && chunk.worldZ == worldZ;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "ChunkToLoad("+worldX+", "+worldZ+")";
	}
}
