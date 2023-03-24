package com.andedit.viewmc.loader;

import java.util.function.Function;

import com.andedit.viewmc.util.Identifier;

public class IdMapper implements Function<String, Identifier> {

	private final int subDirs;
	
	public IdMapper(int subDirs) {
		this.subDirs = subDirs;
	}
	
	@Override
	public Identifier apply(String name) {
		int start = name.indexOf('/')+1;
		int end = name.indexOf('/', start);
		var namespace = name.substring(start, end);
		start = 0;
		for (int i = 0; i < subDirs; i++) {
			start = name.indexOf('/', start) + 1;
		}
		end = name.indexOf('.');
		if (end == -1) end = name.length();
		return new Identifier(namespace, name.substring(start, end));
	}
}
