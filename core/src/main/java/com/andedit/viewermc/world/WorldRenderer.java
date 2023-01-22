package com.andedit.viewermc.world;

import static com.andedit.viewermc.Statics.meshExe;
import static com.andedit.viewermc.Main.main;
import static com.andedit.viewermc.graphic.MeshVert.shader;
import static com.badlogic.gdx.Gdx.gl;
import static com.andedit.viewermc.util.Util.floor;

import java.util.concurrent.Future;

import com.andedit.viewermc.Assets;
import com.andedit.viewermc.Debugs;
import com.andedit.viewermc.GameCore;
import com.andedit.viewermc.block.Blocks;
import com.andedit.viewermc.graphic.Camera;
import com.andedit.viewermc.graphic.MeshProvider;
import com.andedit.viewermc.graphic.QuadIndex;
import com.andedit.viewermc.graphic.RenderLayer;
import com.andedit.viewermc.graphic.Rendering;
import com.andedit.viewermc.graphic.SkyBox;
import com.andedit.viewermc.graphic.TexBinder;
import com.andedit.viewermc.util.FloodFill;
import com.andedit.viewermc.util.Pair;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectSet;

public class WorldRenderer implements Disposable {
	
	static final int RADIUS_H = 16;
	static final float RADIUS_SCL = 0.7f;
	static final int RADIUS_V = Math.min((int)(RADIUS_H*RADIUS_SCL), 11);
	
	static final int DELETE_MESH_OFFSET = 3;
	
	private final Array<Mesh> meshes = new Array<>(false, 500);
	private final Array<Mesh> trans = new Array<>(false, 200);
	private final GridPoint3 chunkPos = new GridPoint3();
	private final Blocks blocks;
	private World world;
	
