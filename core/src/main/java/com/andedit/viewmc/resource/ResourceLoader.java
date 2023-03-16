package com.andedit.viewmc.resource;

import java.util.Collection;

import com.andedit.viewmc.block.TextureAtlas;
import com.andedit.viewmc.loader.ZipLoader;
import com.andedit.viewmc.resource.blockmodel.BlockModelJson;
import com.andedit.viewmc.resource.blockstate.BlockStateJson;
import com.andedit.viewmc.resource.texture.TextureJson;
import com.andedit.viewmc.util.BufferedInputStream;
import com.andedit.viewmc.util.ByteArrayOutput;
import com.andedit.viewmc.util.FixedJsonReader;
import com.andedit.viewmc.util.Identifier;
import com.andedit.viewmc.util.IncompatibleException;
import com.andedit.viewmc.util.LoaderTask;
import com.andedit.viewmc.util.Logger;
import com.andedit.viewmc.util.Progress;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.OrderedSet;
import com.badlogic.gdx.utils.StreamUtils;

public class ResourceLoader implements LoaderTask<Resources> {
	
	public static final Logger LOGGER = new Logger("ResourceLoader");
	
	private final ResourceData[] datas;
	private final Progress progress = new Progress();
	
	public ResourceLoader(ResourceData data) {
		datas = new ResourceData[] {data};
	}
	
	public ResourceLoader(Collection<ResourceData> datas) {
		this.datas = datas.toArray(ResourceData[]::new);
	}
	
	public ResourceLoader(Array<ResourceData> datas) {
		this.datas = datas.toArray(ResourceData.class);
	}
	
	public ResourceLoader(ResourceData[] datas) {
		this.datas = datas.clone();
	}
	
	@Override
	public String getStatus() {
		return progress.getStatus();
	}
	
	@Override
	public float getProgress() {
		return progress.getProgress();
	}

