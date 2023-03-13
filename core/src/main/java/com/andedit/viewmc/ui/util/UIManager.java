package com.andedit.viewmc.ui.util;

import com.andedit.viewmc.StageUI;
import com.andedit.viewmc.input.InputHolder;
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
	
	public void reload() {
		if (current != null) {
			current.bind(stage);
			current.resize(stage.getViewport());
			stage.resize();
			current.show();
		}
	}

	public void setUI(@Null UI ui, boolean update) {
		if (current != null) {
			current.hide();
		}
		current = ui;
		stage.clear();
		if (ui != null) {
			input.set(ui.getInput());
			if (update) reload();
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
	
	@Null
	public UI getCurrentUI() {
		return current;
	}

	public boolean isLocked() {
		return current == null ? false : current.isInputLock();
	}
}
