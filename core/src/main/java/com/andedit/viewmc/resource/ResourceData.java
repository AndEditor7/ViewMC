package com.andedit.viewmc.resource;

import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.andedit.viewmc.util.ByteArrayOutput;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.StreamUtils;

public interface ResourceData extends Disposable {
	FileHandle getFile();
	Optional<Texture> getImage();
	String getTitle();
	String getDescription();
	
	default boolean equals(FileHandle file) {
		return getFile().equals(file);
	}
	
	default boolean isResource() {
		return true;
	}
	
	default boolean isMod() {
		return false;
	}
	
	@Override
	default void dispose() {
		getImage().ifPresent(Texture::dispose);
	}
	
	public static Optional<Texture> loadTexture(ZipFile file, @Null ZipEntry entry) {
		return loadTexture(file, entry, new ByteArrayOutput());
	}
	
	public static Optional<Texture> loadTexture(ZipFile file, @Null ZipEntry entry, ByteArrayOutput bytes) {
		if (entry == null) return Optional.empty();
		try {
			bytes.reset();
			StreamUtils.copyStream(file.getInputStream(entry), bytes);
			var pixmap = new Pixmap(bytes.array(), 0, bytes.size());
			return Optional.of(new Texture(new PixmapTextureData(pixmap, Util.vaildFormat(pixmap.getFormat()), false, true)));
		} catch (Exception e) {
		}
		return Optional.empty();
	}
}
