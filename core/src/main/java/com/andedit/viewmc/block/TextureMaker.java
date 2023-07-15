package com.andedit.viewmc.block;

import static com.andedit.viewmc.block.TextureAtlas.TEXTURE_SIZE;

import com.andedit.viewmc.block.TextureAtlas.Animated;
import com.andedit.viewmc.block.TextureAtlas.Sprite;
import com.andedit.viewmc.graphic.TextureBlend;
import com.andedit.viewmc.resource.RawResources;
import com.andedit.viewmc.util.Identifier;
import com.andedit.viewmc.util.Pair;
import com.andedit.viewmc.util.Progress;
import com.andedit.viewmc.util.TexReg;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

class TextureMaker {
	
	final Pixmap atlas;
	private final int width, height;
	private final int idxWidth, idxHeight;
	
	private final boolean[][] isFilled;
	private int x, y;
	
	TextureMaker(int width, int height) {
		this.atlas = new Pixmap(width, height, Pixmap.Format.RGBA8888);
		this.atlas.setBlending(Pixmap.Blending.None);
		this.atlas.setFilter(Pixmap.Filter.NearestNeighbour);
		this.width = width;
		this.height = height;
		this.idxWidth = width / TEXTURE_SIZE;
		this.idxHeight = height / TEXTURE_SIZE;
		this.isFilled = new boolean[idxWidth][idxHeight];
	}
	
	public ObjectMap<Identifier, Sprite> build(RawResources assets, Array<Pair<Identifier, Pixmap>> pixList, Array<Animated> animatedList, TextureAtlas textures, Progress progress) {
		var spriteMap = new ObjectMap<Identifier, Sprite>(assets.blockTextures.size);
		var pixDispose = new Array<Pixmap>(assets.blockTextures.size);
		
		// Missing texture
		atlas.setColor(Color.MAGENTA);
		atlas.fillRectangle(0, 0, 8, 8);
		atlas.fillRectangle(8, 8, 8, 8);
		atlas.setColor(Color.BLACK);
		atlas.fillRectangle(0, 8, 8, 8);
		atlas.fillRectangle(8, 0, 8, 8);
		spriteMap.put(Identifier.MISSING, new Sprite(TextureBlend.SOLID, new TexReg(0, 0, TEXTURE_SIZE/(float)width, TEXTURE_SIZE/(float)height), new Identifier("missing"), false));
		isFilled[0][0] = true;
		x++;
		
		progress.setStatus("Generating Texture Atlas");
		progress.newStep(pixList.size);
		for (var pair : pixList) {
			Identifier id = pair.left;
			Pixmap pixmap = pair.right;
			progress.incStep();
			
			//System.out.println(id);
			//spriteMap.put(id, newSprite(pixmap, x, y, size));
			
			int width, height;
			var json = assets.blockTextureMetas.get(id);
			boolean isAnimated = json != null;
			if (isAnimated) { // if it's an animated texture.
				width = json.width(pixmap);
				height = json.height(pixmap);
				isAnimated = width != pixmap.getWidth() || height != pixmap.getHeight();
				if (!isAnimated) {
					pixDispose.add(pixmap);
				}
			} else {
				width = pixmap.getWidth();
				height = pixmap.getHeight();
				pixDispose.add(pixmap);
			}
			width = Math.max(width / TEXTURE_SIZE, 1);
			height = Math.max(height / TEXTURE_SIZE, 1);
			
			int x0 = x, y0 = y;
			
			while (true) {
				if (isValidSpot(x0, y0, width, height)) {
					if (isAnimated) { // if it's an animated texture.
						spriteMap.put(id, newSprite(id, pixmap, isAnimated, json.width(pixmap), json.height(pixmap), x0, y0));
						var animated = textures.new Animated(pixmap, x0, y0, json, id);
						animatedList.add(animated);
					} else {
						spriteMap.put(id, newSprite(id, pixmap, false, pixmap.getWidth(), pixmap.getHeight(), x0, y0));
						atlas.drawPixmap(pixmap, x0 * TEXTURE_SIZE, y0 * TEXTURE_SIZE);
					}
					
					for (int xF = 0; xF < width; xF++)
					for (int yF = 0; yF < height; yF++) {
						isFilled[x0+xF][y0+yF] = true;
					}
					break;
				}
				
				x0++;
				if (x0 >= idxWidth) {
					x0 = 0; y0++;
					if (y0 >= idxHeight) {
						throw new RuntimeException();
					}
				}
			}
			
			x++;
			if (x >= idxWidth) {
				x = 0; y++;
				if (y >= idxHeight) {
					throw new RuntimeException();
				}
			}
		}
		
		pixDispose.forEach(Pixmap::dispose);
		atlas.setFilter(Pixmap.Filter.NearestNeighbour);
		
		return spriteMap;
	}
	
	private boolean isValidSpot(int x, int y, int width, int height) {
		if (x+width > idxWidth) {
			return false;
		}
		
		for (int x0 = 0; x0 < width; x0++)
		for (int y0 = 0; y0 < height; y0++) {
			if (isFilled[x+x0][y+y0]) {
				return false;
			}
		}
		
		return true;
	}
	
	/** Create a new Texture Region. The parameters are just index like a tile position. */
	private Sprite newSprite(Identifier id, Pixmap pixmap, boolean isAnimated, int width, int height, int x, int y) {
		var blend = TextureBlend.SOLID;
		
		for (int u = 0; u < pixmap.getWidth(); u++)
		for (int v = 0; v < pixmap.getHeight(); v++) {
			int alpha = pixmap.getPixel(u, v) & 0xFF;
			
			if (alpha < 255) {
				blend = TextureBlend.TRANSPARENT;
				if (alpha > 3) {
					blend = TextureBlend.TRANSLUCENT;
					break;
				}
			}
		}
		
		return newSprite(id, pixmap, blend, isAnimated, width, height, x, y);
	}
	
	/** Create a new Texture Region. The parameters are just index like a tile position. */
	private Sprite newSprite(Identifier id, Pixmap pixmap, TextureBlend blend, boolean isAnimated, int width, int height, int x, int y) {
		x *= TEXTURE_SIZE;
		y *= TEXTURE_SIZE;
		final float w = this.width;
		final float h = this.height;
		return new Sprite(blend, new TexReg(x/w, y/h, (x+width)/w, (y+height)/h), id, isAnimated);
	}
}
