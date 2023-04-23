package com.andedit.viewmc;

import static com.badlogic.gdx.Gdx.files;

import com.andedit.viewmc.graphic.TexBinder;
import com.andedit.viewmc.graphic.renderer.ViewRenderer;
import com.andedit.viewmc.graphic.vertex.Vertex;
import com.andedit.viewmc.ui.drawable.FlashNinePatchDrawable;
import com.andedit.viewmc.ui.drawable.TexRegDrawable;
import com.andedit.viewmc.util.McBitmapFont;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.FloatArray;

public class Assets {
	private static final String DEFAULT = "default";
	
	public static BitmapFont font;
	public static Texture guiTex;
	public static TextureRegion blank, cross, question;
	public static NinePatch frame;
	public static TextureRegion upArrow, downArrow, rightArrow, leftArrow;
	
	public static Texture lightMap;
	public static TexBinder lightMapBind;
	
	public static Texture pack;
	
	public static ViewRenderer viewRenderer;
	public static ShaderProgram ssaaShader;
	public static Vertex quad;
	
	public static final Skin skin = new Skin();
	
	public static BitmapFontCache font() {
		return new BitmapFontCache(font);
	}
	
	static void init() {
		font = new McBitmapFont(files.internal("textures/rose.fnt"));
		font.setUseIntegerPositions(false);
		font.getData().setLineHeight(10);
		font.getData().markupEnabled = true;
		guiTex = font.getRegion().getTexture();
		
		blank = new TextureRegion(guiTex, 1, 121, 1, 1);
		cross = new TextureRegion(guiTex, 13, 112, 16, 16);
		question = new TextureRegion(guiTex, 0, 100, 5, 8);
		
		upArrow = new TextureRegion(guiTex, 0, 109, 11, 7);
		rightArrow = new TextureRegion(guiTex, 5, 117, 7, 11);
		downArrow = new TextureRegion(upArrow);
		downArrow.flip(false, true);
		leftArrow = new TextureRegion(rightArrow);
		leftArrow.flip(true, false);
		
		frame = new NinePatch(new TextureRegion(guiTex, 0, 124, 4, 4), 1, 1, 1, 1);
		
		viewRenderer = new ViewRenderer();
		ssaaShader = Util.newShader("shaders/ssaa");
		
		var attributes = new VertexAttributes(new VertexAttribute(Usage.Position, 2, "a_position"));
		quad = Vertex.newVbo(attributes, GL20.GL_STATIC_DRAW);
		
		var a = 1f;
		var floatArray = new FloatArray(8);
		floatArray.add(-a, -a);
		floatArray.add(-a, a);
		floatArray.add(a, a);
		floatArray.add(a, -a);
		
		quad.setVertices(floatArray.items, floatArray.size, 0);
		
		var pixmap = new Pixmap(files.internal("day_light.png"));
		var buffer = pixmap.getPixels();
		
		float brightness = 0.06f;
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
		
		pack = new Texture("pack.png");
		
		skin();
	}
	
