package com.andedit.viewmc.ui.actor;

import com.andedit.viewmc.Assets;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Align;

public class ProgressBar extends Widget {

	private float progress;
	private final BitmapFontCache text = Assets.font();
	
	public ProgressBar() {
		setSize(getPrefWidth(), getPrefHeight());
	}
	
	public void setProgress(float progress) {
		this.progress = progress;
		text.setText(Integer.toString(Math.round(100f*progress)) + '%', 0, 0, 0, Align.center, false);
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		validate();

		batch.setColor(Color.LIGHT_GRAY);
		batch.draw(Assets.blank, getX()-1, getY()-1, getWidth()+2, getHeight()+2);
		
		batch.setColor(Color.DARK_GRAY);
		batch.draw(Assets.blank, getX(), getY(), getWidth(), getHeight());
		
		batch.setColor(Color.FIREBRICK);
		batch.draw(Assets.blank, getX(), getY(), getWidth() * progress, getHeight());
		
		batch.setColor(Color.WHITE);
		text.setPosition(getX()+(getWidth()/2f), getY()+(getHeight()/2f) + 4f);
		text.draw(batch);
	}
	
	@Override
	public float getMinWidth() {
		return 50;
	}
	
	@Override
	public float getPrefWidth() {
		return 250;
	}

	@Override
	public float getPrefHeight() {
		return 16;
	}
}
