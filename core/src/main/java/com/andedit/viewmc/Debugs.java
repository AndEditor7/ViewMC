package com.andedit.viewmc;

import static com.badlogic.gdx.Gdx.graphics;
import static com.badlogic.gdx.Gdx.input;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;

public class Debugs {
	
	public static final int F1 = Keys.F1;
	
	public static final int F2 = Keys.F2;
	
	public static final int F3 = Keys.F3;
	
	public static final int F4 = Keys.F4;
	
	public static final int F5 = Keys.F5;
	
	public static final int F6 = Keys.F6;
	
	public static final int F7 = Keys.F7;
	
	public static final int F8 = Keys.F8;
	
	public static final int F9 = Keys.F9;
	
	public static final int F10 = Keys.F10;
	
	public static final int F11 = Keys.F11;
	
	public static final int F12 = Keys.F12;
	
	public static boolean isKeyJustPressed(int key) {
		return input.isKeyJustPressed(key);
	}
	
	public static final InputAdapter INPUT = new InputAdapter() {
		@Override
		public boolean keyDown(int keycode) {
			
			if (keycode == F11) {
				if (graphics.isFullscreen()) {
					graphics.setWindowedMode(900, 600);
				} else {
					graphics.setFullscreenMode(graphics.getDisplayMode());
				}
			}
			
			return false;
		};
	};
}