	private static void skin() {
		skin.add(DEFAULT, font);
		skin.add(DEFAULT, new LabelStyle(font, Color.WHITE));

		var frameDraw = new NinePatchDrawable(frame);
		Drawable drawable = new TexRegDrawable(blank, new Color(0.1f, 0.1f, 0.1f, 1f));
		var field = new TextFieldStyle(font, Color.WHITE, new TexRegDrawable(blank),
				new TexRegDrawable(blank, new Color(0.3f, 0.3f, 1, 1)), drawable);
		drawable.setLeftWidth(4);
		
		skin.add(DEFAULT, field);

		var button = new ButtonStyle();
		button.up = frameDraw.tint(Color.FIREBRICK);
		button.disabled = frameDraw.tint(new Color(0.2f, 0.2f, 0.2f, 1));
		button.checked = frameDraw.tint(new Color(77/255f, 184/255f, 27/255f, 1));
		skin.add("checked", button);
		
		button = new ButtonStyle();
		button.up = frameDraw.tint(new Color(0.5f, 0.5f, 0.5f, 1));
		button.down = frameDraw.tint(new Color(0.4f, 0.5f, 0.6f, 1));
		button.disabled = frameDraw.tint(new Color(0.2f, 0.2f, 0.2f, 1));
		skin.add(DEFAULT, button);
		
		
		var textButton = new TextButtonStyle();
		textButton.font = font;
		textButton.up = button.up;
		textButton.down = button.down;
		textButton.disabled = button.disabled;
		skin.add(DEFAULT, textButton);
		
		var flashFrameDraw = new FlashNinePatchDrawable(frame, new Color(0.5f, 0.5f, 0.5f, 1), new Color(0.65f, 0.65f, 0.4f, 1));
		button = new ButtonStyle();
		button.up = flashFrameDraw;
		button.down = flashFrameDraw.tint(new Color(0.4f, 0.5f, 0.6f, 1), new Color(0.6f, 0.6f, 0.5f, 1));
		button.disabled = button.disabled;
		skin.add("flash", button);
		
		textButton = new TextButtonStyle();
		textButton.font = font;
		textButton.up = button.up;
		textButton.down = button.down;
		textButton.disabled = button.disabled;
		skin.add("flash", textButton);
		
		var colorUp = new Color(0.8f, 0.8f, 0.8f, 1);
		var colorDown = new Color(0.7f, 0.8f, 0.9f, 1);
		var colorDis = new Color(0.3f, 0.3f, 0.3f, 1);
		
		button = new ButtonStyle();
		button.up = new TexRegDrawable(upArrow, colorUp);
		button.down = new TexRegDrawable(upArrow, colorDown);
		button.disabled = new TexRegDrawable(upArrow, colorDis);
		skin.add("up arrow", button);
		
		button = new ButtonStyle();
		button.up = new TexRegDrawable(downArrow, colorUp);
		button.down = new TexRegDrawable(downArrow, colorDown);
		button.disabled = new TexRegDrawable(downArrow, colorDis);
		skin.add("down arrow", button);
		
		button = new ButtonStyle();
		button.up = new TexRegDrawable(leftArrow, colorUp);
		button.down = new TexRegDrawable(leftArrow, colorDown);
		button.disabled = new TexRegDrawable(leftArrow, colorDis);
		skin.add("left arrow", button);
		
		button = new ButtonStyle();
		button.up = new TexRegDrawable(rightArrow, colorUp);
		button.down = new TexRegDrawable(rightArrow, colorDown);
		button.disabled = new TexRegDrawable(rightArrow, colorDis);
		skin.add("right arrow", button);
		
		button = new ButtonStyle();
		button.up = new TexRegDrawable(cross, Color.FIREBRICK);
		button.down = new TexRegDrawable(cross, Color.FIREBRICK.cpy().lerp(Color.BLACK, 0.3f));
		skin.add("cross", button);
		
		var scroll = new ScrollPaneStyle();
		scroll.hScroll = new TexRegDrawable(blank, Color.BLACK);
		scroll.hScroll.setMinWidth(6);
		scroll.hScrollKnob = new NinePatchDrawable(frame);
		scroll.hScrollKnob.setMinWidth(6);
		scroll.vScroll = scroll.hScroll;
		scroll.vScrollKnob = scroll.hScrollKnob;
		skin.add(DEFAULT, scroll);
		
		var nine = new NinePatch(frame, Color.DARK_GRAY);
		nine.setTopHeight(12);
		var window = new WindowStyle(font, Color.WHITE, new NinePatchDrawable(nine));
		window.stageBackground = new TexRegDrawable(blank, new Color(0,0,0,0.5f));
		skin.add(DEFAULT, window);
		
		var list = new ListStyle();
		list.font = font;
		list.selection = frameDraw.tint(new Color(0.35f, 0.4f, 0.6f, 1));
		list.background = frameDraw.tint(new Color(0.1f, 0.1f, 0.1f, 1));
		skin.add(DEFAULT, list);
		
		var box = new SelectBoxStyle();
		box.font = font;
		box.background = frameDraw.tint(new Color(0.5f, 0.5f, 0.5f, 1));
		box.backgroundDisabled = frameDraw.tint(new Color(0.2f, 0.2f, 0.2f, 1));
		box.scrollStyle = skin.get(ScrollPaneStyle.class);
		box.listStyle = skin.get(ListStyle.class);
		skin.add(DEFAULT, box);
	}
	
	static void dispose() {
		font.dispose();
		lightMap.dispose();
		lightMapBind.dispose();
		pack.dispose();
		viewRenderer.dispose();
		quad.dispose();
		ssaaShader.dispose();
	}
}
