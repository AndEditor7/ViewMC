package com.andedit.viewermc.world;

import com.andedit.viewermc.graphic.Camera;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;

public final class MeshToLoad {
	/** Chunk coordinate in world space */
	public final int chunkX, chunkY, chunkZ;
	public final Section section;
	
	public MeshToLoad(Section section, int chunkX, int chunkY, int chunkZ) {
		this.section = section;
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;
	}
	
	@Override
	public int hashCode() {
		final int n = (29 * (chunkX << 1 ^ chunkX >> 31) + 463 * (chunkY << 1 ^ chunkY >> 31) + 5867 * (chunkZ << 1 ^ chunkZ >> 31));
        return n ^ n >>> 14;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof MeshToLoad mesh) {
			return mesh.chunkX == chunkX && mesh.chunkY == chunkY && mesh.chunkZ == chunkZ;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "(" + chunkX + "," + chunkY + "," + chunkZ + ")";
	}

	public boolean isVisible(Camera camera) {
		return camera.frustChunk(chunkX, chunkY, chunkZ);
	}
}
