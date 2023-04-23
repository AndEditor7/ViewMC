package com.andedit.viewmc.maker;

import com.andedit.viewmc.structure.Structure;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/** The rotation scene */
public class Scene {
	private static final Quaternion quat = new Quaternion();
	
	public final Color backgroundColor = new Color();
	public final Vector3 center = new Vector3();
	
	public float ambient = 1, shade = 1;
	public float fov = 50, distance, zoom = 1;
	public double time, maxTime = 5;
	public float pitch = 30f, yaw;
	public float rotDegrees = 180;
	
	public void setStructure(Structure structure) {
		center.set(structure.sizeX, structure.sizeY, structure.sizeZ);
		distance = center.len();
		center.set(structure.sizeX, structure.sizeY * 0.8f, structure.sizeZ).scl(0.5f);
	}
	
	public boolean update(Camera camera, double delta) {
		boolean isComplated = false;
		time += delta;
		if (time > maxTime) {
			isComplated = true;
			time %= maxTime;
		}
		yaw = MathUtils.lerp(-rotDegrees, rotDegrees, (float)(time / maxTime)) + 50f;
		
		//reset quaternion and then set its rotation.
		quat.setEulerAngles(yaw, pitch, 0);
		
		//set camera angle back to zero and rotate it.
		camera.direction.set(0f, 0f, 1f);
		camera.up.set(0f, 1f, 0f);
		camera.rotate(quat);
		
		var dir = camera.direction;
		
		if (camera instanceof PerspectiveCamera perspective) {
			perspective.fieldOfView = fov;
			var a = distance / zoom;
			var newZoom = (1f / (fov / 60f)) * a;
			camera.near = newZoom - (a * 0.6f);
			camera.far = newZoom + (a * 0.4f);
			camera.position.set(center.x-(dir.x*newZoom), center.y-(dir.y*newZoom), center.z-(dir.z*newZoom));
		} else if (camera instanceof OrthographicCamera orthographic) {
			camera.position.set(center.x-(dir.x*zoom), center.y-(dir.y*zoom), center.z-(dir.z*zoom));
		}
		
		return isComplated;
	}
	
	public Scene newInstance() {
		var scene = new Scene();
		scene.backgroundColor.set(backgroundColor);
		scene.center.set(center);
		scene.fov = fov;
		scene.zoom = zoom;
		scene.maxTime = maxTime;
		scene.pitch = pitch;
		scene.yaw = yaw;
		scene.distance = distance;
		scene.ambient = ambient;
		scene.shade = shade;
		scene.rotDegrees = rotDegrees;
		return scene;
	}
}
