package com.andedit.viewmc.lwjgl3;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;

import com.andedit.viewmc.util.API;

public class DesktopAPI implements API {
	
	final Window window = new Window();
	
	@Override
	public void glPolygonMode(int face, boolean mode) {
		GL20.glPolygonMode(face, mode?GL20.GL_FILL:GL20.GL_LINE);
	}
	
	@Override
	public void focusWindow() {
		GLFW.glfwFocusWindow(window.handle);
	}
}
