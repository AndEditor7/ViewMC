package com.andedit.viewmc.util;

import com.andedit.viewmc.Assets;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Null;

public class McFontCache extends BitmapFontCache {
	
	private static final StringBuilder builder = new StringBuilder();
	private static final Array<Format> formats = new Array<>();
	private static final IntArray indices = new IntArray();
	
	public int maxLine;
	
	public McFontCache() {
		this(Assets.font);
	}
	
	public McFontCache(BitmapFont font) {
		super(font, font.usesIntegerPositions());
	}
	
	/** Adds glyphs for the specified text. */
	public GlyphLayout setText(CharSequence str, float x, float y, float targetWidth, int halign, String truncate) {
		return super.setText(str, x, y, 0, str.length(), targetWidth, halign, false, truncate);
	}
	
	@Override
	public GlyphLayout addText(CharSequence str, float x, float y, int start, int end, float targetWidth, int halign, boolean wrap, String truncate) {
		formats.clear();
		for (int i = start; i < end; i++) {
			var c = str.charAt(i);
			if (c == '§') {
				if (i+2 >= end) break;
				formats.add(new Format(i, Fonts.getColor(str.charAt(i+1))));
			}
		}
		
		int deleted = 0;
		indices.clear();
		builder.setLength(0);
		builder.append(str, start, end);
		for (var format : formats) {
			int idx = format.index - deleted - start;
			builder.delete(idx, idx+2);
			indices.add(idx);
			deleted += 2;
		}
		
		int extra = 0;
		for (int i = indices.size-1; i >= 0; i--) {
			var index = indices.get(i);
			var format = formats.get(i);
			var color = format.color;
			if (color == null) continue; 
			builder.insert(index, color);
			extra += color.length();
		}
		
		end = end+extra-start-deleted;
		return super.addText(builder, x, y, 0, end, targetWidth, halign, wrap, truncate);
	}
	
	@Override
	public void addText(GlyphLayout layout, float x, float y) {
		if (maxLine > 0) {
			var runs = layout.runs;
			runs.setSize(Math.min(maxLine, runs.size));
		}
		super.addText(layout, x, y);
	}
	
	private static class Format {
		final int index;
		final @Null String color;
		
		Format(int index, @Null String color) {
			this.index = index;
			this.color = color;
		}
	}
}
