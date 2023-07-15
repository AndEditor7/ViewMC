package com.andedit.viewmc.block;

import com.andedit.viewmc.util.Util;
import com.andedit.viewmc.world.BlockView;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectMap;

public class BlockColors {
	
	private static final Vector3[] COLORS = Util.make(new Vector3[16], colors -> {
		colors[0] = new Vector3();
    });
	
	private static final ObjectMap<String, BlockColorProvider> MAP = new ObjectMap<>();
	
	static {
		register((state, view, x, y, z, index) -> {
            return view.getGrassColor(x, state.get("half").equals("upper") ? y-1 : y, z);
        }, "large_fern", "tall_grass");
		
        register((state, view, x, y, z, index) -> {
            return view.getGrassColor(x, y, z);
        }, "grass_block", "fern", "grass", "potted_fern");
        
        register((state, view, x, y, z, index) -> 0x619961, "spruce_leaves");
        
        register((state, view, x, y, z, index) -> 8431445, "birch_leaves");
        
        register((state, view, x, y, z, index) -> {
            return view.getFoliageColor(x, y, z);
        }, "oak_leaves", "jungle_leaves", "acacia_leaves", "dark_oak_leaves", "vine", "mangrove_leaves");
        
        register((state, view, x, y, z, index) -> {
            return view.getBiome(x, y, z).waterColor;
        }, "water", "bubble_column", "water_cauldron");
        
        register((state, view, x, y, z, index) -> {
        	var col = COLORS[state.getInt("power")];
        	return Color.rgb888(col.x, col.y, col.z);
        }, "redstone_wire");
        
        register((state, view, x, y, z, index) -> {
            return view.getGrassColor(x, y, z);
        }, "sugar_cane");
        
        register((state, view, x, y, z, index) -> 14731036, "attached_melon_stem", "attached_pumpkin_stem");
        
        register((state, view, x, y, z, index) -> {
            int i = state.getInt("age");
            int j = i * 32;
            int k = 255 - i * 8;
            int l = i * 4;
            return j << 16 | k << 8 | l;
            
        }, "melon_stem", "pumpkin_stem");
        
        register((state, view, x, y, z, index) -> {
            return view.getBiome(x, y, z).waterColor;
        }, "water");

		register((state, view, x, y, z, index) -> 0x208030, "lily_pad");
	}
	
	private static void register(BlockColorProvider provider, String... blocks) {
		for (var id : blocks) {
			MAP.put("minecraft:".concat(id), provider);
		}
	}
	
	public static float getColorFloat(BlockState state, BlockView view, int x, int y, int z, int index) {
		final int color = getColorInt(state, view, x, y, z, index);
		return Color.toFloatBits((color >>> 16) & 0xFF, (color >>> 8) & 0xFF, color & 0xFF, 255);
	}
	
	public static int getColorInt(BlockState state, BlockView view, int x, int y, int z, int index) {
		var provider = getProvider(state.block);
		return provider == null ? -1 : provider.getColor(state, view, x, y, z, index);
	}
	
	@Null
	public static BlockColorProvider getProvider(Block block) {
		return MAP.get(block.getId());
	}
	
	public static interface BlockColorProvider {
		int getColor(BlockState state, BlockView view, int x, int y, int z, int index);
	}
}
