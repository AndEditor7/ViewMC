package com.andedit.viewmc.maker;

import static com.andedit.viewmc.MakerCore.recordingSettings;
import static com.andedit.viewmc.Statics.threadExe;
import static com.badlogic.gdx.Gdx.files;
import static com.badlogic.gdx.Gdx.graphics;

import java.util.HashMap;
import java.util.Vector;

import com.badlogic.gdx.files.FileHandle;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.MakerCore;
import com.andedit.viewmc.graphic.SSAA;
import com.andedit.viewmc.ui.actor.LoadingIcon;
import com.andedit.viewmc.ui.actor.ProgressBar;
import com.andedit.viewmc.ui.util.UIs.CloseCall;
import com.andedit.viewmc.util.Average;
import com.andedit.viewmc.util.Progress;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.Codec;
import org.jcodec.common.Format;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;

public class Mp4Maker extends Maker {

	private static final float STEP = 0.2f;
	private static final int BUFFER_SIZE = 10;
	
	private final Scene scene;
	private final int fps;
	
	private final Camera camera;
	private final MakerFrame frame;
	private final SequenceEncoder encoder;
	private final Vector<Object> buffer = new Vector<>(BUFFER_SIZE);
	
	private final Window window;
	private final FileHandle file;
	private final LoadingIcon icon;
	private final Label status;
	private final ProgressBar bar;
	private final Progress progress;
	
	private volatile long last;
	private final Average average = new Average(30);
	
	private volatile boolean close;
	private boolean isDone;
	private float time;
	
	public Mp4Maker(MakerCore core, Window window, FileHandle file) throws Exception {
		super(core);
		this.window = window;

		if (!file.exists() && !file.name().endsWith(".mp4")) {
			file = files.absolute(file.path()+".mp4");
		}
		this.file = file;
		
		fps = recordingSettings.getInt("fps");
		progress = new Progress();
		progress.newProgess(1);
		progress.newStep(MathUtils.floor(fps * recordingSettings.getFloat("length")));
		
		// This is a thread-safe method since this will handle the encoding stuff.
		var future = threadExe.submit(() -> {
			var encoder = new SequenceEncoder(NIOUtils.writableChannel(this.file.file()), Rational.R(fps, 1), Format.MOV, Codec.H264, null);
			//var opts = new HashMap<String, String>();
			//encoder.configureCodec(opts);
			return encoder;
		});
		encoder = future.get();
		
		for (int i = 0; i < BUFFER_SIZE; i++) {
			buffer.add(new Object());
		}
		
		scene = core.scene.newInstance();
		camera = new PerspectiveCamera();
		
		var width = recordingSettings.getInt("width");
		var height = recordingSettings.getInt("height");
		var ssaa = (SSAA)recordingSettings.get("quality");
		frame = ssaa != SSAA.NONE ? 
		new MakerFrameSSAA(Pixmap.Format.RGB888, width, height, ssaa.gridSize) :
		new MakerFrameNormal(Pixmap.Format.RGB888, width, height);
		
		window.setSize(200, 120);
		
		icon = new LoadingIcon();
		window.add(icon).size(32).pad(10).row();
		
		status = new Label(null, Assets.skin);
		window.add(status).padLeft(8).padRight(8).row();
		
		bar = new ProgressBar();
		window.add(bar).growX().pad(10);
		
		window.setUserObject(new CloseCall((w, e) -> {
			close = true;
			threadExe.execute(Mp4Maker.this::finish);
		}));
		
		last = System.nanoTime();
	}

	@Override
	public boolean update() {
		if (close) return true;
		
		if (!isDone) {
			frame.begin();
			time += graphics.getDeltaTime();
			loop : do {
				for (int l = 0; l < 2; l++) {
					if (buffer.isEmpty()) break loop;
					var lock = buffer.remove(0);
					
					if (scene.update(camera, 1000d/fps/1000d)) {
						isDone = true;
						threadExe.execute(this::finish);
						break loop;
					}
					
					core.renderFrame(frame.getInWidth(), frame.getInHeight(), camera, scene.backgroundColor);
					
					var pixmap = frame.read();
					var buffer = pixmap.getPixels();
					
					Picture pic;
					synchronized (lock) {
						pic = Picture.create(frame.getOutWidth(), frame.getOutHeight(), ColorSpace.RGB);
						var data = pic.getPlaneData(0);
						var len = data.length;
						for (int i = 0; i < len; i++) {
							int a = Math.min(buffer.get(i) + 15, 255);
							data[i] = (byte) (a - 128);
						}
					}
					
					threadExe.execute(new Encode(pic, lock));
				}
				time -= STEP;
			} while (time >= STEP);
			time %= STEP;
			frame.end();
		}
		
		bar.setProgress(progress.getProgress());
		status.setText(progress.getStatus());
		
		return close;
	}
	
	private boolean finish() {
		try {
			close = true;
			encoder.finish();
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
		return false;
	}

	@Override
	public void dispose() {
		frame.dispose();
		window.remove();
	}
	
	private class Encode implements Runnable {

		private final Picture picture;
		private final Object lock;
		
		public Encode(Picture picture, Object lock) {
			this.picture = picture;
			this.lock = lock;
		}
		
		@Override
		public void run() {
			if (close) return; 
			
			synchronized (lock) {
				try {
					encoder.encodeNativeFrame(picture);
				} catch (Exception ex) {
					ex.printStackTrace();
					close = true;
					threadExe.execute(Mp4Maker.this::finish);
				}
			}
			
			progress.incStep();
			var current = System.nanoTime();
			double value = (current - last) / 1000000d;
			progress.setStatus(value != 0 ? "Rendering (" + average.value((int)(1000/value)) + " FPS)" : "Rendering");
			last = current;
			
			buffer.add(lock);
		}
	}
}
