package com.andedit.viewmc.ui.actor;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Null;

/** {@link ScrollPane} */
public class DesktopScrollPane extends ScrollPane {

	{
		setFadeScrollBars(false);
		setFlickScrollTapSquareSize(5);
		setScrollingDisabled(true, false);
		setOverscroll(false, false);
		setFlingTime(0.1f);
	}
	
	public DesktopScrollPane(@Null Actor widget) {
		super(widget);
	}
	
	public DesktopScrollPane(@Null Actor widget, Skin skin) {
		super(widget, skin);
	}
	
	public DesktopScrollPane(@Null Actor widget, ScrollPaneStyle style) {
		super(widget, style);
	}
	
	public DesktopScrollPane(@Null Actor widget, Skin skin, String styleName) {
		super(widget, skin, styleName);
	}
	
	@Override
	protected void addCaptureListener() {
		super.addCaptureListener();
		addCaptureListener(new ClickListener() {
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				super.enter(event, x, y, pointer, fromActor);
				if (isOver() && getStage() != null) {
					getStage().setScrollFocus(DesktopScrollPane.this);
				}
			}
			
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				super.exit(event, x, y, pointer, toActor);
				if (!isOver() && getStage() != null) {
					getStage().setScrollFocus(null);
				}
			}
		});
	}
	
	@Override
	protected ActorGestureListener getFlickScrollListener() {
		return new ActorGestureListener() {
			@Override
			public boolean handle(Event e) {
				return false;
			}
		};
	}
}
