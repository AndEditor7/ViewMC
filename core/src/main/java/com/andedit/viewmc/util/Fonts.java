package com.andedit.viewmc.util;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Null;

/** A Fonts class that supports color text rendering. */
public class Fonts {
	
	private static final ArrayList<String> colors = new ArrayList<>(16);
	
	public static final Color BLACK = add("#000000");
	public static final Color DARK_BLUE = add("#0000AA");
	public static final Color DARK_GREEN = add("#00AA00");
	public static final Color DARK_AQUA = add("#00AAAA");
	public static final Color DARK_RED = add("#AA0000");
	public static final Color DARK_PURPLE = add("#AA00AA");
	public static final Color GOLD = add("#FFAA00");
	public static final Color GRAY = add("#AAAAAA");
	public static final Color DARK_GRAY = add("#555555");
	public static final Color BLUE = add("#5555FF");
	public static final Color GREEN = add("#55FF55");
	public static final Color AQUA = add("#55FFFF");
	public static final Color RED = add("#FF5555");
	public static final Color LIGHT_PURPLE = add("#FF55FF");
	public static final Color YELLOW = add("#FFFF55");
	public static final Color WHITE = add("#FFFFFF");
	
	private static Color add(String hex) {
		var color = Color.valueOf(hex);
		colors.add('['+hex+']');
		return color;
	}
	
	@Null
	public static String getColor(char code) {
		return Util.get(colors, code-'0');
	}
}
