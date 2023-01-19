package com.andedit.viewermc.lwjgl3;

import com.andedit.viewermc.Main;
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
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("MC Viewer");
		config.useVsync(true);
		//config.setOpenGLEmulation(GLEmulation.ANGLE_GLES20, 3, 1);
		config.enableGLDebugOutput(true, System.err);
		
		var mode = Lwjgl3ApplicationConfiguration.getDisplayMode();
		config.setForegroundFPS(mode.refreshRate == 60 ? 100 : 60);
		
		// Fullscreen
		if (false) {
			config.setFullscreenMode(mode);
		}
		
		//config.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
		return config;
	}
}