package com.andedit.viewmc.graphic.vertex;

import static com.andedit.viewmc.graphic.vertex.VertBuf.buffer;
import static com.badlogic.gdx.Gdx.gl30;

import java.nio.IntBuffer;

import com.andedit.viewmc.graphic.QuadIndex;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.BufferUtils;

/** Vertex Buffer Object with Vertex Array Object (VBO with VAO)
 *  Will always binds QuadIndex. */
public class VAO implements Vertex {
	private static final IntBuffer intBuf = BufferUtils.newIntBuffer(1);

	private int glDraw, size;
	private final int handle, vao;
	private boolean isBound;
	private final VertexAttributes attributes;
	
	private ShaderProgram lastShader;
	
	VAO(VertexAttributes attributes, int glDraw) {
		this.handle = gl30.glGenBuffer();
		this.glDraw = glDraw;
		this.attributes = attributes;
		
		intBuf.clear();
		gl30.glGenVertexArrays(1, intBuf);
		vao = intBuf.get();
		
		gl30.glBindVertexArray(vao);
		gl30.glBindBuffer(GL30.GL_ARRAY_BUFFER, handle);
		QuadIndex.bind();
		gl30.glBindVertexArray(0);
	}
	
	@Override
	public void setVertices(float[] array, int size, int offset) {
		this.size = size;
		BufferUtils.copy(array, buffer, size, offset);
		if (!isBound) {
			gl30.glBindVertexArray(vao);
		}
		gl30.glBindBuffer(GL30.GL_ARRAY_BUFFER, handle);
		gl30.glBufferData(GL30.GL_ARRAY_BUFFER, buffer.remaining(), buffer, glDraw);
		if (!isBound) {
			gl30.glBindVertexArray(0);
		}
	}

	@Override
	public void bind(ShaderProgram shader) {
		gl30.glBindVertexArray(vao);
		
		if (shader != lastShader) {
			gl30.glBindBuffer(GL30.GL_ARRAY_BUFFER, handle);
			if (lastShader != null) {
				unVertexAttributes(lastShader);
			}
			setVertexAttributes(shader, null);
			lastShader = shader;
		}
		
		isBound = true;
	}

	@Override
	public void unbind(ShaderProgram shader) {
		gl30.glBindVertexArray(0);
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
		intBuf.clear();
		intBuf.put(vao);
		intBuf.flip();
		gl30.glDeleteVertexArrays(1, intBuf);
		gl30.glDeleteBuffer(handle);
	}
}
