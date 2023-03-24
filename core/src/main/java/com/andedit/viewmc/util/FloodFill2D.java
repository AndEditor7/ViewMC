package com.andedit.viewmc.util;

import java.util.function.Consumer;

import com.badlogic.gdx.utils.Queue;

public class FloodFill2D implements Runnable {
	
	private final Occupation check;
	private final Queue<PointNode2D> queue;
	private final Consumer<PointNode2D> consumer;
	
	public FloodFill2D(int size, Consumer<PointNode2D> consumer) {
		this(new Queue<>(size*2), size, consumer);
	}
	
	public FloodFill2D(Queue<PointNode2D> queue, int size, Consumer<PointNode2D> consumer) {
		this.check = new Occupation(size);
		this.queue = queue;
		this.consumer = consumer;
	}

	@Override
	public void run() {
		queue.clear();

		check.occupy(0, 0);
		queue.addLast(new PointNode2D(0, 0));
		
		while (queue.notEmpty()) {
			var node = queue.removeFirst();
			consumer.accept(node);
			
			if (check.isAvailable(node.x()-1, node.z())) {
				queue.addLast(check.occupy(node.offset(-1, 0)));
			}
			
			if (check.isAvailable(node.x()+1, node.z())) {
				queue.addLast(check.occupy(node.offset(1, 0)));
			}
			
			if (check.isAvailable(node.x(), node.z()-1)) {
				queue.addLast(check.occupy(node.offset(0, -1)));
			}
			
			if (check.isAvailable(node.x(), node.z()+1)) {
				queue.addLast(check.occupy(node.offset(0, 1)));
			}
		}
	}
}
