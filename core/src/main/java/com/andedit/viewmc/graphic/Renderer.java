package com.andedit.viewmc.graphic;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Disposable;

public interface Renderer extends Disposable {
	void render(Camera camera);
}
