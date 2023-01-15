package com.andedit.viewermc.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.block.model.BlockModelJson;
import com.andedit.viewermc.block.model.Face;
import com.andedit.viewermc.block.model.Rotation;
import com.andedit.viewermc.block.model.UV;
import com.andedit.viewermc.block.state.ModelJson;
import com.andedit.viewermc.graphic.MeshBuilder;
import com.andedit.viewermc.util.Facing;
import com.andedit.viewermc.util.Facing.Axis;
import com.andedit.viewermc.util.TexReg;
import com.andedit.viewermc.util.Util;
import com.andedit.viewermc.world.World;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.OrderedMap;

// v3-----v2
// |       |
// |       |
// v4-----v1
public class BlockModel implements Iterable<Quad> {
	
	/** tolerance */
	private static final float T = 0.001f;
	
	private final ArrayList<Quad> quads = new ArrayList<>();
	private final ArrayList<BoundingBox> boxes = new ArrayList<>();
	
	/** ambient occlusion */
	public boolean ao = true;
	
	public BlockModel() {
		
	}
	
	private BlockModel(BlockModel oldModel, ModelJson model) {
		ao = oldModel.ao;
		
		var set = new OrderedMap<BoundingBox, ArrayList<Quad>>(oldModel.boxes.size());
		for (var quad : oldModel.quads) {
			var list = set.get(quad.box);
			if (list == null) {
				set.put(quad.box, list = new ArrayList<>(6));
			}
			list.add(quad);
		}
		
		for (var entry : set.entries()) {
			var box = new BoundingBox(entry.key);
			boxes.add(box);
			for (var quad : entry.value) {
				quads.add(new Quad(quad, box));
			}
		}
		
		if (model.hasTransformation()) {
			for (var quad : quads) {
				quad.rotate(model);
			}
			boxes.forEach(b -> Util.mul(b, model.matrix));
		}
		
		if (model.uvLock) {
			quads.forEach(Quad::uvLock);
		}
	}
	
	public BlockModel(BlockModelJson model, TextureAtlas textures) {
		ao = model.ambientOcclusion();
		var faces = new ArrayList<Facing>();
		for (var element : model.elements) {
			var cube = cube(element.from, element.to);
			cube.shade(element.shade);
			
			for (var entry : element.faces.entrySet()) {
				var face = entry.getKey();
				faces.add(face);
				cube.get(face).init(entry.getValue(), model, textures);
			}
			cube.removeExcept(faces);
			faces.clear();
			
			cube.rotate(element.rotation);
		}
	}
	
	public BlockModel create(ModelJson model) {
		if (model.hasTransformation() || model.uvLock) {
			return new BlockModel(this, model);
		}
		return this;
	}
	
	public void build(World world, MeshBuilder builder, int x, int y, int z) {
		for (int i=0,s=quads.size(); i < s; i++) {
			quads.get(i).build(world, builder, x, y, z);
		}
	}
	
	public void getQuads(Collection<Quad> list) {
		for (int i=0,s=quads.size(); i < s; i++) {
			list.add(quads.get(i));
		}
	}
	
	public void getBoxes(Collection<BoundingBox> list) {
		for (int i=0,s=boxes.size(); i < s; i++) {
			list.add(boxes.get(i));
		}
	}
	
	public boolean isFullOpaque() {
		return false;
	}
	
	/** a = from, b = to. 0 to 16 */
	public Cube cube(BoundingBox box) {
		return cube(box.min, box.max);
	}
	
	/** a = from, b = to. 0 to 16 */
	public Cube cube(Vector3 a, Vector3 b) {
		return cube(a.x, a.y, a.z, b.x, b.y, b.z);
	}
	
