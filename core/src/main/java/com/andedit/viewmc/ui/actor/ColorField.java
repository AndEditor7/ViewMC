package com.andedit.viewmc.ui.actor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;

public class ColorField extends TextField {
	
	private final Color defaultColor;
	
	public ColorField(String text, Skin skin) {
		this(text, skin.get(TextFieldStyle.class));
	}
	
	public ColorField(String text, Skin skin, String styleName) {
		this(text, skin.get(styleName, TextFieldStyle.class));
	}
	
	public ColorField(String text, TextFieldStyle style) {
		super(text, style);
		defaultColor = Color.valueOf(text);
		
		setTextFieldFilter((f, c) -> {
			if (c == '#' || Character.isDigit(c)) {
				return true;
			}
			if ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
				return true;
			}
			return false;
		});
		
		addListener(new FocusListener() {
			@Override
			public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
				if (focused) return;
				try {
					var text = getText();
					setText((text.charAt(0) == '#' ? "#" : "") + Color.valueOf(text).toString());
				} catch (Exception e) {
					setText(text);
				}
			}
		});
	}
	
	public Color getField() {
		try {
			return Color.valueOf(getText());
		} catch (Exception e) {
			return defaultColor;
		}
	}
}
