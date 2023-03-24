package com.andedit.viewmc;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.viewport.Viewport;

public interface Screen extends Disposable, Event {
	void show();
	
	void hide();
	
	void render();
	
	void resize(Viewport view);
	
	@Override
	void event(Events event, Object arg);
	
	@Null
	default InputProcessor getInput() {
		return null;
	}
	
	/** {@inheritDoc} It might dispose without calling show() */
	@Override
	default void dispose() {
		
	}
}
