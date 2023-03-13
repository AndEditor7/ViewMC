package com.andedit.viewmc.world;

import java.util.Iterator;

import com.andedit.viewmc.util.PointNode;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.OrderedMap;

/** Used for chunk loader. */
public class ChunkSorter implements Iterable<Entry<PointNode, Array<ChunkToLoad>>> {
	/** region coordinate and a list of ChunkToLoad */
	private final OrderedMap<PointNode, Array<ChunkToLoad>> map = new OrderedMap<>();
	
	public ChunkSorter() {
		
	}
	
	public void add(ChunkToLoad chunk) {
		var point = new PointNode(chunk.worldX>>5, chunk.worldZ>>5);
		var list = map.get(point);
		if (list == null) {
			list = new Array<>(200);
			map.put(point, list);
		}
		list.add(chunk);
	}
	
	public void clear() {
		map.clear();
	}

	@Override
	public Iterator<Entry<PointNode, Array<ChunkToLoad>>> iterator() {
		return map.entries();
	}
}
