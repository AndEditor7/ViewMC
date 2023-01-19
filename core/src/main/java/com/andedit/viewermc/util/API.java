package com.andedit.viewermc.util;

public interface API {
	/** @param mode true for fill triangle or false for wireframe (line) */
	void glPolygonMode(int face, boolean mode);
}
