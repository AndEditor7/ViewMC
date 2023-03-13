package com.andedit.viewmc.ui.actor;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.MenuCore;
import com.andedit.viewmc.resource.ResourcePacker;
import com.andedit.viewmc.resource.ResourcePacker.ModSection;
import com.andedit.viewmc.resource.ResourcePacker.PackSection;
import com.andedit.viewmc.resource.ResourceSection;
import com.andedit.viewmc.ui.DropCoreUI;
import com.andedit.viewmc.ui.ModsUI;
import com.andedit.viewmc.ui.drawable.QuestionBox;
import com.andedit.viewmc.ui.util.UiUtil;
import com.andedit.viewmc.util.Fonts;
import com.andedit.viewmc.util.McFontCache;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class Section extends WidgetGroup {

	private static final QuestionBox question = new QuestionBox();
	
	public final ResourceSection resource;
	
	private final McFontCache titleFont, descriptionFont;
	private final boolean isPack;
	public boolean isSelected;
	
	private final ClickListener listener;
	
	{
		this.titleFont = new McFontCache();
		this.descriptionFont = new McFontCache();
		this.descriptionFont.setColor(Fonts.GRAY);
		this.descriptionFont.maxLine = 2;
	}
	
	public Section(MenuCore core, ResourcePacker packer, PackSection resource, boolean available) {
		this(core, packer, resource, available, false);
	}
	
	public Section(MenuCore core, ResourcePacker packer, PackSection resource, boolean available, boolean isCore) {
		this.resource = resource;
		
		if (!isCore)
		if (available) {
			var butt = new SimpleButt(Assets.skin.get("right arrow", ButtonStyle.class));
			UiUtil.scale(butt, 2);
			butt.setPosition(16, 16, Align.center);
			addActor(butt);
			butt.addListener(Util.newListener(() -> {
				packer.move(resource);
			}));
		} else {
			var butt = new SimpleButt(Assets.skin.get("left arrow", ButtonStyle.class));
			UiUtil.scale(butt, 2);
			butt.setPosition(9, 16, Align.center);
			addActor(butt);
			butt.addListener(Util.newListener(() -> {
				packer.move(resource);
			}));
			
			butt = new SimpleButt(Assets.skin.get("up arrow", ButtonStyle.class));
			butt.setPosition(24, 24, Align.center);
			addActor(butt);
			butt.addListener(Util.newListener(() -> {
				packer.swap(resource, true);
			}));
			
			butt = new SimpleButt(Assets.skin.get("down arrow", ButtonStyle.class));
			butt.setPosition(24, 8, Align.center);
			addActor(butt);
			butt.addListener(Util.newListener(() -> {
				packer.swap(resource, false);
			}));
		}
		
		if (isCore) {
			var butt = new SimpleButt(Assets.skin.get("cross", ButtonStyle.class));
			butt.setSize(32, 32);
			butt.setPosition(16, 16, Align.center);
			addActor(butt);
			butt.addListener(Util.newListener(() -> {
				packer.removeCore();
				core.manager.setUI(new DropCoreUI(core), true);
			}));
		}
		
		addListener(listener = new ClickListener());
		isPack = true;
	}
	
	public Section(ModsUI ui, MenuCore core, ResourcePacker packer, ModSection resource, boolean isCore) {
		this.resource = resource;
		addListener(listener = new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ui.select(Section.this);
			}
		});
		isPack = false;
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		validate();
		var data = resource.data();
		
		if (isSelected && !isPack) {
			batch.setColor(Color.LIGHT_GRAY);
			batch.draw(Assets.blank, getX()-1, getY()-1, getWidth()+2, getHeight()+2);
			batch.setColor(Color.BLACK);
			batch.draw(Assets.blank, getX(), getY(), getWidth(), getHeight());
			batch.setColor(Color.WHITE);
		}
		
		var image = data.getImage();
		if (image.isPresent()) 
		batch.draw(image.get(), getX(), getY(), 32, 32);
		else {
			question.draw(batch, getX(), getY(), 32, 32);
		}
		
		if (listener.isOver() && isPack) {
			batch.setColor(0, 0, 0, 0.7f);
			batch.draw(Assets.blank, getX(), getY(), 32, 32);
			batch.setColor(Color.WHITE);
		}
		
		titleFont.setPosition(getX()+35, getY()+31);
		descriptionFont.setPosition(getX()+35, getY()+21);
		titleFont.draw(batch);
		descriptionFont.draw(batch);
		
		if (listener.isOver() && isPack) {
			super.draw(batch, parentAlpha);
		}
		
		if (resource instanceof ModSection mod && mod.isDisabled) {
			batch.setColor(0, 0, 0, 0.5f);
			batch.draw(Assets.blank, getX(), getY(), getWidth(), getHeight());
			batch.setColor(Color.FIREBRICK);
			batch.draw(Assets.cross, getX(), getY(), 32, 32);
			batch.setColor(Color.WHITE);
		}
	}
	
	@Override
	public void layout() {
		var data = resource.data();
		titleFont.setText(data.getTitle(), 0, 0, getWidth()-35, Align.left, "...");
		descriptionFont.setText(data.getDescription(), 0, 0, getWidth()-35, Align.left, true);
	}
	
	@Override
	public float getMinWidth() {
		return 0;
	}
	
	@Override
	public float getMinHeight() {
		return 32;
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
