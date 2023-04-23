package com.andedit.viewmc.ui.util;

import java.util.function.BiConsumer;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.ui.drawable.TexRegDrawable;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public class UIs {
	
	public static Window newWindow(String title) {
		return newWindow(title, (w, e) -> w.remove());
	}
	
	public static Window newWindow(String title, BiConsumer<Window, Event> closeCall) {
		var window = new Window(title, Assets.skin);
		window.setModal(true);
		window.setResizable(true);
		window.getTitleTable().padLeft(1);
		window.setResizeBorder(4);
		window.setUserObject(new CloseCall(closeCall));
		
		var closeButt = new Button(new TexRegDrawable(Assets.blank, Color.FIREBRICK), new TexRegDrawable(Assets.blank, Color.FIREBRICK.cpy().lerp(Color.BLACK, 0.3f)));
		window.getTitleTable().add(closeButt).growY().padTop(1).padBottom(1).width(10);
		closeButt.addListener(Util.newListener(e -> {
			if (window.getUserObject() instanceof CloseCall a) {
				a.accept(window, e);
			}
		}));
		
		closeButt.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				event.stop();
				return true;
			}
		});
		
		return window;
	}
	
	public static class CloseCall implements BiConsumer<Window, Event> {

		private final BiConsumer<Window, Event> consumer;
		
		public CloseCall(BiConsumer<Window, Event> consumer) {
			this.consumer = consumer;
		}
		
		@Override
		public void accept(Window window, Event event) {
			consumer.accept(window, event);
		}
	}
}
