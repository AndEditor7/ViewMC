package com.andedit.viewmc.ui;

import static com.andedit.viewmc.Main.main;
import static com.andedit.viewmc.MakerCore.formatSettings;
import static com.andedit.viewmc.MakerCore.recordingSettings;
import static com.andedit.viewmc.MakerCore.transSettings;
import static com.badlogic.gdx.Gdx.app;
import static com.badlogic.gdx.Gdx.graphics;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.Main;
import com.andedit.viewmc.MakerCore;
import com.andedit.viewmc.ViewCore;
import com.andedit.viewmc.graphic.SSAA;
import com.andedit.viewmc.maker.FrameContext;
import com.andedit.viewmc.maker.GifMaker;
import com.andedit.viewmc.maker.MakerMode;
import com.andedit.viewmc.maker.Mp4Maker;
import com.andedit.viewmc.ui.actor.ColorField;
import com.andedit.viewmc.ui.actor.DecimalField;
import com.andedit.viewmc.ui.actor.DesktopScrollPane;
import com.andedit.viewmc.ui.actor.FramePreview;
import com.andedit.viewmc.ui.actor.NumberField;
import com.andedit.viewmc.ui.util.BaseUI;
import com.andedit.viewmc.ui.util.UIs;
import com.andedit.viewmc.ui.util.UiUtil;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.Dithered.DitherAlgorithm;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;

/** GIF and Video maker UI */
public class MakerUI extends BaseUI {
	
	private final MakerCore core;
	
	private final Table root;
	private final Table leftPanel;
	private final Table rightPanel;
	
	private final FramePreview preview;
	private final FrameContext frame;
	
	
	// Format settings (MP4)
		
	// Format settings (GIF)
	private SelectBox<MakerMode> makerMode;
	private SelectBox<DitherAlgorithm> da;
	private DecimalField ds;

	// Recording settings
	private SelectBox<SSAA> quality;
	private NumberField width;
	private NumberField height;
	private NumberField fps;
	private NumberField fov;
	private DecimalField length;
	private ColorField color;
	
	// Transformation settings
	private Button clockwise;
	private NumberField pitch;

	
	public MakerUI(MakerCore core) {
		this.core = core;
		this.frame = core.previewFrame;
		
		root = add(new Table());
		root.setUserObject(new Vector2(0, 1));
		root.align(Align.topLeft);
		
		leftPanel = table();
		root.add(leftPanel).top().grow();
		
		var table = new Table();
		root.add(table).top().prefWidth(180).growY();
		
		preview = new FramePreview(frame::getFrame);
		preview.flip = false;
		table.add(preview).prefSize(180).row();
		
		rightPanel = new Table();
		table.add(rightPanel).top().grow();
		
		// we are ready!
		
		settings();
		
		var textButton = new TextButton("Render", Assets.skin);
		rightPanel.add(textButton).top().growX().prefSize(20).row();
		textButton.addListener(Util.newListener(() -> {
			var conf = new NativeFileChooserConfiguration();
			conf.directory = Gdx.files.absolute(System.getProperty("user.home"));
			conf.title = "Save render file";
			conf.intent = NativeFileChooserIntent.SAVE;

			var label = new Label("A new save window prompted.", Assets.skin);
			var prompt = UIs.newWindowWoClose("");
			prompt.setSize(200, 70);
			prompt.add(label);
			UiUtil.center(prompt, main.stage);
			main.stage.addActor(prompt);

			if (graphics.isFullscreen()) {
				graphics.setWindowedMode(900, 600);
			}

			app.postRunnable(() -> {
				Main.fc.chooseFile(conf, new NativeFileChooserCallback() {
					@Override
					public void onFileChosen(FileHandle file) {
						var window = UIs.newWindow("Rendering...");
						var format = formatSettings.get("format");

						if (format == MakerMode.MP4) {
							try {
								core.maker = new Mp4Maker(core, window, file);
							} catch (Exception e) {
								e.printStackTrace();
								return;
							}
						} else if (format == MakerMode.GIF) {
							core.maker = new GifMaker(core, window, file);
						} else return;

						prompt.remove();
						UiUtil.center(window, main.stage);
						main.stage.addActor(window);
					}
					@Override
					public void onCancellation() {
						prompt.remove();
					}
					@Override
					public void onError(Exception exception) {
						exception.printStackTrace();
						prompt.remove();
					}
				});
			});
		}));
		
		textButton = new TextButton("Close", Assets.skin);
		rightPanel.add(textButton).top().growX().prefSize(20).row();
		textButton.addListener(Util.newListener(() -> {
			main.setScreen(new ViewCore(core.resources, core.renderer.getStructure()));
		}));
		
		table = table();
		rightPanel.add(table).grow();
	}
	
