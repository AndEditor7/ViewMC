package com.andedit.viewmc.resource.blockstate;

import java.util.ArrayList;
import java.util.List;

import com.andedit.viewmc.util.Identifier;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.OrderedSet;

public class CaseJson {
	public final @Null WhenJson when;
	public final List<ModelJson> apply;
	
	public CaseJson(JsonValue value, OrderedSet<Identifier> modelIds) {
		var obj = value.get("when");
		when = obj == null ? null : new WhenJson(obj);
		obj = value.get("apply");
		if (obj.isArray()) {
			apply = new ArrayList<>(obj.size);
			for (var o : obj) {
				apply.add(new ModelJson(o, modelIds));
			}
		} else {
			apply = List.of(new ModelJson(obj, modelIds));
		}
	}
}
