package com.andedit.viewermc.util;

import java.io.OutputStream;

import com.badlogic.gdx.utils.ByteArray;

public class ByteArrayOutput extends OutputStream {
	
	public final ByteArray array;
	
	public ByteArrayOutput() {
		this(8192);
	}
	
	public ByteArrayOutput(int size) {
		array = new ByteArray(size);
	}
	
	public ByteArrayOutput(ByteArray array) {
		this.array = array;
	}

	@Override
	public void write(int b) {
		array.add((byte)b);
	}
	
	@Override
	public void write(byte b[], int off, int len) {
		array.addAll(b, off, len);
    }

	public byte[] array() {
		return array.items;
	}
	
	public int size() {
		return array.size;
	}
	
	public void reset() {
		array.clear();
	}
}
