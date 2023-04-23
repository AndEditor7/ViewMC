package com.andedit.viewmc.ui;

import static com.andedit.viewmc.Main.main;

import com.andedit.viewmc.Debugs;
import com.andedit.viewmc.GameCore;
import com.andedit.viewmc.input.BasicInput;
import com.andedit.viewmc.ui.actor.DebugInfo;
import com.andedit.viewmc.ui.util.BaseUI;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;

public class GameUI extends BaseUI {
	
	private final GameCore core;
	private final BasicInput input;
	private final DebugInfo debugInfo;
	
	public GameUI(GameCore core) {
		this.core = core;
		this.input = new BasicInput();
		
		debugInfo = new DebugInfo(core);
		debugInfo.setVisible(false);
		add(debugInfo);
	}
	
	@Override
	public void update() {
		if (input.isKeyJustPressed(Debugs.F3)) {
			debugInfo.setVisible(!debugInfo.isVisible());
		}
		
		if (input.isKeyJustPressed(Debugs.F1)) {
			main.renderers.cycle();
		}
		
		if (input.isKeyJustPressed(Keys.ESCAPE)) {
			core.manager.setUI(new PauseUI(core), true);
		}
		
		input.reset();
	}
	
	@Override
	public InputProcessor getInputAfter() {
		return input;
	}
}
