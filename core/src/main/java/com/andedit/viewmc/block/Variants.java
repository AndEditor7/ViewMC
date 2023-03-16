package com.andedit.viewmc.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.resource.blockstate.BlockStateJson;
import com.andedit.viewmc.resource.blockstate.VariantJson;
import com.andedit.viewmc.util.ModelSupplier;
import com.andedit.viewmc.util.Util;
import com.andedit.viewmc.world.Section;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Variants implements Renderable {
	
	public final List<Variant> variants;
	
	public Variants(BlockStateJson state, ModelSupplier supplier) {
		variants = new ArrayList<>(state.variants.size());
		
		for (var json : state.variants) {
			variants.add(new Variant(json, supplier));
		}
	}

	@Override
	public void build(Section section, MeshProvider provider, BlockState state, int x, int y, int z) {
		for (int i = 0; i < variants.size(); i++) {
			var variant = variants.get(i);
			if (variant.test(state)) {
				variant.build(section, provider, state, x, y, z);
				return;
			}
		}
	}

	@Override
	public void getQuads(BlockState state, Collection<Quad> collection, int x, int y, int z) {
		for (int i = 0; i < variants.size(); i++) {
			var variant = variants.get(i);
			if (variant.test(state)) {
				variant.getQuads(state, collection, x, y, z);
				return;
			}
		}
	}

	@Override
	public void getBoxes(BlockState state, Collection<BoundingBox> collection, int x, int y, int z) {
		for (int i = 0; i < variants.size(); i++) {
			var variant = variants.get(i);
			if (variant.test(state)) {
				variant.getBoxes(state, collection, x, y, z);
				return;
			}
		}
	}
	
	@Override
	public boolean isFullOpaque(BlockState state, int x, int y, int z) {
		for (int i = 0; i < variants.size(); i++) {
			var variant = variants.get(i);
			if (variant.test(state)) {
				return variant.isFullOpaque(state, x, y, z);
			}
		}
		return false;
	}
	
	private static class Variant implements Predicate<BlockState>, Renderable {
		final ArrayList<Predicate<BlockState>> cases = new ArrayList<>();
		final Weighted<BlockModel> models = new Weighted<BlockModel>();
		
		Variant(VariantJson variant, ModelSupplier supplier) {
			var strings = Util.split(variant.variant, ',');
			for (var string : strings) {
				if (!string.isEmpty()) {
					var array = Util.split(string, '=');
					if (array.size() == 2) {
						cases.add(new Case(array));
					}
				}
			}
			for (var model : variant.models) {
				models.add(model.weight, supplier.config(model.model, supplier.get(model.model).create(model)));
			}
		}
		
		@Override
		public void build(Section section, MeshProvider provider, BlockState state, int x, int y, int z) {
			models.apply((int)Util.hashCode(x, y, z)).build(section, provider, state, x, y, z);
		}

		@Override
		public void getQuads(BlockState state, Collection<Quad> collection, int x, int y, int z) {
			models.apply((int)Util.hashCode(x, y, z)).getQuads(collection);
		}

		@Override
		public void getBoxes(BlockState state, Collection<BoundingBox> collection, int x, int y, int z) {
			models.apply((int)Util.hashCode(x, y, z)).getBoxes(collection);
		}
		
		@Override
		public boolean isFullOpaque(BlockState state, int x, int y, int z) {
			return models.apply((int)Util.hashCode(x, y, z)).isFullOpaque();
		}

		@Override
		public boolean test(BlockState state) {
			for (int i = 0; i < cases.size(); i++) {
				if (!cases.get(i).test(state)) return false; 
			}
			return true;
		}
		
		static class Case implements Predicate<BlockState>  {
			
			final String key, value;
			
			public Case(List<String> array) {
				key = array.get(0);
				value = array.get(1);
			}
			
			@Override
			public boolean test(BlockState state) {
				return value.equals(state.get(key));
			}
		}
	}
}
