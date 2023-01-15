package com.andedit.viewermc.world;

import static com.badlogic.gdx.Gdx.gl;

import java.util.ArrayList;

import com.andedit.viewermc.graphic.MeshBuilder;
import com.andedit.viewermc.graphic.MeshVert;
import com.andedit.viewermc.graphic.VertConsumer;
import com.andedit.viewermc.graphic.vertex.Vertex;
import com.andedit.viewermc.util.Util;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

public class Mesh implements Disposable {
	private final ArrayList<Vertex> soildVerts, transVerts;
	private final WorldRenderer render;
	private final Section section;
	private final int x, y, z;
	
	public Mesh(WorldRenderer render, Section section, int x, int y, int z) {
		this.render = render;
		this.section = section;
		soildVerts = new ArrayList<>(2);
		transVerts = new ArrayList<>(1);
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/** @return is empty after build. */
	public boolean build(World world, MeshBuilder builder) {
		int xPos = x<<4;
		int yPos = y<<4;
		int zPos = z<<4;
		
		for (int x = 0; x < Section.SIZE; x++)
		for (int y = 0; y < Section.SIZE; y++)
		for (int z = 0; z < Section.SIZE; z++) {
			var state = world.getBlockState(xPos+x, yPos+y, zPos+z);
			state.build(world, builder, xPos+x, yPos+y, zPos+z);
		}
		
		// this looks bad.
		boolean isEmpty = builder.size() == 0;
		build(builder);
		return isEmpty;
	}
	
	public boolean isVisible(final Plane[] planes) {
		final int s = planes.length;
		final float x, y, z;
		x = (this.x<<4)+8;
		y = (this.y<<4)+8;
		z = (this.z<<4)+8;

		for (int i = 2; i < s; i++) {
			final Plane plane = planes[i];
			final Vector3 normal = plane.normal;
			final float dist = normal.dot(x, y, z) + plane.d;
			
			final float radius = 
			8f * Math.abs(normal.x) +
			8f * Math.abs(normal.y) +
			8f * Math.abs(normal.z);

			if (dist < radius && dist < -radius) {
				return false;
			}
		}
		return true;
	}
	
	public void render() {
		for (var vertex : soildVerts) {
			vertex.bind();
			gl.glDrawElements(GL20.GL_TRIANGLES, (vertex.size() / MeshVert.byteSize) * 6, GL20.GL_UNSIGNED_SHORT, 0);
			if (!Util.isGL30()) {
				vertex.unbind();
			}

			//System.out.println(vertex.size() + " " + x + " " + y + " " + z);
		}
	}
	
	public void build(VertConsumer consumer) {
		var it = soildVerts.listIterator(consumer.build(soildVerts, () -> Vertex.newVbo(MeshVert.context, GL20.GL_STREAM_DRAW)));
        while (it.hasNext()) {
            it.next().dispose();
            it.remove();
        }
	}
	
	public boolean isEmpty() {
		return soildVerts.isEmpty();
	}
	
	public boolean pass(GridPoint3 pos, int offset) {
		final int radH = WorldRenderer.RADIUS_H + offset;
		final int radV = WorldRenderer.RADIUS_V + offset;
		return x < (-radH)+pos.x || y < (-radV)+pos.y || z < (-radH)+pos.z || x > radH+pos.x || y > radV+pos.y || z > radH+pos.z;
	}

	@Override
	public void dispose() {
		soildVerts.forEach(Vertex::dispose);
		transVerts.forEach(Vertex::dispose);
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
