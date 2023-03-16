package com.andedit.viewmc.block;

import static com.andedit.viewmc.resource.ResourceLoader.LOGGER;

import static com.badlogic.gdx.Gdx.gl;

import com.andedit.viewmc.graphic.TexBinder;
import com.andedit.viewmc.graphic.TextureBlend;
import com.andedit.viewmc.resource.RawResources;
import com.andedit.viewmc.resource.texture.TextureJson;
import com.andedit.viewmc.resource.texture.TextureJson.Frame;
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
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.OrderedSet;

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
	private final ObjectMap<Identifier, Animated> animatedMap;
	private final ObjectMap<Identifier, Sprite> spriteMap;
	
	private final ObjectSet<Identifier> missingSet = new ObjectSet<>();
	private final Sprite missing;
	private TexBinder binder;
	
	@Null
	private Texture texture;
	private float time = 0;
	private int tick = 0;
	
	public TextureAtlas(RawResources assets, Progress progress) throws Exception {
		// Load every textures into the pixmap array.
		
		progress.setStatus("Loading Textures");
		progress.newStep(assets.blockTextures.size);
		animated = new Array<>(assets.blockTextureMetas.size);
		var pixList = new Array<Pair<Identifier, Pixmap>>(assets.blockTextures.size);
		for (var entry : assets.blockTextures) {
			progress.incStep();
			try {
				var pixmap = new Pixmap(entry.value, 0, entry.value.length);
				pixList.add(new Pair<>(entry.key, pixmap));
			} catch (Exception e) {
				LOGGER.info("Failed to load texture " + entry.key, e);
				missingSet.add(entry.key);
			}
		}

		// Predict the dimension size of the texture atlas and create the pixmap.
		int totalSize = 0;
		for (var pair : pixList) {
			var pixmap = pair.right;
			totalSize += pixmap.getWidth() * pixmap.getHeight();
		}
		
		totalSize = (int)(totalSize / 0.9f);
		int size = MathUtils.nextPowerOfTwo((int) Math.sqrt(totalSize));
		int width = size;
		int height = size;
		if (width * (height >> 1) > totalSize) {
			height >>= 1;
		}
		
		try {
			var pair = new TextureMaker(width, height).build(assets, pixList, animated, this, progress);
			atlas = pair.left;
			spriteMap = pair.right;
			missing = spriteMap.get(new Identifier("missing"));
			animatedMap = new ObjectMap<>(animated.size);
			animated.forEach(a->animatedMap.put(a.id, a));
		} catch (Exception e) {
			pixList.forEach(p -> p.right.dispose());
			animated.forEach(Animated::dispose);
			throw e;
		}
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
	public void update(OrderedSet<Identifier> textureToAnimate) {
		time += Gdx.graphics.getDeltaTime();
		if (time > TICK) {
			if (++tick < 0) tick = 0;
			time -= TICK;
			animated.forEach(a -> a.update(tick));
			for (var id : textureToAnimate) {
				animatedMap.get(id).draw(tick);
			}
		}
	}
	
	public Sprite getSprite(Identifier id) {
		var sprite = spriteMap.get(id);
		if (sprite == null) {
			sprite = missing;
			if (missingSet.add(id)) {
				LOGGER.info("Missing texture: " + id);
			}
		}
		return spriteMap.get(id, sprite);
	}
	
	public boolean isAnimated(Identifier id) {
		return animatedMap.containsKey(id);
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
		public final Identifier id;
		public final boolean isAnimated;
		
		Sprite(TextureBlend blend, TexReg region, Identifier id, boolean isAnimated) {
			this.blend = blend;
			this.region = region;
			this.id = id;
			this.isAnimated = isAnimated;
		}
	}

	class Animated implements Disposable {
		final Pixmap pixmap; // texture
		final Identifier id;
		final int width, height;
		final int length; // frames
		final int frametime;
		final boolean interpolate;
		final Frame[] frames;
		final int x, y;
		
		private int currentTick;
		private int index;
		boolean update = true;

		private final Pixmap pixTemp;
		
		Animated(Pixmap pixmap, int x, int y, TextureJson json, Identifier id) {
			this.pixmap = pixmap;
			this.id = id;
			this.frametime = json.frametime;
			this.interpolate = json.interpolate;
			this.x = x;
			this.y = y;
			
			this.frames = json.frames;

			this.width = json.width(pixmap);
			this.height = json.height(pixmap);
			
			this.length = frames.length == 0 ? pixmap.getHeight() / height : frames.length;
			pixTemp = new Pixmap(width, height, pixmap.getFormat());
			pixTemp.setBlending(Pixmap.Blending.None);
		}
		
		void update(int tick) {
			var t = tick - currentTick;
			if (t < 0 || t >= getFrametime()) {
				index++;
				index %= length;
				update = true;
				currentTick = tick;
			}
		}
		
		void draw(int tick) {
			if (!update) return;
			update = interpolate; // interpolate
			
			pixTemp.drawPixmap(pixmap, 0, 0, 0, getIndex() * height, width, height);
			
			if (interpolate) {
				var frametime = getFrametime();
				var progress = ((tick - currentTick) % frametime) / (float)frametime;
				for (int x = 0; x < width; x++)
				for (int y = 0; y < height; y++) {
					int p = pixmap.getPixel(x, y + (getNextIndex() * height));
					int t = pixTemp.getPixel(x, y);
					
					int r = ((int)MathUtils.lerp((t & 0xff000000) >>> 24, (p & 0xff000000) >>> 24, progress)) << 24;
					int g = ((int)MathUtils.lerp((t & 0x00ff0000) >>> 16, (p & 0x00ff0000) >>> 16, progress)) << 16;
					int b = ((int)MathUtils.lerp((t & 0x0000ff00) >>> 8, (p & 0x0000ff00) >>> 8, progress)) << 8;
					int a = (int)MathUtils.lerp((t & 0x000000ff), (p & 0x000000ff), progress);
					
					pixTemp.drawPixel(x, y, r | g | b | a);
				}
			}
			
			gl.glTexSubImage2D(texture.glTarget, 0, x * TEXTURE_SIZE, y * TEXTURE_SIZE, pixTemp.getWidth(), pixTemp.getHeight(), pixTemp.getGLFormat(), pixTemp.getGLType(), pixTemp.getPixels());
		}
		
		private int getFrametime() {
			return frames.length == 0 ? frametime : frames[getNextIndex()].getFrametime(frametime);
		}
		
		private int getIndex() {
			return frames.length == 0 ? index : frames[index].index;
		}
		
		private int getNextIndex() {
			int idx = (index+1) % length;
			return frames.length == 0 ? idx : frames[idx].index;
		}
		
		@Override
		public void dispose() {
			if (!pixmap.isDisposed()) pixmap.dispose();
			pixTemp.dispose();
		}
	}
}
