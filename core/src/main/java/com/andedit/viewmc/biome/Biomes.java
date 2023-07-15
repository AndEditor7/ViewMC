package com.andedit.viewmc.biome;

import com.badlogic.gdx.utils.ObjectMap;

public class Biomes {

	public static final Biome VOID;
	
	private static final ObjectMap<String, Biome> BIOMES = new ObjectMap<String, Biome>(200);

	static {
		put("the_void", VOID = new Biome(0.5f, 0.5f, 0x7BA4FF, 0));
		put("plains", new Biome(0.8f, 0.4f, 0x78A7FF, 0));
		put("sunflower_plains", new Biome(0.8f, 0.4f, 0x78A7FF, 0));
		put("snowy_plains", new Biome(0.0f, 0.5f, 0x7FA1FF, 0));
		put("ice_spikes", new Biome(0.0f, 0.5f, 0x7FA1FF, 0));
		put("desert", new Biome(2.0f, 0.0f, 0x6EB1FF, 0));
		put("swamp", new Biome(0.8f, 0.9f, 0x78A7FF, 0x617B64, 0x4C763C, 0x6A7039));
		put("mangrove_swamp", new Biome(0.8f, 0.9f, 0x7BA4FF, 0x3A7A6A, 0x6A7039, 0x8DB127));
		put("forest", new Biome(0.7f, 0.8f, 0x79A6FF, 0));
		put("flower_forest", new Biome(0.7f, 0.8f, 0x79A6FF, 0));
		put("birch_forest", new Biome(0.6f, 0.6f, 0x7AA5FF, 0));
		put("dark_forest", new Biome(0.7f, 0.8f, 0x79A6FF, 0));
		put("old_growth_birch_forest", new Biome(0.6f, 0.6f, 0x7AA5FF, 0));
		put("old_growth_pine_taiga", new Biome(0.3f, 0.8f, 0x7CA3FF, 0));
		put("old_growth_spruce_taiga", new Biome(0.25f, 0.8f, 0x7DA3FF, 0));
		put("taiga", new Biome(0.25f, 0.8f, 0x7DA3FF, 0));
		put("snowy_taiga", new Biome(0.2f, 0.4f, 0x839EFF, 0));
		put("savanna", new Biome(2.0f, 0.0f, 0x6EB1FF, 0));
		put("savanna_plateau", new Biome(2.0f, 0.0f, 0x6EB1FF, 0));
		put("windswept_hills", new Biome(0.2f, 0.3f, 0x7DA2FF, 0));
		put("windswept_gravelly_hills", new Biome(0.2f, 0.3f, 0x7DA2FF, 0));
		put("windswept_forest", new Biome(0.2f, 0.3f, 0x7DA2FF, 0));
		put("windswept_savanna", new Biome(2.0f, 0.0f, 0x6EB1FF, 0));
		put("jungle", new Biome(0.95f, 0.9f, 0x77A8FF, 0));
		put("sparse_jungle", new Biome(0.95f, 0.9f, 0x77A8FF, 0));
		put("bamboo_jungle", new Biome(0.95f, 0.9f, 0x77A8FF, 0));
		put("badlands", new Biome(2.0f, 0.0f, 0x6EB1FF, 0));
		put("eroded_badlands", new Biome(2.0f, 0.0f, 0x6EB1FF, 0));
		put("wooded_badlands", new Biome(2.0f, 0.0f, 0x6EB1FF, 0));
		put("meadow", new Biome(0.5f, 0.8f, 0x7BA4FF, 0));
		put("grove", new Biome(0.2f, 0.8f, 0x81A0FF, 0));
		put("snowy_slopes", new Biome(0.2f, 0.9f, 0x829FFF, 0));
		put("frozen_peaks", new Biome(0.2f, 0.9f, 0x859DFF, 0));
		put("jagged_peaks", new Biome(0.2f, 0.9f, 0x859DFF, 0));
		put("stony_peaks", new Biome(1.0f, 0.3f, 0x76A8FF, 0));
		put("river", new Biome(0.5f, 0.5f, 0x7BA4FF, 0));
		put("frozen_river", new Biome(0.0f, 0.5f, 0x7FA1FF, 0));
		put("beach", new Biome(0.8f, 0.4f, 0x78A7FF, 0));
		put("snowy_beach", new Biome(0.05f, 0.3f, 0x7FA1FF, 0));
		put("stony_shore", new Biome(0.2f, 0.3f, 0x7DA2FF, 0));
		put("warm_ocean", new Biome(0.5f, 0.5f, 0x7BA4FF, 0x43D5EE));
		put("lukewarm_ocean", new Biome(0.5f, 0.5f, 0x7BA4FF, 0x45ADF2));
		put("deep_lukewarm_ocean", new Biome(0.5f, 0.5f, 0x7BA4FF, 0x45ADF2));
		put("ocean", new Biome(0.5f, 0.5f, 0x7BA4FF, 0x3F76E4));
		put("deep_ocean", new Biome(0.5f, 0.5f, 0x7BA4FF, 0x3F76E4));
		put("cold_ocean", new Biome(0.5f, 0.5f, 0x7BA4FF, 0x3D57D6));
		put("deep_cold_ocean", new Biome(0.5f, 0.5f, 0x7BA4FF, 0x3D57D6));
		put("frozen_ocean", new Biome(0.0f, 0.5f, 0x7FA1FF, 0x3938C9));
		put("deep_frozen_ocean", new Biome(0.5f, 0.5f, 0x7BA4FF, 0x3938C9));
		put("mushroom_fields", new Biome(0.9f, 1.0f, 0x77A8FF, 0));
		put("dripstone_caves", new Biome(0.8f, 0.4f, 0x78A7FF, 0));
		put("lush_caves", new Biome(0.5f, 0.5f, 0x7BA4FF, 0));
		put("deep_dark", new Biome(0.8f, 0.4f, 0x7BA4FF, 0));
		put("nether_wastes", new Biome(2.0f, 0.0f, 0x6EB1FF, 0));
		put("warped_forest", new Biome(2.0f, 0.0f, 0x6EB1FF, 0));
		put("crimson_forest", new Biome(2.0f, 0.0f, 0x6EB1FF, 0));
		put("soul_sand_valley", new Biome(2.0f, 0.0f, 0x6EB1FF, 0));
		put("basalt_deltas", new Biome(2.0f, 0.0f, 0x6EB1FF, 0));
		put("the_end", new Biome(0.5f, 0.5f, 0, 0));
		put("end_highlands", new Biome(0.5f, 0.5f, 0, 0));
		put("end_midlands", new Biome(0.5f, 0.5f, 0, 0));
		put("small_end_islands", new Biome(0.5f, 0.5f, 0, 0));
		put("end_barrens", new Biome(0.5f, 0.5f, 0, 0));
		put("cherry_grove", new Biome(0.5f, 0.8f, 0x78A7FF, 0, 0xB6DB61));
	}

	private static void put(String id, Biome biome) {
		BIOMES.put("minecraft:".concat(id), biome);
	}

	public static Biome toBiome(String id) {
		return BIOMES.get(id, VOID);
	}
}
