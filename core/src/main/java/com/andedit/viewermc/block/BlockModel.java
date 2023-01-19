package com.andedit.viewermc.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.block.container.Block;
import com.andedit.viewermc.block.model.BlockModelJson;
import com.andedit.viewermc.block.model.Face;
import com.andedit.viewermc.block.model.Rotation;
import com.andedit.viewermc.block.model.UV;
import com.andedit.viewermc.block.state.ModelJson;
import com.andedit.viewermc.graphic.Lighting;
import com.andedit.viewermc.graphic.MeshBuilder;
import com.andedit.viewermc.util.Facing;
import com.andedit.viewermc.util.Facing.Axis;
import com.andedit.viewermc.util.IntsFunction;
import com.andedit.viewermc.util.TexReg;
import com.andedit.viewermc.util.Util;
import com.andedit.viewermc.world.Lights;
import com.andedit.viewermc.world.World;
import com.badlogic.gdx.graphics.Color;
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
	
	public final ArrayList<Quad> quads = new ArrayList<>();
	public final ArrayList<BoundingBox> boxes = new ArrayList<>();
	
	/** ambient occlusion */
	public boolean ao = true;
	
	private boolean isFullCube;
	
	public BlockModel() {
		
	}
	
	private BlockModel(BlockModel oldModel, ModelJson model) {
		ao = oldModel.ao;
		isFullCube = oldModel.isFullCube;
		
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
	
	public void build(World world, MeshBuilder builder, BlockState state, int x, int y, int z) {
		for (int i=0,s=quads.size(); i < s; i++) {
			quads.get(i).build(world, builder, state, x, y, z);
		}
	}
	
	public void getQuads(Collection<Quad> collection) {
		for (int i=0,s=quads.size(); i < s; i++) {
			collection.add(quads.get(i));
		}
	}
	
	public void getBoxes(Collection<BoundingBox> collection) {
		for (int i=0,s=boxes.size(); i < s; i++) {
			collection.add(boxes.get(i));
		}
	}
	
	public boolean isFullOpaque() {
		return isFullCube;
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
		
		if (box.getWidth() > 0.99f && box.getHeight() > 0.99f && box.getDepth() > 0.99f) {
			isFullCube = true;
		}
		
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
	
	private static int signum(float a) {
		return MathUtils.round(Math.signum((a-0.5f)));
	}
	
	private static float dst(float a, float b) {
		a = 1f - (Math.abs(a - 0.5f) * 2.0f);
		b = 1f - (Math.abs(b - 0.5f) * 2.0f);
		return MathUtils.clamp(Math.min(a, b), 0f, 1f);
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
		public boolean culling = true, cullable = true;
		public boolean isAlign = true;
		private boolean borderCollide;
		public TexReg region = TexReg.FULL;
		
		public final BoundingBox box;
		
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
			cullable = quad.cullable;
			this.box = box;
		}

		private Quad(BoundingBox box) {
			this.box = box;
		}
		
		private void init(Face value, BlockModelJson model, TextureAtlas textures) {
			tintIndex = value.tintIndex;
			culling = value.culling;
			var region = textures.getRegion(model.getTexture(value.texture));
			reg(region, value.uv);
			rotateTex(value.rotation);
		}

		public void build(World world, MeshBuilder builder, BlockState state, int x, int y, int z) {
			List<Quad> quads = builder.quads;
			quads.clear();
			
			if (borderCollide && culling) {
				//var off = pos.offset(face);
				//if (World.isOutBound(off)) {
					//return;
				//}
				var nState = world.getBlockState(x+face.xOffset, y+face.yOffset, z+face.zOffset);
				if (!state.canRender(nState, face, x, y, z)) return;
				nState.getQuads(quads, x+face.xOffset, y+face.yOffset, z+face.zOffset);
			}
			
			if (canRender(quads)) {
				render(world, builder, state, x, y, z);
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
				if (quad.borderCollide && quad.cullable && face == quad.face.invert()) {
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
		
		void render(World world, MeshBuilder builder, BlockState state, int x, int y, int z) {
			final float xf = x, yf = y, zf = z;
			float shadeLight;
			builder.setColor(tintIndex == -1 ? Color.WHITE_FLOAT_BITS : BlockColors.getColorFloat(state, world, x, y, z, tintIndex));
			
			shadeLight = shade ? Lighting.getShade(face) : 1;
			
			// Light coord
			int x0 = x + getOffset(Axis.X);
			int y0 = y + getOffset(Axis.Y);
			int z0 = z + getOffset(Axis.Z);
			
			int light = world.getLight(x0, y0, z0);
			
			//if (false) {
			if (ao && face != null) { // ambient occlusion mode
				Facing upFace = face.getUpFace();
				Facing rightFace = face.getRightFace();
				
				int x1 = x + face.offsetValue((Axis.X));
				int y1 = y + face.offsetValue((Axis.Y));
				int z1 = z + face.offsetValue((Axis.Z));
				
				for (int i = 0; i < 4; i++) {
					Vector3 v = getVert(i);
					Vector2 t = getUV(i);
					
					// Mystery horizontal axis coordinate. The face is considered forward/normal facing.
					float a = rightFace.axis.getAxis(v);
					float b = upFace.axis.getAxis(v);
					
					int aSign = signum(a);
					int bSign = signum(b);
					
					int x2 = (aSign*rightFace.axis.getInt(Axis.X)) + (bSign*upFace.axis.getInt(Axis.X));
					int y2 = (aSign*rightFace.axis.getInt(Axis.Y)) + (bSign*upFace.axis.getInt(Axis.Y));
					int z2 = (aSign*rightFace.axis.getInt(Axis.Z)) + (bSign*upFace.axis.getInt(Axis.Z));
					boolean state1 = world.getBlockState(x1+x2, y1+y2, z1+z2).isFullOpque(x1+x2, y1+y2, z1+z2);
					int lit = world.getLight(x0+x2, y0+y2, z0+z2);
					x2 = aSign*rightFace.axis.getInt(Axis.X);
					y2 = aSign*rightFace.axis.getInt(Axis.Y);
					z2 = aSign*rightFace.axis.getInt(Axis.Z);
					boolean stateA = world.getBlockState(x1+x2, y1+y2, z1+z2).isFullOpque(x1+x2, y1+y2, z1+z2);
					int litA = world.getLight(x0+x2, y0+y2, z0+z2);
					x2 = bSign*upFace.axis.getInt(Axis.X);
					y2 = bSign*upFace.axis.getInt(Axis.Y);
					z2 = bSign*upFace.axis.getInt(Axis.Z);
					boolean stateB = world.getBlockState(x1+x2, y1+y2, z1+z2).isFullOpque(x1+x2, y1+y2, z1+z2);
					int litB = world.getLight(x0+x2, y0+y2, z0+z2);
					
					float dst = dst(a, b);
					
					int level = vertAO(world.getBlockState(x1, y1, z1).isFullOpque(x1, y1, z1), stateA, stateB, state1);
					float ao = Lighting.getAmbient(level);
					
					float blockLight = MathUtils.lerp(calcLight(Lights.BLOCK, light, litA, litB, lit), Lights.toBlockF(light), dst);
					float skyLight = MathUtils.lerp(calcLight(Lights.SKY, light, litA, litB, lit), Lights.toSkyF(light), dst);
					
					builder.setLight(shadeLight * ao, blockLight, skyLight);
					builder.vert(v.x+xf, v.y+yf, v.z+zf, t.x, t.y);
				}
			} else {
				builder.setLight(shadeLight, Lights.toBlockF(light), Lights.toSkyF(light));
				builder.vert(v1.x+xf, v1.y+yf, v1.z+zf, t1.x, t1.y);
				builder.vert(v2.x+xf, v2.y+yf, v2.z+zf, t2.x, t2.y);
				builder.vert(v3.x+xf, v3.y+yf, v3.z+zf, t3.x, t3.y);
				builder.vert(v4.x+xf, v4.y+yf, v4.z+zf, t4.x, t4.y);
			}
		}
		
		private static int vertAO(boolean center, boolean side1, boolean side2, boolean corner) {
			//if (side1 && side2) return 1 - toInt(center);
			return 4 - (toInt(center) + toInt(side1) + toInt(side2) + toInt(corner));
		}
		
		private static int toInt(boolean bool) {
			return bool ? 1 : 0;
		}
		
		private static float calcLight(IntsFunction function, int center, int sideA, int sideB, int corner) {
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

			final float value = lightTotal / (float)lightCount;
			return MathUtils.round(value) / Lights.SCL;
		}
		
		/** Get face offset value for lighting */
		int getOffset(Axis axis) {
			return face != null && borderCollide ? face.offsetValue(axis) : 0;
		}
		
		Vector3 getVert(int i) {
			return switch (i) {
			case 0 -> v1;
			case 1 -> v2;
			case 2 -> v3;
			case 3 -> v4;
			default -> null;
			};
		}
		
		Vector2 getUV(int i) {
			return switch (i) {
			case 0 -> t1;
			case 1 -> t2;
			case 2 -> t3;
			case 3 -> t4;
			default -> null;
			};
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
		
		private void rotate(Matrix4 mat, Vector3 origin, boolean rescale) {
			culling = false;
			borderCollide = false;
			cullable = false;
			isAlign = false;
			isFullCube = false;
			rescale(v1.sub(origin).mul(mat).add(origin), rescale);
			rescale(v2.sub(origin).mul(mat).add(origin), rescale);
			rescale(v3.sub(origin).mul(mat).add(origin), rescale);
			rescale(v4.sub(origin).mul(mat).add(origin), rescale);
		}
		
		private static void rescale(Vector3 v, boolean rescale) {
			if (rescale)
			v.set(MathUtils.round(v.x), MathUtils.round(v.y), MathUtils.round(v.z));
		}
		
		private void rotate(ModelJson model) {
			if (face != null)
			face = face.rotate(model.x, model.y);
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
				cullable = false;
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
			forEach(q -> q.rotate(mat, origin, rotation.rescale));
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
