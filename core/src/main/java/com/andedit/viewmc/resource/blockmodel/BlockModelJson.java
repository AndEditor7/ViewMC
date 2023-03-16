package com.andedit.viewmc.resource.blockmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.andedit.viewmc.util.Identifier;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.OrderedSet;

public class BlockModelJson {
	
	public static final BlockModelJson MISSING_MODEL = new BlockModelJson();
	
	public final @Null Identifier parent;
	public OrderedMap<String, String> textureMap;
	public List<Element> elements;
	
	private boolean isLoaded;
	private Boolean ambientOcclusion;
	
	private BlockModelJson() {
		parent = null;
		ambientOcclusion = false;
		elements = List.of(new Element());
		textureMap = new OrderedMap<>();
	}
	
	public BlockModelJson(JsonValue value, OrderedSet<Identifier> textureSet, OrderedSet<Identifier> modelParents) {
		var val = value.get("parent");
		
		if (val == null) {
			parent = null;
		} else {
			parent = new Identifier(val.asString());
			modelParents.add(parent);
		}
		
		val = value.get("ambientocclusion");
		ambientOcclusion = val == null ? null : val.asBoolean();
		
		textureMap = new OrderedMap<>();
		val = value.get("textures");
		if (val != null) 
		for (var v : val) {
			var string = v.asString();
			textureMap.put(v.name, string);
			if (string.charAt(0) != '#') {
				textureSet.add(new Identifier(string));
			}
		}
		
		val = value.get("elements");
		if (val == null) {
			elements = Collections.emptyList();
		} else {
			elements = new ArrayList<>(val.size);
			val.forEach(v -> elements.add(new Element(v)));
		}
	}

	public void load(ObjectMap<Identifier, BlockModelJson> models) {
		if (isLoaded || parent == null) return;
		isLoaded = true;
		
		var model = models.get(parent);
		model.load(models);
		
		// Get the parent's elements if they are empty. Otherwise, don't override it.
		if (elements.isEmpty()) {
			elements = model.elements;
		}
		
		if (ambientOcclusion == null) {
			ambientOcclusion = model.ambientOcclusion;
		}
		
		if (model.textureMap.notEmpty()) {
			var map = new OrderedMap<String, String>();
			map.putAll(model.textureMap);
			map.putAll(textureMap);
			textureMap = map;
		}
	}
	
	public void addAll(ObjectMap<Identifier, byte[]> srcTextures, ObjectMap<Identifier, byte[]> destTextures) {
		for (var texture : textureMap.values()) {
			if (texture.charAt(0) == '#') continue;
			var id = new Identifier(texture);
			var tex = srcTextures.get(id);
			if (tex == null) continue;
			destTextures.put(id, tex);
		}
	}
	
	/** Use # at the first string of the character. Otherwise, self return id; */
	public Identifier getTexture(String key) {
		if (key.charAt(0) != '#') return new Identifier(key);
		String last = key;
		String value = key;
		do {
			value = textureMap.get(value.substring(1));
			if (value == null || value.equals(last)) return new Identifier("missing");
			last = value;
		} while (value.charAt(0) == '#');
		return new Identifier(value);
	}
	
	public boolean ambientOcclusion() {
		return ambientOcclusion == null ? true : ambientOcclusion;
	}
}
