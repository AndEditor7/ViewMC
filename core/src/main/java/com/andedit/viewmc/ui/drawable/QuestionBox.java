package com.andedit.viewmc.ui.drawable;

import com.andedit.viewmc.Assets;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class QuestionBox extends NinePatchDrawable {
	
	private static final Color temp = new Color();
	
	public QuestionBox() {
		super(Assets.frame);
	}
	
	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		super.draw(batch, x, y, width, height);
		temp.set(batch.getColor());
		batch.setColor(Color.GRAY);
		batch.draw(Assets.question, x+11, y+8, 10, 16);
		batch.setColor(temp);
	}
}
