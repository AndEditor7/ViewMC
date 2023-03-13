package com.andedit.viewmc.world;

import static com.andedit.viewmc.Main.main;
import static com.andedit.viewmc.Statics.meshExe;
import static com.badlogic.gdx.Gdx.gl;

import java.util.concurrent.Future;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.Debugs;
import com.andedit.viewmc.graphic.Camera;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.graphic.QuadIndex;
import com.andedit.viewmc.graphic.RenderLayer;
import com.andedit.viewmc.graphic.SkyBox;
import com.andedit.viewmc.graphic.TexBinder;
import com.andedit.viewmc.graphic.renderer.Renderers;
import com.andedit.viewmc.resource.Resources;
import com.andedit.viewmc.util.FloodFill;
import com.andedit.viewmc.util.Pair;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectSet;

public class WorldRenderer implements Disposable {
	
	static final int RADIUS_H = 10;
	static final float RADIUS_SCL = 0.7f;
	static final int RADIUS_V = Math.min((int)(RADIUS_H*RADIUS_SCL), 12);
	static final int BUILDERS = 20;
	
	static final int DELETE_MESH_OFFSET = 3;
	
	private final Array<Mesh> meshes = new Array<>(false, 500);
	private final Array<Mesh> trans = new Array<>(false, 200);
	private final GridPoint3 chunkPos = new GridPoint3();
	private final Renderers renderers;
	private final Resources resources;
	private World world;
	
	private final Array<MeshProvider> providers;
	private final Array<Pair<Future<Void>, MeshLoaderTask>> futures;
	private final ObjectSet<MeshToLoad> pendingMeshes = new ObjectSet<>();
	private final GridPoint3 lastPos = new GridPoint3(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	private final Array<ChunkToLoad> chunkArray = new Array<>(500);
	private final Array<MeshToLoad> meshArray = new Array<>(2000);
	
	public WorldRenderer(Renderers renderers, Resources blocks) {
		this.renderers = renderers;
		this.resources = blocks;
		this.providers = new Array<>(false, BUILDERS);
		this.futures = new Array<>(false, BUILDERS);
		for (int i = 0; i < BUILDERS; i++) {
			providers.add(new MeshProvider(blocks));
		}
	}
	
	public void setWorld(World world) {
		this.world = world;
	}
	
	public void render(Camera camera) {
		final var camPos = camera.position;
		chunkPos.set(camPos.floorX()>>4, camPos.floorY()>>4, camPos.floorZ()>>4);
		
		if (Debugs.isKeyJustPressed(Debugs.F4)) {
			clearMeshes();
		}
		
		// somthing new
		scanForChunks();
		scanForMeshs(camera);
		updateMeshes();
		

		var renderer = renderers.getRenderer();
		var shader = renderer.shader;
		
		if (renderer == renderers.minecraft)
		main.skyBox.renderSky(world, camera);
		gl.glEnable(GL20.GL_CULL_FACE);
		
		// light binding
		Assets.lightMapBind.bind(Assets.lightMap);
		
		float fogEnd = (RADIUS_H-1) * 16f;
		resources.bindTexture();
		resources.update();
		QuadIndex.preBind();
		
		
		shader.bind();
		shader.setUniformMatrix("u_projTrans", camera.combined);
		shader.setUniformf("u_camPos", (float)camPos.x, (float)camPos.y, (float)camPos.z);
		shader.setUniformf("u_factPos", camPos.floatFactX(), camPos.floatFactZ());
		shader.setUniformf("u_fogColor", SkyBox.FOG);
		shader.setUniformf("u_fogStart", fogEnd);
		shader.setUniformf("u_fogEnd", fogEnd * 0.85f);
		shader.setUniformi("u_texture", resources.getTextureUnit());
		shader.setUniformi("u_lightMap", Assets.lightMapBind.unit);
		
		trans.clear();
		renderer.enable(RenderLayer.SOILD);
		for (int i = 0; i < meshes.size; i++) {
			var mesh = meshes.get(i);
			
			if (mesh.isEmpty() || mesh.pass(chunkPos, DELETE_MESH_OFFSET)) {
				meshes.removeIndex(i--);
				mesh.dispose();
				continue;
			}
			
			if (!mesh.pass(chunkPos, 0) && mesh.isVisible(camera)) {
				mesh.render(shader, RenderLayer.SOILD);
				if (!mesh.isEmpty(RenderLayer.TRANS)) {
					trans.add(mesh);
				}
			}
		}
		renderer.disable(RenderLayer.SOILD);

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
		
		renderer.enable(RenderLayer.TRANS);
		for (int i = 0; i < trans.size; i++) {
			trans.get(i).render(shader, RenderLayer.TRANS);
		}
		trans.clear();
		renderer.disable(RenderLayer.TRANS);
		
		gl.glDisable(GL20.GL_CULL_FACE);
		TexBinder.deactive();
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
}
