package com.andedit.viewermc;

import com.andedit.viewermc.graphic.Camera;
import com.andedit.viewermc.input.contoller.GameController;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Player {
	
	private final Camera camera;
	private final GameController controller;
	
	private final Vector3 moveVel = new Vector3();
	private final Vector2 lookVel = new Vector2();
	
	public Player(Camera camera, GameController controller) {
		this.camera = camera;
		this.controller = controller;
		
		camera.position.set(250, 80, 250);
	}
	
	public void update() {
		
		Vector2 look = controller.getLook();
		lookVel.add(look.scl(0.013f));
		lookVel.scl(0.95f);
		camera.yaw += lookVel.x;
		camera.pitch += lookVel.y;
		
		float scl = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) ? 2 : 1;
		scl *= 0.15f;
		
		Vector2 move = controller.getMove().rotateDeg(-camera.yaw).scl(0.07f);
		moveVel.add(move.x * scl, controller.getMoveY() * 0.05f * scl, move.y * scl).scl(0.97f);
		
		camera.position.add(moveVel);
	}
}
