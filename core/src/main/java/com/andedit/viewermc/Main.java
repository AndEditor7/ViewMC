package com.andedit.viewermc;

import com.andedit.viewermc.graphic.FastBatch;
import com.andedit.viewermc.graphic.QuadIndexBuffer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Base {
	public static final Main main = new Main();

	private Batch batch;
	
	@Override
	public void create() {
		QuadIndexBuffer.init();
		stage = new Stage(new ScreenViewport(), batch = new FastBatch());
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, inputs));
	}
	
	@Override
	public void dispose() {
		super.dispose();
		stage.dispose();
		batch.dispose();
		QuadIndexBuffer.dispose();
	}
}