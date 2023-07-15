package com.andedit.viewmc.structure;

import static com.badlogic.gdx.Gdx.gl;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.graphic.Camera;
import com.andedit.viewmc.graphic.Mesh;
import com.andedit.viewmc.graphic.MeshBuilder;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.graphic.QuadIndex;
import com.andedit.viewmc.graphic.RenderLayer;
import com.andedit.viewmc.graphic.TexBinder;
import com.andedit.viewmc.maker.MakerRenderer;
import com.andedit.viewmc.maker.Scene;
import com.andedit.viewmc.resource.Resources;
import com.andedit.viewmc.util.Identifier;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public class StructureRenderer implements MakerRenderer {
	
	private @Null Scene scene;
	private @Null Structure structure;
	private boolean flip;
	
	private final Mesh mesh;
	private final MeshProvider provider;
	private final Resources resources;
	
	private final Array<Identifier> textureToAnimate = new Array<>(200);

	public StructureRenderer(Resources resources) {
		this.resources = resources;
		mesh = new Mesh();
		provider = new MeshProvider(resources, () -> new MeshBuilder() {
			@Override
			protected float toData(float shadeLight, float ambientLight, float blockLight, float skyLight) {
				return Color.toFloatBits(shadeLight, ambientLight, 1, 1);
			}
		});
	}
	
	public void setScene(@Null Scene scene) {
		this.scene = scene;
	}
	
	public void reset() {
		this.scene = null;
	}
	
	public void setStructure(@Null Structure structure) {
		if (structure == null || this.structure == structure) return;
		this.structure = structure;
		
		var water = provider.resources.getWaterBlock();
		provider.clear();
		for (int x = 0; x < structure.sizeX; x++)
		for (int y = 0; y < structure.sizeY; y++)
		for (int z = 0; z < structure.sizeZ; z++) {
			var state = structure.getBlockstate(x, y, z);
			state.build(provider, structure, x, y, z);
			if (state.isWaterlogged() && state.block != water) {
				var renderer = water.getRenderer(structure, water.getState(), x, y, z);
				renderer.build(provider, structure, water.getState(), x, y, z);
			}
		}
		
		mesh.build(provider);
	}
	
	public boolean hasStructure() {
		return structure != null;
	}
	
	public Structure getStructure() {
		return structure;
	}
	
	@Override
	public void setFlip(boolean flip) {
		this.flip = flip;
	}
	
	@Override
	public void render(com.badlogic.gdx.graphics.Camera camera) {
		if (mesh.isEmpty()) return;
		
		gl.glCullFace(flip?GL20.GL_FRONT:GL20.GL_BACK);
		gl.glEnable(GL20.GL_CULL_FACE);
		
		resources.bindTexture();
		QuadIndex.preBind();
		
		textureToAnimate.clear();
		mesh.getTextures(textureToAnimate);
		resources.update(textureToAnimate);
		
		var render = Assets.viewRenderer;
		var shader = render.shader;
		
		
		shader.bind();
		shader.setUniformMatrix("u_projTrans", camera.combined);
		if (camera instanceof Camera cam) {
			final var camPos = cam.position;
			shader.setUniformf("u_camPos", (float)camPos.x, (float)camPos.y, (float)camPos.z);
			shader.setUniformf("u_factPos", camPos.floatFactX(), camPos.floatFactZ());
		} else {
			shader.setUniformf("u_camPos", Vector3.Zero);
			shader.setUniformf("u_factPos", Vector2.Zero);
		}
		if (scene != null) {
			shader.setUniformf("u_shade", 1f-scene.shade);
			shader.setUniformf("u_ambient", 1f-scene.ambient);
		} else {
			shader.setUniformf("u_shade", 0);
			shader.setUniformf("u_ambient", 0);
		}
		shader.setUniformf("u_flip", flip?1:0);
		shader.setUniformi("u_texture", resources.getTextureUnit());
		
		render.enable(RenderLayer.SOLID);
		mesh.render(shader, RenderLayer.SOLID);
		render.disable(RenderLayer.SOLID);
		
		render.enable(RenderLayer.TRANS);
		mesh.render(shader, RenderLayer.TRANS);
		render.disable(RenderLayer.TRANS);
		
		gl.glUseProgram(0);
		gl.glDisable(GL20.GL_CULL_FACE);
		gl.glCullFace(GL20.GL_BACK);
		TexBinder.deactive();
	}
	
	@Override
	public void dispose() {
		mesh.dispose();
	}
}
