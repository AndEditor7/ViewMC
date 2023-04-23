package com.andedit.viewmc.ui.actor;

import java.util.function.Supplier;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Scaling;

public class FramePreview extends Widget {

	public Scaling scaling = Scaling.fit;
	public boolean flip = true;
	
	private final Supplier<FrameBuffer> frame;
	private final TextureRegion texture = new TextureRegion();
	
	public FramePreview(FrameBuffer frame) {
		this(() -> frame);
	}
	
	public FramePreview(Supplier<FrameBuffer> frame) {
		this.frame = frame;
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		var scale = scaling.apply(getPrefWidth(), getPrefHeight(), getWidth(), getHeight());
		
		var width = scale.x;
		var height = scale.y;
		var x = (getWidth() - width) * 0.5f + getX();
		var y = (getHeight() - height) * 0.5f + getY();
		
		texture.setRegion(frame.get().getColorBufferTexture());
		texture.flip(false, flip);
		batch.draw(texture, x, y, width, height);
	}
	
	@Override
	public float getMinWidth() {
		return 16;
	}
	
	@Override
	public float getMinHeight() {
		return 16;
	}
	
	@Override
	public float getPrefWidth() {
		return frame.get().getWidth();
	}
	
	@Override
	public float getPrefHeight() {
		return frame.get().getHeight();
	}
}
