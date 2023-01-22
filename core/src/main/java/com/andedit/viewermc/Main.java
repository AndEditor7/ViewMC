package com.andedit.viewermc;

import static com.badlogic.gdx.Gdx.gl;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.andedit.viewermc.block.BlockLoader;
import com.andedit.viewermc.block.Blocks;
import com.andedit.viewermc.graphic.MeshVert;
import com.andedit.viewermc.graphic.QuadIndex;
import com.andedit.viewermc.graphic.SkyBox;
import com.andedit.viewermc.graphic.TexBinder;
import com.andedit.viewermc.input.InputHolder;
import com.andedit.viewermc.util.API;
import com.andedit.viewermc.util.Util;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
	public static final Main main = new Main();
	
	public static API api;

	public StageUI stage;
	
	private final InputHolder inputs = new InputHolder();
	private Screen screen;
	private @Null Screen newScreen;
	private boolean isCatched;
	
	public SkyBox skyBox;
	
	@Override
	public void create() {
		QuadIndex.init();
		MeshVert.preInit();
		MeshVert.init(Util.newShader("shaders/mesh"));
		stage = new StageUI(new ScreenViewport());
		Gdx.input.setInputProcessor(new InputMultiplexer(Debugs.INPUT, stage, inputs));
		
		ShaderProgram.pedantic = false;
		gl.glEnable(GL20.GL_DEPTH_TEST);
		gl.glCullFace(GL20.GL_BACK);
		gl.glDepthFunc(GL20.GL_LEQUAL);
		gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		
		//gl.glLineWidth(2);
		//api.glPolygonMode(GL20.GL_FRONT_AND_BACK, false);
		
		skyBox = new SkyBox();
		
		var file = Gdx.files.absolute("C:/Users/Owner/Documents/Files/1.19.3.jar");
		setScreen(new LoaderCore<Blocks>(() -> Statics.meshExe.submit(new BlockLoader(file)), b -> setScreen(new GameCore(b))));
		try {
			//setScreen(new GameCore(new BlockLoader(file).call()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		Statics.init();
		Assets.init();
	}
	
	private void nextScreen() {
		if (newScreen == null) return;
		
		screen = newScreen;
		newScreen = null;

		stage.clear(); // Always clear UI when switching screen.
		inputs.clear();
		setCatched(false);
		//inputLocks.clear();

		screen.show();
		inputs.set(screen.getInput());
	}
	
	public void setScreen(Screen screen) {
		if (newScreen != null) {
			newScreen.dispose();
		}
		newScreen = screen;
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
		screen.render();
		Gdx.gl.glUseProgram(0);
		gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		stage.act();
		stage.draw();
		Gdx.gl.glUseProgram(0);
	}
	
	@Override
	public void resize(int width, int height) {
		var view = (ScreenViewport)stage.getViewport();
		view.setUnitsPerPixel(1 / (float)Math.max(1, MathUtils.round(height/320f))); // 0.0065f
		view.update(width, height, true);
		if (screen != null) {
			screen.resize(width, height);
		}
		stage.resize();
	}
	
	@Override
	public void dispose() {
		screen.dispose();
		if (newScreen != null) {
			newScreen.dispose();
		}
		stage.dispose();
		skyBox.dispose();
		QuadIndex.dispose();
		Assets.dispose();
		Statics.dispose();
	}
}