	/** a = from, b = to. 0 to 16 */
	public Cube cube(float Ax, float Ay, float Az, float Bx, float By, float Bz) {
		var box = new BoundingBox(new Vector3(Ax, Ay, Az).scl(1/16f), new Vector3(Bx, By, Bz).scl(1/16f));
		
		Quad up = newQuad(box);
		box.getCorner111(up.v1);
		box.getCorner110(up.v2);
		box.getCorner010(up.v3);
		box.getCorner011(up.v4);
		
		Quad down = newQuad(box);
		box.getCorner100(down.v1);
		box.getCorner101(down.v2);
		box.getCorner001(down.v3);
		box.getCorner000(down.v4);
		
		Quad north = newQuad(box);
		box.getCorner000(north.v1);
		box.getCorner010(north.v2);
		box.getCorner110(north.v3);
		box.getCorner100(north.v4);
		
		Quad east = newQuad(box);
		box.getCorner100(east.v1);
		box.getCorner110(east.v2);
		box.getCorner111(east.v3);
		box.getCorner101(east.v4);
		
		Quad south = newQuad(box);
		box.getCorner101(south.v1);
		box.getCorner111(south.v2);
		box.getCorner011(south.v3);
		box.getCorner001(south.v4);
		
		Quad west = newQuad(box);
		box.getCorner001(west.v1);
		box.getCorner011(west.v2);
		box.getCorner010(west.v3);
		box.getCorner000(west.v4);
		
		boxes.add(box);
		return new Cube(box, up, down, north, east, south, west);
	}
	
	public Quad newQuad() {
		return newQuad(null);
	}
	
	private Quad newQuad(@Null BoundingBox box) {
		var quad = new Quad(box);
		quads.add(quad);
		return quad;
	}
	
	/** no ambient occlusion */
	public BlockModel noAO() {
		ao = false;
		return this;
	}
	
	@Override
	public Iterator<Quad> iterator() {
		return quads.iterator();
	}
	
	public class Quad {
		public final Vector3 v1 = new Vector3();
		public final Vector3 v2 = new Vector3();
		public final Vector3 v3 = new Vector3();
		public final Vector3 v4 = new Vector3();
		
		public final Vector2 t1 = new Vector2();
		public final Vector2 t2 = new Vector2();
		public final Vector2 t3 = new Vector2();
		public final Vector2 t4 = new Vector2();
		
		@Null
		private Facing face;
		public boolean shade = true;
		public int tintIndex = -1;
		public boolean culling = true;
		public boolean isAlign = true;
		public TexReg region = TexReg.FULL;
		
		public final BoundingBox box;
		private boolean borderCollide;
		
		private Quad(Quad quad, BoundingBox box) {
			v1.set(quad.v1);
			v2.set(quad.v2);
			v3.set(quad.v3);
			v4.set(quad.v4);
			
			t1.set(quad.t1);
			t2.set(quad.t2);
			t3.set(quad.t3);
			t4.set(quad.t4);
			
			face = quad.face;
			shade = quad.shade;
			tintIndex = quad.tintIndex;
			culling = quad.culling;
			isAlign = quad.isAlign;
			region = quad.region;
			borderCollide = quad.borderCollide;
			this.box = box;
		}

		private Quad(BoundingBox box) {
			this.box = box;
		}
		
		private void init(Face value, BlockModelJson model, TextureAtlas textures) {
			tintIndex = value.tintIndex;
			culling = value.culling;
			var region = textures.getRegion(model.getTexture(value.texture));
			if (region == null) region = TexReg.FULL; 
			reg(region, value.uv);
			rotateTex(value.rotation);
		}

