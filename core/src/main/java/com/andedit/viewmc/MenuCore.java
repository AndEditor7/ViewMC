package com.andedit.viewmc;

import static com.andedit.viewmc.Main.main;
import static com.badlogic.gdx.Gdx.gl;

import com.andedit.viewmc.input.BasicInput;
import com.andedit.viewmc.resource.ResourcePacker;
import com.andedit.viewmc.ui.MenuUI;
import com.andedit.viewmc.ui.ModsUI;
import com.andedit.viewmc.ui.util.UIManager;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MenuCore implements Screen {
	
	public final UIManager manager;
	
	public final ResourcePacker packer;
	
	private final BasicInput input = new BasicInput();

	public MenuCore() {
		packer = main.packer;
		manager = new UIManager(main.stage);
		manager.setUI(new MenuUI(this), false);
	}
	
	@Override
	public void show() {
		manager.reload();
		gl.glClearColor(0.10f, 0.15f, 0.2f, 1);
	}

	@Override
	public void hide() {
		dispose();
	}
	
	@Override
	public void event(Events event, Object arg) {
		if (manager.getCurrentUI() != null) {
			manager.getCurrentUI().event(event, arg);
		}
	}
	
	@Override
	public void dispose() {
		
	}

	@Override
	public void render() {
		gl.glClearColor(20/255f, 25/255f, 35/255f, 1);
		Util.glClear();
		
		if (input.isKeyJustPressed(Keys.F4)) {
			manager.setUI(new ModsUI(this), true);
		}

		manager.update();
		input.reset();
	}
	
	@Override
	public InputProcessor getInput() {
		return new InputMultiplexer(manager.input, input);
	}

	@Override
	public void resize(Viewport view) {
		manager.resize(view);
	}
}
