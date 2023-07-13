package com.andedit.viewmc.graphic.vertex;

import static com.andedit.viewmc.graphic.vertex.VertBuf.buffer;
import static com.badlogic.gdx.Gdx.gl;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.BufferUtils;

/** Vertex Buffer Object (VBO) */
public class VBO implements Vertex {
	
	private int glDraw;
	private final int handle;
	private int size;
	private boolean isBound;
	private final VertexAttributes attributes;

	VBO(VertexAttributes attributes, int glDraw) {
		this.glDraw = glDraw;
		this.attributes = attributes;

		handle = gl.glGenBuffer();
	}

	@Override
	public void setVertices(float[] array, int size, int offset) {
		buffer.clear();
		this.size = size;
		BufferUtils.copy(array, buffer, size, offset);
		if (isBound) {
			gl.glBufferData(GL20.GL_ARRAY_BUFFER, buffer.remaining(), buffer, glDraw);
		} else {
			gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, handle);
			gl.glBufferData(GL20.GL_ARRAY_BUFFER, buffer.remaining(), buffer, glDraw);
			gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		}
	}

	@Override
	public void bind(ShaderProgram shader) {
		gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, handle);
		setVertexAttributes(shader, null);
		isBound = true;
	}

	@Override
	public void unbind(ShaderProgram shader) {
		unVertexAttributes(shader);
		gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		isBound = false;
	}
	
	@Override
	public void setDraw(int glDraw) {
		this.glDraw = glDraw;
	}

	@Override
	public int getDraw() {
		return glDraw;
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
		gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		gl.glDeleteBuffer(handle);
	}

	
}