		public void build(World world, MeshBuilder builder, int x, int y, int z) {
			List<Quad> quads = builder.quads;
			quads.clear();
			if (borderCollide && culling) {
				//var off = pos.offset(face);
				//if (World.isOutBound(off)) {
					//return;
				//}
				
				var state = world.getBlockState(x+face.xOffset, y+face.yOffset, z+face.zOffset);
				state.getQuads(quads, x+face.xOffset, y+face.yOffset, z+face.zOffset);
			}
			
			if (canRender(quads)) {
				builder.setLight(shade ? (face != null ? BlockForm.getShade(face) : 1) : 1, 1, 1);
				final float xf = x, yf = y, zf = z;
				builder.vert(v1.x+xf, v1.y+yf, v1.z+zf, t1.x, t1.y);
				builder.vert(v2.x+xf, v2.y+yf, v2.z+zf, t2.x, t2.y);
				builder.vert(v3.x+xf, v3.y+yf, v3.z+zf, t3.x, t3.y);
				builder.vert(v4.x+xf, v4.y+yf, v4.z+zf, t4.x, t4.y);
			}
		}
		
		public void getQuads(Collection<Quad> collection) {
			collection.add(this);
		}
		
		public void getBoxes(Collection<BoundingBox> collection) {
			if (isAlign) 
			collection.add(box);
		}
		
		/** Test for whether this quad is'nt blocked by the quads. */
		boolean canRender(Block block) {
			return false;
		}
		
		/** Test for whether this quad is'nt blocked by the quads. */
		boolean canRender(List<Quad> quads) {
			if (!culling) return true;
			
			var boxA = box;
			float areaCovered = 0;
			for (int i=0,s=quads.size(); i < s; i++) {
				var quad = quads.get(i);
				if (quad.borderCollide && face == quad.face.invert()) {
					var boxB = quad.box;
					final float uMinA, uMaxA, uMinB, uMaxB;
					final float vMinA, vMaxA, vMinB, vMaxB;
					if (face.axis == Axis.Y) {
						uMinA = boxA.min.x;
						uMaxA = boxA.max.x;
						uMinB = boxB.min.x;
						uMaxB = boxB.max.x;
						vMinA = boxA.min.z;
						vMaxA = boxA.max.z;
						vMinB = boxB.min.z;
						vMaxB = boxB.max.z;
					} else {
						var axis = face.axis.right();
						uMinA = axis.getAxis(boxA.min);
						uMaxA = axis.getAxis(boxA.max);
						uMinB = axis.getAxis(boxB.min);
						uMaxB = axis.getAxis(boxB.max);
						vMinA = boxA.min.y;
						vMaxA = boxA.max.y;
						vMinB = boxB.min.y;
						vMaxB = boxB.max.y;
					}
					
					if (uMinA < uMaxB && uMaxA > uMinB && vMinA < vMaxB && vMaxA > vMinB) {
						float cWid = Math.min(uMaxA, uMaxB) - Math.max(uMinA, uMinB);
						float cHei = Math.min(vMaxA, vMaxB) - Math.max(vMinA, vMinB);
						float aWid = uMaxA - uMinA;
						float aHei = vMaxA - vMinA;
						areaCovered += (cWid * cHei) / (aWid * aHei);
						if (areaCovered > 1f-T) return false;
					}
				}
			}
			
			return true;
		}
		
		public Quad reg(TexReg region, @Null UV uv) {
			if (uv == null) {
				reg(region);
				return this;
			}
			this.region = region;
			
			final float
			uOffset = region.u1,
			vOffset = region.v1,
			uScale = region.u2 - uOffset,
			vScale = region.v2 - vOffset;
			
			t1.set(uOffset + uScale * (uv.x2 / 16f), vOffset + vScale * (uv.y2 / 16f));
			t2.set(uOffset + uScale * (uv.x2 / 16f), vOffset + vScale * (uv.y1 / 16f));
			t3.set(uOffset + uScale * (uv.x1 / 16f), vOffset + vScale * (uv.y1 / 16f));
			t4.set(uOffset + uScale * (uv.x1 / 16f), vOffset + vScale * (uv.y2 / 16f));
			
			return this;
		}
		
