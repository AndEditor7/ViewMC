package com.andedit.viewermc.graphic;

import static com.badlogic.gdx.Gdx.gl;

import com.andedit.viewermc.GameCore;
import com.andedit.viewermc.util.Util;
import com.andedit.viewermc.world.World;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

public final class SkyBox implements Disposable {

	public static final Color FOG = new Color(0xB5D1FF << 8);
	
	private static final Color TEMP = new Color();
	
	private final Mesh mesh;
	private final ShaderProgram shader;
	private final PerspectiveCamera skyCam = new PerspectiveCamera();
	
	public SkyBox() {
		final MeshBuilder builder = new MeshBuilder();
		builder.begin(Usage.Position, GL20.GL_TRIANGLES);
		SphereShapeBuilder.build(builder, 1, 1, 1, 20, 24);
		mesh = builder.end();
		
		shader = Util.newShader("shaders/skybox");
		skyCam.near = 0.1f;
		skyCam.far  = 8.0f;
	}
	
	public void renderSky(World world, Camera camera) {
		if (GameCore.rendering != Rendering.MINECRAFT) return; 
		
		//if (world.getBlock(camera.position) == Blocks.WATER) {
			//FOG.set(60/255f, 111/255f, 208/255f, 1.0f);
		//}
		
		intsSkyCam(camera);
		
		var pos = camera.position;
		var biome = world.getBiome(pos.floorX(), pos.floorY(), pos.floorZ());
		
		shader.bind();
		shader.setUniformf("fogColor", TEMP.set(FOG));
		shader.setUniformf("skyColor", TEMP.set(biome.skyColor << 8));
		shader.setUniformMatrix("projTrans", skyCam.combined);
		
		gl.glDisable(GL20.GL_CULL_FACE);
		gl.glDepthMask(false);
		mesh.render(shader, GL20.GL_TRIANGLES);
		gl.glDepthMask(true);
		gl.glEnable(GL20.GL_CULL_FACE);
	}
	
	private void intsSkyCam(PerspectiveCamera camera) {
		skyCam.direction.set(camera.direction);
		skyCam.up.set(camera.up);
		skyCam.fieldOfView = camera.fieldOfView;
		skyCam.viewportWidth = camera.viewportWidth;
		skyCam.viewportHeight = camera.viewportHeight;
		skyCam.update(false);
	}

	@Override
	public void dispose() {
		mesh.dispose();
		shader.dispose();
	}
}
