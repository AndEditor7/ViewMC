package com.andedit.viewmc.resource;

import static com.andedit.viewmc.resource.ResourceLoader.LOGGER;

import com.andedit.viewmc.block.Block;
import com.andedit.viewmc.block.TextureAtlas;
import com.andedit.viewmc.block.container.AirBlock;
import com.andedit.viewmc.block.container.LavaBlock;
import com.andedit.viewmc.block.container.LeavesBlock;
import com.andedit.viewmc.block.container.MissingBlock;
import com.andedit.viewmc.block.container.WaterBlock;
import com.andedit.viewmc.resource.blockmodel.BlockModelJson;
import com.andedit.viewmc.resource.blockstate.BlockStateJson;
import com.andedit.viewmc.util.Identifier;
import com.andedit.viewmc.util.Progress;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

/** Contains block models and textures. */
public class Resources implements Disposable {
	
	private final TextureAtlas textures;
	private final int[][] grassColors, foliageColors;
	private final ObjectMap<String, Block> idToBlock;
	private final ObjectMap<String, Block> customBlocks = new ObjectMap<>(200);
	private final Block water;

	public Resources(RawResources resources, TextureAtlas textures, Progress progress) {
		this.textures = textures;
		
		Pixmap pixmap;
		var bytes = resources.textures.get(new Identifier("textures/colormap/grass"));
		pixmap = new Pixmap(bytes, 0, bytes.length);
		grassColors = toIntArray(pixmap);
		pixmap.dispose();
		
		bytes = resources.textures.get(new Identifier("textures/colormap/foliage"));
		pixmap = new Pixmap(bytes, 0, bytes.length);
		foliageColors = toIntArray(pixmap);
		pixmap.dispose();
		
		
		final var map = customBlocks;
		map.put("oak_leaves", new LeavesBlock());
		map.put("birch_leaves", new LeavesBlock());
		map.put("spruce_leaves", new LeavesBlock());
		map.put("jungle_leaves", new LeavesBlock());
		map.put("acacia_leaves", new LeavesBlock());
		map.put("dark_oak_leaves", new LeavesBlock());
		map.put("mangrove_leaves", new LeavesBlock());
		map.put("azalea_leaves", new LeavesBlock());
		map.put("flowering_azalea_leaves", new LeavesBlock());
		map.put("water", water = new WaterBlock());
		map.put("lava", new LavaBlock());
		map.put("air", new AirBlock());
		map.put("cave_air", new AirBlock());
		map.put("void_air", new AirBlock());
		
		progress.setStatus("Generating Blocks");
		progress.newStep(resources.blockStates.size);
		idToBlock = new ObjectMap<>(resources.blockStates.size);
		for (var entry : resources.blockStates) {
			var id = entry.key;
			var state = entry.value;
			idToBlock.put(id.full, createBlock(id, state, resources.blockModels));
			progress.incStep();
		}
		
		progress.setStatus("Finished!");
		LOGGER.info("Finished");
	}

	private Block createBlock(Identifier id, BlockStateJson state, OrderedMap<Identifier, BlockModelJson> blockModels) {
		var block = customBlocks.get(id.path);
		if (block == null) {
			block = new Block();
		}
		
		block.init(id);
		
		try {
			block.loadModel(state, blockModels, textures);
		} catch (Exception e) {
			LOGGER.info("Error from generating the block: " + id);
			block = new MissingBlock();
			block.init(id);
			block.loadModel(state, blockModels, textures);
		}
		
		return block;
	}
	
	public Block toBlock(String id) {
		var block = idToBlock.get(id);
		return block == null ? AirBlock.INSTANCE : block;
	}
	
	public Block getWaterBlock() {
		return water;
	}
	
	public int getGrassColor(float temperature, float humidity) {
        int y = (int)((1f - (humidity * temperature)) * 255f);
        int x = (int)((1f - temperature) * 255f);
        if (((y << 8) | x) < 65536) {
        	return grassColors[x][y];
        }
        return 0xFFFFFFFF;
    }
	
	public int getFoliageColor(float temperature, float humidity) {
        int y = (int)((1f - (humidity * temperature)) * 255f);
        int x = (int)((1f - temperature) * 255f);
        if (((y << 8) | x) < 65536) {
        	return foliageColors[x][y];
        }
        return 4764952;
    }
	
	public void init() {
		textures.createTexture();
	}
	
	public void bindTexture() {
		textures.bind();
	}
	
	public int getTextureUnit() {
		return textures.getUnit();
	}
	
	/** Only update if the texture is binded. */
	public void update() {
		textures.update();
	}

	@Override
	public void dispose() {
		textures.dispose();
		idToBlock.clear();
		customBlocks.clear();
	}
	
	private static int[][] toIntArray(Pixmap pixmap) {
		var ints = new int[pixmap.getWidth()][pixmap.getHeight()];
		for (int x = 0; x < pixmap.getWidth(); x++)
		for (int y = 0; y < pixmap.getHeight(); y++) {
			ints[x][y] = pixmap.getPixel(x, y) >>> 8;
		}
		return ints;
	}
}
