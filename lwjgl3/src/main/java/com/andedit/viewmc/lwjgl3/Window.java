package com.andedit.viewmc.lwjgl3;

import static com.andedit.viewmc.Main.api;
import static com.andedit.viewmc.Main.main;

import com.andedit.viewmc.Events;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;

public class Window extends Lwjgl3WindowAdapter {

	Lwjgl3Window window;
	long handle;
	
	@Override
	public void created(Lwjgl3Window window) {
		this.window = window;
		handle = window.getWindowHandle();
	}
	
	@Override
	public void filesDropped(String[] files) {
		api.focusWindow();
		main.event(Events.FILES_DROPPED, files);
	}
}
