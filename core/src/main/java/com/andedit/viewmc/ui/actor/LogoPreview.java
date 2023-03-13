package com.andedit.viewmc.ui.actor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

public class LogoPreview extends Image {
	public LogoPreview(Texture texture) {
		super(texture);
		setAlign(Align.topLeft);
		setScaling(Scaling.fillY);
	}
	
	@Override
	public float getMinWidth() {
		return super.getMinWidth();
	}
	
	@Override
	public float getMinHeight() {
		return super.getMinHeight();
	}
	
	@Override
	public float getPrefWidth() {
		return 0;
	}
	
	@Override
	public float getPrefHeight() {
		return Math.min(super.getPrefHeight(), 70);
	}
	
	@Override
	public float getMaxWidth() {
		return 0;
	}
	
	@Override
	public float getMaxHeight() {
		return super.getMaxHeight();
	}
}