	public void settings() {
		leftPanel.clear();
		
		Label label;
		Table table;
		Button button;
		NumberField intField;
		DecimalField floatField;
		
		var group = new VerticalGroup();
		group.align(Align.left);
		group.columnAlign(Align.left);
		group.padTop(4).padLeft(4);
		group.space(3);
		var scroll = new DesktopScrollPane(group, Assets.skin);
		leftPanel.add(scroll).growX().expandY().minWidth(Value.prefWidth).top();
		label = new Label("Format Settings:", Assets.skin);
		label.setAlignment(Align.center);
		group.addActor(label);
		
		formatSettings.putIfAbsent("format", MakerMode.GIF);
		makerMode = new SelectBox<MakerMode>(Assets.skin);
		makerMode.setItems(MakerMode.values());
		makerMode.setSelectedIndex(((MakerMode)formatSettings.get("format")).ordinal());
		makerMode.setAlignment(Align.center);
		table = element("Format");
		table.add(makerMode).prefHeight(18);
		group.addActor(table);
		makerMode.addListener(Util.newListener(e -> {
			formatSettings.put("format", makerMode.getSelected());
		}));
		
		formatSettings.putIfAbsent("da", DitherAlgorithm.NONE);
		da = new SelectBox<DitherAlgorithm>(Assets.skin);
		da.setItems(DitherAlgorithm.values());
		da.setSelectedIndex(((DitherAlgorithm)formatSettings.get("da")).ordinal());
		da.setAlignment(Align.center);
		table = element("Dither Algorithm");
		table.add(da).prefHeight(18);
		group.addActor(table);
		da.addListener(Util.newListener(e -> {
			formatSettings.put("da", da.getSelected());
		}));
		
		formatSettings.putIfAbsent("ds", 1f);
		ds = new DecimalField(formatSettings.getFloat("ds"), 2, Assets.skin);
		ds.setClampSize(0, 2);
		table = element("Dither Strength");
		table.add(ds).size(40, 18);
		group.addActor(table);
		ds.addListener(Util.newListener(e -> {
			formatSettings.put("ds", ds.getField());
		}));
		
		label = new Label("\nRecording Settings:", Assets.skin);
		label.setAlignment(Align.center);
		group.addActor(label);
		
		recordingSettings.putIfAbsent("quality", SSAA.SAMPLE2X);
		quality = new SelectBox<SSAA>(Assets.skin);
		quality.setItems(SSAA.values());
		quality.setSelectedIndex(((SSAA)recordingSettings.get("quality")).ordinal());
		quality.setAlignment(Align.center);
		table = element("Quality");
		table.add(quality).prefHeight(18);
		group.addActor(table);
		quality.addListener(Util.newListener(e -> {
			recordingSettings.put("quality", quality.getSelected());
		}));
		
		recordingSettings.putIfAbsent("width", 360);
		width = new NumberField(recordingSettings.getInt("width"), Assets.skin);
		width.setClampSize(1, 1024);
		table = element("Width");
		table.add(width).size(30, 18);
		group.addActor(table);
		width.addListener(Util.newListener(e -> {
			recordingSettings.put("width", width.getField());
		}));
		
		recordingSettings.putIfAbsent("height", 360);
		height = new NumberField(recordingSettings.getInt("height"), Assets.skin);
		height.setClampSize(1, 1024);
		table = element("Height");
		table.add(height).size(30, 18);
		group.addActor(table);
		height.addListener(Util.newListener(e -> {
			recordingSettings.put("height", height.getField());
		}));
		
		recordingSettings.putIfAbsent("fps", 10);
		fps = new NumberField(recordingSettings.getInt("fps"), Assets.skin);
		fps.setClampSize(1, 60);
		table = element("FPS");
		table.add(fps).size(20, 18);
		group.addActor(table);
		fps.addListener(Util.newListener(e -> {
			recordingSettings.put("fps", fps.getField());
		}));
		
		recordingSettings.putIfAbsent("fov", 50);
		fov = new NumberField(recordingSettings.getInt("fov"), Assets.skin);
		fov.setClampSize(5, 135);
		table = element("FOV");
		table.add(fov).size(25, 18);
		group.addActor(table);
		fov.addListener(Util.newListener(e -> {
			recordingSettings.put("fov", fov.getField());
		}));
		
		recordingSettings.putIfAbsent("length", 5d);
		length = new DecimalField(recordingSettings.getDouble("length"), 1, Assets.skin);
		length.setClampSize(0.1, 60);
		table = element("Length");
		table.add(length).size(30, 18);
		group.addActor(table);
		length.addListener(Util.newListener(e -> {
			recordingSettings.put("length", length.getField());
		}));
		
		recordingSettings.putIfAbsent("color", new Color(0.16f, 0.16f, 0.16f, 1));
		color = new ColorField("#" + ((Color)recordingSettings.get("color")).toString(), Assets.skin);
		table = element("Background Color");
		table.add(color).size(62, 18);
		group.addActor(table);
		color.addListener(Util.newListener(e -> {
			recordingSettings.put("color", color.getField());
		}));
		
		label = new Label("\nTransformation Settings:", Assets.skin);
		label.setAlignment(Align.center);
		group.addActor(label);
		
		transSettings.putIfAbsent("clockwise", false);
		clockwise = new Button(Assets.skin, "checked");
		clockwise.setChecked(transSettings.getBool("clockwise"));
		table = element("Clockwise");
		table.add(clockwise).prefSize(20, 18);
		group.addActor(table);
		clockwise.addListener(Util.newListener(e -> {
			transSettings.put("clockwise", clockwise.isChecked());
		}));
		
		transSettings.putIfAbsent("pitch", 30);
		pitch = new NumberField(transSettings.getInt("pitch"), Assets.skin);
		pitch.setClampSize(-90, 90);
		table = element("Pitch");
		table.add(pitch).size(30, 18);
		group.addActor(table);
		pitch.addListener(Util.newListener(e -> {
			transSettings.put("pitch", pitch.getField());
		}));
	}
	
	@Override
	public void update() {
		frame.setFrame(recordingSettings.getInt("width"), recordingSettings.getInt("height"));
		
		core.scene.rotDegrees = transSettings.getBool("clockwise") ? 180 : -180;
		core.scene.pitch = transSettings.getInt("pitch");
		core.scene.fov = recordingSettings.getInt("fov");
		core.scene.maxTime = recordingSettings.getDouble("length");
		core.scene.backgroundColor.set((Color)recordingSettings.get("color"));
		
		var f = frame.getFrame();
		core.scene.update(core.camera, graphics.getDeltaTime());
		f.begin();
		core.renderFrame(f.getWidth(), f.getHeight(), core.camera, core.scene.backgroundColor);
		f.end();
	}
	
	@Override
	public void resize(Viewport view) {
		root.setSize(view.getWorldWidth(), view.getWorldHeight());
	}
	
	private static Table element(String text) {
		var table = table();
		var label = new Label(text, Assets.skin);
		label.setAlignment(Align.center);
		table.add(label).padLeft(5).padRight(5);
		return table;
	}
	
	private static Table table() {
		return new Table().background(new NinePatchDrawable(new NinePatch(Assets.frame, Color.DARK_GRAY)));
	}
}
