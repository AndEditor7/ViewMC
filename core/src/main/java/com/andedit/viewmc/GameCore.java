package com.andedit.viewmc;

import static com.andedit.viewmc.Main.api;
import static com.andedit.viewmc.Main.main;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.Gdx.graphics;

import com.andedit.viewmc.graphic.Camera;
import com.andedit.viewmc.input.InputHolder;
import com.andedit.viewmc.input.contoller.DesktopController;
import com.andedit.viewmc.input.contoller.GameController;
import com.andedit.viewmc.resource.Resources;
import com.andedit.viewmc.ui.GameUI;
import com.andedit.viewmc.ui.util.UIManager;
import com.andedit.viewmc.util.Util;
import com.andedit.viewmc.world.World;
import com.andedit.viewmc.world.WorldRenderer;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameCore implements Screen {
	
	public final Resources resources;
	public final Camera camera;
	public final World world;
	public final WorldRenderer render;
	public final UIManager manager;

	public final Player player;
	public final GameController controller = new DesktopController();
	public final InputHolder inputHolder = new InputHolder();
	
	public GameCore(Resources resources, World world) {
		System.out.println("GameCore Started");
		this.resources = resources;
		this.camera = new Camera();
		this.world = world;
		
		manager = new UIManager(main.stage);
		
		render = new WorldRenderer(main.renderers, resources);
		render.setWorld(world);
		player = new Player(camera, controller, world.level.player);
		
		manager.setUI(new GameUI(this), false);
	}
	
	@Override
	public void show() {
		main.setCatched(true);
		manager.reload();
		gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
	}
	
	@Override
	public void hide() {
		main.setCatched(false);
		dispose();
	}
	
	@Override
	public void event(Events event, Object obj) {
		
	}
	
	private static final float STEP = 0.017f;
	public float speed = 1;
	private float time;

	@Override
	public void render() {
		Util.glClear();
		
		if (manager.isLocked()) {
			controller.reset();
			inputHolder.clear();;
		} else {
			inputHolder.set(controller.getInput());
		}
		
		player.update();
		time += graphics.getDeltaTime();
		do {
			player.fixedUpdate();
			time -= STEP / speed;
		} while (time >= STEP / speed);
		time %= STEP / speed;
		
		manager.update();
		camera.update();
		world.update(camera);
		render.render(camera);
		
		controller.clear();
	}
	
	@Override
	public InputProcessor getInputAfter() {
		return new InputMultiplexer(manager.inputAfter, inputHolder);
	}

	@Override
	public void resize(Viewport view) {
		manager.resize(view);
		camera.setView(view.getScreenWidth(), view.getScreenHeight());
	}

	@Override
	public void dispose() {
		render.dispose();
	}
}
