package com.andedit.viewmc.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.block.TextureAtlas.Sprite;
import com.andedit.viewmc.graphic.Lighting;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.graphic.RenderLayer;
import com.andedit.viewmc.graphic.TextureBlend;
import com.andedit.viewmc.resource.blockmodel.BlockModelJson;
import com.andedit.viewmc.resource.blockmodel.Face;
import com.andedit.viewmc.resource.blockmodel.Rotation;
import com.andedit.viewmc.resource.blockmodel.UV;
import com.andedit.viewmc.resource.blockstate.ModelJson;
import com.andedit.viewmc.util.Cull;
import com.andedit.viewmc.util.Facing;
import com.andedit.viewmc.util.Facing.Axis;
import com.andedit.viewmc.util.Identifier;
import com.andedit.viewmc.util.Pair;
import com.andedit.viewmc.util.TexReg;
import com.andedit.viewmc.util.Util;
import com.andedit.viewmc.world.BlockView;
import com.andedit.viewmc.world.Lights;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Null;

// v3-----v2
// |       |
// |       |
// v4-----v1
public class BlockModel implements BlockLike, Iterable<Quad> {
	
	/** tolerance */
	private static final float T = 0.001f;
	
	public final ArrayList<Quad> quads = new ArrayList<>();
	public final ArrayList<Pair<BoundingBox, List<Quad>>> boxes = new ArrayList<>();
	
	/** ambient occlusion */
	public boolean ao = true;
	
	private boolean isFullOpaque;
	
	public BlockModel() {
		
	}
	
	private BlockModel(BlockModel oldModel, ModelJson model) {
		ao = oldModel.ao;
		isFullOpaque = oldModel.isFullOpaque;
		
		for (var pair : oldModel.boxes) {
			var box = new BoundingBox(pair.left);
			var list = new ArrayList<Quad>(pair.right.size());
			for (var quad : pair.right) {
				var newQuad = new Quad(quad, box);
				quads.add(newQuad);
				list.add(newQuad);
			}
			boxes.add(new Pair<>(box, list));
		}
		
		if (model.hasTransformation()) {
			for (var quad : quads) {
				quad.rotate(model);
			}
			boxes.forEach(b -> Util.mul(b.left, model.matrix));
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
		
		loop :
		for (var pair : boxes) {
			var box = pair.left;
			if (box.getWidth() > 0.99f && box.getHeight() > 0.99f && box.getDepth() > 0.99f) {
				for (var quad : pair.right) {
					if (quad.blend != TextureBlend.SOILD) {
						continue loop;
					}
				}
				isFullOpaque = true;
			}
		}
	}
	
	public static BlockModel missingModel(Sprite sprite) {
		var model = new BlockModel();
		model.ao = false;
		var cube = model.cube(0, 0, 0, 16, 16, 16);
		cube.regAll(sprite);
		return model;
	}
	
	public BlockModel create(ModelJson model) {
		if (model.hasTransformation() || model.uvLock) {
			return new BlockModel(this, model);
		}
		return this;
	}
	
	@Override
	public void build(MeshProvider provider, BlockView view, BlockState state, int x, int y, int z) {
		provider.lighting.reset();
		for (int i=0,s=quads.size(); i < s; i++) {
			quads.get(i).build(view, provider, state, x, y, z);
		}
	}
	
	@Override
	public void getQuads(Collection<Quad> collection, BlockView view, BlockState state, int x, int y, int z) {
		for (int i=0,s=quads.size(); i < s; i++) {
			collection.add(quads.get(i));
		}
	}

	@Override
	public void getBoxes(Collection<BoundingBox> collection, BlockView view, BlockState state, int x, int y, int z) {
		for (int i=0,s=boxes.size(); i < s; i++) {
			collection.add(boxes.get(i).left);
		}
	}

	@Override
	public boolean isFullOpaque(BlockView view, BlockState state, int x, int y, int z) {
		return isFullOpaque;
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
		
//		if (box.getWidth() > 0.99f && box.getHeight() > 0.99f && box.getDepth() > 0.99f) {
//			isFullCube = true;
//		}
		
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
		
		public final BoundingBox box;
		
		public int tintIndex = -1;
		public boolean shade = true;
		public boolean isAlign = true;
		public boolean allowRender = false; // Allows other quad to render.
		public TextureBlend blend = TextureBlend.SOILD;
		private TexReg region;
		private @Null Identifier texAnimated;
		
		private @Null Facing face;
		private RenderLayer layer = RenderLayer.SOILD;
		private boolean culling = true;
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
			texAnimated = quad.texAnimated;
			borderCollide = quad.borderCollide;
			allowRender = quad.allowRender;
			layer = quad.layer;
			blend = quad.blend;
			this.box = box;
		}

		private Quad(BoundingBox box) {
			this.box = box;
		}
		
		private void init(Face value, BlockModelJson model, TextureAtlas textures) {
			tintIndex = value.tintIndex;
			culling = value.culling;
			reg(textures.getSprite(model.getTexture(value.texture)), value.uv);
			rotateTex(value.rotation);
		}

		public void build(BlockView view, MeshProvider provider, BlockState state, int x, int y, int z) {
			List<Quad> quads = provider.quads;
			quads.clear();
			
			@Null
			BlockState nState = face == null ? null : view.getBlockstate(x+face.xOffset, y+face.yOffset, z+face.zOffset);
			if (borderCollide && culling) {
				nState.getQuads(quads, view, x+face.xOffset, y+face.yOffset, z+face.zOffset);
			}
			
			var cull = canRender(quads);
			if (state.canRender(nState, this, face, cull, x, y, z)) {
				render(view, provider, state, x, y, z);
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
		Cull canRender(List<Quad> quads) {
			var boxA = box;
			float areaCoveredAll = 0, areaCovered = 0;
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
						float size = (cWid * cHei) / (aWid * aHei);
						areaCoveredAll += size;
						if (!quad.allowRender) {
							areaCovered += size;
						}
						if (areaCovered > 1f-T) return Cull.CULLED;
					}
				}
			}
			return areaCoveredAll > 1f-T ? Cull.CULLED_BUT_RENDERBALE : Cull.RENDERABLE;
		}
		
