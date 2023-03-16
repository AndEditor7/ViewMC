package com.andedit.viewmc.resource.blockstate;

import java.util.ArrayList;
import java.util.List;

import com.andedit.viewmc.resource.blockmodel.BlockModelJson;
import com.andedit.viewmc.util.Identifier;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedSet;

public class BlockStateJson {
	
	public @Null List<VariantJson> variants;
	public @Null List<CaseJson> cases;
	
	private final OrderedSet<Identifier> models = new OrderedSet<>();
	
	public BlockStateJson(JsonValue value, OrderedSet<Identifier> modelsSet) {
		value = value.child;
		var name = value.name;
		if (name.equals("variants")) {
			variants = new ArrayList<>(value.size);
			for (var obj : value) {
				variants.add(new VariantJson(obj, models));
			}
		} else if (name.equals("multipart")) {
			cases = new ArrayList<>(value.size);
			for (var obj : value) {
				cases.add(new CaseJson(obj, models));
			}
		}
		
		modelsSet.addAll(models);
	}
	
	public boolean isVariants() {
		return variants != null; 
	}
	
	public boolean isMultipart() {
		return cases != null; 
	}

	public void addAll(ObjectMap<Identifier, BlockModelJson> src, ObjectMap<Identifier, BlockModelJson> dest) {
		for (var model : models) {
			dest.put(model, src.get(model));
		}
	}
}
