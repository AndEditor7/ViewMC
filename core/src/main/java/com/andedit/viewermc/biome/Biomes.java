package com.andedit.viewermc.biome;

import com.badlogic.gdx.utils.ObjectMap;

public class Biomes {

	public static final Biome VOID;
	
	private static final ObjectMap<String, Biome> BIOMES = new ObjectMap<String, Biome>(200);

	static {
		put("the_void", VOID = new Biome(0.5f, 0.5f, 0x7BA4FF));
		put("plains", new Biome(0.8f, 0.4f, 0x78A7FF));
		put("sunflower_plains", new Biome(0.8f, 0.4f, 0x78A7FF));
		put("snowy_plains", new Biome(0.0f, 0.5f, 0x7FA1FF));
		put("ice_spikes", new Biome(0.0f, 0.5f, 0x7FA1FF));
		put("desert", new Biome(2.0f, 0.0f, 0x6EB1FF));
		put("swamp", new Biome(0.8f, 0.9f, 0x78A7FF));
		put("mangrove_swamp", new Biome(0.8f, 0.9f, 0x7BA4FF));
		put("forest", new Biome(0.7f, 0.8f, 0x79A6FF));
		put("flower_forest", new Biome(0.7f, 0.8f, 0x79A6FF));
		put("birch_forest", new Biome(0.6f, 0.6f, 0x7AA5FF));
		put("dark_forest", new Biome(0.7f, 0.8f, 0x79A6FF));
		put("old_growth_birch_forest", new Biome(0.6f, 0.6f, 0x7AA5FF));
		put("old_growth_pine_taiga", new Biome(0.3f, 0.8f, 0x7CA3FF));
		put("old_growth_spruce_taiga", new Biome(0.25f, 0.8f, 0x7DA3FF));
		put("taiga", new Biome(0.25f, 0.8f, 0x7DA3FF));
		put("snowy_taiga", new Biome(-0.5f, 0.4f, 0x839EFF));
		put("savanna", new Biome(2.0f, 0.0f, 0x6EB1FF));
		put("savanna_plateau", new Biome(2.0f, 0.0f, 0x6EB1FF));
		put("windswept_hills", new Biome(0.2f, 0.3f, 0x7DA2FF));
		put("windswept_gravelly_hills", new Biome(0.2f, 0.3f, 0x7DA2FF));
		put("windswept_forest", new Biome(0.2f, 0.3f, 0x7DA2FF));
		put("windswept_savanna", new Biome(2.0f, 0.0f, 0x6EB1FF));
		put("jungle", new Biome(0.95f, 0.9f, 0x77A8FF));
		put("sparse_jungle", new Biome(0.95f, 0.9f, 0x77A8FF));
		put("bamboo_jungle", new Biome(0.95f, 0.9f, 0x77A8FF));
		put("badlands", new Biome(2.0f, 0.0f, 0x6EB1FF));
		put("eroded_badlands", new Biome(2.0f, 0.0f, 0x6EB1FF));
		put("wooded_badlands", new Biome(2.0f, 0.0f, 0x6EB1FF));
		put("meadow", new Biome(0.5f, 0.8f, 0x7BA4FF));
		put("grove", new Biome(-0.2f, 0.8f, 0x81A0FF));
		put("snowy_slopes", new Biome(-0.3f, 0.9f, 0x829FFF));
		put("frozen_peaks", new Biome(-0.7f, 0.9f, 0x859DFF));
		put("jagged_peaks", new Biome(-0.7f, 0.9f, 0x859DFF));
		put("stony_peaks", new Biome(1.0f, 0.3f, 0x76A8FF));
		put("river", new Biome(0.5f, 0.5f, 0x7BA4FF));
		put("frozen_river", new Biome(0.0f, 0.5f, 0x7FA1FF));
		put("beach", new Biome(0.8f, 0.4f, 0x78A7FF));
		put("snowy_beach", new Biome(0.05f, 0.3f, 0x7FA1FF));
		put("stony_shore", new Biome(0.2f, 0.3f, 0x7DA2FF));
		put("warm_ocean", new Biome(0.5f, 0.5f, 0x7BA4FF));
		put("lukewarm_ocean", new Biome(0.5f, 0.5f, 0x7BA4FF));
		put("deep_lukewarm_ocean", new Biome(0.5f, 0.5f, 0x7BA4FF));
		put("ocean", new Biome(0.5f, 0.5f, 0x7BA4FF));
		put("deep_ocean", new Biome(0.5f, 0.5f, 0x7BA4FF));
		put("cold_ocean", new Biome(0.5f, 0.5f, 0x7BA4FF));
		put("deep_cold_ocean", new Biome(0.5f, 0.5f, 0x7BA4FF));
		put("frozen_ocean", new Biome(0.0f, 0.5f, 0x7FA1FF));
		put("deep_frozen_ocean", new Biome(0.5f, 0.5f, 0x7BA4FF));
		put("mushroom_fields", new Biome(0.9f, 1.0f, 0x77A8FF));
		put("dripstone_caves", new Biome(0.8f, 0.4f, 0x78A7FF));
		put("lush_caves", new Biome(0.5f, 0.5f, 0x7BA4FF));
		put("deep_dark", new Biome(0.8f, 0.4f, 0x7BA4FF));
		put("nether_wastes", new Biome(2.0f, 0.0f, 0x6EB1FF));
		put("warped_forest", new Biome(2.0f, 0.0f, 0x6EB1FF));
		put("crimson_forest", new Biome(2.0f, 0.0f, 0x6EB1FF));
		put("soul_sand_valley", new Biome(2.0f, 0.0f, 0x6EB1FF));
		put("basalt_deltas", new Biome(2.0f, 0.0f, 0x6EB1FF));
		put("the_end", new Biome(0.5f, 0.5f, 0));
		put("end_highlands", new Biome(0.5f, 0.5f, 0));
		put("end_midlands", new Biome(0.5f, 0.5f, 0));
		put("small_end_islands", new Biome(0.5f, 0.5f, 0));
		put("end_barrens", new Biome(0.5f, 0.5f, 0));
	}

	private static void put(String id, Biome biome) {
		BIOMES.put("minecraft:".concat(id), biome);
	}

	public static Biome toBiome(String id) {
		return BIOMES.get(id, VOID);
	}
}
