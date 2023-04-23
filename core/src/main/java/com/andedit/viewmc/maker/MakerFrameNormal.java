package com.andedit.viewmc.maker;

import static com.badlogic.gdx.Gdx.gl;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Null;

public class MakerFrameNormal implements MakerFrame {
	
	private final FrameBuffer frame;
	private final Format format;
	private @Null Pixmap pixmap;
	
	public MakerFrameNormal(Format format, int width, int height) {
		this.format = format;
		this.frame = new FrameBuffer(format, width, height, true);
	}
	
	@Override
	public void begin() {
		frame.begin();
	}
	
	@Override
	public int getInWidth() {
		return frame.getWidth();
	}
	
	@Override
	public int getInHeight() {
		return frame.getHeight();
	}
	
	@Override
	public int getOutWidth() {
		return frame.getWidth();
	}

	@Override
	public int getOutHeight() {
		return frame.getHeight();
	}
	
	@Override
	public void end() {
		frame.end();
	}
	
	/** Read framebuffer to pixmap */
	@Override
	public Pixmap read() {
		if (pixmap == null) pixmap = new Pixmap(getOutWidth(), getOutHeight(), format);
		return read(pixmap);
	}
	
	/** Create pixmap from framebuffer */
	@Override
	public Pixmap create() {
		return read(new Pixmap(getOutWidth(), getOutHeight(), format));
	}
	
	private Pixmap read(Pixmap pixmap) {
		gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
		pixmap.getPixels().clear();
		gl.glReadPixels(0, 0, getOutWidth(), getOutHeight(), Format.toGlFormat(format), GL20.GL_UNSIGNED_BYTE, pixmap.getPixels());
		return pixmap;
	}
	
	@Override
	public void dispose() {
		frame.dispose();
		if (pixmap != null) pixmap.dispose();
	}
}
