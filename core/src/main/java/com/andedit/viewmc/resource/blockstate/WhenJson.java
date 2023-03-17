package com.andedit.viewmc.resource.blockstate;

import java.util.ArrayList;
import java.util.List;

import com.andedit.viewmc.util.Pair;
import com.badlogic.gdx.utils.JsonValue;

public class WhenJson {
	public final List<List<Pair<String, String>>> states;
	public boolean isOr, isAnd;
	
	public WhenJson(JsonValue value) {
		var gate = value.get("OR");
		if (gate == null) {
			gate = value.get("AND");
			if (gate != null) {
				isAnd = true;
			}
		} else {
			isOr = true;
		}
		
		if (gate != null) {
			states = new ArrayList<>(gate.size);
			for (var obj : gate) {
				var array = new ArrayList<Pair<String, String>>(obj.size);
				obj.forEach(v -> array.add(new Pair<>(v.name, v.asString())));
				states.add(array);
			}
		} else {
			var array = new ArrayList<Pair<String, String>>(value.size);
			value.forEach(v -> array.add(new Pair<>(v.name, v.asString())));
			states = List.of(array);
		}
	}
}
