package com.andedit.viewermc.ui.util;

import static com.andedit.viewermc.Main.main;

import com.andedit.viewermc.util.InputHolder;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.Viewport;

public final class UIManager {
	public final InputHolder input;
	
	final ObjectMap<Class<? extends UI>, UI> map;
	final Array<UI> list;
	@Null UI current;

	public UIManager() {
		map = new ObjectMap<>();
		list = new Array<>(32);
		input = new InputHolder();
	}

	public void put(UI ui) {
		if (ui == null) throw new IllegalArgumentException("UI cannot be null.");
		map.put(ui.getClass(), ui);
		list.add(ui);
		ui.setVisible(false);
	}

	public void bind(Stage stage) {
		for (UI ui : list) ui.bind(stage);
	}

	@SuppressWarnings("unchecked")
	public <T extends UI> T setUI(Class<T> clazz) {
		final UI ui = map.get(clazz);
		if (ui == null) throw new IllegalArgumentException("Invailed class: " + clazz.getName());

		if (current != null) current.setVisible(false);
		ui.setVisible(true);
		current = ui;
		if (ui.isInputLock())
			main.addInputLock("ui");
		else main.removeInputLock("ui");
		input.set(ui.getInput());
		return (T) ui;
	}

	@SuppressWarnings("unchecked")
	public <T extends UI> T getUI(Class<T> clazz) {
		return (T) map.get(clazz);
	}

	@SuppressWarnings("unchecked")
	public <T extends UI> T getCurrentUI() {
		return (T) current;
	}
	
	public boolean isOf(Class<? extends UI> clazz) {
		return current != null ? current.getClass() == clazz : false;
	}
	
	public void setVisible(boolean isVisible) {
		if (current != null) {
			current.setVisible(isVisible);
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
	
	public void clear() {
		map.clear();
		list.clear();
		current = null;
	}
}
