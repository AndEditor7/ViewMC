package com.andedit.viewermc;

import com.andedit.viewermc.graphic.Camera;
import com.andedit.viewermc.input.contoller.GameController;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;

public class Player {
	
	private final Camera camera;
	private final GameController controller;
	
	public Player(Camera camera, GameController controller) {
		this.camera = camera;
		this.controller = controller;
		
		camera.position.set(450, 80, 150);
	}
	
	public void update() {
		Vector2 look = controller.getLook();
		camera.yaw += look.x;
		camera.pitch += look.y;
		float scl = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) ? 2 : 1;
		scl *= 0.15f;
		Vector2 move = controller.getMove().rotateDeg(-camera.yaw).scl(2f);
		camera.translate(move.x * scl, controller.getMoveY()  * scl, move.y * scl);
		camera.updateRotation();
	}
}
