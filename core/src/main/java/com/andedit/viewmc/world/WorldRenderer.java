package com.andedit.viewmc.world;

import static com.andedit.viewmc.Main.main;
import static com.andedit.viewmc.Statics.meshExe;
import static com.badlogic.gdx.Gdx.gl;

import java.util.concurrent.Future;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.Debugs;
import com.andedit.viewmc.graphic.Camera;
import com.andedit.viewmc.graphic.ChunkMesh;
import com.andedit.viewmc.graphic.MeshProvider;
import com.andedit.viewmc.graphic.QuadIndex;
import com.andedit.viewmc.graphic.RenderLayer;
import com.andedit.viewmc.graphic.Renderer;
import com.andedit.viewmc.graphic.SkyBox;
import com.andedit.viewmc.graphic.TexBinder;
import com.andedit.viewmc.graphic.renderer.Renderers;
import com.andedit.viewmc.resource.Resources;
import com.andedit.viewmc.util.Identifier;
import com.andedit.viewmc.util.Pair;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectSet;

public class WorldRenderer implements Renderer {
	
	public static final int RADIUS_H = 12 + 1;
	public static final float RADIUS_SCL = 1f;
	public static final int RADIUS_V = (int)(RADIUS_H*RADIUS_SCL);
	public static final int BUILDERS = 20; // 20
	
	static final int DELETE_MESH_OFFSET = 3;
	
	private final Array<ChunkMesh> meshes = new Array<>(false, 500);
	private final Array<ChunkMesh> transToRender = new Array<>(false, 500);
	private final Array<ChunkMesh> soildToRender = new Array<>(false, 500);
	private final Array<Identifier> textureToAnimate = new Array<>(200);
	private final GridPoint3 chunkPos = new GridPoint3();
	private final Renderers renderers;
	private final Resources resources;
	private World world;
	
	private CaveCullingTest culling;
	
	final Array<MeshProvider> providers;
	private final Array<Pair<Future<Void>, MeshLoaderTask>> futures;
	final ObjectSet<MeshToLoad> pendingMeshes = new ObjectSet<>();
	
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
		culling = new CaveCullingTest(this, world);
	}
	
	@Override
	public void render(com.badlogic.gdx.graphics.Camera cam) {
		var camera = (Camera)cam;
		final var camPos = camera.position;
		chunkPos.set(camPos.floorX()>>4, camPos.floorY()>>4, camPos.floorZ()>>4);
		camera.far = RADIUS_H * 24;
		
		if (Debugs.isKeyJustPressed(Debugs.F4)) {
			clearMeshes();
		}
		
		culling.update(camera);
		
		// somthing new
		//scanForChunks();
		//scanForMeshs(camera);
		updateMeshes();

		transToRender.clear();
		soildToRender.clear();
		for (int i = 0; i < meshes.size; i++) {
			var mesh = meshes.get(i);
			
			if (mesh.isEmpty() || mesh.pass(chunkPos, DELETE_MESH_OFFSET)) {
				meshes.removeIndex(i--);
				mesh.dispose();
				continue;
			}
			
			if (!mesh.pass(chunkPos, 0) && culling.canRender(mesh.x, mesh.y, mesh.z)) {
				if (!mesh.isEmpty(RenderLayer.SOILD)) {
					soildToRender.add(mesh);
				}
				if (!mesh.isEmpty(RenderLayer.TRANS)) {
					transToRender.add(mesh);
				}
			}
		}

		if (transToRender.notEmpty()) {
			var gridA = new GridPoint3();
			var gridB = new GridPoint3();
			var gridPos = camPos.toGrid();;
			transToRender.sort((a, b) -> {
				a.getCenter(gridA);
				b.getCenter(gridB);
				return MathUtils.round(Math.signum(gridPos.dst2(gridB) - gridPos.dst2(gridA)));
			});
		}
		
		var renderer = renderers.getRenderer();
		var shader = renderer.shader;
		
		if (renderer == renderers.minecraft)
		main.skyBox.renderSky(world, camera);
		gl.glEnable(GL20.GL_CULL_FACE);
		
		// light binding
		Assets.lightMapBind.bind(Assets.lightMap);
		
		float fogEnd = (RADIUS_H-1) * 16f;
		resources.bindTexture();
		QuadIndex.preBind();
		
//		camera.fieldOfView = 100;
//		camera.update(false);
		
		textureToAnimate.clear();
		for (var mesh : soildToRender) {
			mesh.getTextures(RenderLayer.SOILD, textureToAnimate);
		}
		for (var mesh : transToRender) {
			mesh.getTextures(RenderLayer.TRANS, textureToAnimate);
		}
		resources.update(textureToAnimate);
		
		shader.bind();
		shader.setUniformMatrix("u_projTrans", camera.combined);
		shader.setUniformf("u_camPos", (float)camPos.x, (float)camPos.y, (float)camPos.z);
		shader.setUniformf("u_factPos", camPos.floatFactX(), camPos.floatFactZ());
		shader.setUniformf("u_fogColor", SkyBox.FOG);
		shader.setUniformf("u_fogStart", fogEnd);
		shader.setUniformf("u_fogEnd", fogEnd * 0.9f);
		shader.setUniformi("u_texture", resources.getTextureUnit());
		shader.setUniformi("u_lightMap", Assets.lightMapBind.unit);
		
//		camera.fieldOfView = 70;
		
		renderer.enable(RenderLayer.SOILD);
		for (int i = 0; i < soildToRender.size; i++) {
			soildToRender.get(i).render(shader, RenderLayer.SOILD);
		}
		soildToRender.clear();
		renderer.disable(RenderLayer.SOILD);
		
		renderer.enable(RenderLayer.TRANS);
		for (int i = 0; i < transToRender.size; i++) {
			transToRender.get(i).render(shader, RenderLayer.TRANS);
		}
		transToRender.clear();
		renderer.disable(RenderLayer.TRANS);
		
		gl.glDisable(GL20.GL_CULL_FACE);
		TexBinder.deactive();
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
						mesh = new ChunkMesh(this, task.mesh);
						meshes.add(mesh);
					}
					synchronized (task.provider) {
						mesh.build(task.provider);
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
	
	void submit(MeshToLoad meshToLoad) {
		var task = new MeshLoaderTask(providers.pop(), meshToLoad);
		futures.add(new Pair<>(meshExe.submit(task), task));
	}
	
	@Null
	ChunkMesh getMesh(int x, int y, int z) {
		for (var mesh : meshes) {
			if (mesh.equals(x, y, z)) {
				return mesh;
			}
		}
		return null;
	}
	
	boolean isDirty(int x, int y, int z) {
		var section = world.getSection(x, y, z);
		return section != null && section.isDirty;
	}
	
	public void setDirt(boolean isDirty, int x, int y, int z) {
		var section = world.getSection(x, y, z);
		if (section != null) {
			section.isDirty = isDirty;
		}
	}
	
	public void clearMeshes() {
		meshes.forEach(ChunkMesh::dispose);
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
		meshes.forEach(ChunkMesh::dispose);
	}
}
