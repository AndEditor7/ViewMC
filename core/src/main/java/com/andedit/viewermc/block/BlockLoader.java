package com.andedit.viewermc.block;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.andedit.viewermc.block.model.BlockModelJson;
import com.andedit.viewermc.block.state.BlockStateJson;
import com.andedit.viewermc.util.Identifier;
import com.andedit.viewermc.util.Pair;
import com.andedit.viewermc.util.Util;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.OrderedSet;

public class BlockLoader implements Callable<Blocks> {

	private final File file;
	
	public BlockLoader(FileHandle file) {
		this(file.file());
	}

	public BlockLoader(File file) {
		this.file = file;
	}

	@Override
	public Blocks call() throws Exception {
		System.out.println("Block Loader Started");
		try (var zipFile = new ZipFile(file)) {
			var blockStates = new ArrayList<Pair<Identifier,BlockStateJson>>(1000);
			var blockModels = new OrderedMap<Identifier, BlockModelJson>(2000);
			
			var blockStatesEntry = new ArrayList<ZipEntry>(1000);
			var blockModelsEntry = new OrderedMap<Identifier, ZipEntry>(2000);
			var blockTexturesEntry = new ObjectMap<Identifier, ZipEntry>(3000);
			var blockTexturesAniEntry = new ObjectMap<Identifier, ZipEntry>(1000);
			var otherEntry = new ObjectMap<Identifier, ZipEntry>(100);
			
			var textureList = new ArrayList<String>();
			textureList.add("textures/colormap/grass.png");
			textureList.add("textures/colormap/foliage.png");

			var entries = zipFile.entries();
			var namespace = "minecraft";
			while (entries.hasMoreElements()) {
				final var entry = entries.nextElement();
				final var name = entry.getName();

				var array = Util.split(name, '/');
				if (!"assets".equals(Util.get(array, 0))) {
					continue;
				}

				//var namespace = Util.get(array, 1);
				var dir1 = Util.get(array, 2);
				var dir2 = Util.get(array, 3);

				if (name.endsWith(".json")) {
					if ("models".equals(dir1) && "block".equals(dir2)) {
						var n = name.substring(name.indexOf("models/") + 7, name.length() - 5);
						blockModelsEntry.put(new Identifier(namespace, n), entry);
					} else if ("blockstates".equals(dir1)) {
						blockStatesEntry.add(entry);
					}
				} else if (name.endsWith(".png")) {
					if ("textures".equals(dir1)) {
						var n = name.substring(name.indexOf("textures/") + 9, name.length() - 4);
						blockTexturesEntry.put(new Identifier(namespace, n), entry);
					}
					for (var path : textureList) {
						if (name.endsWith(path)) {
							otherEntry.put(new Identifier(namespace, path), entry);
							break;
						}
					}
				} else if (name.endsWith(".mcmeta")){
					if ("textures".equals(dir1) && "block".equals(dir2)) {
						var n = name.substring(name.indexOf("textures/") + 9, name.length() - 11);
						blockTexturesAniEntry.put(new Identifier(namespace, n), entry);
					}
				}
			}

			// Load block states as a java object and fetch all models.
			blockStates.ensureCapacity(blockStatesEntry.size());
			var modelIds = new OrderedSet<Identifier>(blockModelsEntry.size);
			for (var entry : blockStatesEntry) {
				var stream = new BufferedInputStream(zipFile.getInputStream(entry), 4096);
				var value = new JsonReader().parse(stream);
				stream.close();

				var name = entry.getName();
				blockStates.add(new Pair<>(new Identifier(namespace, name.substring(name.indexOf("blockstates/")+12, name.length() - 5)), new BlockStateJson(value, modelIds)));
			}

			// Load block models as a java object and fetch all textures.
			blockModels.ensureCapacity(blockModelsEntry.size);
			var textureIds = new OrderedSet<Identifier>(blockTexturesEntry.size);
			for (var id : blockModelsEntry.keys()) {
				var entry = blockModelsEntry.get(id);
				var stream = new BufferedInputStream(zipFile.getInputStream(entry), 4096);
				var value = new JsonReader().parse(stream);
				stream.close();

				blockModels.put(id, new BlockModelJson(value, textureIds));
			}

			// Load parent models
			for (var model : blockModels.values()) {
				model.load(blockModels);
			}

			var textures = new TextureAtlas(zipFile, textureIds, blockTexturesEntry, blockTexturesAniEntry);
			
			return new Blocks(zipFile, otherEntry, blockStates, blockModels, textures);
		}
	}
}