	@Override
	public Resources call() throws Exception {
		LOGGER.info("Started");

		var buffer = new byte[8192];
		var bytes = new ByteArrayOutput();
		var reader = new FixedJsonReader();
		var assets = new RawResources();
		
		var loader = new ZipLoader();
		
		
		loader.addEntryFilter("blockstate", "assets/.*/blockstates/.*\\.json", 3);
		loader.addEntryFilter("blockmodel", "assets/.*/models/.*\\.json", 3);
		loader.addEntryFilter("texture", "assets/.*/textures/.*\\.png", 3);
		loader.addEntryFilter("textureMeta", "assets/.*/textures/.*\\.mcmeta", 3);
		
		loader.addEntryFilter("otherTexture", "assets/.*/textures/colormap/.*\\.png", 2);
		
		var keys = new Array<Identifier>(1000);
		
		progress.newProgess((datas.length*7)+4);
		
		for (var data : datas)
		try (loader) {
			loader.open(data.getFile());
			loader.loadEntries();
			progress.setStatus("Loading " + data.getTitle());
			
			// Load block states as a java object and fetch all models.
			progress.newStep(loader.getMap("blockstate").size);
			var modelIds = new OrderedSet<Identifier>(loader.getMap("blockmodel").size);
			var map = loader.getMap("blockstate");
			for (var entry : map) {
				progress.incStep();
				try (var stream = new BufferedInputStream(loader.getInputStream(entry.value), buffer)) {
					var value = reader.parse(stream);
					assets.blockStates.put(entry.key, new BlockStateJson(value, modelIds));
				} catch (Exception e) {
					LOGGER.info("Failed to load blockstate " + entry.key, e);
				}
			}

			var modelParents = new OrderedSet<Identifier>(loader.getMap("blockmodel").size);
			var textureIds = new OrderedSet<Identifier>(loader.getMap("texture").size);
			
			// Load every models with ids from the previous pack
			progress.newStep(assets.blockModels.size);
			keys.size = 0;
			keys.addAll(assets.blockModels.orderedKeys());
			map = loader.getMap("blockmodel");
			for (var id : keys) {
				progress.incStep();
				var entry = map.get(id);
				if (entry != null) {
					try (var stream = new BufferedInputStream(loader.getInputStream(entry), buffer)) {
						assets.blockModels.put(id, new BlockModelJson(reader.parse(stream), textureIds, modelParents));
					} catch (Exception e) {
						LOGGER.info("Failed to load model " + id, e);
					}
				}
			}
			
			// Load block models as a java object and fetch all textures.
			progress.newStep(modelIds.size);
			map = loader.getMap("blockmodel");
			for (var id : modelIds) {
				progress.incStep();
				if (assets.blockModels.containsKey(id)) continue;
				var json = BlockModelJson.MISSING_MODEL;
				
				var entry = map.get(id);
				if (entry != null) {
					try (var stream = new BufferedInputStream(loader.getInputStream(entry), buffer)) {
						json = new BlockModelJson(reader.parse(stream), textureIds, modelParents);
					} catch (Exception e) {
						LOGGER.info("Failed to load model " + id, e);
					}
				}

				assets.blockModels.put(id, json);
			}
			
			// Load block model parents as a java object and fetch all textures.
			map = loader.getMap("blockmodel");
			while (modelParents.notEmpty()) {
				var newModelParents = new OrderedSet<Identifier>(modelParents.size);
				for (var id : modelParents) {
					var entry = map.get(id);
					if (entry != null) {
						try (var stream = new BufferedInputStream(loader.getInputStream(entry), buffer)) {
							var json = new BlockModelJson(reader.parse(stream), textureIds, newModelParents);
							assets.blockModels.put(id, json);
						} catch (Exception e) {
							LOGGER.info("Failed to load model parent " + id, e);
						}
					}
				}
				modelParents = newModelParents;
			}
			
			// Load every models with ids from the previous pack
			progress.newStep(assets.blockTextures.size);
			keys.size = 0;
			keys.addAll(assets.blockTextures.orderedKeys());
			map = loader.getMap("texture");
			for (var id : keys) {
				progress.incStep();
				var entry = map.get(id);
				if (entry != null) {
					try (var stream = loader.getInputStream(entry)) {
						bytes.reset();
						StreamUtils.copyStream(stream, bytes, buffer);
						assets.blockTextures.put(id, bytes.toArray());
					}  catch (Exception e) {
						LOGGER.info("Failed to load texture " + id, e);
					}
				}
			}
			
			// Load block model parents as a raw bytes
			map = loader.getMap("texture");
			progress.newStep(textureIds.size);
			for (var id : textureIds) {
				progress.incStep();
				if (assets.blockTextures.containsKey(id)) continue;
				var entry = map.get(id);
				if (entry == null) continue;
				bytes.reset();
				try (var stream = loader.getInputStream(entry)) {
					StreamUtils.copyStream(stream, bytes, buffer);
				} catch (Exception e) {
					LOGGER.info("Failed to load texture " + id, e);
					continue;
				}
				assets.blockTextures.put(id, bytes.toArray());
			}
			
			map = loader.getMap("textureMeta");
			progress.newStep(map.size);
			for (var entry : map) {
				progress.incStep();
				var id = entry.value;
				try (var stream = new BufferedInputStream(loader.getInputStream(id), buffer)) {
					var value = reader.parse(stream);
					assets.blockTextureMetas.put(entry.key, new TextureJson(value));
				} catch (Exception e) {
					if (e instanceof IncompatibleException) {
						continue;
					}
					LOGGER.info("Failed to load texture meta " + id, e);
					continue;
				}
			}
			
			map = loader.getMap("otherTexture");
			progress.newStep(map.size);
			for (var entry : map) {
				progress.incStep();
				bytes.reset();
				try (var stream = loader.getInputStream(entry.value)) {
					StreamUtils.copyStream(stream, bytes, buffer);
				} catch (Exception e) {
					continue;
				}
				assets.textures.put(entry.key, bytes.toArray());
			}
		}
		
		assets.finalize(datas.length > 1, progress);

		return new Resources(assets, new TextureAtlas(assets, progress), progress);
	}
}
