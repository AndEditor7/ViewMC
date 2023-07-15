package com.andedit.viewmc.threading;

import java.util.concurrent.Callable;

import com.andedit.viewmc.util.ByteArrayOutput;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ByteArray;
import com.github.tommyettinger.anim8.AnimationWriter;

public class GifWriterTask implements Callable<ByteArray> {

	private final Array<Pixmap> frames;
	private final AnimationWriter writer;
	private final int fps;
	
	public GifWriterTask(AnimationWriter writer, Array<Pixmap> frames, int fps) {
		synchronized (writer) {
			this.writer = writer;
			this.frames = new Array<>(frames);
			this.fps = fps;
		}
	}
	
	@Override
	public ByteArray call() throws Exception {
		synchronized (writer) {
			var output = new ByteArrayOutput();
			writer.write(output, frames, fps);
			return output.array;
		}
	}
}
