package com.andedit.viewmc.graphic;

import java.util.Arrays;

import com.andedit.viewmc.util.Facing;
import com.andedit.viewmc.util.Facing.Axis;
import com.andedit.viewmc.util.IntsFunction;
import com.andedit.viewmc.world.BlockView;
import com.andedit.viewmc.world.Lights;
import com.andedit.viewmc.world.Section;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Null;

public class Lighting {

	public static float getShade(@Null Facing face) {
		return face == null ? 1 : switch (face) {
		case NORTH, SOUTH -> 0.8f; // 0.8f
		case EAST, WEST -> 0.6f; // 0.6f
		case DOWN -> 0.5f; // 0.5f
		default -> 1.0f;
		};
	}
	
	public static float getAmbient(int level) {
		return switch (level) {
		case 0 -> 0.3f;
		case 1 -> 0.4f;
		case 2 -> 0.6f;
		case 3 -> 0.75f;
		default -> 1.0f; // and 4
		};
	}
	
	private final boolean aoMap[][][] = new boolean[3][3][3];
	private final int litMap[][][] = new int[3][3][3];
	
	private final Result result = new Result();
	
	private final FloatGrid aoLitGrid = new FloatGrid();
	private final FloatGrid blockLitGrid = new FloatGrid();
	private final FloatGrid skyLitGrid = new FloatGrid();
	
	private final boolean needCalculation[] = new boolean[Facing.SIZE];
	
	{
		reset();
	}
	
	public boolean needCalculation(Facing face) {
		return needCalculation[face.ordinal()];
	}
	
	public void calculate(BlockView view, Facing face, int x, int y, int z) {
		for (int i = 0; i < 9; i++)
		calculate(view, face, x, y, z, (i%3)-1, (i/3)-1);
		
		needCalculation[face.ordinal()] = false;
	}
	
	private void calculate(BlockView view, Facing face, int x, int y, int z, int u, int v) {
		var upFace = face.getUpFace();
		var rightFace = face.getRightFace();
		
		// face forward offset
		int x0 = x + face.xOffset;
		int y0 = y + face.yOffset;
		int z0 = z + face.zOffset;
		
		int x1 = (rightFace.axis.getInt(Axis.X)*u) + (upFace.axis.getInt(Axis.X)*v);
		int y1 = (rightFace.axis.getInt(Axis.Y)*u) + (upFace.axis.getInt(Axis.Y)*v);
		int z1 = (rightFace.axis.getInt(Axis.Z)*u) + (upFace.axis.getInt(Axis.Z)*v);
		
		setLit(face, view.getLight(x+x1, y+y1, z+z1), x1, y1, z1, 0);
		setLit(face, view.getLight(x0+x1, y0+y1, z0+z1), x1, y1, z1, 1);
		
		setAO(face, view.getBlockstate(x+x1, y+y1, z+z1).isFullOpaque(view, x+x1, y+y1, z+z1), x1, y1, z1, 0);
		setAO(face, view.getBlockstate(x0+x1, y0+y1, z0+z1).isFullOpaque(view, x0+x1, y0+y1, z0+z1), x1, y1, z1, 1);
	}
	
	public void calculateVert(Facing face, int centerU, int centerV, int sideAu, int sideAv, int sideBu, int sideBv, int cornerU, int cornerV, int uIndex, int vIndex) {
		for (int w = 0; w < 2; w++) {
			aoLitGrid.set(getAmbient(vertAO(getAO(face, centerU, centerV, w), getAO(face, sideAu, sideAv, w), getAO(face, sideBu, sideBv, w), getAO(face, cornerU, cornerV, w))), uIndex, vIndex, w);
			blockLitGrid.set(calcLight(Lights.BLOCK, getLit(face, centerU, centerV, w), getLit(face, sideAu, sideAv, w), getLit(face, sideBu, sideBv, w), getLit(face, cornerU, cornerV, w)), uIndex, vIndex, w);
			skyLitGrid.set(calcLight(Lights.SKY, getLit(face, centerU, centerV, w), getLit(face, sideAu, sideAv, w), getLit(face, sideBu, sideBv, w), getLit(face, cornerU, cornerV, w)), uIndex, vIndex, w);
		}
	}
	
