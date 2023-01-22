package com.andedit.viewermc.ui.util;

import com.andedit.viewermc.StageUI;
import com.andedit.viewermc.util.InputHolder;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.viewport.Viewport;

public final class UIManager {
	public final InputHolder input;
	
	@Null UI current;
	final StageUI stage;

	public UIManager(StageUI stage) {
		this.input = new InputHolder();
		this.stage = stage;
	}

	public void setUI(@Null UI ui) {
		current = ui;
		stage.clear();
		if (ui != null) {
			ui.bind(stage);
			input.set(ui.getInput());
			ui.resize(stage.getViewport());
			stage.resize();
		}
	}
	
	public void update() {
		if (current != null) {
			current.update();
		}
	}
	
	public void resize(Viewport view) {
		if (current != null) {
			current.resize(view);
		}
	}
}
