package com.andedit.viewmc.lwjgl3;

import com.andedit.viewmc.Main;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.GLEmulation;
import com.badlogic.gdx.utils.Collections;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
	public static void main(String[] args) {
		Collections.allocateIterators = true;
		Main.api = new DesktopAPI();
		createApplication();
	}

	private static Lwjgl3Application createApplication() {
		return new Lwjgl3Application(Main.main, getDefaultConfiguration());
	}

	@SuppressWarnings("unused")
	private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
		var config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("ViewMC");
		config.setOpenGLEmulation(GLEmulation.GL30, 3, 2);
		config.enableGLDebugOutput(true, System.err);
		config.disableAudio(true);
		
		config.setWindowListener(((DesktopAPI)Main.api).window);
		
		var mode = Lwjgl3ApplicationConfiguration.getDisplayMode();
		config.setForegroundFPS(mode.refreshRate == 60 ? 100 : 60);
		config.setIdleFPS(30);
		//config.setForegroundFPS(1000);
		//config.useVsync(false);
		
		// Fullscreen
		if (false) {
			config.setFullscreenMode(mode);
		}
		
		//config.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
		return config;
	}
}