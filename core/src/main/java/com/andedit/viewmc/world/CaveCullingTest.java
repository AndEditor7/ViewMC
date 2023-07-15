package com.andedit.viewmc.world;

import static com.andedit.viewmc.world.WorldRenderer.RADIUS_H;
import static com.andedit.viewmc.world.WorldRenderer.RADIUS_V;

import java.util.BitSet;

import com.andedit.viewmc.graphic.Camera;
import com.andedit.viewmc.util.Facing;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.utils.LongQueue;
import com.badlogic.gdx.utils.Null;

public class CaveCullingTest {
	
	private final WorldRenderer renderer;
	private final World world;
	
	private final Occupation occupation;
	private final LongQueue queue = new LongQueue();
	private final GridPoint3 chunkPos = new GridPoint3();
	
	public CaveCullingTest(WorldRenderer renderer, World world) {
		this.renderer = renderer;
		this.world = world;
		occupation = new Occupation();
	}
	
	public void rebuild(Camera camera) {
		
	}
	
	public void update(Camera camera) {
		var pos = camera.position;
		chunkPos.set(pos.floorX()>>4, pos.floorY()>>4, pos.floorZ()>>4);
		
		occupation.clear();
		queue.clear();
		
		var faces = Facing.values();
		
		boolean cullEmptyChunk = world.getSection(chunkPos.x, chunkPos.y, chunkPos.z) != null;
		
		queue.addFirst(occupation.occupy(newNode(null, 0, 0, 0, 0, 0)));
		while (queue.notEmpty()) {
			var node = queue.removeFirst();
			
			int xNode = getNodeX(node);
			int yNode = getNodeY(node);
			int zNode = getNodeZ(node);
			int x = xNode + chunkPos.x;
			int y = yNode + chunkPos.y;
			int z = zNode + chunkPos.z;
			
			var section = world.getSection(x, y, z);
			if (cullEmptyChunk && section == null) {
				continue;
			}
			if (section != null && section.isDirty && renderer.providers.notEmpty() && section.chunk.canBuild()) {
				var meshToLoad = new MeshToLoad(section, x, y, z);
				if (!renderer.pendingMeshes.contains(meshToLoad)) {
					renderer.pendingMeshes.add(meshToLoad);
					renderer.submit(meshToLoad);
				}
				section.isDirty = false;
			}
			
			var step = getNodeStep(node);
			if (step > RADIUS_H * 2.5f) continue;
			
			for (var face : faces) {
				if (isBlocked(node, face)) continue;
				if (!occupation.isAvailable(face, xNode, yNode, zNode)) continue;
				if (step <= 0 || section == null || section.canEnter(getNodeFace(node).invert(), face)) {
					if (camera.frustChunk(x+face.xOffset, y+face.yOffset, z+face.zOffset)) {
						//var walk = section == null ? 1 : section.getWalkableSteps();
						queue.addLast(occupation.occupy(offsetNode(node, face, 1)));
					}
				}
			}
		}
	}
	
	public boolean canRender(int x, int y, int z) {
		return !occupation.isAvailable(x-chunkPos.x, y-chunkPos.y, z-chunkPos.z);
	}
	
	public void reset() {
		
	}
	
	private static long newNode(@Null Facing face, long blockedFaces, long step, long x, long y, long z) {
		long node = 0;
		node |= (x & 0xFFl);
		node |= (y & 0xFFl) << 8;
		node |= (z & 0xFFl) << 16;
		node |= (step & 0xFFFFl) << 24;
		node |= (blockedFaces & 0xFFl) << 40;
		if (face == null) {
			node |= 6L << 48;
		} else {
			node |= (long)face.ordinal() << 48;
		}
		return node;
	}
	
	private static long offsetNode(long node, Facing face, long add) {
		return newNode(face, block(node, face), getNodeStep(node)+add, getNodeX(node)+face.xOffset, getNodeY(node)+face.yOffset, getNodeZ(node)+face.zOffset);
	}
	
	private static int block(long node, Facing face) {
		return getNodeBlocks(node) | (1 << face.invert().ordinal());
	}
	
	public static boolean isBlocked(long node, Facing face) {
		return ((getNodeBlocks(node) >>> face.ordinal()) & 1) == 1;
	}
	
	private static int getNodeBlocks(long node) {
		return (int)((node >>> 40) & 0xFFl);
	}
	
	private static byte getNodeX(long node) {
		return (byte)(node & 0xFFl);
	}
	
	private static byte getNodeY(long node) {
		return (byte)((node >>> 8) & 0xFFl);
	}
	
	private static byte getNodeZ(long node) {
		return (byte)((node >>> 16) & 0xFFl);
	}
	
	private static int getNodeStep(long node) {
		return (int)((node >>> 24) & 0xFFFFl);
	}
	
	@Null
	private static Facing getNodeFace(long node) {
		int face = (int)((node >>> 48) & 0xFF);
		return face == 6 ? null : Facing.get(face);
	}
	
	private static class Occupation {
		private final BitSet occupation = new BitSet((RADIUS_H*2) * (RADIUS_V*2) * (RADIUS_H*2));

		public boolean isAvailable(Facing face, int x, int y, int z) {
			return isAvailable(x+face.xOffset, y+face.yOffset, z+face.zOffset);
		}
		
		public boolean isAvailable(int x, int y, int z) {
			x += RADIUS_H;
			y += RADIUS_V;
			z += RADIUS_H;
			
			if (x < 1 || y < 1 || z < 1 || x > RADIUS_H*2 || y > RADIUS_V*2 || z > RADIUS_H*2) {
				return false;
			}

			return !occupation.get(getIndex(x, y, z));
		}
		
		static int getIndex(int x, int y, int z) {
			return y + x * (RADIUS_V*2) + z * (RADIUS_V*2) * (RADIUS_H*2);
		}
		
		public long occupy(long node) {
			occupy(getNodeX(node), getNodeY(node), getNodeZ(node));
			return node;
		}
		
		public void occupy(int x, int y, int z) {
			x += RADIUS_H;
			y += RADIUS_V;
			z += RADIUS_H;
			occupation.set(getIndex(x, y, z));
		}
		
		public void clear() {
			occupation.clear();
		}
	}
}
