package com.andedit.viewermc.world;

import static com.badlogic.gdx.Gdx.gl;

import java.util.ArrayList;
import java.util.EnumMap;

import com.andedit.viewermc.graphic.Camera;
import com.andedit.viewermc.graphic.MeshProvider;
import com.andedit.viewermc.graphic.MeshVert;
import com.andedit.viewermc.graphic.RenderLayer;
import com.andedit.viewermc.graphic.vertex.Vertex;
import com.andedit.viewermc.util.Util;
import com.andedit.viewermc.util.Vector3d;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.utils.Disposable;

public class Mesh implements Disposable {
	private final EnumMap<RenderLayer, ArrayList<Vertex>> verts;
	private final WorldRenderer render;
	private final Section section;
	private final int x, y, z;
	private boolean isEmpty;
	
	public Mesh(WorldRenderer render, MeshToLoad meshToLoad) {
		this(render, meshToLoad.section, meshToLoad.chunkX, meshToLoad.chunkY, meshToLoad.chunkZ);
	}
	
	public Mesh(WorldRenderer render, Section section, int x, int y, int z) {
		this.render = render;
		this.section = section;
		this.x = x;
		this.y = y;
		this.z = z;
		
		verts = new EnumMap<>(RenderLayer.class);
		for (var layer : RenderLayer.VALUES) {
			verts.put(layer, new ArrayList<>(2));
		}
	}
	
	public void update(MeshProvider provider) {
		isEmpty = provider.isEmpty();
		provider.build(verts);
	}
	
	public boolean isVisible(Camera camera) {
		return camera.frustChunk(x, y, z);
	}
	
	public void render(RenderLayer layer) {
		for (var vertex : verts.get(layer)) {
			vertex.bind();
			gl.glDrawElements(GL20.GL_TRIANGLES, (vertex.size() / MeshVert.byteSize) * 6, GL20.GL_UNSIGNED_SHORT, 0);
			if (!Util.isGL30()) {
				vertex.unbind();
			}
		}
	}
	
	public boolean isEmpty(RenderLayer layer) {
		return verts.get(layer).isEmpty();
	}
	
	public boolean isEmpty() {
		return isEmpty;
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
	public void dispose() {
		verts.values().forEach(a -> a.forEach(Vertex::dispose));
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
		if (obj.getClass() == Mesh.class) {
			var c = (Mesh)obj;
			return equals(c.x, c.y, c.z);
		}
		return false;
	}
}
