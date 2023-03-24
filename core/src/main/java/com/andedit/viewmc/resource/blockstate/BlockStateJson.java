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
		
		var variants = value.get("variants");
		if (variants != null) {
			this.variants = new ArrayList<>(variants.size);
			for (var obj : variants) {
				this.variants.add(new VariantJson(obj, models));
			}
		}
		
		var multipart = value.get("multipart");
		if (multipart != null) {
			cases = new ArrayList<>(multipart.size);
			for (var obj : multipart) {
				cases.add(new CaseJson(obj, models));
			}
		}
		
		if (variants == null && multipart == null) {
			throw new IllegalStateException("No variants or multipart");
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
			var srcModel = src.get(model);
			if (srcModel != null) {
				dest.put(model, srcModel);
			}
		}
	}
}
