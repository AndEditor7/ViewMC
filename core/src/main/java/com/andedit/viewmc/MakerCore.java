package com.andedit.viewmc;

import static com.andedit.viewmc.Main.LOGGER;
import static com.andedit.viewmc.Main.main;
import static com.badlogic.gdx.Gdx.files;
import static com.badlogic.gdx.Gdx.gl;

import com.andedit.viewmc.graphic.FastBatch;
import com.andedit.viewmc.maker.FrameContext;
import com.andedit.viewmc.maker.Maker;
import com.andedit.viewmc.maker.Scene;
import com.andedit.viewmc.resource.Resources;
import com.andedit.viewmc.structure.Structure;
import com.andedit.viewmc.structure.StructureRenderer;
import com.andedit.viewmc.ui.MakerUI;
import com.andedit.viewmc.ui.util.UIManager;
import com.andedit.viewmc.util.Properties;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.querz.nbt.io.NBTUtil;

/** Gif/Video maker */
public class MakerCore implements Screen {

	public static final Properties formatSettings = new Properties();
	public static final Properties recordingSettings = new Properties();
	public static final Properties transSettings = new Properties();
	
	public final Resources resources;
	public final UIManager manager;
	public final StructureRenderer renderer;
	public final Color colorBackground = new Color();
	
	public final FastBatch batch;
	public final ScreenViewport viewport;
	public final BitmapFontCache fontCache;
	
	public final FrameContext previewFrame;
	public final Scene scene = new Scene();
	public Camera camera = new PerspectiveCamera();
	public @Null Maker maker;
	
	final Vector3 center = new Vector3();
	
	private final Matrix4 flip = new Matrix4();
	
	public MakerCore(Resources resources) {
		this.resources = resources;
		this.manager = new UIManager(main.stage);
		this.renderer = new StructureRenderer(resources);
		renderer.setFlip(true);
		camera.far = 500;
		camera.near = 2;
		
		flip.setToScaling(1, -1, 1);
		
		batch = new FastBatch(200);
		viewport = new ScreenViewport();
		viewport.setUnitsPerPixel(1/2f);
		fontCache = Assets.font();
		fontCache.setText("Video Rendered By ViewMC", 0, 0);
		
		this.previewFrame = new FrameContext();
		manager.setUI(new MakerUI(this), false);
		this.previewFrame.setFrame(recordingSettings.getInt("width"), recordingSettings.getInt("height"));
	}
	
	public MakerCore(Resources resources, Structure structure) {
		this(resources);
		setStructure(structure);
	}
	
	@Override
	public void show() {
		manager.reload();
	}
	
	@Override
	public void render() {
		ScreenUtils.clear(Color.BLACK, true);
		
		manager.update();
		
		if (maker != null && maker.update()) {
			maker.dispose();
			maker = null;
			System.gc();
		}
	}
	
	/** Requires bind */
	public void renderFrame(int width, int height, Camera camera, Color clearColor) {
		//ScreenUtils.clear(0.15f, 0.15f, 0.15f, 1, true);
		ScreenUtils.clear(clearColor, true);
		//Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		//Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_COVERAGE_BUFFER_BIT_NV);
		
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		camera.update(false);
		renderer.render(camera);
		
//		viewport.update(width, height, true);
//		flip.val[Matrix4.M13] = viewport.getWorldHeight();
//		var mat = viewport.getCamera().combined.mul(flip);
//		batch.setProjectionMatrix(mat);
//		fontCache.setPosition(2, viewport.getWorldHeight() - 3);
//		batch.begin();
//		fontCache.draw(batch);
//		batch.end();
//		gl.glUseProgram(0);
	}

	@Override
	public void resize(Viewport view) {
		manager.resize(view);
	}

	@Override
	public void dispose() {
		if (maker != null) maker.dispose();
		previewFrame.dispose();
		renderer.dispose();
		batch.dispose();
	}
	
	public void setStructure(Structure structure) {
		renderer.setStructure(structure);
		scene.setStructure(structure);
	}
	
	@Override
	public InputProcessor getInputAfter() {
		return manager.inputAfter;
	}

	@Override
	public void event(Events event, Object arg) {
		if (event == Events.FILES_DROPPED) {
			var strings = (String[])arg;
			if (strings.length == 1) {
				try {
					var tag = NBTUtil.read(files.absolute(strings[0]).file());
					setStructure(new Structure(resources, tag));
				} catch (Exception e) {
					LOGGER.info("Fail to load the structure", e);
				}
			} else {
				LOGGER.info("Too many files being dropped at once");
			}
		}
	}
}
