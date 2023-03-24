package com.andedit.viewmc.util;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;

public class FixedJsonReader extends JsonReader {
	
	private char[] data = new char[2048];
	
	@Override
	public JsonValue parse(Reader reader) {
		int offset = 0;
		try (reader) {
			while (true) {
				int length = reader.read(data, offset, data.length - offset);
				if (length == -1) break;
				if (length == 0) {
					data = Arrays.copyOf(data, data.length << 1);
				} else offset += length;
			}
		} catch (IOException ex) {
			throw new SerializationException("Error reading input.", ex);
		}
		return parse(data, 0, offset);
	}
}
