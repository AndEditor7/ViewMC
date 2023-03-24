package com.andedit.viewmc.ui.actor;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.MenuCore;
import com.andedit.viewmc.loader.ModType;
import com.andedit.viewmc.resource.Core;
import com.andedit.viewmc.resource.ResourcePacker;
import com.andedit.viewmc.resource.ResourcePacker.ModSection;
import com.andedit.viewmc.ui.DropCoreUI;
import com.andedit.viewmc.ui.drawable.QuestionBox;
import com.andedit.viewmc.util.Fonts;
import com.andedit.viewmc.util.McFontCache;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class ModInfo extends Widget {
	
	private static final QuestionBox question = new QuestionBox();
	

	private ModSection mod;
	private final McFontCache title = new McFontCache();
	private final McFontCache desc = new McFontCache();
	
	private final ClickListener listener;
	
	public ModInfo(MenuCore core, ResourcePacker packer) {
		desc.setColor(Fonts.GRAY);
		
		Runnable clicked = () -> {
			if (mod.data instanceof Core) {
				packer.removeCore();
				core.manager.setUI(new DropCoreUI(core), true);
			} else {
				mod.isDisabled = !mod.isDisabled;
			}
		};
		
		addListener(listener = new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				clicked.run();
			}
		});
	}
	
	public void setMod(ModSection mod) {
		this.mod = mod;
		invalidate();
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		validate();
		var image = mod.data.getImage();
		
		if (image.isPresent()) 
		batch.draw(image.get(), getX(), getY(), 32, 32);
		else question.draw(batch, getX(), getY(), 32, 32);
		
		if (listener.isOver()) {
			batch.setColor(0, 0, 0, 0.6f);
			batch.draw(Assets.blank, getX(), getY(), 32, 32);
			batch.setColor(Color.FIREBRICK);
			batch.draw(Assets.cross, getX(), getY(), 32, 32);
			batch.setColor(Color.WHITE);
		}
		
		title.setPosition(getX()+35, getY()+31);
		desc.setPosition(getX()+35, getY()+21);
		title.draw(batch);
		desc.draw(batch);
	}
	
	@Override
	public void layout() {
		var data = mod.data;
		var builder = new StringBuilder();
		title.setText(builder.append(data.getTitle()).append(" (").append(data.getType()).append(')'), 0, 0, getWidth()-35, Align.left, "...");
		builder.setLength(0);
		builder.append(data.getVersion()).append('\n');
		
		if (data.getType() == ModType.QUILT) {
			if (!data.getQuiltContributors().isEmpty()) {
				builder.append("By ");
				for (var pair : data.getQuiltContributors()) {
					builder.append(pair.left).append(", ");
				}
				builder.setLength(Math.max(builder.length()-2, 0));
			}
		} else {
			if (!data.getAuthors().isEmpty()) {
				builder.append("By ");
				for (var name : data.getAuthors()) {
					builder.append(name).append(", ");
				}
				builder.setLength(Math.max(builder.length()-2, 0));
			}
		}
		
		desc.setText(builder, 0, 0, getWidth()-35, Align.left, "...");
	}

	@Override
	public float getPrefWidth() {
		return 0;
	}

	@Override
	public float getPrefHeight() {
		return 32;
	}
	
	@Override
	public float getMaxHeight() {
		return 32;
	}
}
