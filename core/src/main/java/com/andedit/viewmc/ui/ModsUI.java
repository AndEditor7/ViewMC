package com.andedit.viewmc.ui;

import static com.badlogic.gdx.Gdx.files;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.Events;
import com.andedit.viewmc.MenuCore;
import com.andedit.viewmc.loader.ModType;
import com.andedit.viewmc.resource.ModData;
import com.andedit.viewmc.resource.ResourcePacker;
import com.andedit.viewmc.resource.ResourcePacker.ModSection;
import com.andedit.viewmc.ui.actor.DesktopScrollPane;
import com.andedit.viewmc.ui.actor.LogoPreview;
import com.andedit.viewmc.ui.actor.ModInfo;
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
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ModsUI extends BaseUI {
	
	private final MenuCore core;
	private final ResourcePacker packer;
	
	private final Table root = new Table();
	private final Table mods = new Table();
	private final Table info = new Table();
	private final Label labelInfo = new Label(null, Assets.skin);
	private final ModInfo sumInfo;
	
	private @Null Section lastSection;
	
	public ModsUI(MenuCore core) {
		this.core = core;
		this.packer = core.packer;
		add(root);
		root.align(Align.topLeft);
		root.setUserObject(new Vector2(0, 1));
		
		labelInfo.setWrap(true);
		
		var label = new Label("Toggle Mods", Assets.skin);
		label.setAlignment(Align.center);
		root.add(label).center().growX().colspan(2).padTop(6).row();
		
		label = new Label("Drag and drop files into this window to add mods", Assets.skin);
		label.setWrap(true);
		label.setColor(Fonts.GRAY);
		label.setAlignment(Align.center);
		root.add(label).growX().colspan(2).padTop(6).row();
		
		var scroll = new DesktopScrollPane(mods, Assets.skin);
		var table = new Table().background(new TexRegDrawable(Assets.blank, new Color(0, 0, 0, 0.3f))); 
		table.add(scroll).top().growX().expandY();
		root.add(table).grow().pad(5).padLeft(6);
		
		var table2 = new Table();
		table2.add(sumInfo = new ModInfo(core, packer)).growX().padBottom(4).row();
		
		scroll = new DesktopScrollPane(info, Assets.skin);
		table = new Table().background(new TexRegDrawable(Assets.blank, new Color(0, 0, 0, 0.3f))); 
		table.add(scroll).pad(5).top().growX().expandY();
		table2.add(table).grow();
		root.add(table2).grow().pad(5).padLeft(6).row();
		
		table = new Table();
		var butt = new TextButton("Cancel", Assets.skin);
		table.add(butt).prefSize(120, 20).minHeight(20).pad(1, 8, 6, 8);
		butt.addListener(Util.newListener(() -> {
			core.packer.cancel();
			core.manager.setUI(new MenuUI(core), true);
		}));
		
		butt = new TextButton("Go to Packs", Assets.skin);
		table.add(butt).prefSize(140, 20).minHeight(20).pad(1, 8, 6, 8);
		butt.addListener(Util.newListener(() -> {
			core.manager.setUI(new ResourcesUI(core), true);
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
		if (packer.isModDirty) {
			packer.isModDirty = false;
			mods.clearChildren();
			info.clearChildren();
			refresh();
		}
	}
	
	private void refresh() {
		var list = packer.modRes;
		
		var s = new Section(this, core, packer, packer.coreMod, true);
		addSection(s);
		for (var mod : list) {
			addSection(mod);
		}
		
		select(s);
	}
	
	public void select(Section section) {
		if (lastSection != null) {
			lastSection.isSelected = false;
		}
		lastSection = section;
		section.isSelected = true;
		
		var data = (ModData)section.resource.data();
		sumInfo.setMod((ModSection)section.resource);
		info.clearChildren();
		
		var text = labelInfo.getText();
		text.clear();
		if (!data.getDescription().isEmpty()) {
			text.append(data.getDescription()).append("\n\n");
		}
		
		if (!data.getLicense().isEmpty()) {
			text.append("License:\n");
			for (var license : data.getLicense()) {
				text.append("[#aaaaaa]").append(license).append('\n');
			}
			text.append('\n');
		}
		
		if (data.getType() == ModType.FABRIC) {
			if (!data.getAuthors().isEmpty() || !data.getContributors().isEmpty()) {
				text.append("[#ffffff]").append("Credits:\n");
				for (var name : data.getAuthors()) {
					text.append("[#aaaaaa]").append(name).append(" (Author)\n");
				}
				for (var name : data.getContributors()) {
					text.append("[#aaaaaa]").append(name).append(" (Contributor)\n");
				}
			}
		} else if (data.getType() == ModType.FORGE) {
			if (!data.getAuthors().isEmpty()) {
				text.append("[#ffffff]").append("Authors:\n");
				text.append("[#aaaaaa]").append(data.getAuthors().get(0)).append("\n\n");
			}
			if (!data.getContributors().isEmpty()) {
				text.append("[#ffffff]").append("Credits:\n");
				text.append("[#aaaaaa]").append(data.getContributors().get(0));
			}
		} else if (data.getType() == ModType.QUILT) {
			if (!data.getQuiltContributors().isEmpty()) {
				text.append("[#ffffff]").append("Credits:\n");
				for (var pair : data.getQuiltContributors()) {
					text.append("[#aaaaaa]").append(pair.left).append(' ');
					text.append('(');
					for (var role : pair.right) {
						text.append(role).append(", ");
					}
					text.setLength(Math.max(text.length()-2, 0));
					text.append(")\n");
				}
			}
		}
		
		var logo = data.getLogo();
		if (logo.isPresent()) {
			var image = new LogoPreview(logo.get());
			info.add(image).align(Align.topLeft).expand().pad(2).padBottom(6).row();
		}
		
		labelInfo.invalidateHierarchy();
		info.add(labelInfo).grow();
	}
	
	private void addSection(ModSection mod) {
		addSection(new Section(this, core, packer, mod, false));
	}
	
	private void addSection(Section section) {
		mods.add(section).growX().pad(2, 3, 1, 3).row();
	}
	
	@Override
	public void resize(Viewport view) {
		root.setSize(view.getWorldWidth(), view.getWorldHeight());
	}
}
