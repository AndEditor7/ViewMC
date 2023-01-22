package com.andedit.viewermc.ui.util;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

public class BaseUI implements UI {

	protected final Array<Actor> actors = new Array<>();
	protected Stage stage;

	@Override
	public void bind(Stage stage) {
		actors.forEach(stage::addActor);
		this.stage = stage;
	}

	protected final Actor add(Actor actor) {
		actors.add(actor);
		return actor;
	}
}
