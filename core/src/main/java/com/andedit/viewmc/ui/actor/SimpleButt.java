package com.andedit.viewmc.ui.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Pools;

/** Simple Button */
public class SimpleButt extends Widget {
	
	public boolean isChecked, isDisabled;
	
	private final ButtonStyle style;
	private final ClickListener listener;
	
	public SimpleButt(ButtonStyle style) {
		this.style = style;
		addListener(listener = new ClickListener() {
			public void clicked (InputEvent event, float x, float y) {
				if (isDisabled) return;
				setCheckedFire(!isChecked);
			}
		});
		
		Drawable drawable = getBackgroundDrawable();
		if (drawable != null) {
			setSize(drawable.getMinWidth(), drawable.getMinHeight());
		}
	}
	
	public SimpleButt(Skin skin, String name) {
		this(skin.get(name, ButtonStyle.class));
	}
	
	public SimpleButt(@Null Drawable up) {
		this(new ButtonStyle(up, null, null));
	}

	public SimpleButt(@Null Drawable up, @Null Drawable down) {
		this(new ButtonStyle(up, down, null));
	}

	public SimpleButt(@Null Drawable up, @Null Drawable down, @Null Drawable checked) {
		this(new ButtonStyle(up, down, checked));
	}

	public float getPrefWidth () {
		Drawable drawable = getBackgroundDrawable();
		if (drawable != null) return drawable.getMinWidth();
		return 0;
	}

	public float getPrefHeight () {
		Drawable drawable = getBackgroundDrawable();
		if (drawable != null) return drawable.getMinHeight();
		return 0;
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		Drawable drawable = getBackgroundDrawable();
		if (drawable != null) drawable.draw(batch, getX(), getY(), getWidth(), getHeight());
	}
	
	public boolean isPressed() {
		return listener.isVisualPressed();
	}

	public boolean isOver() {
		return listener.isOver();
	}
	
	private void setCheckedFire(boolean bool) {
		if (isChecked == bool) return;
		isChecked = bool;
		
		ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
		if (fire(changeEvent)) this.isChecked = !isChecked;
		Pools.free(changeEvent);
	}
	
	/** Returns appropriate background drawable from the style based on the current button state. */
	protected @Null Drawable getBackgroundDrawable() {
		if (isDisabled && style.disabled != null) return style.disabled;
		if (isPressed()) {
			if (isChecked && style.checkedDown != null) return style.checkedDown;
			if (style.down != null) return style.down;
		}
		if (isOver()) {
			if (isChecked) {
				if (style.checkedOver != null) return style.checkedOver;
			} else {
				if (style.over != null) return style.over;
			}
		}
		if (isChecked) {
			if (style.checked != null) return style.checked;
			if (isOver() && style.over != null) return style.over;
		}
		return style.up;
	}
}
