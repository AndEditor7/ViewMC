package com.andedit.viewmc.graphic;

import com.andedit.viewmc.util.Util;
import com.andedit.viewmc.util.Vector3d;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Plane.PlaneSide;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/** A {@link PerspectiveCamera} with extra stuff. */
public final class Camera extends PerspectiveCamera 
{
	/** A pitch of up and down. */
	public float pitch;
	/** A yaw of left and right. */
	public float yaw;
	/** A roll like a doing barrel roll. */
	public float roll;
	
	public final Vector3 right = new Vector3();
	public final Vector3 upward = new Vector3();
	public final Vector3 down = new Vector3();
	
	public final Vector3d position = new Vector3d();
	
	private final Vector3 posF = new Vector3();
	
	private static final Quaternion quat = new Quaternion();
	
	{
		far = 500;
		near = 0.2f;
		fieldOfView = 70;
		setView(Util.getW(), Util.getH());
	}
	
	/** Frustum point. */
	public boolean frust(float x, float y, float z) {
		final Plane[] planes = getPlanes();
		x -= posF.x;
		y -= posF.y;
		z -= posF.z;
		for (int i = 1; i < planes.length; i++) {
			PlaneSide result = planes[i].testPoint(x, y, z);
			if (result == PlaneSide.Back) return false;
		}
		return true;
	}
	
	/** Frustum sphere. */
	public boolean frust(float x, float y, float z, float radius) {
		final Plane[] planes = getPlanes();
		x -= posF.x;
		y -= posF.y;
		z -= posF.z;
		for (int i = 1; i < 6; i++) {
			final Plane plane = planes[i];
			final Vector3 normal = plane.normal;
			if (normal.dot(x, y, z) < (-radius - plane.d)) {
				return false;
			}
		}
		return true;
	}
	
	/** Frustum bounding box. */
	public boolean frust(float x, float y, float z, float w, float l, float h) {
		final Plane[] planes = getPlanes();
		final int s = planes.length;
		w *= 0.5f;
		l *= 0.5f;
		h *= 0.5f;
		x -= posF.x;
		y -= posF.y;
		z -= posF.z;
		x += w;
		y += l;
		z += h;

		for (int i = 1; i < s; i++) {
			final Plane plane = planes[i];
			final Vector3 normal = plane.normal;
			final float dist = normal.dot(x, y, z) + plane.d;
			
			final float radius = 
			w * Math.abs(normal.x) +
			l * Math.abs(normal.y) +
			h * Math.abs(normal.z);

			if (dist < radius && dist < -radius) {
				return false;
			}
		}
		return true;
	}
	
	public boolean frustChunk(int chunkX, int chunkY, int chunkZ) {
		final Plane[] planes = getPlanes();
		final int s = planes.length;
		float x, y, z;
		x = (chunkX<<4)+8;
		y = (chunkY<<4)+8;
		z = (chunkZ<<4)+8;
		x -= posF.x;
		y -= posF.y;
		z -= posF.z;
		x += position.floatFactX();
		z += position.floatFactX();
		
		for (int i = 1; i < s; i++) {
			final Plane plane = planes[i];
			final Vector3 normal = plane.normal;
			final float dist = normal.dot(x, y, z) + plane.d;
			
			final float radius = 
			8f * Math.abs(normal.x) +
			8f * Math.abs(normal.y) +
			8f * Math.abs(normal.z);

			if (dist < radius && dist < -radius) {
				return false;
			}
		}
		
		return true;
	}
	
	public Plane[] getPlanes() {
		return frustum.planes;
	}
	
	public void setView(int width, int height) {
		viewportWidth = width;
		viewportHeight = height;
	}
	
	@Override
	public void update(boolean updateFrustum) {
		updateRotation();
		super.update(updateFrustum);
		position.toVecF(posF);
	}
	
	private void updateRotation() {
		pitch = MathUtils.clamp(pitch, -90f, 90f);
		yaw = Util.modAngle(yaw);
		
		//reset quaternion and then set its rotation.
		quat.setEulerAngles(yaw, pitch, roll);
		
		//set camera angle back to zero and rotate it.
		direction.set(0f, 0f, 1f);
		up.set(0f, 1f, 0f);
		rotate(quat);
		
		right.set(direction).crs(up);
		upward.set(right).add(up).scl(0.5f);
		down.set(right).sub(up).scl(0.5f);
	}
}
