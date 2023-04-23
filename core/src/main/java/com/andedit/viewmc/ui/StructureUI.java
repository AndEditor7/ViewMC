package com.andedit.viewmc.ui;

import static com.andedit.viewmc.Main.main;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.MakerCore;
import com.andedit.viewmc.MenuCore;
import com.andedit.viewmc.ViewCore;
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

public class StructureUI extends BaseUI {
	
	private final ViewCore core;
	private final Table root = new Table();
	private final Table box;
	private final TextButton gifButton;
	
	public StructureUI(ViewCore core) {
		this.core = core;
		add(root);
		
		var label = new Label("Drag and drop the Structure NBT file here to open", Assets.skin);
		label.setWrap(true);
		label.setAlignment(Align.center);
		
		box = new Table();
		box.background(new TexRegDrawable(Assets.blank, new Color(0,0,0,0.3f)));
		box.add(label).grow().pad(5);
		
		root.setUserObject(new Vector2(0, 1));
		root.align(Align.topLeft);
		root.add(box).grow().pad(20).padBottom(10).colspan(2).row();
		
		var button = new TextButton("Close", Assets.skin);
		root.add(button).right().prefSize(70, 16).padBottom(5).padRight(10);
		button.addListener(Util.newListener(() -> {
			main.setScreen(new MenuCore());
		}));
		
		gifButton = new TextButton("Make GIF", Assets.skin);
		gifButton.setDisabled(true);
		root.add(gifButton).left().prefSize(70, 16).padBottom(5).padLeft(10);
		gifButton.addListener(Util.newListener(() -> {
			main.setScreen(new MakerCore(core.resources, core.renderer.getStructure()));
		}));
	}
	
	@Override
	public void update() {
		box.setVisible(!core.renderer.hasStructure());
		gifButton.setDisabled(!core.renderer.hasStructure());
	}
	
	@Override
	public void resize(Viewport view) {
		root.setSize(view.getWorldWidth(), view.getWorldHeight());
	}
}
