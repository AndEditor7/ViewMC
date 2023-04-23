package com.andedit.viewmc;

import static com.badlogic.gdx.Gdx.app;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.Gdx.input;

import com.andedit.viewmc.graphic.MeshVert;
import com.andedit.viewmc.graphic.QuadIndex;
import com.andedit.viewmc.graphic.SkyBox;
import com.andedit.viewmc.graphic.renderer.Renderers;
import com.andedit.viewmc.input.InputHolder;
import com.andedit.viewmc.resource.ResourcePacker;
import com.andedit.viewmc.resource.Resources;
import com.andedit.viewmc.util.API;
import com.andedit.viewmc.util.Logger;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
	public static final Main main = new Main();
	
	public static final Logger LOGGER = new Logger("Main");
	
	public static API api;
	public static Array<Runnable> postInits = new Array<Runnable>();

	public StageUI stage;
	
	private final InputHolder inputsAfter = new InputHolder();
	private final InputHolder inputsBefore = new InputHolder();
	private final Array<Runnable> updates = new Array<>();
	private Screen screen;
	private @Null Screen newScreen;
	private boolean isCatched;
	
	public SkyBox skyBox;
	public Renderers renderers;
	public ResourcePacker packer;
	public @Null Resources resources;
	
	@Override
	public void create() {
		//app.setLogLevel(ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp")?Logger.DEBUG:Logger.INFO);
		app.setLogLevel(Logger.DEBUG);
		
		QuadIndex.init();
		MeshVert.preInit();
		stage = new StageUI(new ScreenViewport());
		input.setInputProcessor(new InputMultiplexer(Debugs.INPUT, inputsBefore, stage, inputsAfter));
		
		ShaderProgram.pedantic = false;
		gl.glEnable(GL20.GL_DEPTH_TEST);
		gl.glCullFace(GL20.GL_BACK);
		gl.glDepthFunc(GL20.GL_LEQUAL);
		gl.glClearColor(0.17f, 0.17f, 0.17f, 1);
		
		//gl.glLineWidth(2);
		//api.glPolygonMode(GL20.GL_FRONT_AND_BACK, false);
		
		skyBox = new SkyBox();
		renderers = new Renderers();
		
		Statics.init();
		Assets.init();
		
		postInits.forEach(Runnable::run);
		postInits.clear();
		
		try {
			//setScreen(new GameCore(new ResourceLoader(files).call()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		packer = new ResourcePacker();
		setScreen(new MenuCore());
	}
	
	public void setResources(@Null Resources resources) {
		if (this.resources != null) {
			this.resources.dispose();
		}
		this.resources = resources;
		resources.init();
		System.gc();
	}
	
	private void nextScreen() {
		if (newScreen == null) return;
		
		if (screen != null) {
			screen.hide();
		}
		
		screen = newScreen;
		newScreen = null;

		stage.clear(); // Always clear UI when switching screen.
		screen.show();
		inputsBefore.set(screen.getInputBefore());
		inputsAfter.set(screen.getInputAfter());
		
		System.gc();
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
		input.setCursorCatched(isCatched);
		setCursorPos(!isCatched);
		this.isCatched = isCatched;
	}
	
	public void setCursorPos(boolean centor) {
		if (centor) {
			input.setCursorPosition(Util.getW()>>1, Util.getH()>>1);
		} else {
			input.setCursorPosition(0, 0);
		}
	}
	
	public void postUpdate(Runnable run) {
		updates.add(run);
	}
	
	public void event(Events event, Object obj) {
		if (screen != null) {
			screen.event(event, obj);
		}
	}
	
	@Override
	public void render() {
		updates.forEach(Runnable::run);
		nextScreen();
		screen.render();
		gl.glUseProgram(0);
		stage.act();
		stage.draw();
		gl.glUseProgram(0);
	}
	
	@Override
	public void resize(int width, int height) {
		var view = (ScreenViewport)stage.getViewport();
		view.setUnitsPerPixel(1 / (float)Math.max(1, MathUtils.round(height/320f))); // 320f
		view.update(width, height, true);
		if (screen != null) {
			screen.resize(view);
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
		renderers.dispose();
		if (resources != null) resources.dispose();
		packer.dispose();
		QuadIndex.dispose();
		Assets.dispose();
		Statics.dispose();
	}
}