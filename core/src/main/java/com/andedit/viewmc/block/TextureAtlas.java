package com.andedit.viewmc.block;

import static com.andedit.viewmc.resource.ResourceLoader.LOGGER;

import static com.badlogic.gdx.Gdx.gl;

import com.andedit.viewmc.graphic.TexBinder;
import com.andedit.viewmc.graphic.TextureBlend;
import com.andedit.viewmc.resource.RawResources;
import com.andedit.viewmc.resource.texture.TextureJson;
import com.andedit.viewmc.util.Identifier;
import com.andedit.viewmc.util.Pair;
import com.andedit.viewmc.util.Progress;
import com.andedit.viewmc.util.TexReg;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * The Block Texture Atlas. It contains all block textures in the texture sheet
 * loaded from a resource file. The uv texture coordinate can be obtained by a
 * identifier. It also contains animated textures that updates when the update
 * method is called.
 */
public class TextureAtlas implements Disposable {
	
	static final int TEXTURE_SIZE = 16;
	/** Fixed 20 per-second tick rate. It happens in every 50ms interval. */
	private static final float TICK = 50f / 1000f;

	private final Pixmap atlas;
	private final Array<Animated> animated;
	private final ObjectMap<Identifier, Sprite> spriteMap;
	
	private final Sprite missing;
	private TexBinder binder;
	
	@Null
	private Texture texture;
	private float time = 0;
	private int tick = 0;
	
	public TextureAtlas(RawResources assets, Progress progress) {
		// Load every textures into the pixmap array.
		
		progress.setStatus("Loading Textures");
		progress.newStep(assets.blockTextures.size);
		animated = new Array<>(assets.blockTextureMetas.size);
		var pixList = new Array<Pair<Identifier, Pixmap>>(assets.blockTextures.size);
		for (var entry : assets.blockTextures) {
			var pixmap = new Pixmap(entry.value, 0, entry.value.length);
			pixList.add(new Pair<>(entry.key, pixmap));
			progress.incStep();
		}

		// Predict the dimension size of the texture atlas and create the pixmap.
		int totalSize = 0;
		for (var pair : pixList) {
			var pixmap = pair.right;
			totalSize += pixmap.getWidth() * pixmap.getHeight();
		}
		
		totalSize = (int)(totalSize / 0.8f);
		int size = MathUtils.nextPowerOfTwo((int) Math.sqrt(totalSize));
		int width = size;
		int height = size;
		if (width * (height >> 1) > totalSize) {
			height >>= 1;
		}
		var pair = new TextureMaker(width, height).build(assets, pixList, animated, this, progress);
		atlas = pair.left;
		spriteMap = pair.right;
		missing = spriteMap.get(new Identifier("missing"));
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
	
	public Sprite getSprite(Identifier id) {
		var sprite = spriteMap.get(id);
		if (sprite == null) {
			sprite = missing;
			LOGGER.info("Missing texture: " + id);
		}
		return spriteMap.get(id, sprite);
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
	
	public static class Sprite {
		public final TextureBlend blend;
		public final TexReg region;
		
		Sprite(TextureBlend blend, TexReg region) {
			this.blend = blend;
			this.region = region;
		}
	}

	class Animated implements Disposable {
		final Pixmap pixmap; // texture
		final int width, height;
		final int length; // frames
		final int frametime;
		final boolean interpolate;
		final @Null int frames[];
		final int x, y;
		
		private int index;
		boolean update = true;

		private final Pixmap pixTemp;
		
		Animated(Pixmap pixmap, int x, int y, TextureJson json) {
			this.pixmap = pixmap;
			this.frametime = json.frametime;
			this.interpolate = json.interpolate;
			this.x = x;
			this.y = y;
			
			this.frames = json.frames;

			this.width = json.width(pixmap);
			this.height = json.height(pixmap);
			this.length = this.frames == null ? pixmap.getHeight() / height : this.frames.length;
			pixTemp = new Pixmap(width, height, pixmap.getFormat());
			pixTemp.setBlending(Pixmap.Blending.None);
		}
		
		void tick(int tick) {
			if ((tick % frametime) == 0) {
				index++;
				index %= length;
				update = true;
			}
		}
		
		void update(int tick) {
			if (!update) return;
			//update = interpolate; // interpolate
			
			pixTemp.drawPixmap(pixmap, 0, 0, 0, (getIndex()) * height, width, height);
			
			if (false) {
				var tempPixels = pixTemp.getPixels();
				var mainPixels = pixmap.getPixels();
				var offset = tempPixels.limit() * getNextIndex();
				var progress = (tick % frametime) / (float)frametime;
				if (tempPixels.limit() != mainPixels.limit()) {
					for (int i = 0; i < tempPixels.limit(); i++) {
						var c = MathUtils.lerp(tempPixels.get(i) & 0xFF, mainPixels.get(i+offset) & 0xFF, progress);
						tempPixels.put(i, (byte)c);
					}
				}
				
			}
			
			gl.glTexSubImage2D(texture.glTarget, 0, x * TEXTURE_SIZE, y * TEXTURE_SIZE, pixTemp.getWidth(), pixTemp.getHeight(), pixTemp.getGLFormat(), pixTemp.getGLType(), pixTemp.getPixels());
		}
		
		private int getIndex() {
			return hasFrames() ? frames[index] : index;
		}
		
		private int getNextIndex() {
			int idx = (index+1) % length;
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
