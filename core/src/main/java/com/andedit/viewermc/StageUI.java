package com.andedit.viewermc;

import com.andedit.viewermc.graphic.FastBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

public class StageUI extends Stage {
	public StageUI(Viewport viewport) {
		super(viewport, new FastBatch());
	}
	
	@Override
	public void dispose() {
		super.dispose();
		getBatch().dispose();
	}
}
