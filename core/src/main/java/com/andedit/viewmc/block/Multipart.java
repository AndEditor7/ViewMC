package com.andedit.viewmc.block;

import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.resource.blockstate.BlockStateJson;
import com.andedit.viewmc.resource.blockstate.CaseJson;
import com.andedit.viewmc.resource.blockstate.WhenJson;
import com.andedit.viewmc.util.ModelSupplier;
import com.andedit.viewmc.util.Pair;
import com.andedit.viewmc.util.Util;
import com.andedit.viewmc.world.BlockView;
import com.badlogic.gdx.math.collision.BoundingBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class Multipart implements BlockRenderers {

	protected final BlockRenderer renderer;

	public Multipart(BlockStateJson state, ModelSupplier supplier) {
		var cases = new ArrayList<BlockRenderer>(state.cases.size());
		for (var json : state.cases) {
			cases.add(new Case(json, supplier));
		}
		renderer = new BlockRenderer() {
			@Override
			public void build(MeshProvider provider, BlockView view, BlockState state, int x, int y, int z) {
				for (int i = 0; i < cases.size(); i++) {
					cases.get(i).build(provider, view, state, x, y, z);
				}
			}

			@Override
			public void getQuads(Collection<BlockModel.Quad> collection, BlockView view, BlockState state, int x, int y, int z) {
				for (int i = 0; i < cases.size(); i++) {
					cases.get(i).getQuads(collection, view, state, x, y, z);
				}
			}

			@Override
			public void getBoxes(Collection<BoundingBox> collection, BlockView view, BlockState state, int x, int y, int z) {
				for (int i = 0; i < cases.size(); i++) {
					cases.get(i).getBoxes(collection, view, state, x, y, z);
				}
			}

			@Override
			public boolean isFullOpaque(BlockView view, BlockState state, int x, int y, int z) {
				return false;
			}
		};
	}
	
	@Override
	public BlockRenderer getRenderer(BlockView view, BlockState state, int x, int y, int z) {
		return renderer;
	}

	private static class Case implements BlockRenderer {

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
		public void build(MeshProvider provider, BlockView view, BlockState state, int x, int y, int z) {
			if (when.test(state)) {
				models.apply((int)Util.hashCode(x, y, z)).build(provider, view, state, x, y, z);
			}
		}

		@Override
		public void getQuads(Collection<BlockModel.Quad> collection, BlockView view, BlockState state, int x, int y, int z) {
			if (when.test(state)) {
				models.apply((int)Util.hashCode(x, y, z)).getQuads(collection, view, state, x, y, z);
			}
		}

		@Override
		public void getBoxes(Collection<BoundingBox> collection, BlockView view, BlockState state, int x, int y, int z) {
			if (when.test(state)) {
				models.apply((int)Util.hashCode(x, y, z)).getBoxes(collection, view, state, x, y, z);
			}
		}
		
		@Override
		public boolean isFullOpaque(BlockView view, BlockState state, int x, int y, int z) {
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

						var array = Util.split(pair.right, '|');
						array.trimToSize();
						values = array;
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
