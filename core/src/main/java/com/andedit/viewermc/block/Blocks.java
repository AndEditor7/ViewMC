package com.andedit.viewermc.block;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.andedit.viewermc.block.container.AirBlock;
import com.andedit.viewermc.block.container.LavaBlock;
import com.andedit.viewermc.block.container.LeavesBlock;
import com.andedit.viewermc.block.container.WaterBlock;
import com.andedit.viewermc.block.model.BlockModelJson;
import com.andedit.viewermc.block.state.BlockStateJson;
import com.andedit.viewermc.util.ByteArrayOutput;
import com.andedit.viewermc.util.Identifier;
import com.andedit.viewermc.util.Pair;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.StreamUtils;

/** Contains block models and textures. */
public class Blocks implements Disposable {
	
	private final TextureAtlas textures;
	private final Pixmap grassColors, foliageColors;
	private final ObjectMap<String, Block> idToBlock;
	private final ObjectMap<String, Block> customBlocks = new ObjectMap<>(200);
	private final Block water;

	public Blocks(ZipFile file, ObjectMap<Identifier, ZipEntry> entries, List<Pair<Identifier, BlockStateJson>> blockStates, OrderedMap<Identifier, BlockModelJson> blockModels, TextureAtlas textures) throws Exception {
		this.textures = textures;
		
		var bytes = new ByteArrayOutput();
		var entry = entries.get(new Identifier("textures/colormap/grass.png"));
		try (var stream = new BufferedInputStream(file.getInputStream(entry))) {
			StreamUtils.copyStream(stream, bytes);
		}
		grassColors = new Pixmap(bytes.array(), 0, bytes.size());
		
		bytes.reset();
		entry = entries.get(new Identifier("textures/colormap/foliage.png"));
		try (var stream = new BufferedInputStream(file.getInputStream(entry))) {
			StreamUtils.copyStream(stream, bytes);
		}
		foliageColors = new Pixmap(bytes.array(), 0, bytes.size());
		
		
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
		
		idToBlock = new ObjectMap<>(blockStates.size());
		for (var pair : blockStates) {
			var id = pair.left;
			var state = pair.right;
			idToBlock.put(id.full, createBlock(id, state, blockModels));
		}
		
		System.out.println("Block Loader Finished");
	}
	
	private Block createBlock(Identifier id, BlockStateJson state, OrderedMap<Identifier, BlockModelJson> blockModels) {
		var block = customBlocks.get(id.path);
		if (block == null) {
			block = new Block();
		}
		
		block.init(id);
		block.loadModel(state, blockModels, textures);
		
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
        if (y > grassColors.getHeight() || x > grassColors.getWidth()) {
        	return 0xFFFFFFFF;
        }
        return grassColors.getPixel(x, y) >>> 8;
    }
	
	public int getFoliageColor(float temperature, float humidity) {
        int y = (int)((1f - (humidity * temperature)) * 255f);
        int x = (int)((1f - temperature) * 255f);
        if (((y << 8) | x) < 65536) {
        	return foliageColors.getPixel(x, y) >>> 8;
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
		grassColors.dispose();
		foliageColors.dispose();
		idToBlock.clear();
		customBlocks.clear();
	}
}
