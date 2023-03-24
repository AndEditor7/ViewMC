package com.andedit.viewmc.graphic;

import java.nio.ByteBuffer;

import com.andedit.viewmc.graphic.vertex.VertBuf;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.utils.BufferUtils;

public class MeshVert {
	public static VertexAttributes attributes;
	
	public static ByteBuffer buffer;
	
	/** 28 bytes in a single vertex. */ 
	public static int byteSize;
	
	/** 7 floats in a single vertex. */ 
	public static int floatSize;
	
	public static void preInit() {
		attributes = new VertexAttributes(
			new VertexAttribute(Usage.Position, 3, "a_position"),
			new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"),
			new VertexAttribute(Usage.ColorPacked, 4, "a_color"),
			new VertexAttribute(Usage.Generic, 4, GL20.GL_UNSIGNED_BYTE, true, "a_data")
		);
		
		byteSize = attributes.vertexSize;
		floatSize = byteSize/Float.BYTES;
		
		int bytes = byteSize * QuadIndex.maxVertex;
		buffer = BufferUtils.newByteBuffer(bytes);
		VertBuf.buffer = buffer;
	}
}
