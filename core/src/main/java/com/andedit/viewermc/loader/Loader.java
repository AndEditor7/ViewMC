package com.andedit.viewermc.loader;

import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.andedit.viewermc.util.Identifier;
import com.andedit.viewermc.util.Util;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StreamUtils;

public class Loader {
	
	public static void load(FileHandle file) {
		ZipFile zipFile = null;
		var blockStates   = new ArrayList<ZipEntry>(1000);
		var blockModels   = new ObjectMap<Identifier, ZipEntry>(1000);
		var blockTextures = new ObjectMap<Identifier, ZipEntry>(1000);
		
		try {
			zipFile = new ZipFile(file.file());
			var entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				var entry = entries.nextElement();
				var array = entry.getName().split("/");
				var name = Util.get(array, 1);
				var dir1 = Util.get(array, 2);
				var dir2 = Util.get(array, 3);
				if (entry.getName().endsWith(".json")) {
					if ("models".equals(dir1) && "block".equals(dir2)) {
						var n = entry.getName();
						n = n.substring(0, n.length()-5).substring(n.indexOf("block/")+6);
						blockModels.put(new Identifier(name, n), entry);
					} else if ("blockstates".equals(dir1)) {
						blockStates.add(entry);
					}
				} else if (entry.getName().endsWith(".png")) {
					if ("textures".equals(dir1) && "block".equals(dir2)) {
						var n = entry.getName();
						n = n.substring(0, n.length()-4).substring(n.indexOf("block/")+6);
						blockTextures.put(new Identifier(name, n), entry);
					}
				}
			}
			
			for (var entry : blockStates) {
				
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			StreamUtils.closeQuietly(zipFile);
		}
	}
	
	public static void dispose() {
		
	}
}
