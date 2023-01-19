package com.andedit.viewermc.lwjgl3;

import org.lwjgl.opengl.GL20;

import com.andedit.viewermc.util.API;

public class DesktopAPI implements API {
	@Override
	public void glPolygonMode(int face, boolean mode) {
		GL20.glPolygonMode(face, mode?GL20.GL_FILL:GL20.GL_LINE);
	}
}
