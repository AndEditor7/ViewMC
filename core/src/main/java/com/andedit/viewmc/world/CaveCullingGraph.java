package com.andedit.viewmc.world;

import static com.andedit.viewmc.world.Section.SIZE;

import java.util.BitSet;
import java.util.EnumSet;

import com.andedit.viewmc.util.Facing;
import com.andedit.viewmc.util.Facing.Axis;
import com.andedit.viewmc.util.IntBit;
import com.andedit.viewmc.util.IntQueue;
import com.badlogic.gdx.utils.Array;

class CaveCullingGraph {
	
	private final Section section;
	private final BitSet occupation = new BitSet(SIZE * SIZE * SIZE);
	private final IntQueue queue = new IntQueue(64);
	private final Array<EnumSet<Facing>> list = new Array<>();
	private final Graph graph = new Graph();
	
	public CaveCullingGraph(Section section) {
		this.section = section;
	}
	
	void buildGraph() {
		
		var faces = Facing.values();
		
		for (var side : faces) {
			queue.clear();
			var upFace   = side.getUpFace();
			var riteFace = side.getRightFace();
			int x15 = side.isPositive() ? side.xOffset*15 : 0;
			int y15 = side.isPositive() ? side.yOffset*15 : 0;
			int z15 = side.isPositive() ? side.zOffset*15 : 0;
			
			//occupation.clear();
			for (int a = 0; a < SIZE; a++)
			for (int b = 0; b < SIZE; b++) {
				int x = (riteFace.axis.getInt(Axis.X)*a) + (upFace.axis.getInt(Axis.X)*b) + x15;
				int y = (riteFace.axis.getInt(Axis.Y)*a) + (upFace.axis.getInt(Axis.Y)*b) + y15;
				int z = (riteFace.axis.getInt(Axis.Z)*a) + (upFace.axis.getInt(Axis.Z)*b) + z15;
				if (!isAvailable(x, y, z)) continue;
				queue.addLast(occupy(newNode(x, y, z)));
			}

			var set = EnumSet.noneOf(Facing.class);
			
			while (queue.notEmpty()) {
				var node = queue.removeFirst();
				for (var face : faces) {
					if (isAvailable(set, face, getNodeX(node), getNodeY(node), getNodeZ(node))) {
						queue.addLast(occupy(offsetNode(node, face)));
					}
				}
			}
			
			if (set.size() > 1) {
				list.add(set);
			}
		}
		
		/*
		for (int x = 0; x < SIZE; x++)
		for (int y = 0; y < SIZE; y++) 
		for (int z = 0; z < SIZE; z++) {
			if (!isAvailable(x, y, z)) continue;
			
			queue.clear();
			queue.addLast(occupy(new PointNode3D(x, y, z)));
			var set = EnumSet.noneOf(Facing.class);
			
			while (queue.notEmpty()) {
				var node = queue.removeFirst();
				for (var face : faces) {
					if (isAvailable(set, face, node.x, node.y, node.z)) {
						queue.addLast(occupy(node.offset(face)));
					}
				}
			}
			
			if (set.size() > 1) {
				list.add(set);
			}
		} */
		
		for (var faceSet : list) {
			for (var from : faceSet) for (var to : faceSet) {
				if (from.axis == to.axis) {
					graph.set(to.xOffset, to.yOffset, to.zOffset);
				} else {
					graph.set(from.xOffset+to.xOffset, from.yOffset+to.yOffset, from.zOffset+to.zOffset);
				}
			}
		}
		
		section.graph = graph.bits;
	}
		
	private int occupy(int node) {
		occupy(getNodeX(node), getNodeY(node), getNodeZ(node));
		return node;
	}
	
	private void occupy(int x, int y, int z) {
		occupation.set(getVoxelIndex(x, y, z));
	}
	
	private boolean isAvailable(EnumSet<Facing> set, Facing face, int x, int y, int z) {
		x += face.xOffset;
		y += face.yOffset;
		z += face.zOffset;
		if (x < 0 || y < 0 || z < 0 || x >= SIZE || y >= SIZE || z >= SIZE) {
			set.add(face);
			return false;
		}
		return isAvailable(x, y, z);
	}
	
	private boolean isAvailable(int x, int y, int z) {
		return !occupation.get(getVoxelIndex(x, y, z)) && !section.getBlockstateAt(x, y, z).isFullOpaque(section, x, y, z);
	}
	
	private int getVoxelIndex(int x, int y, int z) {
		return section.getVoxelIndex(x, y, z);
	}
	
	static boolean getGraph(int bits, int x, int y, int z) {
		x++; y++; z++;
		return IntBit.get(bits, getBitIndex(x, y, z));
	}
	
	static int getBitIndex(int x, int y, int z) {
		return (y * 9) + (z * 3) + x;
	}
	
	static class Graph {
		int bits = 0;
		void set(int x, int y, int z) {
			x++; y++; z++;
			bits = IntBit.set(bits, getBitIndex(x, y, z));
		}
	}
	
	private static int offsetNode(int node, Facing face) {
		return newNode(getNodeX(node)+face.xOffset, getNodeY(node)+face.yOffset, getNodeZ(node)+face.zOffset);
	}
	
	private static int newNode(int x, int y, int z) {
		return y << 8 | z << 4 | x;
	}
	
	private static int getNodeX(int node) {
		return node & 0xF;
	}
	
	private static int getNodeY(int node) {
		return node >>> 8;
	}
	
	private static int getNodeZ(int node) {
		return (node >>> 4) & 0xF;
	}
}
