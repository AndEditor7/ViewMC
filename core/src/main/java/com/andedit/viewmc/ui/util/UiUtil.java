package com.andedit.viewmc.ui.util;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class UiUtil {
	
	public static <T extends Actor> T scale(T actor, float scl) {
		actor.setSize(actor.getWidth()*scl, actor.getHeight()*scl);
		return actor;
	}
}
