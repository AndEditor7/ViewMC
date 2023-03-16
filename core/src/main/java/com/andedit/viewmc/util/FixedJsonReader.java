package com.andedit.viewmc.util;

import java.io.IOException;
import java.io.Reader;

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
					char[] newData = new char[data.length * 2];
					System.arraycopy(data, 0, newData, 0, data.length);
					data = newData;
				} else
					offset += length;
			}
		} catch (IOException ex) {
			throw new SerializationException("Error reading input.", ex);
		}
		return parse(data, 0, offset);
	}
	
	@Override
	public JsonValue parse(char[] data, int offset, int length) {
		return super.parse(data, offset, length);
	}
}
