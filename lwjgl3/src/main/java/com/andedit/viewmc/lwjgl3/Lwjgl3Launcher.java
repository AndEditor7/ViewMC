package com.andedit.viewmc.lwjgl3;

import java.lang.management.ManagementFactory;

import com.andedit.viewmc.Main;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.GLEmulation;
import com.badlogic.gdx.utils.Collections;
import games.spooky.gdx.nativefilechooser.desktop.DesktopFileChooser;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
	public static void main(String[] args) {
		Collections.allocateIterators = true;
		Main.api = new DesktopAPI();
		Main.fc = new DesktopFileChooser();
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
		if (ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp")) {
			config.enableGLDebugOutput(true, System.err);
		}
		config.disableAudio(true);
		config.setWindowedMode(900, 600);
		config.setWindowListener(((DesktopAPI)Main.api).window);
		
//		config.setForegroundFPS(800);
//		config.useVsync(false);
		
		//config.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
		
		return config;
	}
}