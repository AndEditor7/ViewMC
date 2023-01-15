package com.andedit.viewermc;

import static com.badlogic.gdx.Gdx.gl;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.andedit.viewermc.block.BlockLoader;
import com.andedit.viewermc.block.Blocks;
import com.andedit.viewermc.graphic.MeshVert;
import com.andedit.viewermc.graphic.QuadIndex;
import com.andedit.viewermc.input.InputHolder;
import com.andedit.viewermc.util.Util;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
	public static final Main main = new Main();

	public StageUI stage;
	public ExecutorService executor;
	
	private final InputHolder inputs = new InputHolder();
	private Optional<Screen> screen = Optional.empty();
	private Optional<Screen> newScreen = Optional.empty();
	private boolean isCatched;
	
	@Override
	public void create() {
		QuadIndex.init();
		MeshVert.preInit();
		MeshVert.init(Util.newShader("shaders/mesh"));
		stage = new StageUI(new ScreenViewport());
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, inputs));
		
		ShaderProgram.pedantic = false;
		gl.glEnable(GL20.GL_DEPTH_TEST);
		gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		gl.glCullFace(GL20.GL_BACK);
		gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		
		executor = Executors.newSingleThreadExecutor();
		var file = Gdx.files.absolute("C:/Users/Owner/Documents/Files/1.19.3.jar");
		//setScreen(new LoaderCore<Blocks>(e -> e.submit(new BlockLoader(file)), b -> setScreen(new GameCore(b))));
		try {
			setScreen(new GameCore(new BlockLoader(file).call()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void nextScreen() {
		if (newScreen.isEmpty()) return;
		
		screen = newScreen;
		newScreen = Optional.empty();

		stage.clear(); // Always clear UI when switching screen.
		inputs.clear();
		setCatched(false);
		//inputLocks.clear();

		if (screen.isPresent()) {
			screen.get().show();
			inputs.set(screen.get().getInput());
		}
	}
	
	public void setScreen(Screen screen) {
		newScreen.ifPresent(Screen::dispose);
		newScreen = Optional.of(screen);
	}
	
	public boolean isCatched() {
		return isCatched;
	}
	
	public void setCatched(boolean isCatched) {
		Gdx.input.setCursorCatched(isCatched);
		this.isCatched = isCatched;
	}
	
	public void setCursorPos(boolean centor) {
		if (centor) {
			Gdx.input.setCursorPosition(Util.getW()>>1, Util.getH()>>1);
		} else {
			Gdx.input.setCursorPosition(0, 0);
		}
	}
	
	@Override
	public void render() {
		nextScreen();
		screen.ifPresent(Screen::render);
		Gdx.gl.glUseProgram(0);
		
		stage.act();
		stage.draw();
		Gdx.gl.glUseProgram(0);
	}
	
	@Override
	public void resize(int width, int height) {
		screen.ifPresent(s -> s.resize(width, height));
	}
	
	@Override
	public void dispose() {
		screen.ifPresent(Screen::dispose);
		newScreen.ifPresent(Screen::dispose);
		stage.dispose();
		executor.shutdown();
		QuadIndex.dispose();
	}
}