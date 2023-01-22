package com.andedit.viewermc.ui;

import com.andedit.viewermc.Debugs;
import com.andedit.viewermc.GameCore;
import com.andedit.viewermc.input.BasicInput;
import com.andedit.viewermc.ui.actor.DebugInfo;
import com.andedit.viewermc.ui.util.BaseUI;
import com.badlogic.gdx.InputProcessor;

public class GameUI extends BaseUI {
	
	private final GameCore core;
	private final BasicInput input;
	private final DebugInfo debugInfo;
	
	public GameUI(GameCore core) {
		this.core = core;
		this.input = new BasicInput();
		
		debugInfo = new DebugInfo(core);
		//debugInfo.setVisible(false);
		add(debugInfo);
	}
	
	@Override
	public void update() {
		if (input.isKeyJustPressed(Debugs.F3)) {
			debugInfo.setVisible(!debugInfo.isVisible());
		}
		
		if (input.isKeyJustPressed(Debugs.F1)) {
			GameCore.rendering = GameCore.rendering.cycle();
			if (GameCore.rendering.reloadMesh) {
				core.render.clearMeshes();
			}
		}
		
		input.reset();
	}
	
	@Override
	public InputProcessor getInput() {
		return input;
	}
}