		void render(BlockView view, MeshProvider provider, BlockState state, int x, int y, int z) {
			final float xf = x, yf = y, zf = z;
			final var builder = provider.getBuilder(layer);
			builder.setColor(tintIndex == -1 ? Color.WHITE_FLOAT_BITS : BlockColors.getColorFloat(state, view, x, y, z, tintIndex));
			float shadeLight = shade ? Lighting.getShade(face) : 1;
			
			// ambient occlusion mode
			if (shade && ao && face != null) {
				var lighting = provider.lighting;
				var upFace = face.getUpFace();
				var rightFace = face.getRightFace();
				
				//for (int i = 0; i < 9; i++)
				//calc(builder, section, x, y, z, (i%3)-1, (i/3)-1);
				
				if (lighting.needCalculation(face)) {
					lighting.calculate(view, face, x, y, z);
				}
				
				lighting.calculateVert(face, 0, 0,  -1, 0,  0,-1,  -1, -1,  0, 0);
				lighting.calculateVert(face, 0, 0,   0,-1,  1, 0,   1, -1,  1, 0);
				lighting.calculateVert(face, 0, 0,  -1, 0,  0, 1,   -1,  1,  0, 1);
				lighting.calculateVert(face, 0, 0,   1, 0,  0, 1,   1,  1,  1, 1);
				
				for (int i = 0; i < 4; i++) {
					Vector3 v = getVert(i);
					Vector2 t = getUV(i);
					
					// Mystery horizontal axis coordinate. The face is considered forward/normal facing.
					float a = rightFace.axis.getAxis(v);
					float b = upFace.axis.getAxis(v);
					float c = face.axis.getAxis(v);
					
					var result = lighting.getResult(a, b, face.isPositive() ? c : 1f-c);
					
					builder.setLight(shadeLight, result.aoLit, result.blockLit, result.skyLit);
					builder.vert(v.x+xf, v.y+yf, v.z+zf, t.x, t.y);
				}
			} else {
				// Light coord
				int x0 = x + getOffset(Axis.X);
				int y0 = y + getOffset(Axis.Y);
				int z0 = z + getOffset(Axis.Z);
				
				int light = view.getLight(x0, y0, z0);
				builder.setLight(shadeLight, 1, Lights.toBlockF(light), Lights.toSkyF(light));
				builder.vert(v1.x+xf, v1.y+yf, v1.z+zf, t1.x, t1.y);
				builder.vert(v2.x+xf, v2.y+yf, v2.z+zf, t2.x, t2.y);
				builder.vert(v3.x+xf, v3.y+yf, v3.z+zf, t3.x, t3.y);
				builder.vert(v4.x+xf, v4.y+yf, v4.z+zf, t4.x, t4.y);
			}
			
			builder.addTexture(texAnimated);
		}
		
