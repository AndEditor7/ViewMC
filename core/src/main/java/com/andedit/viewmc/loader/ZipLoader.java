package com.andedit.viewmc.loader;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.andedit.viewmc.util.Identifier;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

public class ZipLoader implements Closeable {
	private static final OrderedMap<Identifier, ZipEntry> EMPTY = new OrderedMap<>(1);
	
	private final ObjectMap<String, OrderedMap<Identifier, ZipEntry>> entryMap = new ObjectMap<>();
	private final ArrayList<EntryFilter> filters = new ArrayList<>();
	
	private ZipFile file;
	
	public ZipLoader() {
		
	}
	
	public void open(FileHandle file) throws Exception {
		close();
		this.file = new ZipFile(file.file());
	}
	
	/** @param regexFilter {@link Pattern} */
	public void addEntryFilter(String key, String regexFilter, int subDirs) {
		addEntryFilter(key, regexFilter, new IdMapper(subDirs));
	}
	
	/** @param regexFilter {@link Pattern} */
	public void addEntryFilter(String key, String regexFilter, Function<String, Identifier> mapper) {
		addEntryFilter(key, Pattern.compile(regexFilter.replaceAll("/", "\\\\/")).asMatchPredicate(), mapper);
	}
	
	public void addEntryFilter(String key, Predicate<String> filter, Function<String, Identifier> mapper) {
		filters.add(new EntryFilter(key, filter, mapper));
	}
	
	public void clearEntryFilters() {
		filters.clear();
		entryMap.clear();
	}
	
	/** Load zip entries into hash tables / entry map. */
	public void loadEntries() throws Exception {
		var entries = file.entries();
		while (entries.hasMoreElements()) {
			var entry = entries.nextElement();
			if (entry.isDirectory()) continue;
			var name = entry.getName();
			for (var filter : filters) {
				if (filter.test(name)) {
					var map = entryMap.get(filter.key);
					if (map == null) {
						map = new OrderedMap<>(800);
						entryMap.put(filter.key, map);
					}
					map.put(filter.apply(name), entry);
				}
			}
		}
	}
	
	public OrderedMap<Identifier, ZipEntry> getMap(String key) {
		return entryMap.get(key, EMPTY);
	}
	
	public InputStream getInputStream(ZipEntry entry) throws Exception {
		return file.getInputStream(entry);
	}

	@Override
	public void close() throws IOException {
		entryMap.clear();
		if (file != null) {
			file.close();
			file = null;
		}
	}
	
	static class EntryFilter implements Predicate<String>, Function<String, Identifier> {
		final String key;
		final Predicate<String> filter;
		final Function<String, Identifier> mapper;
		
		public EntryFilter(String key, Predicate<String> filter, Function<String, Identifier> mapper) {
			this.key = key;
			this.filter = filter;
			this.mapper = mapper;
		}

		@Override
		public Identifier apply(String name) {
			return mapper.apply(name);
		}

		@Override
		public boolean test(String name) {
			return filter.test(name);
		}
	}
}
