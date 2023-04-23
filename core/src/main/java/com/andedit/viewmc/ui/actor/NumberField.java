package com.andedit.viewmc.ui.actor;

import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;

public class NumberField extends TextField {
	
	public int defaultNumber;
	public int minNum = Integer.MIN_VALUE, maxNum = Integer.MAX_VALUE;

	public NumberField(int number, Skin skin, String styleName) {
		this(number, skin.get(styleName, TextFieldStyle.class));
	}

	public NumberField(int number, Skin skin) {
		this(number, skin.get(TextFieldStyle.class));
	}

	public NumberField(int number, TextFieldStyle style) {
		super(String.valueOf(number), style);
		defaultNumber = number;
		
		setTextFieldFilter((t, c) -> {
			return Character.isDigit(c) || c == '-';
		});
		
		addListener(Util.newListener(() -> {
			String text = getText();
			if (text.isEmpty()) return;
			int num = 0;
			try {
				num = Integer.parseInt(text);
			} catch (NumberFormatException e) {
				return;
			}
			if (num < 0) {
				num = Math.max(num, minNum);
			} else {
				num = Math.min(num, maxNum);
			}
			setText(String.valueOf(num));
		}));
		
		addListener(new FocusListener() {
			@Override
			public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
				if (!focused) {
					try {
						setText(String.valueOf(Math.max(Integer.parseInt(text), minNum)));
					} catch (NumberFormatException e) {
						setText(String.valueOf(defaultNumber));
					}
				}
			}
		});
	}
	
	public int getField() {
		var text = getText();
		
		if (text.isEmpty()) {
			return defaultNumber;
		}
		
		try {
			return Math.max(Integer.parseInt(text), minNum);
		} catch (NumberFormatException e) {
			return defaultNumber;
		}
	}
	
	public void setClampSize(int min, int max) {
		minNum = min;
		maxNum = max;
	}
}