	private int setLit(Facing face, int lit, int x, int y, int z, int offset) {
		x += (face.xOffset * offset) + 1;
		y += (face.yOffset * offset) + 1;
		z += (face.zOffset * offset) + 1;
		return litMap[x][y][z] = lit;
	}
	
	private void setAO(Facing face, boolean ao, int x, int y, int z, int offset) {
		x += (face.xOffset * offset) + 1;
		y += (face.yOffset * offset) + 1;
		z += (face.zOffset * offset) + 1;
		aoMap[x][y][z] = ao;
	}
	
	public int getLit(Facing face, int u, int v, int offset) {
		var upFace = face.getUpFace();
		var rightFace = face.getRightFace();
		
		int x = (rightFace.axis.getInt(Axis.X)*u) + (upFace.axis.getInt(Axis.X)*v) + (face.xOffset * offset) + 1;
		int y = (rightFace.axis.getInt(Axis.Y)*u) + (upFace.axis.getInt(Axis.Y)*v) + (face.yOffset * offset) + 1;
		int z = (rightFace.axis.getInt(Axis.Z)*u) + (upFace.axis.getInt(Axis.Z)*v) + (face.zOffset * offset) + 1;
		
		return litMap[x][y][z];
	}
	
	public boolean getAO(Facing face, int u, int v, int offset) {
		var upFace = face.getUpFace();
		var rightFace = face.getRightFace();
		
		int x = (rightFace.axis.getInt(Axis.X)*u) + (upFace.axis.getInt(Axis.X)*v) + (face.xOffset * offset) + 1;
		int y = (rightFace.axis.getInt(Axis.Y)*u) + (upFace.axis.getInt(Axis.Y)*v) + (face.yOffset * offset) + 1;
		int z = (rightFace.axis.getInt(Axis.Z)*u) + (upFace.axis.getInt(Axis.Z)*v) + (face.zOffset * offset) + 1;
		
		return aoMap[x][y][z];
	}
	
	public Result getResult(float u, float v, float w) {
		u = MathUtils.clamp(u, 0f, 1f);
		v = MathUtils.clamp(v, 0f, 1f);
		w = MathUtils.clamp(w, 0f, 1f);
		
		result.aoLit = aoLitGrid.trilinear(u, v, w);
		result.blockLit = blockLitGrid.trilinear(u, v, w);
		result.skyLit = skyLitGrid.trilinear(u, v, w);
		
		return result;
	}
	
	public void reset() {
		Arrays.fill(needCalculation, true);
	}
	
	private static int vertAO(boolean center, boolean side1, boolean side2, boolean corner) {
		if (side1 && side2) return 1 - toInt(center);
		return 4 - (toInt(center) + toInt(side1) + toInt(side2) + toInt(corner));
	}
	
	private static int toInt(boolean bool) {
		return bool ? 1 : 0;
	}
	
	private float calcLight(IntsFunction function, int center, int sideA, int sideB, int corner) {
		int lightTotal = function.apply(center);
		int lightCount = 1;
		if (function.apply(sideA) != 0) {
			lightCount++;
			lightTotal += function.apply(sideA);
		}

		if (function.apply(sideB) != 0) {
			lightCount++;
			lightTotal += function.apply(sideB);
		}

		if ((function.apply(corner) != 0 || function.apply(center) == 1)) {
			lightCount++;
			lightTotal += (function.apply(corner) == 1 && function.apply(center) == 1) ? 0 : function.apply(corner);
		}

		float value = lightTotal / (float)lightCount;
		return value / Lights.SCL;
	}
	
	public static class Result {
		public float aoLit, blockLit, skyLit;
	}
	
	public static class FloatGrid {
		private final float[][][] floats = new float[2][2][2];
		
		public void set(float val, int u, int v, int w) {
			floats[u][v][w] = val;
		}
		
		public float trilinear(float u, float v, float w) {
			float u00 = MathUtils.lerp(floats[0][0][0], floats[1][0][0], u);
		    float u10 = MathUtils.lerp(floats[0][1][0], floats[1][1][0], u);
		    float u01 = MathUtils.lerp(floats[0][0][1], floats[1][0][1], u);
		    float u11 = MathUtils.lerp(floats[0][1][1], floats[1][1][1], u);
		    return MathUtils.lerp(MathUtils.lerp(u00, u10, v), MathUtils.lerp(u01, u11, v), w);
		}
	}
}
