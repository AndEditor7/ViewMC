package com.andedit.viewmc.block;

import com.andedit.viewmc.util.Util;
import com.andedit.viewmc.world.BlockRenderView;
import com.andedit.viewmc.world.BlockView;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectMap;

public class BlockColors {
	
	private static final Vector3[] COLORS = Util.make(new Vector3[16], colors -> {
        for (int i = 0; i <= 15; ++i) {
            float f = 0;
            float r = f * 0.6f + ((f = (float)i / 15.0f) > 0.0f ? 0.4f : 0.3f);
            float g = MathUtils.clamp(f * f * 0.7f - 0.5f, 0.0f, 1.0f);
            float b = MathUtils.clamp(f * f * 0.6f - 0.7f, 0.0f, 1.0f);
            colors[i] = new Vector3(r, g, b);
        }
    });
	
	private static final ObjectMap<String, BlockColorProvider> MAP = new ObjectMap<>();
	
	static {
		register((state, view, x, y, z, index) -> {
			if (view instanceof BlockRenderView renderView ) {
				return renderView.getGrassColor(x, state.get("half").equals("upper") ? y-1 : y, z);
			}
            return 0x79C05A;
        }, "large_fern", "tall_grass");
		
        //blockColors.registerColorProperty(TallPlantBlock.HALF, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
		
        register((state, view, x, y, z, index) -> {
        	if (view instanceof BlockRenderView renderView ) {
        		return renderView.getGrassColor(x, y, z);
        	}
            return 0x79C05A;
        }, "grass_block", "fern", "grass", "potted_fern");
        
        register((state, view, x, y, z, index) -> 0x619961, "spruce_leaves");
        
        register((state, view, x, y, z, index) -> 8431445, "birch_leaves");
        
        register((state, view, x, y, z, index) -> {
        	if (view instanceof BlockRenderView renderView ) {
        		return renderView.getFoliageColor(x, y, z);
        	}
            return 0x59AE30;
        }, "oak_leaves", "jungle_leaves", "acacia_leaves", "dark_oak_leaves", "vine", "mangrove_leaves");
        
        register((state, view, x, y, z, index) -> {
            return -1; // BiomeColors.getWaterColor(world, pos);
        }, "water", "bubble_column", "water_cauldron");
        
        register((state, view, x, y, z, index) -> {
        	var col = COLORS[state.getInt("power")];
        	return Color.rgb888(col.x, col.y, col.z);
        }, "redstone_wire");
        
        //blockColors.registerColorProperty(RedstoneWireBlock.POWER, Blocks.REDSTONE_WIRE);
        
        register((state, view, x, y, z, index) -> {
        	if (view instanceof BlockRenderView renderView) {
        		return renderView.getGrassColor(x, y, z);
        	}
            return 0x79C05A;
        }, "sugar_cane");
        
        register((state, view, x, y, z, index) -> 14731036, "attached_melon_stem", "attached_pumpkin_stem");
        
        register((state, view, x, y, z, index) -> {
            int i = state.getInt("age");
            int j = i * 32;
            int k = 255 - i * 8;
            int l = i * 4;
            return j << 16 | k << 8 | l;
            
        }, "melon_stem", "pumpkin_stem");
        
        //blockColors.registerColorProperty(StemBlock.AGE, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
        
        register((state, view, x, y, z, index) -> {
            return 0x3f76e4;
        }, "water");
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
