package com.andedit.viewmc.ui.actor;

import static com.andedit.viewmc.Assets.blank;
import static com.andedit.viewmc.Assets.font;
import static com.andedit.viewmc.Main.main;
import static com.badlogic.gdx.Gdx.graphics;

import com.andedit.viewmc.GameCore;
import com.andedit.viewmc.ui.util.Alignment;
import com.andedit.viewmc.ui.util.PosOffset;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;

import net.querz.mca.MCAUtil;

public class DebugInfo extends Actor implements Alignment {
	
	private final GameCore core;
	private final StringBuilder builder;
	private final BitmapFontCache cache;
	
	public DebugInfo(GameCore core) {
		this.core = core;
		this.builder = new StringBuilder();
		this.cache = new BitmapFontCache(font);
		setUserObject(new PosOffset(0, 1, 2, -2));
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		int i = 0;
		builder.append("ViewMC. ").append("Rendering Mode: ").append(main.renderers.getRenderer());
		drawText(batch, i++);
		
		var totalMem = Runtime.getRuntime().totalMemory();
		var freeMem = Runtime.getRuntime().freeMemory();
		builder.append(graphics.getFramesPerSecond()).append(" FPS, ");
		builder.append("Memory: ").append(Math.round(((totalMem - freeMem) / (double)totalMem) * 100d)).append("% ").append((totalMem - freeMem) / 1024 / 1024).append('/').append(totalMem / 1024 / 1024).append("mb");
		drawText(batch, i++);
		
		//builder.append("Renderer: ").append(graphics.getGLVersion().getVendorString());
		//drawText(batch, 2);
		
		i++;
		var camPos = core.camera.position;
		int x = camPos.floorX();
		int y = camPos.floorY();
		int z = camPos.floorZ();
		
		builder.append("Block: ").append(x).append(" / ").append(y).append(" / ").append(z);
		drawText(batch, i++);
		builder.append("Chunk: ").append(x>>4).append(" / ").append(y>>4).append(" / ").append(z>>4);
		builder.append(" [").append((x>>4)&31).append(' ').append((z>>4)&31).append(" in ");
		builder.append(MCAUtil.createNameFromRegionLocation(x>>9, z>>9)).append(']');
		drawText(batch, i++);
		//drawText(batch, 1);
	}
	
	void drawText(Batch batch, int line) {
		float y = getY() - (line * 9);
		var layout = cache.setText(builder, getX(), y);
		batch.setColor(0, 0, 0, 0.3f);
		batch.draw(blank, getX() - 1, y - 8, layout.width+2, layout.height + 1);
		batch.setColor(getColor());
		cache.draw(batch);
		builder.setLength(0);
	}

	@Override
	public int getAlign() {
		return Align.topLeft;
	}
}
