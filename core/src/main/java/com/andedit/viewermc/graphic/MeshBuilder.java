package com.andedit.viewermc.graphic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.GameCore;
import com.andedit.viewermc.block.Blocks;
import com.andedit.viewermc.graphic.vertex.Vertex;
import com.andedit.viewermc.util.TexReg;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.NumberUtils;

// v3-----v2
// |       |
// |       |
// v4-----v1
/** pos,uv,col,dat */
public class MeshBuilder implements VertConsumer {
	
	public final Blocks blocks;
	
	private final FloatArray array = new FloatArray(512);
	
	/** Ambient light, block light, and sky light */
	protected float dat = Color.toFloatBits(1f, 0f, 1f, 0);
	protected float col = Color.WHITE_FLOAT_BITS;
	
	protected float uOffset, vOffset;
	protected float uScale = 1, vScale = 1;
	
	protected TexReg region = TexReg.FULL;
	
	public MeshBuilder(Blocks blocks) {
		this.blocks = blocks;
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
	}
	
	public static float toData(float ambientLight, float blockLight, float skyLight) {
		if (GameCore.rendering.lighting) {
			return Color.toFloatBits(ambientLight, blockLight, skyLight, 0);
		} else {
			return Color.toFloatBits(MathUtils.lerp(ambientLight, 1.0f, 0.2f), 1, 1, 1);
		}
	}
	
	/* A cached instances temporary uses. */
	
	public final BoolGrid aoGrid = new BoolGrid();
	public final IntGrid litGrid = new IntGrid();
	public final FloatGrid aoGridF = new FloatGrid();
	public final FloatGrid blockLitGridF = new FloatGrid();
	public final FloatGrid skyLitGridF = new FloatGrid();
	
	
	public static class FloatGrid {
		private final float[][] floats = new float[2][2];
		
		public float get(int i) {
			return floats[i&1][i>>>1];
		}
		
		public void set(float val, int i) {
			floats[i&1][i>>>1] = val;
		}
		
		public float get(int x, int y) {
			return floats[x][y];
		}
		
		public void set(float val, int x, int y) {
			floats[x][y] = val;
		}
		
		public float bilinear(float x, float y) {
			float x0 = MathUtils.lerp(floats[0][0], floats[1][0], x);
		    float x1 = MathUtils.lerp(floats[0][1], floats[1][1], x);
		    return MathUtils.lerp(x0, x1, y);
		}
	}
	
	
	public static class IntGrid {
		private final int[][] ints = new int[3][3];
		
		public int get(int i) {
			return ints[i%3][i/3];
		}
		
		public void set(int val, int i) {
			ints[i%3][i/3] = val;
		}
		
		public int get(int x, int y) {
			return ints[x+1][y+1];
		}
		
		public void set(int val, int x, int y) {
			ints[x+1][y+1] = val;
		}
		
		/** getter in zero based */
		public int getI(int x, int y) {
			return ints[x][y];
		}
		
		/** setter in zero based */
		public void setI(int val, int x, int y) {
			ints[x][y] = val;
		}
	}
	
	public static class BoolGrid {
		private final boolean[][] bools = new boolean[3][3];
		
		public boolean get(int i) {
			return bools[i%3][i/3];
		}
		
		public void set(boolean val, int i) {
			bools[i%3][i/3] = val;
		}
		
		public boolean get(int x, int y) {
			return bools[x+1][y+1];
		}
		
		public void set(boolean val, int x, int y) {
			bools[x+1][y+1] = val;
		}
		
		/** getter in zero based */
		public boolean getI(int x, int y) {
			return bools[x][y];
		}
		
		/** setter in zero based */
		public void setI(boolean val, int x, int y) {
			bools[x][y] = val;
		}
	}
}
