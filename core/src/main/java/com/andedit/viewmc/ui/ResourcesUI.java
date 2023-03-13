package com.andedit.viewmc.ui;

import static com.badlogic.gdx.Gdx.files;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.Events;
import com.andedit.viewmc.MenuCore;
import com.andedit.viewmc.resource.ResourcePacker;
import com.andedit.viewmc.ui.actor.DesktopScrollPane;
import com.andedit.viewmc.ui.actor.Section;
import com.andedit.viewmc.ui.drawable.TexRegDrawable;
import com.andedit.viewmc.ui.util.BaseUI;
import com.andedit.viewmc.util.Fonts;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ResourcesUI extends BaseUI {
	
	private final MenuCore core; 
	private final ResourcePacker packer;
	
	private final Table root = new Table();
	private final Table availableTable = new Table();
	private final Table selectedTable = new Table();
	
	public ResourcesUI(MenuCore core) {
		this.core = core;
		this.packer = core.packer;
		add(root);
		root.setUserObject(new Vector2(0, 1));
		root.align(Align.topLeft);
		
		var label = new Label("Select Resource Packs", Assets.skin);
		label.setAlignment(Align.center);
		root.add(label).center().growX().colspan(2).padTop(6).row();
		
		label = new Label("Drag and drop files into this window to add packs", Assets.skin);
		label.setWrap(true);
		label.setColor(Fonts.GRAY);
		label.setAlignment(Align.center);
		root.add(label).center().growX().colspan(2).padTop(6).row();
		
		var scroll = new DesktopScrollPane(availableTable, Assets.skin);
		var table = new Table().background(new TexRegDrawable(Assets.blank, new Color(0, 0, 0, 0.3f)));
		label = new Label("Available", Assets.skin);
		label.setAlignment(Align.center);
		table.add(label).center().growX().pad(3).row();
		table.add(scroll).top().growX().expandY();
		root.add(table).grow().pad(5).padLeft(6);
		
		scroll = new DesktopScrollPane(selectedTable, Assets.skin);
		table = new Table().background(new TexRegDrawable(Assets.blank, new Color(0, 0, 0, 0.3f)));
		label = new Label("Selected", Assets.skin);
		label.setAlignment(Align.center);
		table.add(label).center().growX().pad(3).row();
		table.add(scroll).top().growX().expandY();
		root.add(table).grow().pad(5).padRight(6);
		root.row();
		
		table = new Table();
		var butt = new TextButton("Cancel", Assets.skin);
		table.add(butt).prefSize(120, 20).minHeight(20).pad(1, 8, 6, 8);
		butt.addListener(Util.newListener(() -> {
			core.packer.cancel();
			core.manager.setUI(new MenuUI(core), true);
		}));
		
		butt = new TextButton("Go to Mods", Assets.skin);
		table.add(butt).prefSize(140, 20).minHeight(20).pad(1, 8, 6, 8);
		butt.addListener(Util.newListener(() -> {
			core.manager.setUI(new ModsUI(core), true);
		}));
		
		butt = new TextButton("Done", Assets.skin);
		table.add(butt).prefSize(120, 20).minHeight(20).pad(1, 8, 6, 8);
		butt.addListener(Util.newListener(() -> {
			core.manager.setUI(LoadingUI.of(packer, core), true);
		}));
		
		root.add(table).colspan(2);
		
		refresh();
	}
	
	@Override
	public void event(Events event, Object arg) {
		if (event == Events.FILES_DROPPED) {
			var strings = (String[])arg;
			for (var string : strings) {
				try {
					packer.add(files.absolute(string));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void update() {
		if (packer.isPackDirty) {
			packer.isPackDirty = false;
			selectedTable.clearChildren();
			availableTable.clearChildren();
			refresh();
		}
	}
	
	private void refresh() {
		var list = packer.selectedRes;
		for (int i = list.size()-1; i >= 0; i--) {
			addSection(selectedTable, new Section(core, packer, list.get(i), false));
		}
		
		addSection(selectedTable, new Section(core, packer, packer.corePack, false, true));
		
		for (var res : packer.availableRes) {
			addSection(availableTable, new Section(core, packer, res, true));
		}
	}
	
	public void addSection(Table table, Section section) {
		table.add(section).growX().pad(3, 3, 0, 3).row();
	}
	
	@Override
	public void resize(Viewport view) {
		root.setSize(view.getWorldWidth(), view.getWorldHeight());
	}
}
