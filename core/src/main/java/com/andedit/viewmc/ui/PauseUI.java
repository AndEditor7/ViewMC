package com.andedit.viewmc.ui;

import static com.andedit.viewmc.Main.main;

import java.util.Vector;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.GameCore;
import com.andedit.viewmc.MenuCore;
import com.andedit.viewmc.input.BasicInput;
import com.andedit.viewmc.ui.drawable.TexRegDrawable;
import com.andedit.viewmc.ui.util.BaseUI;
import com.andedit.viewmc.ui.util.PosOffset;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PauseUI extends BaseUI {
	
	private final GameCore core;
	private final BasicInput input;
	private final Image background;

	public PauseUI(GameCore core) {
		this.core = core;
		this.input = new BasicInput();
		
		background = add(new Image(new TexRegDrawable(Assets.blank, new Color(0, 0, 0, 0.6f))));
		background.setUserObject(new Vector<>(0, 1));
		background.setAlign(Align.topLeft);
		
		var group = add(new Group());
		group.setUserObject(new PosOffset(0.5f, 0.5f, 0, -20f));
		
		var hasResource = main.resources != null;
		
		var button = new TextButton("Resume", Assets.skin);
		button.setDisabled(!hasResource);
		button.setSize(140, 25);
		button.setPosition(0, 10, Align.center);
		group.addActor(button);
		button.addListener(Util.newListener(() -> {
			core.manager.setUI(new GameUI(core), true);
		}));
		
		button = new TextButton("Exit World", Assets.skin);
		button.setSize(140, 25);
		button.setPosition(0, -30, Align.center);
		group.addActor(button);
		button.addListener(Util.newListener(() -> {
			main.setScreen(new MenuCore());
		}));
	}
	
	@Override
	public void show() {
		main.setCatched(false);
	}
	
	@Override
	public void hide() {
		main.setCatched(true);
	}
	
	@Override
	public void update() {
		if (input.isKeyJustPressed(Keys.ESCAPE)) {
			core.manager.setUI(new GameUI(core), true);
		}
	}
	
	@Override
	public InputProcessor getInput() {
		return input;
	}
	
	@Override
	public boolean isInputLock() {
		return true;
	}
	
	@Override
	public void resize(Viewport view) {
		background.setSize(view.getWorldWidth(), view.getWorldHeight());
	}
}
