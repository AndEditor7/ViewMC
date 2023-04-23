package com.andedit.viewmc.maker;

import static com.andedit.viewmc.MakerCore.formatSettings;
import static com.andedit.viewmc.MakerCore.recordingSettings;
import static com.badlogic.gdx.Gdx.files;
import static com.badlogic.gdx.Gdx.graphics;

import java.io.OutputStream;
import java.util.concurrent.Future;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.MakerCore;
import com.andedit.viewmc.Statics;
import com.andedit.viewmc.graphic.SSAA;
import com.andedit.viewmc.threading.GifWriterTask;
import com.andedit.viewmc.ui.actor.LoadingIcon;
import com.andedit.viewmc.ui.actor.ProgressBar;
import com.andedit.viewmc.ui.util.UIs.CloseCall;
import com.andedit.viewmc.util.Average;
import com.andedit.viewmc.util.Progress;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.Null;
import com.github.tommyettinger.anim8.Dithered.DitherAlgorithm;
import com.github.tommyettinger.anim8.FastGif;

public class GifMaker extends Maker {
	
	private static final float STEP = 0.1f;
	private float time;
	
	private final FastGif gif;
	private final Array<Pixmap> pixmaps;
	private final Scene scene;
	private final int fps;
	
	private final Camera camera;
	private final MakerFrame frame;
	
	private final Window window;
	private final LoadingIcon icon;
	private final Label status;
	private final ProgressBar bar;
	private final Progress progress;
	private final Average average = new Average(50);
	
	private volatile boolean close;
	private boolean isDoneRecording;
	
	@Null
	private Future<ByteArray> future;

	public GifMaker(MakerCore core, Window window) {
		super(core);
		this.window = window;
		scene = core.scene.newInstance();
		pixmaps = new Array<>();

		fps = recordingSettings.getInt("fps");
		progress = new Progress();
		progress.newProgess(2);
		progress.newStep(MathUtils.floor(fps * recordingSettings.getFloat("length")));
		
		gif = new FastGif() {
			private long lastMill;
			private final Average average = new Average(30);
			
			@Override
			public void write(OutputStream output, Array<Pixmap> frames, int fps) {
				progress.newStep(MathUtils.floor(fps * recordingSettings.getFloat("length")));
				lastMill = System.nanoTime();
				super.write(output, frames, fps);
			}
			
			@Override
			public boolean addFrame(Pixmap im) {
				if (close) throw new RuntimeException("close"); 
				progress.incStep();
				var currentMill = System.nanoTime();
				double value = (currentMill - lastMill) / 1000000d;
				lastMill = currentMill;
				progress.setStatus(value != 0 ? "Encoding (" + average.value((int)(1000/value)) + " FPS)" : "Encoding");
				return super.addFrame(im);
			}
		};
		
		synchronized (gif) {
			gif.setDitherAlgorithm((DitherAlgorithm)formatSettings.get("da"));
			gif.setSize(recordingSettings.getInt("width"), recordingSettings.getInt("height"));
			gif.setDitherStrength(formatSettings.getFloat("ds"));
			gif.fastAnalysis = false;
		}
		
		
		camera = new PerspectiveCamera();
		var width = recordingSettings.getInt("width");
		var height = recordingSettings.getInt("height");
		var ssaa = (SSAA)recordingSettings.get("quality");
		frame = ssaa != SSAA.NONE ? 
		new MakerFrameSSAA(Format.RGB888, width, height, ssaa.gridSize) : 
		new MakerFrameNormal(Format.RGB888, width, height);
		
		window.setSize(200, 120);
		
		icon = new LoadingIcon();
		window.add(icon).size(32).pad(10).row();
		
		status = new Label(null, Assets.skin);
		window.add(status).padLeft(8).padRight(8).row();
		
		bar = new ProgressBar();
		window.add(bar).growX().pad(10);
		
		window.setUserObject(new CloseCall((w, e) -> {
			close = true;
		}));
	}
	
	@Override
	public boolean update() {
		
		if (!isDoneRecording) {
			time += graphics.getDeltaTime();
			synchronized (gif) {
				int frames = 0;
				frame.begin();	
				loop : do {
					for (int i = 0; i < 10; i++) {
						if (scene.update(camera, 1000d/fps/1000d)) {
							isDoneRecording = true;
							break loop;
						}
						core.renderFrame(frame.getInWidth(), frame.getInHeight(), camera, scene.backgroundColor);
						pixmaps.add(frame.create());
						progress.incStep();
						
						if (pixmaps.size == 1) {
							PixmapIO.writePNG(files.absolute("/home/andeditor7/Pictures/test.png"), pixmaps.first());
						}
						
						frames++;
					}
					time -= STEP;
					
				} while (time >= STEP);
				frame.end();
				progress.setStatus("Recording (" + average.value((int)(frames / graphics.getDeltaTime())) + " FPS)");
			}
			time %= STEP;
		}
		
		if (isDoneRecording && future == null) {
			future = Statics.theadExe.submit(new GifWriterTask(gif, pixmaps, fps));
		}
		
		if (future != null && future.isDone()) {
			ByteArray array = null;
			
			try {
				array = future.get();
				var file = files.absolute("/home/andeditor7/Pictures/test.gif");
				synchronized (gif) {
					file.writeBytes(array.items, 0, array.size, false);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return true;
		}
		
		bar.setProgress(progress.getProgress());
		status.setText(progress.getStatus());
		
		return close;
	}

	@Override
	public void dispose() {
		if (future != null) {
			try {
				future.get();
			} catch (Exception e) {}
		}
		pixmaps.forEach(Pixmap::dispose);
		frame.dispose();
		window.remove();
	}
}
