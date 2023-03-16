package com.andedit.viewmc.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.andedit.viewmc.loader.ModType;
import com.andedit.viewmc.util.BufferedInputStream;
import com.andedit.viewmc.util.ByteArrayOutput;
import com.andedit.viewmc.util.Pair;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.Null;
import com.moandjiezana.toml.Toml;

public class Mod implements ModData {

	private static final Pattern PATTERN_DIGIT = Pattern.compile("\\d");
	
	private final FileHandle file;
	private Optional<Texture> image = Optional.empty();
	private Optional<Texture> logo = Optional.empty();
	private String title = "";
	private String description = "";
	private String version = "";
	private List<String> license = List.of();
	private List<String> authors = List.of(), contributors = List.of();
	private List<Pair<String, List<String>>> quiltContributors = List.of();
	private ModType type = ModType.VANILLA;
	
	public Mod(FileHandle file) throws Exception {
		try (var zip = new ZipFile(file.file())) {
			var bytes = new ByteArrayOutput();
			ZipEntry entry = null;
			if ((entry = zip.getEntry("quilt.mod.json")) != null) { // Quilt
				type = ModType.QUILT;
				var value = new JsonReader().parse(new BufferedInputStream(zip.getInputStream(entry), bytes.array()));
				@Null var loader = value.get("quilt_loader");
				@Null var meta = loader.get("metadata");
				if (meta != null) {
					title = Util.unescapeJavaString(meta.getString("name", loader == null ? file.nameWithoutExtension() : loader.getString("id", file.nameWithoutExtension())));
					description = Util.unescapeJavaString(meta.getString("description", ""));
					@Null var json = meta.get("contributors");
					if (json != null) {
						quiltContributors = new ArrayList<>();
						for (var val : json) {
							if (val.isArray()) {
								quiltContributors.add(new Pair<String, List<String>>(val.name, List.of(val.asStringArray())));
							} else {
								quiltContributors.add(new Pair<String, List<String>>(val.name, List.of(val.asString())));
							}
						}
					}
					
					json = meta.get("license");
					if (json != null) 
					if (json.isArray()) {
						license = List.of(json.asStringArray());
					} else if (json.isObject() && json.notEmpty()) {
						license = List.of(json.getString("name"));
					} else {
						license = List.of(json.asString());
					}
					
					json = meta.get("icon");
					if (json != null) 
					if (json.isObject() && json.notEmpty()) {
						var array = new IntArray();
						for (var val : json) {
							array.add(Integer.parseInt(val.name));
						}
						array.sort();
						int size = array.first();
						for (int i = 0; i < array.size; i++) {
							size = array.get(i);
							if (size >= 64) {
								break;
							}
						}
						entry = zip.getEntry(json.getString(Integer.toString(size)));
						image = ResourceData.loadTexture(zip, entry, bytes);
					} else {
						entry = zip.getEntry(json.asString());
						image = ResourceData.loadTexture(zip, entry, bytes);
					}
				} else {
					title = Util.unescapeJavaString(loader == null ? file.nameWithoutExtension() : loader.getString("id", file.nameWithoutExtension()));
				}
				
				version = loader.getString("version", "");
			} else if ((entry = zip.getEntry("fabric.mod.json")) != null) { // Fabric
				type = ModType.FABIC;
				var value = new JsonReader().parse(new BufferedInputStream(zip.getInputStream(entry), bytes.array()));
				
				title = Util.unescapeJavaString(value.getString("name", value.getString("id", file.nameWithoutExtension())));
				description = Util.unescapeJavaString(value.getString("description", ""));
				
				version = value.getString("version", "");
				
				
				@Null var json = value.get("license");
				if (json != null)
				if (json.isArray()) {
					license = List.of(json.asStringArray());
				} else {
					license = List.of(json.asString());
				}
				
				authors = new ArrayList<>();
				json = value.get("authors");
				if (json != null) 
				for (var val : json) {
					if (val.isObject()) {
						authors.add(val.getString("name"));
					} else {
						authors.add(val.asString());
					}
				}
				
				contributors = new ArrayList<>();
				json = value.get("contributors");
				if (json != null) 
				for (var val : json) {
					if (val.isObject()) {
						contributors.add(val.getString("name"));
					} else {
						contributors.add(val.asString());
					}
				}
				
				entry = zip.getEntry(value.getString("icon", ""));
				image = ResourceData.loadTexture(zip, entry, bytes);
			} else if ((entry = zip.getEntry("META-INF/mods.toml")) != null) { // Forge
				type = ModType.FORGE;
				var value = new Toml().read(new BufferedInputStream(zip.getInputStream(entry), bytes.array()));
				var mods = (Map<String, String>) value.getList("mods").get(0);
				
				title = Util.unescapeJavaString(mods.getOrDefault("displayName", mods.getOrDefault("modId", file.nameWithoutExtension())));
				description = Util.unescapeJavaString(mods.getOrDefault("description", ""));
				authors = List.of(Util.unescapeJavaString(mods.getOrDefault("authors", "")));
				if (authors.get(0).isEmpty()) authors = List.of();
				contributors = List.of(Util.unescapeJavaString(mods.getOrDefault("credits", "")));
				if (contributors.get(0).isEmpty()) contributors = List.of();
				version = Util.unescapeJavaString(mods.getOrDefault("version", ""));
				license = List.of(Util.unescapeJavaString(mods.getOrDefault("license", "")));
				if (license.get(0).isEmpty()) license = List.of();
				
				String string = value.getString("displayName");
				if (string != null) {
					title = Util.unescapeJavaString(string);
				}
				if (description.isEmpty()) 
				description = Util.unescapeJavaString(value.getString("description", ""));
				if (authors.isEmpty()) {
					authors = List.of(Util.unescapeJavaString(value.getString("authors", "")));
					if (authors.get(0).isEmpty()) authors = List.of();
				}
				if (contributors.isEmpty()) {
					contributors = List.of(Util.unescapeJavaString(value.getString("credits", "")));
					if (contributors.get(0).isEmpty()) contributors = List.of();
				}
				if (version.isEmpty()) 
				version = Util.unescapeJavaString(value.getString("version", ""));
				if (license.isEmpty()) {
					license = List.of(Util.unescapeJavaString(value.getString("license", "")));
					if (license.get(0).isEmpty()) license = List.of();
				}
				
				if (version.equals("${file.jarVersion}")) {
					var name = file.nameWithoutExtension();
					var match = PATTERN_DIGIT.matcher(name);
					version = match.find() ? name.substring(match.start()) : name;
				}
				
				entry = zip.getEntry(value.getString("logoFile", ""));
				logo = ResourceData.loadTexture(zip, entry, bytes);
				if (logo.isEmpty()) {
					entry = zip.getEntry(mods.getOrDefault("logoFile", ""));
					logo = ResourceData.loadTexture(zip, entry, bytes);
				}
				
				entry = zip.getEntry("pack.png");
				image = ResourceData.loadTexture(zip, entry, bytes);
				
				if (image.isEmpty() && logo.isPresent()) {
					if (logo.get().getWidth() == logo.get().getHeight()) {
						image = logo;
					}
				}
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
		return logo;
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
		return contributors;
	}
	
	@Override
	public List<Pair<String, List<String>>> getQuiltContributors() {
		return quiltContributors;
	}

	@Override
	public ModType getType() {
		return type;
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
