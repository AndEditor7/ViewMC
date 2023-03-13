package com.andedit.viewmc.graphic.vertex;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.BufferUtils;

/** Vertex Array (VA) */
public class VA implements Vertex {
	final VertexAttributes attributes;
	final ByteBuffer buffer;
	int size;
	
	VA(VertexAttributes attributes, ByteBuffer buffer) {
		this.attributes = attributes;
		this.buffer = buffer;
	}
	
	@Override
	public void setVertices(float[] array, int size, int offset) {
		BufferUtils.copy(array, buffer, size, offset);
		this.size = size;
	}

	@Override
	public void bind(ShaderProgram shader) {
		Buffer buf = (Buffer)buffer;
		int pos = buf.position();
		int limit = buf.limit();
		buf.clear();
		setVertexAttributes(shader, buffer);
		buf.position(pos);
		buf.limit(limit);
	}

	@Override
	public void unbind(ShaderProgram shader) {
		unVertexAttributes(shader);
	}
	
	@Override
	public void setDraw(int glDraw) {
	}

	@Override
	public int getDraw() {
		return GL20.GL_DYNAMIC_DRAW;
	}
	
	@Override
	public int size() {
		return size;
	}
	
	@Override
	public VertexAttributes getAttributes() {
		return attributes;
	}

	@Override
	public void dispose() {
	}
}