	private final MeshProvider provider;
	private final Array<MeshProvider> providers = new Array<>();
	private final Array<Pair<Future<Void>, MeshLoaderTask>> futures = new Array<>();
	private final ObjectSet<MeshToLoad> pendingMeshes = new ObjectSet<>();
	private final GridPoint3 lastPos = new GridPoint3(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	private final Array<ChunkToLoad> chunkArray = new Array<>(500);
	private final Array<MeshToLoad> meshArray = new Array<>(2000);
	
	public WorldRenderer(Blocks blocks) {
		this.blocks = blocks;
		this.provider = new MeshProvider(blocks);
		for (int i = 0; i < 20; i++) {
			providers.add(new MeshProvider(blocks));
		}
	}
	
	public void setWorld(World world) {
		this.world = world;
	}
	
	public void render(Camera camera) {
		final var camPos = camera.position;
		chunkPos.set(camPos.floorX()>>4, camPos.floorY()>>4, camPos.floorZ()>>4);
		var planes = camera.getPlanes();
		var camPosF = camPos.toVecF();
		
		if (Debugs.isKeyJustPressed(Debugs.F4)) {
			clearMeshes();
		}
		
		// somthing new
		scanForChunks();
		scanForMeshs(camera);
		updateMeshes();
		
		main.skyBox.renderSky(world, camera);
		
		gl.glEnable(GL20.GL_CULL_FACE);
		
		// light binding
		Assets.lightMapBind.bind(Assets.lightMap);
		
		float fogEnd = (GameCore.rendering != Rendering.MINECRAFT ? 10000 : 0) + (RADIUS_H-1) * 16f;
		blocks.bindTexture();
		blocks.update();
		QuadIndex.preBind();
		shader.bind();
		shader.setUniformMatrix("u_projTrans", camera.combined);
		shader.setUniformf("u_camPos", (float)camPos.x, (float)camPos.y, (float)camPos.z);
		shader.setUniformf("u_factPos", camPos.floatFactX(), camPos.floatFactZ());
		shader.setUniformf("u_fogColor", SkyBox.FOG);
		shader.setUniformf("u_start", fogEnd);
		shader.setUniformf("u_end", fogEnd * 0.85f);
		shader.setUniformi("u_texture", blocks.getTextureUnit());
		shader.setUniformi("u_lightMap", Assets.lightMapBind.unit);
		
		trans.clear();
		for (int i = 0; i < meshes.size; i++) {
			var mesh = meshes.get(i);
			
			if (mesh.isEmpty() || mesh.pass(chunkPos, DELETE_MESH_OFFSET)) {
				meshes.removeIndex(i--);
				mesh.dispose();
				continue;
			}
			
			if (!mesh.pass(chunkPos, 0) && mesh.isVisible(camera)) {
				mesh.render(RenderLayer.SOILD);
				if (!mesh.isEmpty(RenderLayer.TRANS)) {
					trans.add(mesh);
				}
			}
		}

		if (trans.notEmpty()) {
			var gridA = new GridPoint3();
			var gridB = new GridPoint3();
			var gridPos = camPos.toGrid();;
			trans.sort((a, b) -> {
				a.getCenter(gridA);
				b.getCenter(gridB);
				return MathUtils.round(Math.signum(gridPos.dst2(gridB) - gridPos.dst2(gridA)));
			});
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
		
		world.isDirty = false;
	}
	
	private void scanForChunks() {
		if (world.isDirty || lastPos.x != chunkPos.x || lastPos.z != chunkPos.z) {
			chunkArray.size = 0;
			new FloodFill(RADIUS_H*2, node -> {
				chunkArray.add(new ChunkToLoad(chunkPos.x+node.x(), chunkPos.z+node.z()));
			}).run();
		}
	}
	
	private void scanForMeshs(Camera camera) {
		
		if (world.isDirty || !lastPos.equals(chunkPos)) {
			lastPos.set(chunkPos);
			meshArray.clear();
			//System.out.println("Scan for mesh!");
			for (var chunkToLoad : chunkArray) {
				var chunk = world.getChunk(chunkToLoad.worldX, chunkToLoad.worldZ);
				if (chunk == null || !chunk.canBuild()) continue;
				for (int y = chunkPos.y+RADIUS_V; y >= chunkPos.y-RADIUS_V; y--) {
					var section = chunk.getSection(y);
					if (section == null) continue;
					if (section.isDirty) {
						meshArray.add(new MeshToLoad(section, chunkToLoad.worldX, y, chunkToLoad.worldZ));
					}
				}
			}
		}
		
		
		for (int i = 0; providers.notEmpty() && i < meshArray.size; i++) {
			var meshToLoad = meshArray.get(i);
			if (!meshToLoad.isVisible(camera)) continue;
			
			if (!pendingMeshes.contains(meshToLoad)) {
				pendingMeshes.add(meshToLoad);
				submit(new MeshLoaderTask(providers.pop(), meshToLoad));
				meshArray.removeIndex(i--);
			}
			
			meshToLoad.section.isDirty = false;
		}
	}
	
	private void updateMeshes() {
		int built = 0;
		for (int i = 0; i < futures.size; i++) {
			var pair = futures.get(i);
			var future = pair.left;
			if (future.isDone()) {
				var task = pair.right;
				try {
					future.get();
					var mesh = getMesh(task.mesh.chunkX, task.mesh.chunkY, task.mesh.chunkZ);
					if (mesh == null) {
						mesh = new Mesh(this, task.mesh);
						meshes.add(mesh);
					}
					synchronized (task.provider) {
						mesh.update(task.provider);
						
					}
				} catch (Exception e) {
					System.err.println("Error chunk mesh at " + task.mesh);
					e.printStackTrace();
				}
				
				built++;
				providers.add(task.provider);
				pendingMeshes.remove(task.mesh);
				futures.removeIndex(i--);
			}
		}
		//if (built > 10) System.out.println("built: " + built);
	}
	
	void submit(MeshLoaderTask task) {
		futures.add(new Pair<>(meshExe.submit(task), task));
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
		world.isDirty = true;
		meshes.forEach(Mesh::dispose);
		meshes.clear();
		
		for (var pair : futures) {
			var task = pair.right;
			providers.add(task.provider);
			task.section.isDirty = true;
		}
		futures.clear();
		pendingMeshes.clear();
	}

	@Override
	public void dispose() {
		meshes.forEach(Mesh::dispose);
	}
	
	/**
	loop :
	for (int i = RADIUS_H / 2; i < RADIUS_H; i++)
	for (int x = chunkPos.x-i; x <= chunkPos.x+i; x++)
	for (int z = chunkPos.z-i; z <= chunkPos.z+i; z++) {
		var chunk = world.getChunk(x, z);
		if (chunk == null || !chunk.canBuild()) {
			startChunkLoading = true;
			continue;
		}
		for (int y = chunkPos.y+RADIUS_V; y > chunkPos.y-RADIUS_V; y--) {
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
				if (!mesh.build(provider)) {
					//loadAmount++;
					//if (loadAmount >= 2) {
						//break loop;
					//}
				}
				
				skipAmount++;
				if (skipAmount >= 2) {
					break loop;
				}
			}
		}
	} **/
}
