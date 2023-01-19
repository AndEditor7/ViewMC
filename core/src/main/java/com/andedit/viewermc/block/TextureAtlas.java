package com.andedit.viewermc.block;

import static com.badlogic.gdx.Gdx.gl;

import java.io.BufferedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.andedit.viewermc.graphic.TexBinder;
import com.andedit.viewermc.util.ByteArrayOutput;
import com.andedit.viewermc.util.Identifier;
import com.andedit.viewermc.util.Pair;
import com.andedit.viewermc.util.TexReg;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedSet;
import com.badlogic.gdx.utils.StreamUtils;

/**
 * The Block Texture Atlas. It contains all block textures in the texture sheet
 * loaded from a resource file. The uv texture coordinate can be obtained by a
 * identifier. It also contains animated textures that updates when the update
 * method is called.
 */
public class TextureAtlas implements Disposable {
	
	private static final int TEXTURE_SIZE = 16;
	/** Fixed 20 per-second tick rate. It happens in every 50ms interval. */
	private static final float TICK = 50f / 1000f;

	private final Pixmap atlas;
	private final Array<Animated> animated;
	private final ObjectMap<Identifier, TexReg> regMap;
	
	private final TexReg missing;
	private TexBinder binder;
	
	@Null
	private Texture texture;
	private float time = 0;
	private int tick = 0;

	TextureAtlas(ZipFile file, OrderedSet<Identifier> textureIds, ObjectMap<Identifier, ZipEntry> texEntryMap,
			ObjectMap<Identifier, ZipEntry> texAniEntryMap) throws Exception {
		// Load every textures into the pixmap array.
		animated = new Array<>(texAniEntryMap.size);
		var pixList = new Array<Pair<Identifier, Pixmap>>(textureIds.size);
		var bytes = new ByteArrayOutput();
		var buffer = new byte[8192];
		for (var id : textureIds) {
			var entry = texEntryMap.get(id);
			try (var stream = new BufferedInputStream(file.getInputStream(entry))) {
				StreamUtils.copyStream(stream, bytes, buffer);
			}
			var pixmap = new Pixmap(bytes.array(), 0, bytes.size());
			pixList.add(new Pair<>(id, pixmap));
			bytes.reset();
		}

		// Predict the dimension size of the texture atlas and create the pixmap.
		int size = MathUtils.nextPowerOfTwo((int) Math.sqrt((pixList.size+1) * (TEXTURE_SIZE * TEXTURE_SIZE)));
		atlas = new Pixmap(size, size, Pixmap.Format.RGBA8888);
		atlas.setBlending(Pixmap.Blending.None);
		atlas.setFilter(Pixmap.Filter.NearestNeighbour);
		
		regMap = new ObjectMap<Identifier, TexReg>(textureIds.size);
		var pixDispose = new Array<Pixmap>(textureIds.size);
		final int idxSize = size / TEXTURE_SIZE;
		int x=1, y=0;
		
		// Missing texture
		atlas.setColor(Color.MAGENTA);
		atlas.drawRectangle(0, 0, 8, 8);
		atlas.drawRectangle(8, 8, 8, 8);
		atlas.setColor(Color.BLACK);
		atlas.drawRectangle(0, 8, 8, 8);
		atlas.drawRectangle(8, 0, 8, 8);
		missing = newReg(0, 0, size);
		
		for (var pair : pixList) {
			Identifier id = pair.left;
			Pixmap pixmap = pair.right;
			regMap.put(id, newReg(x, y, size));
			
			var entry = texAniEntryMap.get(id);
			if (entry != null) { // if it's an animated texture.
				var stream = new BufferedInputStream(file.getInputStream(entry), 2048);
				var value = new JsonReader().parse(stream);
				stream.close();
				animated.add(new Animated(pixmap, x, y, value.get("animation")));
			} else {
				atlas.drawPixmap(pixmap, x * TEXTURE_SIZE, y * TEXTURE_SIZE);
				pixDispose.add(pixmap);
			}
			
			x++;
			if (x >= idxSize) {
				x = 0; y++;
			}
		}

		pixDispose.forEach(Pixmap::dispose);
		atlas.setFilter(Pixmap.Filter.NearestNeighbour);
	}
	
