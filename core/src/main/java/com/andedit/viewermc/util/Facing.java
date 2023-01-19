package com.andedit.viewermc.util;

import java.util.Iterator;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Null;

public enum Facing {
	DOWN(Axis.Y, 0, -1, 0),
	UP(Axis.Y, 0, 1, 0),
	NORTH(Axis.Z, 0, 0, -1),
	SOUTH(Axis.Z, 0, 0, 1),
	WEST(Axis.X, -1, 0, 0),
	EAST(Axis.X, 1, 0, 0);
	
	public final Axis axis;
	public final int xOffset, yOffset, zOffset;
	
	private Facing(Axis axis, int xOffset, int yOffset, int zOffset) {
		this.axis = axis;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.zOffset = zOffset;
	}
	
	@Null
	public static Facing from(String string) {
		if (string == null) {
			return null;
		}
		if (string.equalsIgnoreCase("DOWN") || string.equalsIgnoreCase("BOTTOM")) {
			return DOWN;
		} if (string.equalsIgnoreCase("UP")) {
			return UP;
		} if (string.equalsIgnoreCase("NORTH")) {
			return NORTH;
		} if (string.equalsIgnoreCase("SOUTH")) {
			return SOUTH;
		} if (string.equalsIgnoreCase("WEST")) {
			return WEST;
		} if (string.equalsIgnoreCase("EAST")) {
			return EAST;
		}
		return null;
	}
	
	public int offsetValue(Axis axis) {
		return axis.getAxis(xOffset, yOffset, zOffset);
	}
	
	public Facing getUpFace() {
		return switch (this) {
		case NORTH, EAST, SOUTH, WEST -> UP;
		case UP  -> NORTH;
		case DOWN -> SOUTH;
		};
	}
	
	public Facing getRightFace() {
		return switch (this) {
		case NORTH, EAST, SOUTH, WEST -> rotateY();
		case UP  -> WEST;
		case DOWN -> EAST;
		};
	}
	
	public Facing invert() {
		return switch (this) {
		case UP    -> DOWN;
		case DOWN  -> UP;
		case NORTH -> SOUTH;
		case EAST  -> WEST;
		case SOUTH -> NORTH;
		case WEST  -> EAST;
		};
	}
	
	public Facing rotate(int x, int y) {
		Facing face = this;
		for (int i = 0; i < x / 90; i++) {
			face = face.rotateX();
		}
		
		for (int i = 0; i < y / 90; i++) {
			face = face.rotateY();
		}
		return face;
	}
	
	public Facing rotateX() {
		 /*
		return switch (this) {
		case UP    -> SOUTH;
		case SOUTH -> DOWN;
		case DOWN  -> NORTH;
		case NORTH -> UP;
		default -> this;
		}; 
		 */
		
		// /*
		return switch (this) {
		case UP    -> NORTH;
		case SOUTH -> UP;
		case DOWN  -> SOUTH;
		case NORTH -> DOWN;
		default -> this;
		};
		// */
	}
	
	public Facing rotateY() {
		
		 /*
		return switch (this) {
		case SOUTH -> EAST;
		case WEST  -> SOUTH;
		case NORTH -> WEST;
		case EAST  -> NORTH;
		default -> this;
		}; 
		 */
		
		// /*
		return switch (this) {
		case SOUTH -> WEST;
		case WEST  -> NORTH;
		case NORTH -> EAST;
		case EAST  -> SOUTH;
		default -> this;
		};
		// */
	}
	
	public boolean isPositive() {
		return this == UP || this == EAST || this == SOUTH;
	}
	
	private static final Facing[] ARRAY = values();
	public static final int SIZE = ARRAY.length;
	public static Facing get(int ordinal) {
		return ARRAY[MathUtils.clamp(ordinal, 0, 5)];
	}
	
	public float getAxis(BoundingBox box) {
		return axis.getAxis(isPositive() ? box.max : box.min);
	}
	
	public static enum Axis {
		X, Y, Z;
		
		public boolean isSide() {
			return this != Axis.Y;
		}
		
		public Axis right() {
			return switch (this) {
			case X, Y -> Z;
			case Z -> X;
			};
		}
		
		public int getInt(Axis axis) {
			return this == axis ? 1 : 0;
		}
		
		public Vector3 getVec() {
			return switch (this) {
			case X -> Vector3.X;
			case Y -> Vector3.Y;
			case Z -> Vector3.Z;
			default -> null;
			};
		}
		
		public float getAxis(Vector3 vec) {
			return getAxis(vec.x, vec.y, vec.z);
		}
		
		public int getAxis(int x, int y, int z) {
			return switch (this) {
			case X -> x;
			case Y -> y;
			case Z -> z;
			};
		}
		
		public float getAxis(float x, float y, float z) {
			return switch (this) {
			case X -> x;
			case Y -> y;
			case Z -> z;
			};
		}
		
		@Null
		public static Axis from(char c) {
			return switch (Character.toLowerCase(c)) {
			case 'x' -> Axis.X;
			case 'y' -> Axis.Y;
			case 'z' -> Axis.Z;
			default -> null;
			};
		}
	}
	
	public static Iterable<Facing> allIter() {
		return () -> iter(0);
	}
	
	public static Iterable<Facing> sideIter() {
		return () -> iter(2);
	}
	
	private static Iterator<Facing> iter(int start) {
		return new Iterator<Facing>() {
			int i = start;
			@Override
			public boolean hasNext() {
				return i < SIZE;
			}

			@Override
			public Facing next() {
				return ARRAY[i++];
			}
		};
	}
}