		/** Set the face first for a proper uv. */
		public Quad reg(TexReg region) {
			this.region = region; 
			
			if (face == null) {
				t1.set(region.u2, region.v2);
				t2.set(region.u2, region.v1);
				t3.set(region.u1, region.v1);
				t4.set(region.u1, region.v2);
				return this;
			}
			
			float 
			cu1, cv1,
			cu2, cv2,
			cu3, cv3,
			cu4, cv4;
			
			switch (face) {
			case UP:
				cu1 = v1.x; cv1 = v1.z;
				cu2 = v2.x; cv2 = v2.z;
				cu3 = v3.x; cv3 = v3.z;
				cu4 = v4.x; cv4 = v4.z;
				break;
			case DOWN:
				cu1 = 1f-v1.x; cv1 = 1f-v1.z;
				cu2 = 1f-v2.x; cv2 = 1f-v2.z;
				cu3 = 1f-v3.x; cv3 = 1f-v3.z;
				cu4 = 1f-v4.x; cv4 = 1f-v4.z;
				break;
			case NORTH:
				cu1 = 1f-v1.x; cv1 = 1f-v1.y;
				cu2 = 1f-v2.x; cv2 = 1f-v2.y;
				cu3 = 1f-v3.x; cv3 = 1f-v3.y;
				cu4 = 1f-v4.x; cv4 = 1f-v4.y;
				break;
			case EAST:
				cu1 = 1f-v1.z; cv1 = 1f-v1.y;
				cu2 = 1f-v2.z; cv2 = 1f-v2.y;
				cu3 = 1f-v3.z; cv3 = 1f-v3.y;
				cu4 = 1f-v4.z; cv4 = 1f-v4.y;
				break;
			case SOUTH:
				cu1 = v1.x; cv1 = 1f-v1.y;
				cu2 = v2.x; cv2 = 1f-v2.y;
				cu3 = v3.x; cv3 = 1f-v3.y;
				cu4 = v4.x; cv4 = 1f-v4.y;
				break;
			case WEST:
				cu1 = v1.z; cv1 = 1f-v1.y;
				cu2 = v2.z; cv2 = 1f-v2.y;
				cu3 = v3.z; cv3 = 1f-v3.y;
				cu4 = v4.z; cv4 = 1f-v4.y;
				break;
			default:
				cu1 = 1; cv1 = 1;
				cu2 = 1; cv2 = 1;
				cu3 = 1; cv3 = 1;
				cu4 = 1; cv4 = 1;
			}
			
			final float
			uOffset = region.u1,
			vOffset = region.v1,
			uScale = region.u2 - uOffset,
			vScale = region.v2 - vOffset;
			
			t1.set(uOffset + uScale * cu1, vOffset + vScale * cv1);
			t2.set(uOffset + uScale * cu2, vOffset + vScale * cv2);
			t3.set(uOffset + uScale * cu3, vOffset + vScale * cv3);
			t4.set(uOffset + uScale * cu4, vOffset + vScale * cv4);
			
			return this;
		}
		
		public void uvLock() {
			reg(region);
		}
		
		public void rotateTex(int rotation) {
			rotate(switch (rotation) {
				case 90 -> 1;
				case 180 -> 2;
				case 270 -> 3;
				default -> 0;
			});
		}
		
		// v3-----v2
		// |       |
		// |       |
		// v4-----v1
		private void rotate(int iteration) {
			for (int i = 0; i < iteration; i++) {
				float x, y;
				x = t1.x;
				y = t1.y;
				
				t1.set(t2);
				t2.set(t3);
				t3.set(t4);
				t4.set(x, y);
			}
		}
		
		private void rotate(Matrix4 mat, Vector3 origin) {
			culling = false;
			borderCollide = false;
			isAlign = false;
			face = null;
			v1.sub(origin).mul(mat).add(origin);
			v2.sub(origin).mul(mat).add(origin);
			v3.sub(origin).mul(mat).add(origin);
			v4.sub(origin).mul(mat).add(origin);
		}
		
