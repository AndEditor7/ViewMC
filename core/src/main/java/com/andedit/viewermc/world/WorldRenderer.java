package com.andedit.viewermc.world;

import static com.andedit.viewermc.Main.main;
import static com.andedit.viewermc.graphic.MeshVert.shader;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.math.MathUtils.floor;

import com.andedit.viewermc.block.Blocks;
import com.andedit.viewermc.graphic.Camera;
import com.andedit.viewermc.graphic.MeshProvider;
import com.andedit.viewermc.graphic.QuadIndex;
import com.andedit.viewermc.graphic.RenderLayer;
import com.andedit.viewermc.graphic.SkyBox;
import com.andedit.viewermc.graphic.TexBinder;
import com.andedit.viewermc.util.Debugs;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

public class WorldRenderer implements Disposable {
	
	static final int RADIUS_H = 10;
	static final float RADIUS_SCL = 0.7f;
	static final int RADIUS_V = (int)(RADIUS_H*RADIUS_SCL);
	
	private final Array<Mesh> meshes = new Array<>(500);
	private final Array<Mesh> trans = new Array<>();
	private final GridPoint3 chunkPos = new GridPoint3();
	private final MeshProvider provider;
	private final Blocks blocks;
	private World world;
	
	public WorldRenderer(Blocks blocks) {
		this.blocks = blocks;
		this.provider = new MeshProvider(blocks);
	}
	
	public void setWorld(World world) {
		this.world = world;
	}
	
	public void render(Camera camera) {
		final Vector3 camPos = camera.position;
		chunkPos.set(floor(camPos.x)>>4, floor(camPos.y)>>4, floor(camPos.z)>>4);
		
		if (Debugs.isKeyJustPressed(Keys.F1)) {
			clearMeshes();
		}
		
		loop :
		for (int i = 2; i < RADIUS_H; i++)
		for (int x = chunkPos.x-i; x <= chunkPos.x+i; x++)
		for (int z = chunkPos.z-i; z <= chunkPos.z+i; z++) {
			var chunk = world.getChunk(x, z);
			if (chunk == null) continue;
			for (int y = chunkPos.y+(int)(i*RADIUS_SCL); y > chunkPos.y-(int)(i*RADIUS_SCL); y--) {
				//if (isOutBound(x, y, z)) continue;
				var section = chunk.getSection(y);
				if (section == null) continue;
				
				if (section.isDirty) {
					section.isDirty = false;
					var mesh = getMesh(x, y, z);
					if (mesh == null) {
						mesh = new Mesh(this, section, x, y, z);
						meshes.add(mesh);
					}
					if (!mesh.build(world, provider)) {
						break loop;
					}
				}
			}
		}
		
		main.skyBox.renderSky(world, camera);
		
		gl.glEnable(GL20.GL_CULL_FACE);
		
		// light binding
		main.lightMapBind.bind(main.lightMap);
		
		float fogEnd = (RADIUS_H-1) * 16f;
		blocks.bindTexture();
		blocks.update();
		QuadIndex.preBind();
		shader.bind();
		shader.setUniformMatrix("u_projTrans", camera.combined);
		shader.setUniformf("u_camPos", camera.position);
		shader.setUniformf("u_fogColor", SkyBox.FOG);
		shader.setUniformf("u_start", fogEnd);
		shader.setUniformf("u_end", fogEnd * 0.85f);
		shader.setUniformi("u_texture", blocks.getTextureUnit());
		shader.setUniformi("u_lightMap", main.lightMapBind.unit);
		var planes = camera.getPlanes();
		trans.clear();
		for (int i = 0; i < meshes.size; i++) {
			var mesh = meshes.get(i);
			
			if (mesh.pass(chunkPos, 3) || mesh.isEmpty()) {
				meshes.removeIndex(i--);
				mesh.dispose();
				continue;
			}
			
			if (!mesh.pass(chunkPos, 0) && mesh.isVisible(planes)) {
				mesh.render(RenderLayer.SOILD);
				if (!mesh.isEmpty(RenderLayer.TRANS)) {
					trans.add(mesh);
				}
			}
		}

		gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL20.GL_BLEND);
		for (int i = 0; i < trans.size; i++) {
			trans.get(i).render(RenderLayer.TRANS);
		}
		trans.clear();
		
		TexBinder.deactive();
		gl.glDisable(GL20.GL_BLEND);
		gl.glDisable(GL20.GL_CULL_FACE);
	}
	
	@Null
	Mesh getMesh(int x, int y, int z) {
		for (var mesh : meshes) {
			if (mesh.equals(x, y, z)) {
				return mesh;
			}
		}
		return null;
	}
	
	boolean isDirty(int x, int y, int z) {
		var section = world.getSection(x, y, z);
		return section == null ? false : section.isDirty;
	}
	
	public void setDirt(boolean isDirty, int x, int y, int z) {
		var section = world.getSection(x, y, z);
		if (section != null) {
			section.isDirty = isDirty;
		}
	}
	
	public void clearMeshes() {
		meshes.forEach(Mesh::dispose);
		meshes.clear();
	}

	@Override
	public void dispose() {
		meshes.forEach(Mesh::dispose);
	}
}
