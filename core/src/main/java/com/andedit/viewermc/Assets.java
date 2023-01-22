package com.andedit.viewermc;

import static com.badlogic.gdx.Gdx.files;

import com.andedit.viewermc.graphic.TexBinder;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.math.MathUtils;

public class Assets {
	
	public static BitmapFont font;
	public static Texture guiTex;
	public static TextureRegion blank;
	
	public static Texture lightMap;
	public static TexBinder lightMapBind;
	
	static void init() {
		font = new BitmapFont(files.internal("textures/mozart.fnt"));
		font.setUseIntegerPositions(false);
		guiTex = font.getRegion().getTexture();
		blank = new TextureRegion(guiTex, 1, 121, 1, 1);
		
		var pixmap = new Pixmap(files.internal("day_light.png"));
		var buffer = pixmap.getPixels();
		
		float brightness = 0.1f;
		for (int i = buffer.position(); i < buffer.limit(); i++) {
			float num = (buffer.get(i) & 0xFF) / 255f;
			num = (float)Math.pow(num, 1.0/2.2);
			num = MathUtils.lerp(num, 1.0f, brightness);
			num = (float)Math.pow(num, 2.2);
			buffer.put(i, (byte)(num * 255f));
		}
		
		lightMap = new Texture(new PixmapTextureData(pixmap, Format.RGB888, false, true));
		lightMap.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		lightMapBind = new TexBinder();
	}
	
	static void dispose() {
		font.dispose();
		lightMap.dispose();
		lightMapBind.dispose();
	}
}
