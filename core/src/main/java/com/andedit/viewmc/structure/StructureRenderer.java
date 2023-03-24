package com.andedit.viewmc.structure;

import static com.badlogic.gdx.Gdx.gl;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.graphic.Camera;
import com.andedit.viewmc.graphic.Mesh;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.graphic.QuadIndex;
import com.andedit.viewmc.graphic.RenderLayer;
import com.andedit.viewmc.graphic.TexBinder;
import com.andedit.viewmc.resource.Resources;
import com.andedit.viewmc.util.Identifier;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

public class StructureRenderer implements Disposable {
	
	private @Null Structure structure;
	
	private final Mesh mesh;
	private final MeshProvider provider;
	private final Resources resources;
	
	private final Array<Identifier> textureToAnimate = new Array<>(200);

	public StructureRenderer(Resources resources) {
		this.resources = resources;
		mesh = new Mesh();
		provider = new MeshProvider(resources);
	}
	
	public void setStructure(Structure structure) {
		this.structure = structure;
		
		var water = provider.resources.getWaterBlock();
		provider.clear();
		for (int x = 0; x < structure.sizeX; x++)
		for (int y = 0; y < structure.sizeY; y++)
		for (int z = 0; z < structure.sizeZ; z++) {
			var state = structure.getBlockstate(x, y, z);
			state.build(provider, structure, x, y, z);
			if (state.isWaterlogged() && state.block != water) {
				water.build(provider, structure, water.getState(), x, y, z);
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
	
	public void render(Camera camera) {
		if (mesh.isEmpty()) return;
		final var camPos = camera.position;
		
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
		shader.setUniformf("u_camPos", (float)camPos.x, (float)camPos.y, (float)camPos.z);
		shader.setUniformf("u_factPos", camPos.floatFactX(), camPos.floatFactZ());
		shader.setUniformi("u_texture", resources.getTextureUnit());
		
		render.enable(RenderLayer.SOILD);
		mesh.render(shader, RenderLayer.SOILD);
		render.disable(RenderLayer.SOILD);
		
		render.enable(RenderLayer.TRANS);
		mesh.render(shader, RenderLayer.TRANS);
		render.disable(RenderLayer.TRANS);
		
		gl.glDisable(GL20.GL_CULL_FACE);
		TexBinder.deactive();
	}
	
	@Override
	public void dispose() {
		mesh.dispose();
	}
}
