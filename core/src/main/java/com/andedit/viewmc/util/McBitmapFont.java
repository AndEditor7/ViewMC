package com.andedit.viewmc.util;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;

public class McBitmapFont extends BitmapFont {
	public McBitmapFont(FileHandle internal) {
		super(internal);
	}

	@Override
	public BitmapFontCache newFontCache() {
		return new McFontCache(this);
	}
}
