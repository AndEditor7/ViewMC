package com.andedit.viewmc.input;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Null;

public class InputHolder implements InputProcessor {
	
	@Null
	private InputProcessor processor;
	
	public void set(@Null InputProcessor processor) {
		this.processor = processor;
	}
	
	public void clear() {
		set(null);
	}

	@Override
	public boolean keyDown(int keycode) {
		return processor != null && processor.keyDown(keycode);
	}

	@Override
	public boolean keyUp(int keycode) {
		return processor != null && processor.keyUp(keycode);
	}

	@Override
	public boolean keyTyped(char character) {
		return processor != null && processor.keyTyped(character);
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return processor != null && processor.touchDown(screenX, screenY, pointer, button);
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return processor != null && processor.touchUp(screenX, screenY, pointer, button);
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return processor != null && processor.touchDragged(screenX, screenY, pointer);
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return processor != null && processor.mouseMoved(screenX, screenY);
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return processor != null && processor.scrolled(amountX, amountY);
	}

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		return processor != null && processor.touchCancelled(screenX, screenY, pointer, button);
	}
}
