package com.andedit.viewermc.world;

import static com.andedit.viewermc.graphic.MeshVert.shader;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.math.MathUtils.floor;

import com.andedit.viewermc.block.Blocks;
import com.andedit.viewermc.graphic.Camera;
import com.andedit.viewermc.graphic.MeshBuilder;
import com.andedit.viewermc.graphic.QuadIndex;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

public class WorldRenderer implements Disposable {
	
	static final int RADIUS_H = 8;
	static final float RADIUS_SCL = 0.7f;
	static final int RADIUS_V = (int)(RADIUS_H*RADIUS_SCL);
	
	private final Array<Mesh> meches = new Array<>(500);
	private final GridPoint3 chunkPos = new GridPoint3();
	private final MeshBuilder builder = new MeshBuilder();
	private final Blocks blocks;
	private World world;
	
	public WorldRenderer(Blocks blocks) {
		this.blocks = blocks;
	}
	
	public void setWorld(World world) {
		this.world = world;
	}
	
	public void render(Camera camera) {
		final Vector3 camPos = camera.position;
		chunkPos.set(floor(camPos.x)>>4, floor(camPos.y)>>4, floor(camPos.z)>>4);
		
		loop :
		for (int i = 0; i < RADIUS_H; i++)
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
						meches.add(mesh);
					}
					if (!mesh.build(world, builder)) {
						break loop;
					}
				}
			}
		}
		
		
		gl.glEnable(GL20.GL_CULL_FACE);
		blocks.bindTexture();
		blocks.update();
		QuadIndex.preBind();
		shader.bind();
		shader.setUniformMatrix("u_projTrans", camera.combined);
		var planes = camera.getPlanes();
		for (int i = 0; i < meches.size; i++) {
			var mesh = meches.get(i);
			
			if (mesh.pass(chunkPos, 3) || mesh.isEmpty()) {
				meches.removeIndex(i--);
				mesh.dispose();
				continue;
			}
			
			if (!mesh.pass(chunkPos, 0) && mesh.isVisible(planes)) {
				mesh.render();
			}
		}
		gl.glDisable(GL20.GL_CULL_FACE);
	}
	
	@Null
	Mesh getMesh(int x, int y, int z) {
		for (var mesh : meches) {
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

	@Override
	public void dispose() {
		meches.forEach(Mesh::dispose);
	}
}
