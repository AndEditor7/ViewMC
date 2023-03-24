package com.andedit.viewmc.resource;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

public interface ResourceSection extends Disposable {
	ResourceData data();

	boolean isPack();
	
	default boolean isMod() {
		return !isPack();
	}
	
	default boolean equals(FileHandle file) {
		return data().equals(file);
	}
	
	@Override
	default void dispose() {
		data().dispose();
	}
}
