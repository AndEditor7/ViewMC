package com.andedit.viewmc;

import static com.andedit.viewmc.Main.LOGGER;
import static com.andedit.viewmc.Main.main;
import static com.badlogic.gdx.Gdx.files;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.Gdx.graphics;
import static com.badlogic.gdx.math.Interpolation.smoother;

import com.andedit.viewmc.graphic.Camera;
import com.andedit.viewmc.input.InputHolder;
import com.andedit.viewmc.resource.Resources;
import com.andedit.viewmc.structure.Structure;
import com.andedit.viewmc.structure.StructureRenderer;
import com.andedit.viewmc.ui.StructureUI;
import com.andedit.viewmc.ui.util.UIManager;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.querz.nbt.io.NBTUtil;

public class ViewCore implements Screen {
	
	public final Resources resources;
	public final UIManager manager;
	public final Camera camera;
	public final StructureRenderer renderer;
	
	float zoom, targetZoom;
	final Vector3 pos = new Vector3();
	final Vector3 center = new Vector3();
	float clampMax;
	float lerp = 1.1f;
	
	float lastZoom;
	final Vector3 lastPos = new Vector3();
	
	final InputHolder inputHolder = new InputHolder();
	final Input inputCore = new Input();

	public ViewCore(Resources resources) {
		this.resources = resources;
		manager = new UIManager(main.stage);
		camera = new Camera();
		camera.fieldOfView = 60;
		camera.near = 1;
		camera.far = 500;
		
		renderer = new StructureRenderer(resources);
		inputHolder.set(inputCore);
		
		manager.setUI(new StructureUI(this), false);
	}
	
	@Override
	public void show() {
		manager.reload();
		gl.glClearColor(40/255f, 40/255f, 40/255f, 1);
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void render() {
		Util.glClear();
		manager.update();
		if (!renderer.hasStructure()) return;
		
		if (lerp < 1f) {
			lerp += graphics.getDeltaTime() * 1f;
			pos.set(lastPos).interpolate(center, lerp, smoother);
			zoom = smoother.apply(lastZoom, targetZoom, lerp);
			if (lerp > 1f) reset();
		}
		
		//pos.sub(center).clamp(0, clampMax).add(center);
		zoom = MathUtils.clamp(zoom, 0, 100);
		var dir = camera.direction;
		camera.position.set(pos.x-(dir.x*zoom), pos.y-(dir.y*zoom), pos.z-(dir.z*zoom));
		
		camera.update(false);
		renderer.render(camera);
	}

	@Override
	public void resize(Viewport view) {
		manager.resize(view);
		camera.setView(view.getScreenWidth(), view.getScreenHeight());
	}
	
	public void reset() {
		pos.set(center);
		zoom = targetZoom;
		
	}

	public void setStructure(Structure structure) {
		renderer.setStructure(structure);
		center.set(structure.sizeX, structure.sizeY * 0.8f, structure.sizeZ);
		clampMax = center.len() * 2f;
		targetZoom = center.len() * 1.2f;
		center.scl(0.5f);
		reset();
	}
	
	@Override
	public InputProcessor getInput() {
		return new InputMultiplexer(manager.input, inputHolder);
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
	
	@Override
	public void dispose() {
		renderer.dispose();
	}
	
	private class Input extends InputAdapter {
		
		boolean reset, move;
		int lastX, lastY;
		int lastXp, lastYp;
		
		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			lerp = 1.1f;
			if (button == Buttons.MIDDLE) {
				reset = true;
			}
			move = button == Buttons.RIGHT;
			lastX = lastXp = screenX;
			lastY = lastYp = screenY;
			return false;
		}
		
		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			if (reset && Math.abs(lastXp - screenX)+Math.abs(lastYp - screenY) < 10) {
				lerp = 0;
				reset = false;
				lastZoom = zoom;
				lastPos.set(pos);
			}
			return false;
		}
		
		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			float deltaX = lastX - screenX;
			float deltaY = screenY - lastY;
			
			if (move) {
				var up = camera.up;
				var rit = camera.right;
				deltaX *= 0.025f;
				deltaY *= 0.025f;
				float x = (up.x*deltaY) + (rit.x*deltaX);
				float y = (up.y*deltaY) + (rit.y*deltaX);
				float z = (up.z*deltaY) + (rit.z*deltaX);
				pos.add(x, y, z);
			} else {
				camera.pitch += deltaY * 0.6f;
				camera.yaw += deltaX * 0.6f;
				camera.updateRotation();
			}
			
			lastX = screenX;
			lastY = screenY;
			return false;
		}
		
		@Override
		public boolean scrolled(float amountX, float amountY) {
			zoom += amountY * 0.1f * zoom;
			return false;
		}
	}
}
