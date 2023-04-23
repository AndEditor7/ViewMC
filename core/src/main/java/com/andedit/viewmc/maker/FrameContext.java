package com.andedit.viewmc.maker;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Disposable;

public class FrameContext implements Disposable {
	
	private FrameBuffer frame;
	private TextureRegion texture;
	private boolean useAlpha = true;
	
	public FrameContext() {
		
	}
	
	public FrameContext(int width, int height) {
		this(true, width, height);
	}
	
	public FrameContext(boolean useAlpha, int width, int height) {
		setFrame(useAlpha, width, height);
	}
	
	public void setFrame(boolean useAlpha, int width, int height) {
		this.useAlpha = useAlpha;
		if (frame != null) {
			if (frame.getWidth() == width && frame.getHeight() == height) {
				return;
			}
			frame.dispose(); 
		}
		frame = new FrameBuffer(useAlpha ? Format.RGBA8888 : Format.RGB888, width, height, true);
		texture = new TextureRegion(frame.getColorBufferTexture());
		texture.flip(false, true);
	}
	
	public void setFrame(int width, int height) {
		setFrame(useAlpha, width, height);
	}
	
	public void setFrame(boolean useAlpha) {
		setFrame(useAlpha, frame.getWidth(), frame.getHeight());
	}
	
	public boolean useAlpha() {
		return useAlpha;
	}
	
	public FrameBuffer getFrame() {
		return frame;
	}
	
	public TextureRegion getTexture() {
		return texture;
	}

	@Override
	public void dispose() {
		if (frame != null) frame.dispose(); 
	}
}
