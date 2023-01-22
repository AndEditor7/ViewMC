package com.andedit.viewermc;

import static com.andedit.viewermc.Main.main;
import static com.badlogic.gdx.Gdx.files;

import com.andedit.viewermc.block.Blocks;
import com.andedit.viewermc.graphic.Camera;
import com.andedit.viewermc.graphic.Rendering;
import com.andedit.viewermc.input.contoller.DesktopController;
import com.andedit.viewermc.input.contoller.GameController;
import com.andedit.viewermc.ui.GameUI;
import com.andedit.viewermc.ui.util.UIManager;
import com.andedit.viewermc.util.Util;
import com.andedit.viewermc.world.World;
import com.andedit.viewermc.world.WorldRenderer;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;

public class GameCore implements Screen {
	
	public static volatile Rendering rendering = Rendering.MINECRAFT;
	
	public final Blocks blocks;
	public final Camera camera;
	public final World world;
	public final WorldRenderer render;
	public final UIManager manager;

	public final Player player;
	public final GameController controller = new DesktopController();
	
	public GameCore(Blocks blocks) {
		System.gc();
		
		System.out.println("GameCore Started");
		this.blocks = blocks;
		this.camera = new Camera();
		
		try {
			this.world = new World(blocks, files.absolute("C:/Users/Owner/AppData/Roaming/MultiMC/instances/Quilt 1.19.3/.minecraft/saves/Better"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		manager = new UIManager(main.stage);
		
		render = new WorldRenderer(blocks);
		render.setWorld(world);
		player = new Player(camera, controller);
		
		rendering = Rendering.MINECRAFT;
	}
	
	@Override
	public void show() {
		blocks.init();
		main.setCatched(true);
		manager.setUI(new GameUI(this));
		
		System.out.println("GameCore Finished");
	}

	@Override
	public void render() {
		Util.glClear();
		
		camera.fieldOfView = 70;
		manager.update();
		player.update();
		camera.update();
		world.update(camera);
		render.render(camera);
		
		controller.clear();
	}
	
	@Override
	public InputProcessor getInput() {
		return new InputMultiplexer(manager.input, controller.getInput());
	}

	@Override
	public void resize(int width, int height) {
		manager.resize(main.stage.getViewport());
		camera.setView(width, height);
	}

	@Override
	public void dispose() {
		blocks.dispose();
		render.dispose();
	}
}