	public void createTexture() {
		if (texture != null) throw new IllegalStateException("Texture is already created.");
		texture = new Texture(atlas);
		binder = new TexBinder();
	}
	
	public Texture getTexture() {
		return texture;
	}
	
	public void bind() {
		binder.bind(texture);
	}
	
	public int getUnit() {
		return binder.unit;
	}

	/** Only update if the texture is binded. */
	public void update() {
		
		
		time += Gdx.graphics.getDeltaTime();
		if (time > TICK) {
			tick++;
			time -= TICK;
			animated.forEach(a -> a.tick(tick));
			animated.forEach(a -> a.update(tick));
		}
	}
	
	public TexReg getRegion(Identifier id) {
		return regMap.get(id, missing); // TODO: Missing texture
	}

	@Override
	public void dispose() {
		atlas.dispose();
		animated.forEach(Animated::dispose);
		if (texture != null) {
			texture.dispose();
			binder.dispose();
		}
	}
	
	/** Create a new Texture Region. The parameters are just index like a tile position. */
	private static TexReg newReg(int x, int y, float size) {
		x *= TEXTURE_SIZE;
		y *= TEXTURE_SIZE;
		return new TexReg(x/size, y/size, (x+TEXTURE_SIZE)/size, (y+TEXTURE_SIZE)/size);
	}

	private class Animated implements Disposable {
		final Pixmap pixmap; // texture
		final int size; // frames
		final int frametime;
		final boolean interpolate;
		final @Null int frames[];
		final int x, y;
		
		private int index;
		boolean update = true;

		private final Pixmap pixTemp;
		
		Animated(Pixmap pixmap, int x, int y, JsonValue value) {
			this.pixmap = pixmap;
			this.frametime = value.getInt("frametime", 1);
			this.interpolate = value.getBoolean("interpolate", false);
			this.x = x;
			this.y = y;
			
			var frames = value.get("frames");
			this.frames = frames == null ? null : frames.asIntArray();

			this.size = this.frames == null ? pixmap.getHeight() / TEXTURE_SIZE : this.frames.length;
			
			pixTemp = new Pixmap(TEXTURE_SIZE, TEXTURE_SIZE, Pixmap.Format.RGBA8888);
			pixTemp.setBlending(Pixmap.Blending.None);
		}
		
		void tick(int tick) {
			if ((tick % frametime) == 0) {
				index++;
				index %= size;
				update = true;
			}
		}
		
		void update(int tick) {
			if (!update) return;
			update = interpolate; // interpolate
			
			pixTemp.drawPixmap(pixmap, 0, 0, 0, (getIndex()) * TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
			
			if (interpolate) {
				var tempPixels = pixTemp.getPixels();
				var mainPixels = pixmap.getPixels();
				var offset = tempPixels.limit() * getNextIndex();
				var progress = (tick % frametime) / (float)frametime;
				for (int i = 0; i < tempPixels.limit(); i++) {
					var c = MathUtils.lerp(tempPixels.get(i) & 0xFF, mainPixels.get(i+offset) & 0xFF, progress);
					tempPixels.put(i, (byte)c);
				}
			}
			
			gl.glTexSubImage2D(texture.glTarget, 0, x * TEXTURE_SIZE, y * TEXTURE_SIZE, pixTemp.getWidth(), pixTemp.getHeight(), pixTemp.getGLFormat(), pixTemp.getGLType(), pixTemp.getPixels());
		}
		
		private int getIndex() {
			return hasFrames() ? frames[index] : index;
		}
		
		private int getNextIndex() {
			int idx = (index+1) % size;
			return hasFrames() ? frames[idx] : idx;
		}
		
		private boolean hasFrames() {
			return frames != null;
		}

		@Override
		public void dispose() {
			pixmap.dispose();
			pixTemp.dispose();
		}
	}
}
