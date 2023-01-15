package com.andedit.viewermc;

import static com.andedit.viewermc.Main.main;
import static com.badlogic.gdx.Gdx.files;

import com.andedit.viewermc.block.Blocks;
import com.andedit.viewermc.graphic.Camera;
import com.andedit.viewermc.input.contoller.DesktopController;
import com.andedit.viewermc.input.contoller.GameController;
import com.andedit.viewermc.util.Util;
import com.andedit.viewermc.world.World;
import com.andedit.viewermc.world.WorldRenderer;
import com.badlogic.gdx.InputProcessor;

public class GameCore implements Screen {
	
	public final Blocks blocks;
	public final Camera camera;
	public final World world;
	public final WorldRenderer render;

	public final Player player;
	public final GameController controller = new DesktopController();
	
	public GameCore(Blocks blocks) {
		System.out.println("GameCore Started");
		this.blocks = blocks;
		this.camera = new Camera();
		System.gc();
		try {
			this.world = new World(blocks, files.absolute("C:/Users/Owner/AppData/Roaming/MultiMC/instances/Quilt 1.19.3/.minecraft/saves/Large Test"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		render = new WorldRenderer(blocks);
		render.setWorld(world);
		player = new Player(camera, controller);
	}
	
	@Override
	public void show() {
		blocks.init();
		main.setCatched(true);
		System.out.println("GameCore Finished");
	}

	@Override
	public void render() {
		Util.glClear();
		
		player.update();
		camera.update();
		render.render(camera);
		
		controller.clear();
	}
	
	@Override
	public InputProcessor getInput() {
		return controller.getInput();
	}

	@Override
	public void resize(int width, int height) {
		camera.setView(width, height);
	}

	@Override
	public void dispose() {
		blocks.dispose();
		render.dispose();
	}
}
