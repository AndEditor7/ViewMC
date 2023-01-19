package com.andedit.viewermc.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import com.andedit.viewermc.block.BlockModel.Quad;
import com.andedit.viewermc.block.state.BlockStateJson;
import com.andedit.viewermc.block.state.CaseJson;
import com.andedit.viewermc.block.state.WhenJson;
import com.andedit.viewermc.graphic.MeshBuilder;
import com.andedit.viewermc.util.ModelSupplier;
import com.andedit.viewermc.util.Pair;
import com.andedit.viewermc.util.Util;
import com.andedit.viewermc.world.Section;
import com.andedit.viewermc.world.World;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Multipart implements Renderable {

	private final List<Case> cases;
	
	public Multipart(BlockStateJson state, ModelSupplier supplier) {
		cases = new ArrayList<>(state.cases.size());
		
		for (var json : state.cases) {
			cases.add(new Case(json, supplier));
		}
	}

	@Override
	public void build(Section section, MeshBuilder builder, BlockState state, int x, int y, int z) {
		for (int i = 0; i < cases.size(); i++) {
			cases.get(i).build(section, builder, state, x, y, z);
		}
	}

	@Override
	public void getQuads(BlockState state, Collection<Quad> collection, int x, int y, int z) {
		for (int i = 0; i < cases.size(); i++) {
			cases.get(i).getQuads(state, collection, x, y, z);
		}
	}

	@Override
	public void getBoxes(BlockState state, Collection<BoundingBox> collection, int x, int y, int z) {
		for (int i = 0; i < cases.size(); i++) {
			cases.get(i).getBoxes(state, collection, x, y, z);
		}
	}
	
	@Override
	public boolean isFullOpaque(BlockState state, int x, int y, int z) {
		return false;
	}
	
	private static class Case implements Renderable {
		
		final Predicate<BlockState> when;
		final Weighted<BlockModel> models; // apply
		
		private Case(CaseJson caseJson, ModelSupplier supplier) {
			when = caseJson.when == null ? s -> true : new When(caseJson.when);
			
			models = new Weighted<>();
			for (var model : caseJson.apply) {
				models.add(model.weight, supplier.config(model.model, supplier.get(model.model).create(model)));
			}
		}

		@Override
		public void build(Section section, MeshBuilder builder, BlockState state, int x, int y, int z) {
			if (when.test(state)) {
				models.apply((int)Util.hashCode(x, y, z)).build(section, builder, state, x, y, z);
			}
		}

		@Override
		public void getQuads(BlockState state, Collection<Quad> collection, int x, int y, int z) {
			if (when.test(state)) {
				models.apply((int)Util.hashCode(x, y, z)).getQuads(collection);
			}
		}

		@Override
		public void getBoxes(BlockState state, Collection<BoundingBox> collection, int x, int y, int z) {
			if (when.test(state)) {
				models.apply((int)Util.hashCode(x, y, z)).getBoxes(collection);
			}
		}
		
		@Override
		public boolean isFullOpaque(BlockState state, int x, int y, int z) {
			return false;
		}
		
		static class When implements Predicate<BlockState> {
			
			// Test AND/OR
			final List<Condition> conditions;
			final boolean isAnd, isOr;
			
			private When(WhenJson whenJson) {
				isAnd = whenJson.isAnd;
				isOr = whenJson.isOr;
				
				conditions = new ArrayList<>(whenJson.states.size());
				for (var list : whenJson.states) {
					conditions.add(new Condition(list));
				}
			}

			@Override
			public boolean test(BlockState state) {
				if (isOr) {
					for (int i = 0; i < conditions.size(); i++) {
						if (conditions.get(i).test(state)) {
							return true;
						}
					}
					return false;
				}
				
				if (isAnd) {
					for (int i = 0; i < conditions.size(); i++) {
						if (!conditions.get(i).test(state)) {
							return false;
						}
					}
					return true;
				}
				
				return conditions.get(0).test(state);
			}
			
			static class Condition implements Predicate<BlockState> {
				
				final List<State> states;

				private Condition(List<Pair<String, String>> list) {
					states = new ArrayList<>(list.size());
					for (var pair : list) {
						states.add(new State(pair));
					}
				}
				
				@Override
				public boolean test(BlockState state) {
					for (int i = 0; i < states.size(); i++) {
						if (!states.get(i).test(state)) return false;
					}
					return true;
				}
				
				static class State implements Predicate<BlockState> {
					
					final String key;
					final List<String> values;
					
					private State(Pair<String, String> pair) {
						key = pair.left;
						values = Util.split(pair.right, '|'); 
					}

					@Override
					public boolean test(BlockState state) {
						var value = state.get(key);
						for (int i = 0; i < values.size(); i++) {
							if (values.get(i).equals(value)) {
								return true;
							}
						}
						return false;
					}
				}
			}
		}
	}
}
