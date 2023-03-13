package com.andedit.viewmc.ui.util;

import com.andedit.viewmc.Event;
import com.andedit.viewmc.Events;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.viewport.Viewport;

public interface UI extends Event {
	void bind(Stage stage);
	
	default void show() {
		
	}
	
	default void hide() {
		
	}
	
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
	
	@Override
	default void event(Events event, Object arg) {
		
	}
}
