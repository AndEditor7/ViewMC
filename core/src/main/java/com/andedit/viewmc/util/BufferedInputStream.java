package com.andedit.viewmc.util;

import java.io.InputStream;

/** {@inheritDoc} */
public class BufferedInputStream extends java.io.BufferedInputStream {

	/** {@inheritDoc} */
	public BufferedInputStream(InputStream in) {
		super(in);
	}

	/** {@inheritDoc} */
	public BufferedInputStream(InputStream in, int size) {
        super(in, size);
    }
	
	/** {@inheritDoc} */
	public BufferedInputStream(InputStream in, byte[] buffer) {
		super(in, 1);
		buf = buffer;
	}
}
