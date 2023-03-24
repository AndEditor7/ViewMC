package com.andedit.viewmc.graphic.vertex;

import static com.andedit.viewmc.graphic.vertex.VertBuf.buffer;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

/** Vertex Data */
public interface Vertex extends Disposable {
	VertexAttributes getAttributes();
	void setVertices(float[] array, int size, int offset);
	void bind(ShaderProgram shader);
	void unbind(ShaderProgram shader);
	void setDraw(int glDraw);
	int getDraw();
	int size();
	
	default void setVertexAttributes(ShaderProgram shader, @Null Buffer buffer) {
		final VertexAttributes attributes = getAttributes();
		for (int i = 0; i < attributes.size(); i++) {
			final VertexAttribute attribute = attributes.get(i);
			final int location = shader.getAttributeLocation(attribute.alias);
			if (location < 0) continue;
			shader.enableVertexAttribute(location);

			if (buffer == null) {
				shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized,
						attributes.vertexSize, attribute.offset);
			} else {
				buffer.position(attribute.offset);
				shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized,
						attributes.vertexSize, buffer);
			}
		}
	}
	
	default void unVertexAttributes(ShaderProgram shader) {
		final VertexAttributes attributes = getAttributes();
		for (int i = 0; i < attributes.size(); i++) {
			shader.disableVertexAttribute(attributes.get(i).alias);
		}
	}

	static Vertex newVbo(VertexAttributes attributes, int draw) {
		return Util.isGL30() ? new VAO(attributes, draw) : new VBO(attributes, draw);
	}
	
	static Vertex newVa(VertexAttributes attributes) {
		return newVa(attributes, buffer);
	}
	
	static Vertex newVa(VertexAttributes attributes, ByteBuffer buffer) {
		return Util.isGL30() ? new VAO(attributes, GL20.GL_DYNAMIC_DRAW) : new VA(attributes, buffer);
	}
}
