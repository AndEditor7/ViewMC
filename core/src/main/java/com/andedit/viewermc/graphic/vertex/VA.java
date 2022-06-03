package com.andedit.viewermc.graphic.vertex;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;

/** Vertex Array (VA) */
public class VA implements Vertex {
	final VertContext context;
	final ByteBuffer buffer;
	
	VA(VertContext context, ByteBuffer buffer) {
		this.context = context;
		this.buffer = buffer;
	}
	
	@Override
	public void setVertices(float[] array, int size, int offset) {
		BufferUtils.copy(array, buffer, size, offset);
	}

	@Override
	public void bind() {
		Buffer buf = (Buffer)buffer;
		int pos = buf.position();
		int limit = buf.limit();
		buf.clear();
		context.setVertexAttributes(buffer);
		buf.position(pos);
		buf.limit(limit);
	}

	@Override
	public void unbind() {
		context.unVertexAttributes();
	}
	
	@Override
	public void setDraw(int glDraw) {
	}

	@Override
	public int getDraw() {
		return GL20.GL_DYNAMIC_DRAW;
	}

	@Override
	public void dispose() {
	}
}
