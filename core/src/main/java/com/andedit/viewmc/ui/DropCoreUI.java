package com.andedit.viewmc.ui;

import static com.badlogic.gdx.Gdx.files;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.Events;
import com.andedit.viewmc.MenuCore;
import com.andedit.viewmc.ui.drawable.TexRegDrawable;
import com.andedit.viewmc.ui.util.BaseUI;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

public class DropCoreUI extends BaseUI {
	
	private final MenuCore core;
	private final Table root = new Table();
	
	public DropCoreUI(MenuCore core) {
		this.core = core;
		add(root);
		
		var label = new Label("Drag and drop the Minecraft jar file here to load the required resources", Assets.skin);
		label.setWrap(true);
		label.setAlignment(Align.center);
		
		var table = new Table();
		table.background(new TexRegDrawable(Assets.blank, new Color(0,0,0,0.3f)));
		table.add(label).grow().pad(5);
		
		root.setUserObject(new Vector2(0, 1));
		root.align(Align.topLeft);
		root.add(table).grow().pad(20).padBottom(10).row();
		
		var button = new TextButton("Cancel", Assets.skin);
		root.add(button).prefSize(160, 20).padBottom(8);
		button.addListener(Util.newListener(() -> {
			core.manager.setUI(new MenuUI(core), true);
		}));
	}
	
	@Override
	public void event(Events event, Object arg) {
		if (event == Events.FILES_DROPPED) {
			var strings = (String[])arg;
			if (strings.length == 1) {
				try {
					core.packer.addCore(files.absolute(strings[0]));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
		}
	}
	
	@Override
	public void update() {
		if (core.packer.hasCore()) {
			core.packer.start();
			core.manager.setUI(new ResourcesUI(core), true);
		}
	}
	
	@Override
	public void resize(Viewport view) {
		root.setSize(view.getWorldWidth(), view.getWorldHeight());
	}
}
