package com.andedit.viewmc.ui;

import static com.andedit.viewmc.Main.main;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.MenuCore;
import com.andedit.viewmc.ui.util.BaseUI;
import com.andedit.viewmc.ui.util.PosOffset;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Align;

public class MenuUI extends BaseUI {

	public MenuUI(MenuCore core) {
		var group = add(new Group());
		group.setUserObject(new PosOffset(0.5f, 0.5f, 0, -20f));
		
		var hasResource = main.resources != null;
		
		var button = new TextButton("Open World", Assets.skin);
		button.setDisabled(!hasResource);
		button.setSize(140, 25);
		button.setPosition(0, 10, Align.center);
		group.addActor(button);
		button.addListener(Util.newListener(() -> {
			core.manager.setUI(new DropWorldUI(core), true);
		}));
		
		button = new TextButton("Resources", Assets.skin.get(hasResource?"default":"flash", TextButtonStyle.class));
		button.setSize(140, 25);
		button.setPosition(0, -30, Align.center);
		group.addActor(button);
		button.addListener(Util.newListener(() -> {
			core.manager.setUI(new DropCoreUI(core), true);
		}));
	}
	
	@Override
	public InputProcessor getInput() {
		return super.getInput();
	}
}
