package com.andedit.viewermc.input;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.IntArray;

public class BasicInput extends InputAdapter {
	private final IntArray keysPressed = new IntArray(false, 16);
	private final IntArray keysJustPre = new IntArray(false, 16);
	private final IntArray buttPressed = new IntArray(false, 8);
	private final IntArray buttJustPre = new IntArray(false, 8);
	
	public boolean blockInputs;
	
	@Override
	public boolean keyDown(int keycode) {
		if (!keysPressed.contains(keycode)) {
			keysPressed.add(keycode);
		}
		if (!keysJustPre.contains(keycode)) {
			keysJustPre.add(keycode);
		}
		return blockInputs;
	}

	@Override
	public boolean keyUp(int keycode) {
		keysPressed.removeValue(keycode);
		return blockInputs;
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (!buttPressed.contains(button)) {
			buttPressed.add(button);
		}
		if (!buttJustPre.contains(button)) {
			buttJustPre.add(button);
		}
		return blockInputs;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		buttPressed.removeValue(button);
		return blockInputs;
	}
	
	public void reset() {
		keysJustPre.clear();
		buttJustPre.clear();
	}
	
	public boolean isButtonPressed(int button) {
		return buttPressed.contains(button);
	}

	public boolean isButtonJustPressed(int button) {
		return buttJustPre.contains(button);
	}
	
	public boolean justTouched() {
		return buttJustPre.notEmpty();
	}

	public boolean isKeyPressed(int key) {
		return keysPressed.contains(key);
	}

	public boolean isKeyJustPressed(int key) {
		return keysJustPre.contains(key);
	}
}
