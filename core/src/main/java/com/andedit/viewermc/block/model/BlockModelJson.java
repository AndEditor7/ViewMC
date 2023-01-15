package com.andedit.viewermc.block.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.andedit.viewermc.util.Identifier;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.OrderedSet;

public class BlockModelJson {
	public final Identifier parent;
	public OrderedMap<String, String> textures;
	public List<Element> elements;
	
	private boolean isLoaded;
	private Boolean ambientOcclusion;
	
	public BlockModelJson(JsonValue value, OrderedSet<Identifier> textureIds) {
		var val = value.get("parent");
		parent = val == null ? null : new Identifier(val.asString());
		
		val = value.get("ambientocclusion");
		ambientOcclusion = val == null ? null : val.asBoolean();
		
		textures = new OrderedMap<>();
		val = value.get("textures");
		if (val != null) 
		for (var v : val) {
			var string = v.asString();
			textures.put(v.name, string);
			if (string.charAt(0) != '#') {
				textureIds.add(new Identifier(string));
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

	public void load(OrderedMap<Identifier, BlockModelJson> models) {
		if (isLoaded || parent == null) return;
		var model = models.get(parent);
		model.load(models);
		
		// Get the parent's elements if they are empty. Otherwise, don't override it.
		if (elements.isEmpty()) {
			elements = model.elements;
		}
		
		if (ambientOcclusion == null) {
			ambientOcclusion = model.ambientOcclusion;
		}
		
		if (model.textures.notEmpty()) {
			var map = new OrderedMap<String, String>();
			map.putAll(model.textures);
			map.putAll(textures);
			textures = map;
		}
		
		isLoaded = true;
	}
	
	/** Use # at the first string of the character. Otherwise, self return id; */
	public Identifier getTexture(String key) {
		if (key.charAt(0) != '#') return new Identifier(key);
		String value = key;
		do {
			value = textures.get(value.substring(1));
		} while (value.charAt(0) == '#');
		return new Identifier(value);
	}
	
	public boolean ambientOcclusion() {
		return ambientOcclusion == null ? true : ambientOcclusion;
	}
}
