package com.andedit.viewmc.graphic;

import com.andedit.viewmc.graphic.vertex.Vertex;
import com.andedit.viewmc.world.MeshToLoad;
import com.andedit.viewmc.world.Section;
import com.andedit.viewmc.world.WorldRenderer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.GridPoint3;

public class ChunkMesh extends Mesh {

	public final int x, y, z;
	private final Section section;
	
	public ChunkMesh(WorldRenderer render, MeshToLoad meshToLoad) {
		this(render, meshToLoad.section, meshToLoad.chunkX, meshToLoad.chunkY, meshToLoad.chunkZ);
	}
	
	public ChunkMesh(WorldRenderer render, Section section, int x, int y, int z) {
		this.section = section;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public boolean isVisible(Camera camera) {
		return camera.frustChunk(x, y, z);
	}
	
	public GridPoint3 getCenter(GridPoint3 grid) {
		return grid.set((x<<4)+8, (y<<4)+8, (z<<4)+8);
	}
	
	public boolean pass(GridPoint3 pos, int offset) {
		final int radH = WorldRenderer.RADIUS_H + offset;
		final int radV = WorldRenderer.RADIUS_V + offset;
		return x <= (-radH)+pos.x || y <= (-radV)+pos.y || z <= (-radH)+pos.z || x > radH+pos.x || y > radV+pos.y || z > radH+pos.z;
	}
	
	@Override
	protected Vertex newVertex() {
		return Vertex.newVbo(MeshVert.attributes, GL20.GL_STATIC_DRAW);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (!isEmpty()) setDirty(true);
	}
	
	public boolean equals(int x, int y, int z) {
		return this.x == x && this.y == y && this.z == z;
	}
	
	@Override
	public int hashCode() {
		return 29 * z + 1721 * x + 95713 * y;
	}
	
	public void setDirty(boolean isDirty) {
		section.isDirty = isDirty;
	}
	
	public boolean isDirty() {
		return section.isDirty;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true; 
		if (obj == null) return false;
		if (obj.getClass() == ChunkMesh.class) {
			var c = (ChunkMesh)obj;
			return equals(c.x, c.y, c.z);
		}
		return false;
	}
}
