package com.andedit.viewmc.ui.drawable;

import static com.andedit.viewmc.Main.main;
import static com.badlogic.gdx.Gdx.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class FlashNinePatchDrawable extends NinePatchDrawable {
	private static final Color TEMP = new Color();
	
	private static float time;
	
	public final Color normal;
	public final Color bright;
	
	public FlashNinePatchDrawable(NinePatch patch, Color normal, Color bright) {
		super(new NinePatch(patch));
		this.normal = normal;
		this.bright = bright;
	}
	
	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		getPatch().setColor(TEMP.set(normal).lerp(bright, MathUtils.sin(time)));
		super.draw(batch, x, y, width, height);
	}
	
	@Override
	public void draw(Batch batch, float x, float y, float originX, float originY, float width, float height,
			float scaleX, float scaleY, float rotation) {
		getPatch().setColor(TEMP.set(normal).lerp(bright, MathUtils.sin(time)));
		super.draw(batch, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
	}
	
	/** Creates a new drawable that renders the same as this drawable tinted the specified color. */
	public FlashNinePatchDrawable tint(Color normal, Color bright) {
		return new FlashNinePatchDrawable(getPatch(), normal, bright);
	}
	
	static {
		main.postUpdate(() -> {
			time += graphics.getDeltaTime() * 4.0f;
			time %= MathUtils.PI;
		});
	}
}
