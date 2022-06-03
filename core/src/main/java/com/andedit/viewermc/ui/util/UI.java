package com.andedit.viewermc.ui.util;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.viewport.Viewport;

public interface UI {
	void bind(Stage stage);
	
	void setVisible(boolean visible);
	
	default void resize(Viewport view) {
		
	}
	
	@Null
	default InputProcessor getInput() {
		return null;
	};
	
	default boolean isInputLock() {
		return false;
	}
	
	default void update() {
		
	}
}
