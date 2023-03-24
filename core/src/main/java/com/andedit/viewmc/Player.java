package com.andedit.viewmc;

import com.andedit.viewmc.graphic.Camera;
import com.andedit.viewmc.input.contoller.GameController;
import com.andedit.viewmc.world.LevelDat.PlayerDat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Player {
	
	private final Camera camera;
	private final GameController controller;
	
	private final Vector3 moveVel = new Vector3();
	private final Vector2 lookVel = new Vector2();
	private final Vector2 look = new Vector2();
	
	public Player(Camera camera, GameController controller, PlayerDat player) {
		this.camera = camera;
		this.controller = controller;
		camera.position.set(player.xPos, player.yPos + 1.7f, player.zPos);
		camera.pitch = player.pitch;
		camera.yaw = -player.yaw;
	}
	
	public void update() {
		look.set(controller.getLook());
	}
	
	public void fixedUpdate() {
		float scl = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) ? 2 : 1;
		scl *= 0.15f;
		lookVel.add(look.scl(0.014f));
		lookVel.scl(0.95f);
		camera.yaw += lookVel.x;
		camera.pitch += lookVel.y;
		look.setZero();
		
		Vector2 move = controller.getMove().rotateDeg(-camera.yaw).scl(0.07f);
		moveVel.add(move.x * scl, controller.getMoveY() * 0.05f * scl, move.y * scl).scl(0.97f);
		
		camera.position.add(moveVel);
	}
}
