package com.andedit.viewmc.maker;

import static com.andedit.viewmc.Assets.ssaaShader;
import static com.andedit.viewmc.Assets.quad;
import static com.badlogic.gdx.Gdx.gl;

import com.andedit.viewmc.graphic.QuadIndex;
import com.andedit.viewmc.graphic.TexBinder;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ScreenUtils;

public class MakerFrameSSAA implements MakerFrame {
	
	private final FrameBuffer frameIn, frameOut;
	private final TexBinder binder;
	private final Format format;
	private @Null Pixmap pixmap;
	
	public MakerFrameSSAA(Format format, int width, int height, int gridSize) {
		this.format = format;
		this.frameIn = new FrameBuffer(format, width*gridSize, height*gridSize, true);
		this.frameOut = new FrameBuffer(format, width, height, true);
		
		frameIn.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		frameOut.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		
		binder = new TexBinder();
		binder.bind(frameIn.getColorBufferTexture());
		TexBinder.deactive();
		
		ssaaShader.bind();
		ssaaShader.setUniformi("u_texture", binder.unit);
		ssaaShader.setUniformf("u_dim", getInWidth(), getInHeight());
		ssaaShader.setUniformi("u_gridSize", gridSize);
		gl.glUseProgram(0);
	}
	
	@Override
	public void begin() {
		frameIn.begin();
	}
	
	@Override
	public int getInWidth() {
		return frameIn.getWidth();
	}
	
	@Override
	public int getInHeight() {
		return frameIn.getHeight();
	}
	
	@Override
	public int getOutWidth() {
		return frameOut.getWidth();
	}
	
	@Override
	public int getOutHeight() {
		return frameOut.getHeight();
	}
	
	@Override
	public void end() {
		frameIn.end();
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
		
		QuadIndex.preBind();
		gl.glDisable(GL20.GL_CULL_FACE);
		gl.glDisable(GL20.GL_BLEND);
		frameOut.begin();
		ScreenUtils.clear(Color.BLACK, true);
		ssaaShader.bind();
		quad.bind(ssaaShader);
		gl.glDrawElements(GL20.GL_TRIANGLES, 6, GL20.GL_UNSIGNED_SHORT, 0);
		quad.unbind(ssaaShader);
		gl.glUseProgram(0);
		
		gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
		pixmap.getPixels().clear();
		gl.glReadPixels(0, 0, getOutWidth(), getOutHeight(), Format.toGlFormat(format), GL20.GL_UNSIGNED_BYTE, pixmap.getPixels());
		
		frameIn.begin();
		return pixmap;
	}
	
	@Override
	public void dispose() {
		frameIn.dispose();
		frameOut.dispose();
		binder.dispose();
		if (pixmap != null) pixmap.dispose();
	}
}
