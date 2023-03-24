package com.andedit.viewmc.graphic;

import static com.badlogic.gdx.Gdx.gl;

import java.util.ArrayList;
import java.util.EnumMap;

import com.andedit.viewmc.graphic.vertex.Vertex;
import com.andedit.viewmc.util.Identifier;
import com.andedit.viewmc.util.Util;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class Mesh implements Disposable {
	protected final EnumMap<RenderLayer, ArrayList<Vertex>> verts;
	protected final EnumMap<RenderLayer, Array<Identifier>> textureToAnimate;
	
	protected boolean isEmpty;
	
	{
		verts = new EnumMap<>(RenderLayer.class);
		textureToAnimate = new EnumMap<>(RenderLayer.class);
		for (var layer : RenderLayer.VALUES) {
			verts.put(layer, new ArrayList<>(2));
			textureToAnimate.put(layer, new Array<>());
		}
	}
	
	public void render(ShaderProgram shader, RenderLayer layer) {
		for (var vertex : verts.get(layer)) {
			vertex.bind(shader);
			gl.glDrawElements(GL20.GL_TRIANGLES, (vertex.size() / MeshVert.byteSize) * 6, GL20.GL_UNSIGNED_SHORT, 0);
			if (!Util.isGL30()) {
				vertex.unbind(shader);
			}
		}
	}
	
	public void getTextures(Array<Identifier> array) {
		for (var val : RenderLayer.VALUES) {
			getTextures(val, array);
		}
	}
	
	public void getTextures(RenderLayer layer, Array<Identifier> array) {
		array.addAll(textureToAnimate.get(layer));
	}
	
	public boolean isEmpty(RenderLayer layer) {
		return verts.get(layer).isEmpty();
	}
	
	public boolean isEmpty() {
		return isEmpty;
	}
	
	protected Vertex newVertex() {
		return Vertex.newVbo(MeshVert.attributes, GL20.GL_STREAM_DRAW);
	}
	
	public void build(MeshProvider provider) {
		isEmpty = provider.isEmpty();
		
		for (var entry : verts.entrySet()) {
			var builder = provider.getBuilder(entry.getKey());
			var it = entry.getValue().listIterator(builder.build(entry.getValue(), this::newVertex));
	        while (it.hasNext()) {
	            it.next().dispose();
	            it.remove();
	        }
		}
		
		for (var entry : textureToAnimate.entrySet()) {
			var builder = provider.getBuilder(entry.getKey());
			var array = entry.getValue();
			array.clear();
			array.addAll(builder.textureToAnimate.orderedItems());
		}
		
		provider.clear();
	}

	@Override
	public void dispose() {
		verts.values().forEach(a -> a.forEach(Vertex::dispose));
	}
}
