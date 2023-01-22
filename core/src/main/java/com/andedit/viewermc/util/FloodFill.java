package com.andedit.viewermc.util;

import java.util.function.Consumer;

import com.badlogic.gdx.utils.Queue;

public class FloodFill implements Runnable {
	
	private final Check check;
	private final Queue<PointNode> queue;
	private final Consumer<PointNode> consumer;
	
	public FloodFill(int size, Consumer<PointNode> consumer) {
		this(new Queue<>(size*2), size, consumer);
	}
	
	public FloodFill(Queue<PointNode> queue, int size, Consumer<PointNode> consumer) {
		this.check = new Check(size);
		this.queue = queue;
		this.consumer = consumer;
	}

	@Override
	public void run() {
		queue.clear();

		check.invaildate(0, 0);
		queue.addLast(new PointNode(0, 0));
		
		while (queue.notEmpty()) {
			var node = queue.removeFirst();
			consumer.accept(node);
			
			if (check.isVaild(node.x()-1, node.z())) {
				queue.addLast(check.invaildate(node.offset(-1, 0)));
			}
			
			if (check.isVaild(node.x()+1, node.z())) {
				queue.addLast(check.invaildate(node.offset(1, 0)));
			}
			
			if (check.isVaild(node.x(), node.z()-1)) {
				queue.addLast(check.invaildate(node.offset(0, -1)));
			}
			
			if (check.isVaild(node.x(), node.z()+1)) {
				queue.addLast(check.invaildate(node.offset(0, 1)));
			}
		}
	}
}
