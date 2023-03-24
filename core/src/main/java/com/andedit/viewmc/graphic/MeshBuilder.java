package com.andedit.viewmc.graphic;

import java.util.List;
import java.util.function.Supplier;

import com.andedit.viewmc.graphic.vertex.Vertex;
import com.andedit.viewmc.resource.Resources;
import com.andedit.viewmc.util.Identifier;
import com.andedit.viewmc.util.TexReg;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.OrderedSet;

// v3-----v2
// |       |
// |       |
// v4-----v1
/** pos,uv,col,dat */
public class MeshBuilder implements VertConsumer {
	
	private final FloatArray array = new FloatArray(512);
	final OrderedSet<Identifier> textureToAnimate = new OrderedSet<>(200);
	
	/** Ambient light, block light, and sky light */
	protected float dat = Color.toFloatBits(1f, 0f, 1f, 0);
	protected float col = Color.WHITE_FLOAT_BITS;
	
	protected float uOffset, vOffset;
	protected float uScale = 1, vScale = 1;
	
	protected TexReg region = TexReg.FULL;
	
	/** Add texture to animate */
	public void addTexture(@Null Identifier id) {
		if (id != null) {
			textureToAnimate.add(id);
		}
	}
	
	public void setUVRange(TexReg region) {
		this.region = region;
		setUVRange(region.u1, region.v1, region.u2, region.v2);
	}
	
	public void setUVRange(float u1, float v1, float u2, float v2) {
		uOffset = u1;
		vOffset = v1;
		uScale = u2 - u1;
		vScale = v2 - v1;
	}
	
	public void setLight(float ambientLight, float blockLight, float skyLight) {
		dat = toData(ambientLight, blockLight, skyLight);
	}
	
	public void setColor(int color) {
		this.col = NumberUtils.intToFloatColor(color);
	}
	
	public void setColor(float color) {
		this.col = color;
	}
	
	public void vert1(float x, float y, float z) {
		array.add(x, y, z);
		uv(region.u2, region.v2);
		array.add(col, dat);
	}
	
	public void vert2(float x, float y, float z) {
		array.add(x, y, z);
		uv(region.u2, region.v1);
		array.add(col, dat);
	}
	
	public void vert3(float x, float y, float z) {
		array.add(x, y, z);
		uv(region.u1, region.v1);
		array.add(col, dat);
	}
	
	public void vert4(float x, float y, float z) {
		array.add(x, y, z);
		uv(region.u1, region.v2);
		array.add(col, dat);
	}
	
	public void vert(Vector3 pos, Vector2 uv) {
		array.add(pos.x, pos.y, pos.z);
		uv(uv.x, uv.y);
		array.add(col, dat);
	}
	
	public void vert(float x, float y, float z, float u, float v) {
		array.add(x, y, z);
		uv(u, v);
		array.add(col, dat);
	}
	
	public void vert(float x, float y, float z, float u, float v, float dat) {
		array.add(x, y, z);
		uv(u, v);
		array.add(col, dat);
	}
	
	public void vert(float x, float y, float z, float u, float v, float col, float dat) {
		array.add(x, y, z);
		uv(u, v);
		array.add(col, dat);
	}

	@Override
	public void pos(float x, float y, float z) {
		array.add(x, y, z);
	}

	@Override
	public void uv(float u, float v) {
		array.add(uOffset + uScale * u, vOffset + vScale * v);
	}
	
	@Override
	public void col(int color) {
		array.add(NumberUtils.intToFloatColor(color));
	}
	
	@Override
	public void col(float r, float g, float b, float a) {
		array.add(Color.toFloatBits(r, g, b, a));
	}
	
	public void lit(float ambientLight, float blockLight, float skyLight) {
		array.add(toData(ambientLight, blockLight, skyLight));
	}

	@Override
	public void val(float val) {
		array.add(val);
	}
	
	@Override
	public int size() {
		return array.size;
	}

	@Override
	public void build(Vertex vertex) {
		vertex.setVertices(array.items, size(), 0);
		array.clear();
	}
	
	@Override
	public int build(List<Vertex> list, Supplier<Vertex> supplier) {
		int size = size();
		int max = QuadIndex.maxVertex * MeshVert.floatSize;
		int off = 0;
		int itr = 0;
		while (size != 0) {
			final Vertex vertex;
			if (itr >= list.size()) {
				vertex = supplier.get();
				list.add(vertex);
			} else vertex = list.get(itr);
			
			int min = Math.min(size, max);
			vertex.setVertices(array.items, min, off);
			size -= min;
			off += min;
			itr++;
		}
		array.clear();
		return itr;
	}
	
	public void clear() {
		array.clear();
		textureToAnimate.clear();
	}
	
	public static float toData(float ambientLight, float blockLight, float skyLight) {
		return Color.toFloatBits(ambientLight, blockLight, skyLight, 0);
	}
}
