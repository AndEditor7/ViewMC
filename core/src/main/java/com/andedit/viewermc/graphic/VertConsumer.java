package com.andedit.viewermc.graphic;

import java.util.List;
import java.util.function.Supplier;

import com.andedit.viewermc.graphic.vertex.Vertex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public interface VertConsumer {
	
	/** Append position. */
	void pos(float x, float y, float z);
	
	/** Append position. */
	default void pos(Vector3 pos) {
		pos(pos.x, pos.y, pos.z);
	}
	
	/** Append texture coordinate (UV). */
	void uv(float u, float v);
	
	/** Append texture coordinate (UV). */
	default void uv(Vector2 uv) {
		uv(uv.x, uv.y);
	}
	
	/** Append color. */
	void col(float r, float g, float b, float a);
	
	/** Append color. */
	default void col(float r, float g, float b) {
		col(r, g, b, 1);
	}
	
	/** Append color. */
	default void col(Color col) {
		col(col.r, col.g, col.b, col.a);
	}
	
	/** Append attribute value. */
	void val(float val);
	
	/** @return vertex size of floats appended. */
	int size();
	
	/** Build vertex and clear/reset the consumer. */
	void build(Vertex vertex);
	
	/** Build vertex and clear/reset the consumer.
	 * @return a new array size - used for disposing vertices. */
	int build(List<Vertex> vertices, Supplier<Vertex> supplier);
}
