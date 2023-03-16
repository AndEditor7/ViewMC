package com.andedit.viewmc.ui.actor;

import java.util.Optional;

import com.andedit.viewmc.resource.ResourceData;
import com.andedit.viewmc.resource.ResourcePacker;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class Marching extends Actor {
	
	private final Texture[] array;
	private final Array<Marcher> marchers = new Array<Marcher>(false, 32);
	private int index;
	private float time = -2;
	
	public Marching(ResourcePacker packer) {
		array = packer.compile().stream().map(ResourceData::getImage).filter(Optional::isPresent).map(Optional::get).toArray(Texture[]::new);
		for (int i = array.length - 1; i >= 0; i--) {
			int idx = MathUtils.random(i);
			var temp = array[i];
			array[i] = array[idx];
			array[idx] = temp;
		}
	}
	
	@Override
	public void act(float delta) {
		time += delta;
		if (time > 1.75f) {
			time %= 1.75f;
			if (index < array.length) {
				marchers.add(new Marcher(array[index++]));
			}
		}
		
		for (int i = 0; i < marchers.size; i++) {
			var marcher = marchers.get(i);
			marcher.update(delta);
		}
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		marchers.forEach(m -> m.draw(batch, getX(), getHeight()*0.15f));
	}
	
	private static class Marcher {
		private final Sprite sprite;
		private float yVel;
		private float wobbleTime;
		
		public Marcher(Texture texture) {
			sprite = new Sprite(texture);
			sprite.setSize(60, 60);
			sprite.setOriginCenter();
			sprite.setOrigin(sprite.getOriginX(), sprite.getOriginY() * 0.45f);
			sprite.setX(-70);
		}
		
		void update(float delta) {
			wobbleTime += 30f * delta;
			yVel -= 6f * 1.2f;
			sprite.translate(45f * delta, yVel * delta);
			if (sprite.getY() < 0f) {
				sprite.setY(0);
				yVel = 100f * 1.2f;
				wobbleTime = 0;
			}
		}
		
		void draw(Batch batch, float x, float y) {
			var strength = (float)Math.pow((wobbleTime * 0.25) + 1, 1.46);
			sprite.setScale(MathUtils.cos(wobbleTime) * 0.08f / strength + 1f, MathUtils.sin(wobbleTime) * 0.08f / strength + 1f);
			sprite.translate(x, y);
			sprite.draw(batch);
			sprite.translate(-x, -y);
		}
	}
}
