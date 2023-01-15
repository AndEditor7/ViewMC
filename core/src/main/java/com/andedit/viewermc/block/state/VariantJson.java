package com.andedit.viewermc.block.state;

import java.util.ArrayList;
import java.util.List;

import com.andedit.viewermc.util.Identifier;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.OrderedSet;

public class VariantJson {
	
	public final String variant;
	public final List<ModelJson> models;
	
	public VariantJson(JsonValue value, OrderedSet<Identifier> modelIds) {
		variant = value.name;
		if (value.isArray()) {
			models = new ArrayList<>(value.size);
			for (var obj : value) {
				models.add(new ModelJson(obj, modelIds));
			}
		} else {
			models = List.of(new ModelJson(value, modelIds));
		}
	}
}
