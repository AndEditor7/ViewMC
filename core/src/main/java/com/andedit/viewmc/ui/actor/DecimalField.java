package com.andedit.viewmc.ui.actor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;

public class DecimalField extends TextField {
	
	public double defaultNumber;
	public double minNum = Float.NEGATIVE_INFINITY, maxNum = Float.POSITIVE_INFINITY;
	
	private final DecimalFormat formatter;

	public DecimalField(double number, int fracts, Skin skin, String styleName) {
		this(number, fracts, skin.get(styleName, TextFieldStyle.class));
	}

	public DecimalField(double number, int fracts, Skin skin) {
		this(number, fracts, skin.get(TextFieldStyle.class));
	}

	public DecimalField(double number, int fracts, TextFieldStyle style) {
		super(null, style);
		formatter = new DecimalFormat("#.".concat("#".repeat(fracts)), new DecimalFormatSymbols(Locale.US));
		setText(formatter.format(number));
		defaultNumber = Double.parseDouble(getText());
		
		setTextFieldFilter((f, c) -> {
			if (Character.isDigit(c) || c == '-') return true;
			if (c == '.' && Util.countChar(getText(), c) < 1) {
				return true;
			}
			return false;
		});
		
		addListener(Util.newListener(() -> {
			String text = getText();
			if (text.isEmpty()) return; 
			double num = 0;
			try {
				num = Double.parseDouble(text);
			} catch (NumberFormatException e) {
				num = defaultNumber;
			}
			String formated = formatter.format(Math.min(num, maxNum));
			if (Double.parseDouble(formated) != num) {
				setText(formated);
			}
		}));
		
		addListener(new FocusListener() {
			@Override
			public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
				if (!focused) {
					try {
						setText(formatter.format(Math.max(Double.parseDouble(text), minNum)));
					} catch (NumberFormatException e) {}
				}
			}
		});
	}
	
	public double getField() {
		var text = getText();
		
		if (text.isEmpty()) {
			return defaultNumber;
		}
		
		try {
			return Math.max(Double.parseDouble(text), minNum);
		} catch (NumberFormatException e) {
			return defaultNumber;
		}
	}
	
	public void setClampSize(double min, double max) {
		minNum = min;
		maxNum = max;
	}
}