		private void rotate(ModelJson model) {
			if (face != null)
			face = face.rotate(model.x, model.y);
			if (model.x != 0) {
				//culling = false;
				//borderCollide = false;
			}
			float a = 0.5f;
			v1.sub(a).mul(model.matrix).add(a);
			v2.sub(a).mul(model.matrix).add(a);
			v3.sub(a).mul(model.matrix).add(a);
			v4.sub(a).mul(model.matrix).add(a);
		}
		
		/** Set face. Will disable shade if set to face with null. */
		public Quad face(@Null Facing face) {
			if (face == this.face) {
				return this;
			}
			this.face = face;
			if (face == null) {
				borderCollide = false;
				return noShade();
			}
			
			if (box != null) {
				borderCollide = MathUtils.isEqual(Math.abs(face.getAxis(box)-0.5f), 0.5f, T);  
			}
			
			return this;
		}
		
		public Quad noShade() {
			shade = false;
			return this;
		}
	}
	
	public class Cube implements Iterable<Quad> {
		public final BoundingBox box;
		public final Quad up;
		public final Quad down;
		public final Quad north;
		public final Quad east;
		public final Quad south;
		public final Quad west;
		
		private Cube(BoundingBox box, Quad up, Quad down, Quad north, Quad east, Quad south, Quad west) {
			this.box = box;
			this.up = up.face(Facing.UP);
			this.down = down.face(Facing.DOWN);
			this.north = north.face(Facing.NORTH);
			this.east = east.face(Facing.EAST);
			this.south = south.face(Facing.SOUTH);
			this.west = west.face(Facing.WEST);
		}

		/** This should called last */
		private void rotate(@Null Rotation rotation) {
			if (rotation == null || MathUtils.isZero(rotation.angle)) return;
			var origin = rotation.origin.cpy().scl(1/16f);
			var mat = new Matrix4(rotation.quat); // TODO: Try setting the matrix's origin.
			forEach(q -> q.rotate(mat, origin));
		}

		public void shade(boolean shade) {
			for (var quad : this) {
				quad.shade = shade;
			}
		}

		public Cube regAll(TexReg region) {
			forEach(q->q.reg(region));
			return this;
		}
		
		public Cube regSide(TexReg region) {
			north.reg(region);
			east.reg(region);
			south.reg(region);
			west.reg(region);
			return this;
		}
		
		/** vertical (up and down) */
		public Cube regVert(TexReg region) {
			up.reg(region);
			down.reg(region);
			return this;
		}
		
		public Cube removeExcept(Collection<Facing> faces) {
			loop :
			for (Facing face : Facing.allIter()) {
				for (Facing toNotRemove : faces) {
					if (face == toNotRemove) {
						continue loop;
					}
				}
				remove(face);
			}
			return this;
		}
		
		public Cube removeExcept(Facing... faces) {
			loop :
			for (Facing face : Facing.allIter()) {
				for (Facing toNotRemove : faces) {
					if (face == toNotRemove) {
						continue loop;
					}
				}
				remove(face);
			}
			return this;
		}
		
		public Cube remove(Facing... faces) {
			quads.removeIf(q -> {
				for (var face : faces) {
					if (face == q.face) {
						return true;
					}
				}
				return false;
			});
			return this;
		}
		
		public Cube remove(Facing face) {
			quads.remove(get(face));
			return this;
		}
		
		public Quad get(Facing face) {
			return switch (face) {
				case UP -> up;
				case DOWN -> down;
				case NORTH -> north;
				case EAST -> east;
				case SOUTH -> south;
				case WEST -> west;
				default -> null;
			};
		}

		@Override
		public Iterator<Quad> iterator() {
			return new Iterator<Quad>() {
				final Iterator<Facing> faces = Facing.allIter().iterator();

				@Override
				public boolean hasNext() {
					return faces.hasNext();
				}

				@Override
				public Quad next() {
					return get(faces.next());
				}
			};
		}
	}
}
