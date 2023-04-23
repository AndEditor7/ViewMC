package com.andedit.viewmc.ui.util;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class UiUtil {
	
	public static <T extends Actor> T scale(T actor, float scl) {
		actor.setSize(actor.getWidth()*scl, actor.getHeight()*scl);
		return actor;
	}
	
	public static void center(Actor actor, Stage stage) {
		center(actor, 0, 0, stage.getWidth(), stage.getHeight());
	}
	
	public static void center(Actor actor, float x, float y, float w, float h) {
		actor.setPosition(x+w/2f-actor.getWidth()/2f, y+h/2f-actor.getHeight()/2f);
	}
}
