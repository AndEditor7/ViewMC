package com.andedit.viewmc.resource;

import java.util.List;
import java.util.Optional;
import java.util.zip.ZipFile;

import com.andedit.viewmc.loader.ModType;
import com.andedit.viewmc.util.BufferedInputStream;
import com.andedit.viewmc.util.Pair;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.JsonReader;

public class Core implements ModData {
	
	private final FileHandle file;
	private final Optional<Texture> image;
	private final List<String> authors = List.of("Mojang Studios");
	private final List<String> license = List.of("Minecraft EULA");
	private final String version;

	public Core(FileHandle file) throws Exception {
		try (var zip = new ZipFile(file.file())) {
			var entry = zip.getEntry("META-INF/MANIFEST.MF");
			if (entry == null) throw new IllegalStateException("Not a minecraft jar file: " + file);
			
			entry = zip.getEntry("pack.png");
			image = ResourceData.loadTexture(zip, entry);
			
			entry = zip.getEntry("version.json");
			if (entry != null) {
				var value = new JsonReader().parse(new BufferedInputStream(zip.getInputStream(entry)));
				version = value.getString("id", value.getString("name", "unknown"));
			} else {
				version = "unknown";
			}
			
		}
		this.file = file;
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
	public Optional<Texture> getLogo() {
		return Optional.empty();
	}

	@Override
	public String getTitle() {
		return "Minecraft";
	}

	@Override
	public String getDescription() {
		return "The core resource";
	}

	@Override
	public String getVersion() {
		return version;
	}
	
	@Override
	public List<String> getLicense() {
		return license;
	}

	@Override
	public List<String> getAuthors() {
		return authors;
	}

	@Override
	public List<String> getContributors() {
		return List.of();
	}
	
	@Override
	public List<Pair<String, List<String>>> getQuiltContributors() {
		return List.of();
	}

	@Override
	public ModType getType() {
		return ModType.VANILLA;
	}
}
