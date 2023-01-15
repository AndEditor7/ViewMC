package com.andedit.viewermc;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

public interface Screen extends Disposable {
	void show();
	
	void render();
	
	void resize(int width, int height);
	
	@Null
	default InputProcessor getInput() {
		return null;
	}
	
	/** {@inheritDoc} It might dispose without calling show() */
	@Override
	default void dispose() {
		
	}
}
