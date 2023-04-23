package com.andedit.viewmc.ui.actor;

import com.andedit.viewmc.Assets;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class LoadingIcon extends Widget {

	public float speed;
	public float lerp;
	
	private final Quad[] quads = new Quad[8];
	private int index;
	private float time;
	
	public LoadingIcon() {
		quads[0] = new Quad(0, 0);
		quads[1] = new Quad(0, 1);
		quads[2] = new Quad(0, 2);
		quads[3] = new Quad(1, 2);
		quads[4] = new Quad(2, 2);
		quads[5] = new Quad(2, 1);
		quads[6] = new Quad(2, 0);
		quads[7] = new Quad(1, 0);
	}
	
	@Override
	public void act(float delta) {
		speed = 8;
		
		for (var quad : quads) {
			quad.alpha = MathUtils.lerp(quad.alpha, 0f, 1f-(float)Math.pow(0.002, delta));
		}
		
		time += delta * speed;
		if (time > 1f) {
			quads[index].alpha = 1;
			index = (index+1) % quads.length;
			time %= 1f;
		}
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		validate();
		//batch.setColor(Color.FIREBRICK);
		//batch.draw(Assets.blank, getX(), getY(), getWidth(), getHeight());
		
		var color = getColor();
		for (var quad : quads) {
			batch.setColor(color.r, color.g, color.b, quad.alpha);
			float xPos = getWidth()  * (quad.x / 3f);
			float yPos = getHeight() * (quad.y / 3f);
			batch.draw(Assets.blank, getX() + xPos, getY() + yPos, getWidth()/3f, getHeight()/3f);
		}
		batch.setColor(Color.WHITE);
	}
	
	@Override
	public float getPrefWidth() {
		return 16;
	}
	
	@Override
	public float getPrefHeight() {
		return 16;
	}
	
	private static class Quad {
		final byte x, y;
		float alpha;
		
		public Quad(int x, int y) {
			this.x = (byte)x;
			this.y = (byte)y;
		}
	}
}
