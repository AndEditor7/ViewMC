package com.andedit.viewermc.block.state;

import java.util.ArrayList;
import java.util.List;

import com.andedit.viewermc.util.Identifier;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.OrderedSet;

public class BlockStateJson {
	
	public @Null List<VariantJson> variants;
	public @Null List<CaseJson> cases;
	
	public BlockStateJson(JsonValue value, OrderedSet<Identifier> models) {
		value = value.child;
		var name = value.name;
		if (name.equalsIgnoreCase("variants")) {
			variants = new ArrayList<>(value.size);
			for (var obj : value) {
				variants.add(new VariantJson(obj, models));
			}
		} else if (name.equalsIgnoreCase("multipart")) {
			cases = new ArrayList<>(value.size);
			for (var obj : value) {
				cases.add(new CaseJson(obj, models));
			}
		}
	}
	
	public boolean isVariants() {
		return variants != null; 
	}
	
	public boolean isMultipart() {
		return cases != null; 
	}
}