		/** Get face offset value for lighting */
		int getOffset(Axis axis) {
			return face != null && borderCollide ? face.offsetValue(axis) : 0;
		}
		
		public Vector3 getVert(int i) {
			return switch (i) {
			case 0 -> v1;
			case 1 -> v2;
			case 2 -> v3;
			case 3 -> v4;
			default -> null;
			};
		}
		
		public Vector2 getUV(int i) {
			return switch (i) {
			case 0 -> t1;
			case 1 -> t2;
			case 2 -> t3;
			case 3 -> t4;
			default -> null;
			};
		}
		
		void setSprite(Sprite sprite) {
			layer = sprite.blend.getRenderLayer();
			allowRender = sprite.blend != TextureBlend.SOILD;
			blend = sprite.blend;
			texAnimated = sprite.isAnimated ? sprite.id : null;
		}
		
		Quad reg(Sprite sprite, @Null UV uv) {
			setSprite(sprite);
			
			if (uv == null) {
				reg(sprite);
				return this;
			}
			this.region = sprite.region;
			
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
		
		public void reg(Sprite sprite) {
			setSprite(sprite);
			reg(sprite.region);
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
			rotate(rotation / 90);
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
		
		private void rotate(Rotation rotation, Vector3 origin) {
			//face = null;
			culling = false;
			borderCollide = false;
			allowRender = true;
			isAlign = false;
			isFullOpaque = false;
			
			if (rotation.rescale) {
				var scale = 1f + 1f / (MathUtils.cosDeg(45f) - 1f);
				var axis = rotation.axis;
				rescale(v1, origin, axis, scale);
				rescale(v2, origin, axis, scale);
				rescale(v3, origin, axis, scale);
				rescale(v4, origin, axis, scale);
			}
			
			v1.sub(origin).mul(rotation.matrix).add(origin);
			v2.sub(origin).mul(rotation.matrix).add(origin);
			v3.sub(origin).mul(rotation.matrix).add(origin);
			v4.sub(origin).mul(rotation.matrix).add(origin);
		}
		
		private void rescale(Vector3 vert, Vector3 origin, Axis axis, float scale) {
			var vec = axis.getVec();
			vert.x += scale * (vert.x - box.getCenterX()) * (1f - vec.x);
			vert.z += scale * (vert.z - box.getCenterZ()) * (1f - vec.z);
			// TODO implement the Y rescale
		}
		
		private void rotate(ModelJson model) {
			if (face != null)
			face = face.rotate(model.x, model.y);
			final float a = 0.5f;
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
				allowRender = false;
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

		@Null
		public Facing getFace() {
			return face;
		}
	}
	
	public class Cube implements Iterable<Quad> {
		public final Pair<BoundingBox, List<Quad>> pair;
		public final Quad up;
		public final Quad down;
		public final Quad north;
		public final Quad east;
		public final Quad south;
		public final Quad west;
		
		private Cube(BoundingBox box, Quad up, Quad down, Quad north, Quad east, Quad south, Quad west) {
			
			this.up = up.face(Facing.UP);
			this.down = down.face(Facing.DOWN);
			this.north = north.face(Facing.NORTH);
			this.east = east.face(Facing.EAST);
			this.south = south.face(Facing.SOUTH);
			this.west = west.face(Facing.WEST);
			
			var array = new ArrayList<Quad>(6);
			array.add(up);
			array.add(down);
			array.add(north);
			array.add(east);
			array.add(south);
			array.add(west);
			pair = new Pair<>(box, array);
			boxes.add(pair);
		}

		/** This should called last */
		private void rotate(@Null Rotation rotation) {
			if (rotation == null || MathUtils.isZero(rotation.angle)) return;
			var origin = rotation.origin.cpy().scl(1/16f);
			forEach(q -> q.rotate(rotation, origin));
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
		
		public Cube regAll(Sprite sprite) {
			forEach(q->q.reg(sprite));
			return this;
		}
		
		public Cube regSide(Sprite sprite) {
			north.reg(sprite);
			east.reg(sprite);
			south.reg(sprite);
			west.reg(sprite);
			return this;
		}
		
		/** vertical (up and down) */
		public Cube regVert(Sprite sprite) {
			up.reg(sprite);
			down.reg(sprite);
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
			pair.right.removeIf(q -> {
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
			var quad = get(face);
			quads.remove(quad);
			pair.right.remove(quad);
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
			return pair.right.iterator();
		}
	}
}
