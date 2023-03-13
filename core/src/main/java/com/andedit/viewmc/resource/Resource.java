package com.andedit.viewmc.resource;

import java.util.Optional;
import java.util.zip.ZipFile;

import com.andedit.viewmc.util.BufferedInputStream;
import com.andedit.viewmc.util.ByteArrayOutput;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.JsonReader;

public class Resource implements ResourceData {

	private final FileHandle file;
	private Optional<Texture> image = Optional.empty();
	private final String title;
	private final String description;
	
	public Resource(FileHandle file) throws Exception {
		try (var zip = new ZipFile(file.file())) {
			var entry = zip.getEntry("pack.mcmeta");
			if (entry == null) throw new IllegalStateException("The pack.mcmeta file does not exist in " + file);
			
			var bytes = new ByteArrayOutput();
			var value = new JsonReader().parse(new BufferedInputStream(zip.getInputStream(entry), bytes.array()));
			
			description = Util.unescapeJavaString(value.get("pack").getString("description", ""));
			
			entry = zip.getEntry("pack.png");
			image = ResourceData.loadTexture(zip, entry, bytes);
		}
		
		this.file = file;
		this.title = file.nameWithoutExtension();
	}
	
	@Override
	public FileHandle getFile() {
		return file;
	}

	@Override
	public Optional<Texture> getImage() {
		return image;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public int hashCode() {
		return file.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof ResourceData data) {
			return file.equals(data.getFile());
		}
		if (obj instanceof FileHandle file) {
			return getFile().equals(file);
		}
		return false;
	}
}
